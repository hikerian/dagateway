package dagateway.server;


import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.socket.server.WebSocketService;
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService;
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.DumperOptions.ScalarStyle;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.module.SimpleModule;

import dagateway.api.context.BackendServers;
import dagateway.api.context.GatewayContext;
import dagateway.api.context.RouteRequestContext;
import dagateway.api.context.predicate.RoutePredicate;
import dagateway.api.context.predicate.RoutePredicateBuilder;
import dagateway.api.context.route.ContentHandling;
import dagateway.api.context.route.GatewayRoutes;
import dagateway.api.handler.ContentHandlerFactory;
import dagateway.api.http.JdkClientResolver;
import dagateway.api.http.ReactorClientResolver;
import dagateway.api.http.WebClientResolver;
import dagateway.api.inserter.BodyInserterBuilderFactory;
import dagateway.api.resolver.http.ClientRequestResolverId;
import dagateway.api.resolver.http.ClientResolverFactory;
import dagateway.api.resolver.http.ClientResponseResolverId;
import dagateway.api.resolver.ws.WebSocketMessageResolverFactory;
import dagateway.api.service.ServiceBrokerBuilder;
import dagateway.api.service.ServiceExceptionResolver;
import dagateway.api.transform.DataTransformerFactory;
import dagateway.api.utils.ServerWebExchangeUtils;
import dagateway.server.controller.ApiDocsController;
import dagateway.server.controller.HttpRequestRouteController;
import dagateway.server.exception.ServiceExceptionResolverImpl;
import dagateway.server.handler.CharDelimiterFluxDataBufferHandler;
import dagateway.server.handler.DataBuffer2JSONObjectHandler;
import dagateway.server.handler.DataBuffer2ServerSentEventHandler;
import dagateway.server.handler.DataBuffer2TextPlainHandler;
import dagateway.server.handler.FormDataHandler;
import dagateway.server.handler.JSONObject2StringHandler;
import dagateway.server.handler.MultiDataBufferHandler;
import dagateway.server.handler.MultipartHandler;
import dagateway.server.handler.TextPlainHandler;
import dagateway.server.resolver.request.FormDataRequestResolver;
import dagateway.server.resolver.request.JSONObjectRequestResolver;
import dagateway.server.resolver.request.MultipartRequestResolver;
import dagateway.server.resolver.request.RawDataRequestResolver;
import dagateway.server.resolver.response.DynamicResponseResolver;
import dagateway.server.resolver.response.JSONGraphMultiResponseResolver;
import dagateway.server.resolver.response.JSONGraphSingleResponseResolver;
import dagateway.server.resolver.response.NDJSONResponseResolver;
import dagateway.server.resolver.response.NDJSONStreamResponseResolver;
import dagateway.server.resolver.response.RawDataMultiResponseResolver;
import dagateway.server.resolver.response.RawDataSingleResponseResolver;
import dagateway.server.resolver.response.TextEventMultiStreamResponseResolver;
import dagateway.server.resolver.response.TextEventSingleStreamResponseResolver;
import dagateway.server.transform.BinaryDataTransformer;
import dagateway.server.transform.FormDataTransformer;
import dagateway.server.transform.JSONGraphTransformer;
import dagateway.server.transform.JSONObjectTransformer;
import dagateway.server.transform.MultipartDataTransformer;
import dagateway.server.transform.PassDataTransformer;
import dagateway.server.transform.SSV2TSVCharTransformer;
import dagateway.server.transform.TextEventStreamTransformer;
import dagateway.server.transform.TextPlainDataTransformer;



/**
 * @author Dong-il Cho
 */
@Configuration
public class GatewayConfiguration {
	private final Logger log = LoggerFactory.getLogger(GatewayConfiguration.class);
	
	private final String DEFAULT_APIDOCS_PATH = "/v3/api-docs";
	
	private final String DEFAULT_ROUTE_PATH = "classpath:route/*.yml";
	private final String DEFAULT_BACKEND_PATH = "classpath:backend/*.yml";
	
	
	public GatewayConfiguration() {
	}
	
	@Bean
	RouterFunction<?> routerFunction(HttpRequestRouteController httpRequestRouteController
			, @Value("${dagateway.api-docs.path}") String apiDocsPath
			, ApiDocsController apiDocsController) {
		
		this.log.debug("routerFunction, apiDocsPath: " + apiDocsPath);
		
		if(apiDocsPath == null) {
			apiDocsPath = this.DEFAULT_APIDOCS_PATH;
		}

		RouterFunctions.Builder routerFunctionBuilder = RouterFunctions.route();
		
		routerFunctionBuilder.GET(apiDocsPath, apiDocsController::service)
		.route(request -> {
			RouteRequestContext routeContext = ServerWebExchangeUtils.getRouteContext(request);
			
			return routeContext != null;
		}, httpRequestRouteController::service);
		
		return routerFunctionBuilder.build();
	}
	
	@Bean
	RoutePredicateBuilder routePredicateBuilder() {
		this.log.debug("routePredicateBuilder");
		
		RoutePredicateBuilder routePredicateBuilder = new RoutePredicateBuilder();
		return routePredicateBuilder;
	}
	
	@Bean
	GatewayContext gatewayContext(ConfigurableApplicationContext configurableApplicationContext
			, RoutePredicateBuilder predicateBuilder
			, @Value("${dagateway.server.route-path}") String routePath
			, @Value("${dagateway.server.backend-path}") String backendPath) throws Exception {
		this.log.debug("gatewayContext routePath: " + routePath + ", backendPath: " + backendPath);
		
		if(routePath == null) {
			routePath = this.DEFAULT_ROUTE_PATH;
		}
		if(backendPath == null) {
			backendPath = this.DEFAULT_BACKEND_PATH;
		}

		// load routes
		GatewayContext gatewayContext = new GatewayContext();
		this.loadGatewayContext(gatewayContext, routePath, backendPath, configurableApplicationContext, predicateBuilder);
		
		return gatewayContext;
	}
	
	private void loadGatewayContext(GatewayContext gatewayContext
			, String routePath
			, String backendPath
			, ConfigurableApplicationContext configurableApplicationContext
			, RoutePredicateBuilder predicateBuilder) throws IOException {

		SimpleModule deserializerModule = new SimpleModule();
		deserializerModule.addDeserializer(RoutePredicate.class, new JsonDeserializer<RoutePredicate>() {
			@Override
			public RoutePredicate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
				String predicateShortcut = p.getValueAsString();
				
				return predicateBuilder.build(predicateShortcut);
			}
		});
		
		DumperOptions dumperOptions = new DumperOptions();
		dumperOptions.setPrettyFlow(true);
		dumperOptions.setDefaultScalarStyle(ScalarStyle.DOUBLE_QUOTED);
		dumperOptions.setDefaultFlowStyle(FlowStyle.FLOW);
		
		Yaml yaml = new Yaml(dumperOptions);
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(deserializerModule);
		mapper.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);

		Resource[] routeYamls = configurableApplicationContext.getResources(routePath);
		for(Resource routeYaml : routeYamls) {
			Map<String, Object> yamlMap = yaml.load(routeYaml.getInputStream());
			GatewayRoutes routes = mapper.convertValue(yamlMap, GatewayRoutes.class);
			gatewayContext.addRoutes(routes);
		}
		
		Resource[] backendYamls = configurableApplicationContext.getResources(backendPath);
		for(Resource backendYaml : backendYamls) {
			Map<String, Object> yamlMap = yaml.load(backendYaml.getInputStream());
			BackendServers backendServers = mapper.convertValue(yamlMap, BackendServers.class);
			gatewayContext.addBackends(backendServers);
		}
	}
	
	@Bean
	BodyInserterBuilderFactory bodyInserterBuilderFactory() {
		this.log.debug("bodyInserterBuilderFactory");
		
		BodyInserterBuilderFactory factory = new BodyInserterBuilderFactory();
		return factory;
	}
	
	@Bean
	ServiceExceptionResolver exceptionResolver() {
		this.log.debug("exceptionResolver");
		
		ServiceExceptionResolver exceptionResolver = new ServiceExceptionResolverImpl();
		return exceptionResolver;
	}
	
	@Bean
	WebClientResolver webClientResolver(@Value("${dagateway.webclient}") String webClient) {
		this.log.debug("webClientResolver");
		
		if("reactor".equalsIgnoreCase(webClient)) {
			return new ReactorClientResolver();
		}
		return new JdkClientResolver();
	}
	
	@Bean
	ServiceBrokerBuilder serviceBrokerBuilder(ContentHandlerFactory contentHandlerFactory
			, ClientResolverFactory clientResolverFactory
			, BodyInserterBuilderFactory bodyInserterBuilderFactory
			, ServiceExceptionResolver exceptionResolver
			, WebClientResolver webClientResolver) {
		this.log.debug("serviceBrokerBuilder");

		ServiceBrokerBuilder serviceBrokerBuilder = new ServiceBrokerBuilder();
		serviceBrokerBuilder.init(contentHandlerFactory
				, clientResolverFactory
				, bodyInserterBuilderFactory
				, exceptionResolver
				, webClientResolver);
		
		return serviceBrokerBuilder;
	}

	@Bean
	ClientResolverFactory clientResolverFactory(BodyInserterBuilderFactory bodyInserterBuilderFactory) {
		this.log.debug("clientResolverFactory");
		
		ClientResolverFactory clientResolverFactory = new ClientResolverFactory();
		
		clientResolverFactory.addRequestResolver(new ClientRequestResolverId(MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_FORM_URLENCODED, false)
				, () -> new FormDataRequestResolver());
		clientResolverFactory.addRequestResolver(new ClientRequestResolverId(RouteRequestContext.NONE, MediaType.APPLICATION_FORM_URLENCODED, false)
				, () -> new FormDataRequestResolver());
		clientResolverFactory.addRequestResolver(new ClientRequestResolverId(MediaType.MULTIPART_FORM_DATA, MediaType.MULTIPART_FORM_DATA, false)
				, () -> new MultipartRequestResolver());
		clientResolverFactory.addRequestResolver(new ClientRequestResolverId(RouteRequestContext.NONE, MediaType.MULTIPART_FORM_DATA, false)
				, () -> new MultipartRequestResolver());
		clientResolverFactory.addRequestResolver(new ClientRequestResolverId(MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, false)
				, () -> new JSONObjectRequestResolver());
		clientResolverFactory.addRequestResolver(new ClientRequestResolverId(RouteRequestContext.NONE, MediaType.APPLICATION_JSON, false)
				, () -> new JSONObjectRequestResolver());
		clientResolverFactory.addRequestResolver(new ClientRequestResolverId(MediaType.ALL, MediaType.APPLICATION_OCTET_STREAM, true)
				, () -> new RawDataRequestResolver());
		clientResolverFactory.addRequestResolver(new ClientRequestResolverId(MediaType.ALL, MediaType.ALL, true)
				, () -> new RawDataRequestResolver());
		clientResolverFactory.addRequestResolver(new ClientRequestResolverId(RouteRequestContext.NONE, MediaType.ALL, true)
				, () -> new RawDataRequestResolver());
		
		clientResolverFactory.addResponseResolver(new ClientResponseResolverId(MediaType.APPLICATION_NDJSON, ContentHandling.PASSTHROUGH, true)
				, () -> new NDJSONResponseResolver());
		clientResolverFactory.addResponseResolver(new ClientResponseResolverId(MediaType.APPLICATION_NDJSON, ContentHandling.PASSTHROUGH, true)
				, () -> new NDJSONStreamResponseResolver());
		clientResolverFactory.addResponseResolver(new ClientResponseResolverId(MediaType.TEXT_EVENT_STREAM, ContentHandling.PASSTHROUGH, false)
				, () -> new TextEventSingleStreamResponseResolver());
		clientResolverFactory.addResponseResolver(new ClientResponseResolverId(MediaType.TEXT_EVENT_STREAM, ContentHandling.PASSTHROUGH, true)
				, () -> new TextEventMultiStreamResponseResolver());
		clientResolverFactory.addResponseResolver(new ClientResponseResolverId(MediaType.ALL, ContentHandling.PASSTHROUGH, false)
				, () -> new DynamicResponseResolver<Object>(bodyInserterBuilderFactory));
		clientResolverFactory.addResponseResolver(new ClientResponseResolverId(MediaType.ALL, ContentHandling.PASSTHROUGH, false)
				, () -> new RawDataSingleResponseResolver());
		clientResolverFactory.addResponseResolver(new ClientResponseResolverId(MediaType.ALL, ContentHandling.PASSTHROUGH, true)
				, () -> new RawDataMultiResponseResolver());
		clientResolverFactory.addResponseResolver(new ClientResponseResolverId(MediaType.APPLICATION_JSON, ContentHandling.COMPOSE, false)
				, () -> new JSONGraphSingleResponseResolver());
		clientResolverFactory.addResponseResolver(new ClientResponseResolverId(MediaType.APPLICATION_JSON, ContentHandling.COMPOSE, true)
				, () -> new JSONGraphMultiResponseResolver());
		
		return clientResolverFactory;
	}
	
	@Bean
	WebSocketMessageResolverFactory webSocketMessageResolverFactory(AutowireCapableBeanFactory autowireCapableBeanFactory
			, DataTransformerFactory dataTransformerFactory) {
		this.log.debug("webSocketMessageResolverFactory");
		
		WebSocketMessageResolverFactory webSocketMessageResolverFactory = new WebSocketMessageResolverFactory();
		
		return webSocketMessageResolverFactory;
	}
	
	@Bean
	ContentHandlerFactory contentHandlerFactory(DataTransformerFactory dataTransformerFactory) {
		this.log.debug("contentHandlerFactory");
		
		ContentHandlerFactory contentHandlerFactory = new ContentHandlerFactory();
		contentHandlerFactory.init(dataTransformerFactory);
		
		contentHandlerFactory.addServiceRequestHandler(MediaType.valueOf("text/semi-colon-seperated-values")
				, CharDelimiterFluxDataBufferHandler.ARGUMENT_TYPE
				, CharDelimiterFluxDataBufferHandler.RETURN_TYPE
				, () -> new CharDelimiterFluxDataBufferHandler());
		contentHandlerFactory.addServiceRequestHandler(MediaType.APPLICATION_FORM_URLENCODED
				, FormDataHandler.ARGUMENT_TYPE
				, FormDataHandler.RETURN_TYPE
				, () -> new FormDataHandler());
		contentHandlerFactory.addServiceRequestHandler(MediaType.APPLICATION_JSON
				, JSONObject2StringHandler.ARGUMENT_TYPE
				, JSONObject2StringHandler.RETURN_TYPE
				, () -> new JSONObject2StringHandler());
		contentHandlerFactory.addServiceRequestHandler(MediaType.APPLICATION_JSON
				, DataBuffer2JSONObjectHandler.ARGUMENT_TYPE
				, DataBuffer2JSONObjectHandler.RETURN_TYPE
				, () -> new DataBuffer2JSONObjectHandler());
		contentHandlerFactory.addServiceRequestHandler(MediaType.MULTIPART_FORM_DATA
				, MultipartHandler.ARGUMENT_TYPE
				, MultipartHandler.RETURN_TYPE
				, () -> new MultipartHandler());
		contentHandlerFactory.addServiceRequestHandler(MediaType.TEXT_EVENT_STREAM
				, DataBuffer2ServerSentEventHandler.ARGUMENT_TYPE
				, DataBuffer2ServerSentEventHandler.RETURN_TYPE
				, () -> new DataBuffer2ServerSentEventHandler());
		contentHandlerFactory.addServiceRequestHandler(MediaType.TEXT_PLAIN
				, TextPlainHandler.ARGUMENT_TYPE
				, TextPlainHandler.RETURN_TYPE
				, () -> new TextPlainHandler());
		contentHandlerFactory.addServiceRequestHandler(MediaType.TEXT_PLAIN
				, DataBuffer2TextPlainHandler.ARGUMENT_TYPE
				, DataBuffer2TextPlainHandler.RETURN_TYPE
				, () -> new DataBuffer2TextPlainHandler());
		contentHandlerFactory.addServiceRequestHandler(MediaType.ALL
				, MultiDataBufferHandler.ARGUMENT_TYPE
				, MultiDataBufferHandler.RETURN_TYPE
				, () -> new MultiDataBufferHandler());
		
		return contentHandlerFactory;
	}

	@Bean
	DataTransformerFactory dataTransformerFactory() {
		this.log.debug("dataTransformerFactory");
		
		DataTransformerFactory dataTransformerFactory = new DataTransformerFactory();
		
		dataTransformerFactory.addDataTransformer(MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON
				, JSONObjectTransformer.ARGUMENT_TYPE, JSONObjectTransformer.RETURN_TYPE
				, () -> new JSONObjectTransformer());
		dataTransformerFactory.addDataTransformer(MediaType.APPLICATION_JSON, MediaType.APPLICATION_NDJSON
				, JSONObjectTransformer.ARGUMENT_TYPE, JSONObjectTransformer.RETURN_TYPE
				, () -> new JSONObjectTransformer());

		dataTransformerFactory.addDataTransformer(MediaType.TEXT_EVENT_STREAM, MediaType.TEXT_EVENT_STREAM
				, TextEventStreamTransformer.ARGUMENT_TYPE, TextEventStreamTransformer.RETURN_TYPE
				, () -> new TextEventStreamTransformer());
		dataTransformerFactory.addDataTransformer(MediaType.MULTIPART_FORM_DATA, MediaType.MULTIPART_FORM_DATA
				, MultipartDataTransformer.ARGUMENT_TYPE, MultipartDataTransformer.RETURN_TYPE
				, () -> new MultipartDataTransformer());
		dataTransformerFactory.addDataTransformer(MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_FORM_URLENCODED
				, FormDataTransformer.ARGUMENT_TYPE, FormDataTransformer.RETURN_TYPE
				, () -> new FormDataTransformer());
		dataTransformerFactory.addDataTransformer(MediaType.valueOf("text/semi-colon-seperated-values"), MediaType.valueOf("text/tab-separated-values")
				, SSV2TSVCharTransformer.ARGUMENT_TYPE, SSV2TSVCharTransformer.RETURN_TYPE
				, () -> new SSV2TSVCharTransformer());
		
		dataTransformerFactory.addDataTransformer(MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON
				, JSONGraphTransformer.ARGUMENT_TYPE, JSONGraphTransformer.RETURN_TYPE
				, () -> new JSONGraphTransformer());
		
		dataTransformerFactory.addDataTransformer(MediaType.TEXT_PLAIN, MediaType.TEXT_PLAIN
				, TextPlainDataTransformer.ARGUMENT_TYPE, TextPlainDataTransformer.RETURN_TYPE
				, () -> new TextPlainDataTransformer());
		dataTransformerFactory.addDataTransformer(MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_OCTET_STREAM
				, BinaryDataTransformer.ARGUMENT_TYPE, BinaryDataTransformer.RETURN_TYPE
				, () -> new BinaryDataTransformer());
		
		dataTransformerFactory.addDataTransformer(MediaType.ALL, MediaType.ALL
				, PassDataTransformer.ARGUMENT_TYPE, PassDataTransformer.RETURN_TYPE
				, () -> new PassDataTransformer());
		
		return dataTransformerFactory;
	}

	@Bean
	WebSocketService webSocketService() {
		this.log.debug("webSocketService");
		
		ReactorNettyRequestUpgradeStrategy strategy = new ReactorNettyRequestUpgradeStrategy();

		return new HandshakeWebSocketService(strategy);
	}


}
