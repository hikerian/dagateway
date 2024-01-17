package dagateway.server.controller;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.function.server.ServerResponse.BodyBuilder;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dagateway.api.context.BackendServer;
import dagateway.api.context.GatewayContext;
import dagateway.api.http.WebClientResolver;
import dagateway.api.utils.Utils;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;



/**
 * @author Dong-il Cho
 * TODO implements
 */
@Component
public class ApiDocsController {
	private final Logger log = LoggerFactory.getLogger(ApiDocsController.class);
	
	private final GatewayContext gatewayContext;
	private final WebClientResolver webClientResolver;
	
	
	public ApiDocsController(GatewayContext gatewayContext, WebClientResolver webClientResolver) {
		this.gatewayContext = gatewayContext;
		this.webClientResolver = webClientResolver;
	}
	
	public Mono<ServerResponse> service(ServerRequest serverRequest) {
		this.log.debug("service");
		
		List<BackendServer> backendList = this.gatewayContext.getBackendList();
		List<URI> apiDocUris = backendList.stream().map((backend) -> {
			String apiDoc = backend.getApiDocs();
			if(apiDoc != null && "".equals(apiDoc) == false) {
				String uri = backend.getUrl() + apiDoc;
				this.log.debug(uri);
				
				try {
					return new URI(uri);
				} catch (URISyntaxException e) {
					throw new RuntimeException(e);
				}
			} else {
				return null;
			}
		}).filter((url) -> url != null).toList();
		
		Flux<URI> apiDocUriFlux = Flux.fromIterable(apiDocUris);
		Flux<String> openAPIFlux = apiDocUriFlux.flatMap((apiDocUri) -> {
//			WebClient webClient = Utils.newWebClient();
			WebClient webClient = this.webClientResolver.createWebClient();
			
			RequestBodyUriSpec requestBodyUriSpec = webClient.method(HttpMethod.GET);
			requestBodyUriSpec.uri(apiDocUri);
			ResponseSpec responseSpec = requestBodyUriSpec.retrieve();
			
			return responseSpec.bodyToMono(String.class);
		});
		
		OpenAPIParser openAPIParser = new OpenAPIParser();
		
		Mono<String> resOpenAPI = openAPIFlux.collect(() -> new OpenAPI(), (openApiIns, backendApiStr) -> {
			this.log.debug(backendApiStr);
			
			SwaggerParseResult parseResult = openAPIParser.readContents(backendApiStr, null, null);
			
			if (parseResult.getMessages() != null) {
				parseResult.getMessages().forEach(this.log::error);
			}
			
			// TODO Filtering and Merging for Service API...
			
			OpenAPI openApi = parseResult.getOpenAPI();
			
			openApiIns.components(openApi.getComponents());
			openApiIns.extensions(openApi.getExtensions());
			openApiIns.externalDocs(openApi.getExternalDocs());
			openApiIns.info(openApi.getInfo());
			openApiIns.jsonSchemaDialect(openApi.getJsonSchemaDialect());
			openApiIns.paths(openApi.getPaths());
			openApiIns.security(openApi.getSecurity());
			openApiIns.servers(openApi.getServers());
			openApiIns.tags(openApi.getTags());
		}).map((openApiIns) -> {
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.setSerializationInclusion(Include.NON_NULL);
			
			try {
				return objectMapper.writeValueAsString(openApiIns);
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		});
		
		BodyBuilder bodyBuilder = ServerResponse.ok();

		return bodyBuilder.body(resOpenAPI, String.class);
	}


}
