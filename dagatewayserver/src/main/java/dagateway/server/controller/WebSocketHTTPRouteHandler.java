package dagateway.server.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;

import dagateway.api.context.RouteContext.ServiceSpec;
import dagateway.api.resolver.ws.WebSocketMessageResolver;
import dagateway.api.service.ServiceDelegator;
import dagateway.api.service.ServiceDelegatorImpl;
import dagateway.api.service.ServiceResult;
import dagateway.api.utils.Utils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public class WebSocketHTTPRouteHandler implements WebSocketHandler {
	private final Logger log = LoggerFactory.getLogger(WebSocketHTTPRouteHandler.class);
	
	private ServiceSpec serviceSpec;
	private WebSocketMessageResolver<?> requestResolver;
	private WebSocketMessageResolver<?> responseResolver;
	
	
	public WebSocketHTTPRouteHandler(ServiceSpec serviceSpec
			, WebSocketMessageResolver<?> requestResolver
			, WebSocketMessageResolver<?> responseResolver) {
		this.serviceSpec = serviceSpec;
		this.requestResolver = requestResolver;
		this.responseResolver = responseResolver;
	}

	@Override
	public Mono<Void> handle(WebSocketSession session) {
		this.log.debug("handle");
		
		// TODO complete below codes
		
		Flux<ServiceResult<Mono<String>>> serviceResults = session.receive().flatMap(message -> {
			// TODO add WebSocketMessageResolver
			String payload = message.getPayloadAsText();
			
			// TODO ServiceDelegator
			WebClient webClient = Utils.newWebClient();
			RequestBodyUriSpec requestBodyUriSpec = webClient.method(this.serviceSpec.getMethod());
			requestBodyUriSpec.uri(this.serviceSpec.createBackendURI());
			ServiceDelegator<Mono<String>, String, Mono<String>> serviceDelegator = new ServiceDelegatorImpl<>(requestBodyUriSpec
					, null
					, this.serviceSpec
					, "reactor.core.publisher.Mono<java.lang.String>");
			Mono<ServiceResult<Mono<String>>> serviceResult = serviceDelegator.run(null, null);
			
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
