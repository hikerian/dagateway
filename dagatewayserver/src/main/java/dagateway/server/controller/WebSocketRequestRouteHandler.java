package dagateway.server.controller;

import java.net.URI;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.socket.HandshakeInfo;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;

import dagateway.api.context.RouteContext.ServiceSpec;
import dagateway.api.resolver.ws.WebSocketMessageResolver;
import dagateway.api.utils.Utils;
import reactor.core.publisher.Mono;


public class WebSocketRequestRouteHandler implements WebSocketHandler {
	private final Logger log = LoggerFactory.getLogger(WebSocketRequestRouteHandler.class);
	
	private ServiceSpec serviceSpec;
	private WebSocketMessageResolver<?> requestResolver;
	private WebSocketMessageResolver<?> responseResolver;
	
	
	public WebSocketRequestRouteHandler(ServiceSpec serviceSpec
			, WebSocketMessageResolver<?> requestResolver
			, WebSocketMessageResolver<?> responseResolver) {
		this.serviceSpec = serviceSpec;
		this.requestResolver = requestResolver;
		this.responseResolver = responseResolver;
	}

	@Override
	public Mono<Void> handle(WebSocketSession session) {
		this.log.debug("handle"); // TODO protocol Convert

		this.log.debug("create WebSocketClient");
		
		HandshakeInfo handshakeInfo = session.getHandshakeInfo();
		HttpHeaders requestHeaders = handshakeInfo.getHeaders();
		HttpHeaders handshakeHeaders = new HttpHeaders();

		// handshake header 처리
		Map<String, String> variables = this.serviceSpec.getVariables();
		Utils.filterHeader(requestHeaders, handshakeHeaders, this.serviceSpec.getServiceRequestHeaderSpec(), variables);

		this.log.debug("execute WebSocketClient");
		URI backendURI = this.serviceSpec.createBackendURI();
		WebSocketClient client = new ReactorNettyWebSocketClient();

		return client.execute(backendURI, handshakeHeaders
				, new ProxyHandler(session, this.requestResolver, this.responseResolver));
	}

	private static class ProxyHandler implements WebSocketHandler {
		private WebSocketSession clientSession;
		private WebSocketMessageResolver<?> requestResolver;
		private WebSocketMessageResolver<?> responseResolver;

		
		ProxyHandler(WebSocketSession clientSession
				, WebSocketMessageResolver<?> requestResolver
				, WebSocketMessageResolver<?> responseResolver) {

			this.clientSession = clientSession;
			this.requestResolver = requestResolver;
			this.responseResolver = responseResolver;
		}

		@Override
		public Mono<Void> handle(WebSocketSession backendSession) {
			// TODO session closing 처리
			Mono<Void> returnValue = backendSession.send(this.clientSession.receive()
					.map(clientMessage -> {
						return this.requestResolver.resolve(backendSession, clientMessage);
					}))
				.and(this.clientSession.send(backendSession.receive()
					.map(backendMessage -> {
						return this.responseResolver.resolve(this.clientSession, backendMessage);
					})));
			
			return returnValue;
		}
		
	}
}
