package dagateway.server.controller;

import java.net.URI;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.socket.HandshakeInfo;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;

import dagateway.api.context.RouteContext.ServiceSpec;
import dagateway.api.handler.ContentHandler;
import dagateway.api.handler.ContentHandlerFactory;
import dagateway.api.resolver.ws.MessageResolver;
import dagateway.api.utils.Utils;
import reactor.core.publisher.Mono;



public class WebSocket2WebSocketHandler<T, V> implements WebSocketHandler {
	private final Logger log = LoggerFactory.getLogger(WebSocket2WebSocketHandler.class);

	private ContentHandlerFactory contentHandlerFactory;
	
	private ServiceSpec serviceSpec;
	private MessageResolver<T> clientResolver;
	private MessageResolver<V> backendResolver;
	
	
	public WebSocket2WebSocketHandler(ContentHandlerFactory contentHandlerFactory
			, ServiceSpec serviceSpec
			, MessageResolver<T> clientResolver
			, MessageResolver<V> backendResolver) {
		
		this.contentHandlerFactory = contentHandlerFactory;
		this.serviceSpec = serviceSpec;
		this.clientResolver = clientResolver;
		this.backendResolver = backendResolver;
	}

	@Override
	public Mono<Void> handle(WebSocketSession session) {
		this.log.debug("handle");
		
		// create websocket client
		HandshakeInfo handshakeInfo = session.getHandshakeInfo();
		HttpHeaders requestHeaders = handshakeInfo.getHeaders();
		HttpHeaders handshakeHeaders = new HttpHeaders();
		
		// handshake header 처리
		Map<String, String> variables = this.serviceSpec.getVariables();
		Utils.filterHeader(requestHeaders, handshakeHeaders, this.serviceSpec.getServiceRequestHeaderSpec(), variables);
		
		// execute websocketclient
		URI backendURI = this.serviceSpec.createBackendURI();
		WebSocketClient client = new ReactorNettyWebSocketClient();
		
		ContentHandler<Mono<T>, T, T, V, Mono<V>> requestHandler = this.contentHandlerFactory.getContentHandler(this.serviceSpec.getAggregateType()
				, this.serviceSpec.getAggregateType()
				, this.clientResolver.getTypeName()
				, this.backendResolver.getTypeName()
				, this.serviceSpec.getServiceRequestTransformSpec());
		
		ContentHandler<Mono<V>, V, V, T, Mono<T>> responseHandler = this.contentHandlerFactory.getContentHandler(this.serviceSpec.getServiceResponseType()
				, this.serviceSpec.getClientResponseType()
				, this.backendResolver.getTypeName()
				, this.clientResolver.getTypeName()
				, this.serviceSpec.getServiceResponseTransformSpec(this.serviceSpec.getServiceResponseType()));
		
		return client.execute(backendURI, handshakeHeaders
				, new ProxyHandler<T, V>(session, this.clientResolver, this.backendResolver, requestHandler, responseHandler));
	}

	private static class ProxyHandler<T, V> implements WebSocketHandler {
		private WebSocketSession clientSession;
		private MessageResolver<T> clientResolver;
		private MessageResolver<V> backendResolver;
		private ContentHandler<Mono<T>, T, T, V, Mono<V>> requestHandler;
		private ContentHandler<Mono<V>, V, V, T, Mono<T>> responseHandler;

		
		ProxyHandler(WebSocketSession clientSession
				, MessageResolver<T> clientResolver
				, MessageResolver<V> backendResolver
				, ContentHandler<Mono<T>, T, T, V, Mono<V>> requestHandler
				, ContentHandler<Mono<V>, V, V, T, Mono<T>> responseHandler) {

			this.clientSession = clientSession;
			this.clientResolver = clientResolver;
			this.backendResolver = backendResolver;
			this.requestHandler = requestHandler;
			this.responseHandler = responseHandler;
		}

		@Override
		public Mono<Void> handle(WebSocketSession backendSession) {
			// TODO session closing 처리
			Mono<Void> returnValue = backendSession.send(this.clientSession.receive()
					.flatMap(clientMessage -> {
						T message = this.clientResolver.extract(clientMessage);
						Mono<WebSocketMessage> handledMessage = this.requestHandler.handle(Mono.just(message))
								.map(handled -> this.backendResolver.build(backendSession, handled));
						
						return handledMessage;
					}))
				.and(this.clientSession.send(backendSession.receive()
					.flatMap(backendMessage -> {
						V message = this.backendResolver.extract(backendMessage);
						Mono<WebSocketMessage> handledMessage = this.responseHandler.handle(Mono.just(message))
								.map(handled -> this.clientResolver.build(this.clientSession, handled));
						
						return handledMessage;
					})));
			
			return returnValue;
		}
		
	}

}
