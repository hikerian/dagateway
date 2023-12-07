package dagateway.api.service;

import java.util.List;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec;

import dagateway.api.composer.MessageSchema;
import dagateway.api.composer.graphql.GraphQLComposerBuilder;
import dagateway.api.context.RouteRequestContext;
import dagateway.api.context.RouteRequestContext.ResponseSpec;
import dagateway.api.context.RouteRequestContext.ServiceSpec;
import dagateway.api.context.route.ContentHandling;
import dagateway.api.context.route.EndpointType;
import dagateway.api.handler.ContentHandlerFactory;
import dagateway.api.inserter.BodyInserterBuilderFactory;
import dagateway.api.resolver.http.ClientRequestResolver;
import dagateway.api.resolver.http.ClientResolverFactory;
import dagateway.api.resolver.http.ClientResponseResolver;
import dagateway.api.utils.Utils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public class ServiceBrokerBuilder {
	private final Logger log = LoggerFactory.getLogger(ServiceBrokerBuilder.class);
	
	private ContentHandlerFactory contentHandlerFactory;
	private ClientResolverFactory clientResolverFactory;
	private BodyInserterBuilderFactory bodyInserterBuilderFactory;
	private ServiceExceptionResolver exceptionResolver;
	
	
	public ServiceBrokerBuilder() {
	}
	
	public void init(ContentHandlerFactory contentHandlerFactory
			, ClientResolverFactory clientResolverFactory
			, BodyInserterBuilderFactory bodyInserterBuilderFactory
			, ServiceExceptionResolver exceptionResolver) {

		this.contentHandlerFactory = contentHandlerFactory;
		this.clientResolverFactory = clientResolverFactory;
		this.bodyInserterBuilderFactory = bodyInserterBuilderFactory;
		this.exceptionResolver = exceptionResolver;
	}
	
	public <P extends Publisher<Cq>, Cq, Sr> ServiceBroker<P, Cq, Sr> build(RouteRequestContext routeContext) {
		this.log.debug("build");
		
		ResponseSpec responseSpec = routeContext.getResponseSpec();
		List<ServiceSpec> serviceSpecList = routeContext.getServiceSpecList();
		
		this.buildMessageStructure(routeContext, responseSpec, serviceSpecList);
		
		ServiceBroker<P, Cq, Sr> serviceBroker = null;
		
		if(serviceSpecList.size() == 1) {
			serviceBroker = this.buildSingleBackend(routeContext, responseSpec, serviceSpecList.get(0));
		} else if(serviceSpecList.size() > 1) {
			serviceBroker = this.buildMultiBackend(routeContext, responseSpec, serviceSpecList);
		}
		
		return serviceBroker;
	}
	
	@SuppressWarnings("unchecked")
	private <P extends Publisher<Cq>, Cq, Sr> ServiceBroker<P, Cq, Sr> buildMultiBackend(RouteRequestContext routeContext, ResponseSpec responseSpec, List<ServiceSpec> serviceSpecList) {
		ClientRequestResolver<Mono<Cq>, Cq> requestResolver = this.clientResolverFactory.getClientRequestResolver(routeContext.getClientRequestType(), routeContext.getRequestAggregateType(), false);
		ClientResponseResolver<Flux<ServiceResult<Sr>>, Sr> responseResolver = this.clientResolverFactory.getClientResponseResolver(responseSpec.getContentHandling(), routeContext.getResponseType(), true);
		
		String requestResolverTypeName = requestResolver.getReturnTypeName();
		String resolverArgTypeName = responseResolver.getTypeArgument();
		
//		this.log.debug("resolverArgTypeName: " + resolverArgTypeName);
		
		MultiServiceBroker<Cq, Sr> serviceBroker = new MultiServiceBroker<>(routeContext, requestResolver, responseResolver);
		
		for(ServiceSpec serviceSpec : serviceSpecList) {
			ServiceDelegator<Mono<Cq>, Cq, Sr> serviceDelegator = this.createServiceDelegator(requestResolverTypeName, resolverArgTypeName, serviceSpec);
			serviceBroker.addServiceDelegator(serviceDelegator);
		}
		
		return (ServiceBroker<P, Cq, Sr>) serviceBroker;
	}
	
	private <P extends Publisher<Cq>, Cq, Sr> ServiceBroker<P, Cq, Sr> buildSingleBackend(RouteRequestContext routeContext, ResponseSpec responseSpec, ServiceSpec serviceSpec) {
		ClientRequestResolver<P, Cq> requestResolver = this.clientResolverFactory.getClientRequestResolver(routeContext.getClientRequestType(), routeContext.getRequestAggregateType());
		String requestResolverTypeName = requestResolver.getReturnTypeName();
		ClientResponseResolver<Mono<ServiceResult<Sr>>, Sr> responseResolver = this.clientResolverFactory.getClientResponseResolver(responseSpec.getContentHandling(), routeContext.getResponseType(), false);
		String resolverArgTypeName = responseResolver.getTypeArgument();
		
//		this.log.debug("resolverArgTypeName: " + resolverArgTypeName);
		
		SingleServiceBroker<P, Cq, Sr> serviceBroker = new SingleServiceBroker<>(routeContext, requestResolver, responseResolver);
		
		ServiceDelegator<P, Cq, Sr> serviceDelegator = this.createServiceDelegator(requestResolverTypeName, resolverArgTypeName, serviceSpec);
		serviceBroker.setServiceDelegator(serviceDelegator);
		
		return serviceBroker;
	}
	
	private <P extends Publisher<Cq>, Cq, Sr> ServiceDelegator<P, Cq, Sr> createServiceDelegator(String requestResolverTypeName, String resolverArgTypeName, ServiceSpec serviceSpec) {
		HttpMethod method = serviceSpec.getMethod();
		
		WebClient webClient = Utils.newWebClient();
		RequestBodyUriSpec requestBodyUriSpec = webClient.method(method);
		requestBodyUriSpec.uri(serviceSpec.createBackendURI());
		
		ServiceDelegator<P, Cq, Sr> serviceDelegator = null;
		EndpointType endpointType = serviceSpec.getEndpointType();
		switch(endpointType) {
		case HTTP: {
			serviceDelegator = new ServiceDelegatorImpl<>(requestBodyUriSpec
					, this.contentHandlerFactory
					, this.bodyInserterBuilderFactory
					, this.exceptionResolver
					, serviceSpec
					, requestResolverTypeName
					, resolverArgTypeName);
			break;
		}
		// TODO add other case
		case WEBSOCKET:
		default:
			throw new UnsupportedOperationException(endpointType.getValue());
		}
		
		return serviceDelegator;
	}
	
	private void buildMessageStructure(RouteRequestContext routeContext, ResponseSpec responseSpec, List<ServiceSpec> serviceSpecList) {
		// TODO additional context setting
		ContentHandling contentHandling = responseSpec.getContentHandling();
		if(contentHandling != ContentHandling.COMPOSE) {
			return;
		}
		
		String query = responseSpec.getBodyGraph();
		MessageSchema messageStructure = GraphQLComposerBuilder.build(query, routeContext, GraphQLComposerBuilder.CLIENT_RESPONSE_GRAPH_KEY, serviceSpecList);
		if(messageStructure == null) {
			return;
		}
		
		routeContext.setMessageStructure(messageStructure);
	}
	

}
