package dagateway.server;


import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import dagateway.api.context.ContentHandling;
import dagateway.api.context.GatewayContext;
import dagateway.api.context.GatewayRoutes;
import dagateway.api.context.RouteContext;
import dagateway.api.context.RoutePredicate;
import dagateway.api.context.predicate.RoutePredicateBuilder;
import dagateway.api.handler.ContentHandlerFactory;
import dagateway.api.inserter.BodyInserterBuilderFactory;
import dagateway.api.resolver.http.ClientRequestResolverId;
import dagateway.api.resolver.http.ClientResolverFactory;
import dagateway.api.resolver.http.ClientResponseResolverId;
import dagateway.api.resolver.ws.WebSocketMessageResolverFactory;
import dagateway.api.service.ServiceBrokerBuilder;
import dagateway.api.transform.DataTransformerFactory;
import dagateway.api.utils.ServerWebExchangeUtils;
import dagateway.server.controller.HttpRequestRouteController;
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
import dagateway.server.resolver.response.JSONNDResponseResolver;
import dagateway.server.resolver.response.JSONNDStreamResponseResolver;
import dagateway.server.resolver.response.RawDataMultiResponseResolver;
import dagateway.server.resolver.response.RawDataSingleResponseResolver;
import dagateway.server.resolver.response.TextEventMultiStreamResponseResolver;
import dagateway.server.resolver.response.TextEventSingleStreamResponseResolver;
import dagateway.server.transform.support.BinaryDataTransformer;
import dagateway.server.transform.support.FormDataTransformer;
import dagateway.server.transform.support.JSONGraphTransformer;
import dagateway.server.transform.support.JSONObjectTransformer;
import dagateway.server.transform.support.MultipartDataTransformer;
import dagateway.server.transform.support.PassDataTransformer;
import dagateway.server.transform.support.SSV2TSVCharTransformer;
import dagateway.server.transform.support.TextEventStreamTransformer;
import dagateway.server.transform.support.TextPlainDataTransformer;



@Configuration
public class GatewayConfiguration {
	private final Logger log = LoggerFactory.getLogger(GatewayConfiguration.class);
	
	
	public GatewayConfiguration() {
	}
	
	@Bean
	RouterFunction<?> routerFunction(HttpRequestRouteController httpRequestRouteController) {
		this.log.debug("routerFunction");

		RouterFunctions.Builder routerFunctionBuilder = RouterFunctions.route();
		routerFunctionBuilder.route(request -> {
			RouteContext routeContext = ServerWebExchangeUtils.getRouteContext(request);
			
			this.log.debug("## RouteContext HIT!!!: " + routeContext);
			
			return routeContext != null;
		}, httpRequestRouteController::service);
		
		return routerFunctionBuilder.build();
	}
	
	@Bean
	RoutePredicateBuilder routePredicateBuilder() {
		RoutePredicateBuilder routePredicateBuilder = new RoutePredicateBuilder();
		return routePredicateBuilder;
	}
	
	@Bean
	GatewayContext gatewayContext(ConfigurableApplicationContext configurableApplicationContext, RoutePredicateBuilder predicateFactory) throws Exception {
		this.log.debug("bffGatewayContext");
		
		SimpleModule deserializerModule = new SimpleModule();
		deserializerModule.addDeserializer(RoutePredicate.class, new JsonDeserializer<RoutePredicate>() {
			@Override
			public RoutePredicate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
				String predicateShortcut = p.getValueAsString();
				
				return predicateFactory.build(predicateShortcut);
			}
		});
		
		GatewayContext bffGatewayContext = new GatewayContext();
		
		Resource[] routeYamls = configurableApplicationContext.getResources("classpath:route/*.yml");
		
		DumperOptions dumperOptions = new DumperOptions();
		dumperOptions.setPrettyFlow(true);
		dumperOptions.setDefaultScalarStyle(ScalarStyle.DOUBLE_QUOTED);
		dumperOptions.setDefaultFlowStyle(FlowStyle.FLOW);
		
		Yaml yaml = new Yaml(dumperOptions);
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(deserializerModule);
		mapper.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);
			
		for(Resource routeYaml : routeYamls) {
			Map<String, Object> yamlMap = yaml.load(routeYaml.getInputStream());
			GatewayRoutes routes = mapper.convertValue(yamlMap, GatewayRoutes.class);
			bffGatewayContext.addRoutes(routes);
			this.log.debug(routes.toString());
		}
		
		return bffGatewayContext;
	}
	
	@Bean
	BodyInserterBuilderFactory bodyInserterBuilderFactory() {
		BodyInserterBuilderFactory factory = new BodyInserterBuilderFactory();
		return factory;
	}
	
	@Bean
	ServiceBrokerBuilder serviceBrokerBuilder(ContentHandlerFactory contentHandlerFactory
			, ClientResolverFactory clientResolverFactory
			, BodyInserterBuilderFactory bodyInserterBuilderFactory) {

		ServiceBrokerBuilder serviceBrokerBuilder = new ServiceBrokerBuilder();
		serviceBrokerBuilder.init(contentHandlerFactory, clientResolverFactory, bodyInserterBuilderFactory);
		
		return serviceBrokerBuilder;
	}
	
	@Bean
	ClientResolverFactory clientResolverFactory(AutowireCapableBeanFactory autowireCapableBeanFactory) {
		ClientResolverFactory clientResolverFactory = new ClientResolverFactory();
		clientResolverFactory.setAutowireCapableBeanFactory(autowireCapableBeanFactory);
		
		clientResolverFactory.addRequestResolver(new ClientRequestResolverId(MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_FORM_URLENCODED, false), FormDataRequestResolver.class);
		clientResolverFactory.addRequestResolver(new ClientRequestResolverId(RouteContext.NONE, MediaType.APPLICATION_FORM_URLENCODED, false), FormDataRequestResolver.class);
		clientResolverFactory.addRequestResolver(new ClientRequestResolverId(MediaType.MULTIPART_FORM_DATA, MediaType.MULTIPART_FORM_DATA, false), MultipartRequestResolver.class);
		clientResolverFactory.addRequestResolver(new ClientRequestResolverId(RouteContext.NONE, MediaType.MULTIPART_FORM_DATA, false), MultipartRequestResolver.class);
		clientResolverFactory.addRequestResolver(new ClientRequestResolverId(MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, false), JSONObjectRequestResolver.class);
		clientResolverFactory.addRequestResolver(new ClientRequestResolverId(RouteContext.NONE, MediaType.APPLICATION_JSON, false), JSONObjectRequestResolver.class);
		clientResolverFactory.addRequestResolver(new ClientRequestResolverId(MediaType.ALL, MediaType.APPLICATION_OCTET_STREAM, true), RawDataRequestResolver.class);
		clientResolverFactory.addRequestResolver(new ClientRequestResolverId(MediaType.ALL, MediaType.ALL, true), RawDataRequestResolver.class);
		clientResolverFactory.addRequestResolver(new ClientRequestResolverId(RouteContext.NONE, MediaType.ALL, true), RawDataRequestResolver.class);
		
		clientResolverFactory.addResponseResolver(new ClientResponseResolverId(MediaType.APPLICATION_NDJSON, ContentHandling.PASSTHROUGH, true), JSONNDResponseResolver.class);
		clientResolverFactory.addResponseResolver(new ClientResponseResolverId(MediaType.APPLICATION_NDJSON, ContentHandling.PASSTHROUGH, true), JSONNDStreamResponseResolver.class);
		clientResolverFactory.addResponseResolver(new ClientResponseResolverId(MediaType.TEXT_EVENT_STREAM, ContentHandling.PASSTHROUGH, false), TextEventSingleStreamResponseResolver.class);
		clientResolverFactory.addResponseResolver(new ClientResponseResolverId(MediaType.TEXT_EVENT_STREAM, ContentHandling.PASSTHROUGH, true), TextEventMultiStreamResponseResolver.class);
		clientResolverFactory.addResponseResolver(new ClientResponseResolverId(MediaType.ALL, ContentHandling.PASSTHROUGH, false), DynamicResponseResolver.class);
		clientResolverFactory.addResponseResolver(new ClientResponseResolverId(MediaType.ALL, ContentHandling.PASSTHROUGH, false), RawDataSingleResponseResolver.class);
		clientResolverFactory.addResponseResolver(new ClientResponseResolverId(MediaType.ALL, ContentHandling.PASSTHROUGH, true), RawDataMultiResponseResolver.class);
		clientResolverFactory.addResponseResolver(new ClientResponseResolverId(MediaType.APPLICATION_JSON, ContentHandling.COMPOSE, false), JSONGraphSingleResponseResolver.class);
		clientResolverFactory.addResponseResolver(new ClientResponseResolverId(MediaType.APPLICATION_JSON, ContentHandling.COMPOSE, true), JSONGraphMultiResponseResolver.class);
		
		return clientResolverFactory;
	}
	
	@Bean
	WebSocketMessageResolverFactory webSocketMessageResolverFactory(AutowireCapableBeanFactory autowireCapableBeanFactory
			, DataTransformerFactory dataTransformerFactory) {
		
		WebSocketMessageResolverFactory webSocketMessageResolverFactory = new WebSocketMessageResolverFactory();
		
		return webSocketMessageResolverFactory;
	}
	
	@Bean
	ContentHandlerFactory contentHandlerFactory(AutowireCapableBeanFactory autowireCapableBeanFactory, DataTransformerFactory dataTransformerFactory) {
		ContentHandlerFactory contentHandlerFactory = new ContentHandlerFactory();
		contentHandlerFactory.init(autowireCapableBeanFactory, dataTransformerFactory);
		
		contentHandlerFactory.addServiceRequestHandler(MediaType.valueOf("text/semi-colon-seperated-values"), CharDelimiterFluxDataBufferHandler.class);
		contentHandlerFactory.addServiceRequestHandler(MediaType.APPLICATION_FORM_URLENCODED, FormDataHandler.class);
		contentHandlerFactory.addServiceRequestHandler(MediaType.APPLICATION_JSON, JSONObject2StringHandler.class);
		contentHandlerFactory.addServiceRequestHandler(MediaType.APPLICATION_JSON, DataBuffer2JSONObjectHandler.class);
		contentHandlerFactory.addServiceRequestHandler(MediaType.MULTIPART_FORM_DATA, MultipartHandler.class);
		contentHandlerFactory.addServiceRequestHandler(MediaType.TEXT_EVENT_STREAM, DataBuffer2ServerSentEventHandler.class);
		contentHandlerFactory.addServiceRequestHandler(MediaType.TEXT_PLAIN, TextPlainHandler.class);
		contentHandlerFactory.addServiceRequestHandler(MediaType.TEXT_PLAIN, DataBuffer2TextPlainHandler.class);
		contentHandlerFactory.addServiceRequestHandler(MediaType.ALL, MultiDataBufferHandler.class);
		
		return contentHandlerFactory;
	}
	
	@Bean
	DataTransformerFactory dataTransformerFactory(AutowireCapableBeanFactory autowireCapableBeanFactory) {
		DataTransformerFactory dataTransformerFactory = new DataTransformerFactory();
		dataTransformerFactory.setAutowireCapableBeanFactory(autowireCapableBeanFactory);
		
		dataTransformerFactory.addDataTransformer(MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, JSONObjectTransformer.class);
		dataTransformerFactory.addDataTransformer(MediaType.APPLICATION_JSON, MediaType.APPLICATION_NDJSON, JSONObjectTransformer.class);
		
		dataTransformerFactory.addDataTransformer(MediaType.TEXT_EVENT_STREAM, MediaType.TEXT_EVENT_STREAM, TextEventStreamTransformer.class);
		dataTransformerFactory.addDataTransformer(MediaType.MULTIPART_FORM_DATA, MediaType.MULTIPART_FORM_DATA, MultipartDataTransformer.class);
		dataTransformerFactory.addDataTransformer(MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_FORM_URLENCODED, FormDataTransformer.class);
		dataTransformerFactory.addDataTransformer(MediaType.valueOf("text/semi-colon-seperated-values"), MediaType.valueOf("text/tab-separated-values"), SSV2TSVCharTransformer.class);
		
		dataTransformerFactory.addDataTransformer(MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON, JSONGraphTransformer.class);
		
		dataTransformerFactory.addDataTransformer(MediaType.TEXT_PLAIN, MediaType.TEXT_PLAIN, TextPlainDataTransformer.class);
		dataTransformerFactory.addDataTransformer(MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_OCTET_STREAM, BinaryDataTransformer.class);
		
		dataTransformerFactory.addDataTransformer(MediaType.ALL, MediaType.ALL, PassDataTransformer.class);
		
		return dataTransformerFactory;
	}

	@Bean
	WebSocketService webSocketService() {
		ReactorNettyRequestUpgradeStrategy strategy = new ReactorNettyRequestUpgradeStrategy();

		return new HandshakeWebSocketService(strategy);
	}



}
