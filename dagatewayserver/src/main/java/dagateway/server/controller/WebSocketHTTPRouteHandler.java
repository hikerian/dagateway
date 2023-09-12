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
import dagateway.api.handler.ServiceHandlerFactory;
import dagateway.api.service.ServiceDelegator;
import dagateway.api.service.ServiceDelegatorImpl;
import dagateway.api.service.ServiceResult;
import dagateway.api.utils.Utils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public class WebSocketHTTPRouteHandler implements WebSocketHandler {
	private final Logger log = LoggerFactory.getLogger(WebSocketHTTPRouteHandler.class);
	
	private ServiceSpec serviceSpec;
	private ServiceHandlerFactory serviceHandlerFactory;
	
	
	public WebSocketHTTPRouteHandler(ServiceSpec serviceSpec
			, ServiceHandlerFactory serviceHandlerFactory) {
		this.serviceSpec = serviceSpec;
		this.serviceHandlerFactory = serviceHandlerFactory;
	}

	@Override
	public Mono<Void> handle(WebSocketSession session) {
		this.log.debug("handle");
		
		// TODO complete below codes
		HandshakeInfo handShakeInfo = session.getHandshakeInfo();
		HttpHeaders headers = handShakeInfo.getHeaders();
		
		Flux<ServiceResult<Mono<String>>> serviceResults = session.receive().flatMap(message -> {
			// TODO add WebSocketMessageResolver
			String payload = message.getPayloadAsText();
			
			// TODO ServiceDelegator
			WebClient webClient = Utils.newWebClient();
			RequestBodyUriSpec requestBodyUriSpec = webClient.method(this.serviceSpec.getMethod());
			requestBodyUriSpec.uri(this.serviceSpec.createBackendURI());
			ServiceDelegator<Mono<String>, String, Mono<String>> serviceDelegator = new ServiceDelegatorImpl<>(requestBodyUriSpec
					, this.serviceHandlerFactory
					, this.serviceSpec
					, "reactor.core.publisher.Mono<java.lang.String>");

			Mono<ServiceResult<Mono<String>>> serviceResult = serviceDelegator.run(headers, Mono.just(payload));
			
			return serviceResult;
		});
		
		Mono<Void> sessionResult = session.send(serviceResults.flatMap(result -> {
			Mono<String> strBody = result.getBody();
			
			Mono<WebSocketMessage> rtnMsg = strBody.map(stringBody -> {
				
				// TODO add WebSocketMessageResolver
				WebSocketMessage message = session.textMessage(stringBody);
				return message;
			});
			
			return rtnMsg;
		}));
		
		return sessionResult;
	}

}
