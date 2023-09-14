package dagateway.server.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec;
import org.springframework.web.reactive.socket.HandshakeInfo;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;

import dagateway.api.context.RouteContext.ServiceSpec;
import dagateway.api.handler.ContentHandlerFactory;
import dagateway.api.inserter.BodyInserterBuilderFactory;
import dagateway.api.resolver.ws.WebSocketMessageResolver;
import dagateway.api.service.ServiceDelegator;
import dagateway.api.service.ServiceDelegatorImpl;
import dagateway.api.service.ServiceResult;
import dagateway.api.utils.Utils;
import reactor.core.publisher.Mono;


public class WebSocket2HTTPHandler<T> implements WebSocketHandler {
	private final Logger log = LoggerFactory.getLogger(WebSocket2HTTPHandler.class);

	private ContentHandlerFactory contentHandlerFactory;
	private BodyInserterBuilderFactory bodyInserterBuilderFactory;
	
	private ServiceSpec serviceSpec;
	private WebSocketMessageResolver<T> clientResolver;
	
	
	public WebSocket2HTTPHandler(ContentHandlerFactory contentHandlerFactory
			, BodyInserterBuilderFactory bodyInserterBuilderFactory
			, ServiceSpec serviceSpec
			, WebSocketMessageResolver<T> clientResolver) {
		
		this.contentHandlerFactory = contentHandlerFactory;
		this.bodyInserterBuilderFactory = bodyInserterBuilderFactory;
		
		this.serviceSpec = serviceSpec;
		this.clientResolver = clientResolver;
	}

	@Override
	public Mono<Void> handle(WebSocketSession session) {
		this.log.debug("handle");
		
		// TODO complete below codes
		HandshakeInfo handShakeInfo = session.getHandshakeInfo();
		HttpHeaders headers = handShakeInfo.getHeaders();
		
		return session.receive().flatMap(message -> {
			// TODO add WebSocketMessageResolver
			T payload = this.clientResolver.extract(message);
			
			// TODO ServiceDelegator
			WebClient webClient = Utils.newWebClient();
			RequestBodyUriSpec requestBodyUriSpec = webClient.method(this.serviceSpec.getMethod());
			requestBodyUriSpec.uri(this.serviceSpec.createBackendURI());
			
			ServiceDelegator<Mono<T>, T, Mono<T>> serviceDelegator = new ServiceDelegatorImpl<>(requestBodyUriSpec
					, this.contentHandlerFactory
					, this.bodyInserterBuilderFactory
					, this.serviceSpec
					, this.clientResolver.getTypeName()
					, this.clientResolver.getTypeName());

			Mono<ServiceResult<Mono<T>>> serviceResult = serviceDelegator.run(headers, Mono.just(payload));
			
			return serviceResult.flatMap(result -> {
				Mono<T> strBody = result.getBody();
				Mono<WebSocketMessage> rtnMsg = strBody.map(stringBody -> {
					return this.clientResolver.build(session, stringBody);
				});
				
				return session.send(rtnMsg);
			});
		}).then();
	}









































}
