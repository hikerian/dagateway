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

import dagateway.api.context.RouteRequestContext.ServiceSpec;
import dagateway.api.handler.ContentHandlerFactory;
import dagateway.api.http.WebClientResolver;
import dagateway.api.inserter.BodyInserterBuilderFactory;
import dagateway.api.resolver.ws.WebSocketMessageResolver;
import dagateway.api.service.ServiceDelegator;
import dagateway.api.service.ServiceDelegatorImpl;
import dagateway.api.service.ServiceExceptionResolver;
import dagateway.api.service.ServiceResult;
import reactor.core.publisher.Mono;



/**
 * @author Dong-il Cho
 */
public class WebSocket2HTTPHandler<T> implements WebSocketHandler {
	private final Logger log = LoggerFactory.getLogger(WebSocket2HTTPHandler.class);

	private final ContentHandlerFactory contentHandlerFactory;
	private final BodyInserterBuilderFactory bodyInserterBuilderFactory;
	private final ServiceExceptionResolver exceptionResolver;
	
	private final ServiceSpec serviceSpec;
	private final WebSocketMessageResolver<T> clientResolver;
	
	private final WebClientResolver webClientResolver;
	
	
	public WebSocket2HTTPHandler(ContentHandlerFactory contentHandlerFactory
			, BodyInserterBuilderFactory bodyInserterBuilderFactory
			, ServiceExceptionResolver exceptionResolver
			, ServiceSpec serviceSpec
			, WebSocketMessageResolver<T> clientResolver
			, WebClientResolver webClientResolver) {
		
		this.contentHandlerFactory = contentHandlerFactory;
		this.bodyInserterBuilderFactory = bodyInserterBuilderFactory;
		this.exceptionResolver = exceptionResolver;
		
		this.serviceSpec = serviceSpec;
		this.clientResolver = clientResolver;
		
		this.webClientResolver = webClientResolver;
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
			
			WebClient webClient = this.webClientResolver.createWebClient();
			
			RequestBodyUriSpec requestBodyUriSpec = webClient.method(this.serviceSpec.getMethod());
			requestBodyUriSpec.uri(this.serviceSpec.createBackendURI());
			
			ServiceDelegator<Mono<T>, T, Mono<T>> serviceDelegator = new ServiceDelegatorImpl<>(requestBodyUriSpec
					, this.contentHandlerFactory
					, this.bodyInserterBuilderFactory
					, this.exceptionResolver
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
