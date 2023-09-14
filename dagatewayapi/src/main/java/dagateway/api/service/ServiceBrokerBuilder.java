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
import dagateway.api.context.ContentHandling;
import dagateway.api.context.EndpointType;
import dagateway.api.context.RouteContext;
import dagateway.api.context.RouteContext.ResponseSpec;
import dagateway.api.context.RouteContext.ServiceSpec;
import dagateway.api.handler.ContentHandlerFactory;
import dagateway.api.inserter.BodyInserterBuilderFactory;
import dagateway.api.resolver.http.ClientRequestResolver;
import dagateway.api.resolver.http.ClientResolverFactory;
import dagateway.api.resolver.http.ClientResponseResolver;
import dagateway.api.utils.Utils;
import graphql.ExecutionInput;
import graphql.ParseAndValidate;
import graphql.ParseAndValidateResult;
import graphql.execution.instrumentation.DocumentAndVariables;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public class ServiceBrokerBuilder {
	private final Logger log = LoggerFactory.getLogger(ServiceBrokerBuilder.class);
	
	private ContentHandlerFactory contentHandlerFactory;
	private ClientResolverFactory clientResolverFactory;
	private BodyInserterBuilderFactory bodyInserterBuilderFactory;
	
	
	public ServiceBrokerBuilder() {
	}
	
	public void init(ContentHandlerFactory contentHandlerFactory
			, ClientResolverFactory clientResolverFactory
			, BodyInserterBuilderFactory bodyInserterBuilderFactory) {

		this.contentHandlerFactory = contentHandlerFactory;
		this.clientResolverFactory = clientResolverFactory;
		this.bodyInserterBuilderFactory = bodyInserterBuilderFactory;
	}
	
	public <P extends Publisher<Cq>, Cq, Sr> ServiceBroker<P, Cq, Sr> build(RouteContext routeContext) {
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
	
	private <P extends Publisher<Cq>, Cq, Sr> ServiceBroker<P, Cq, Sr> buildMultiBackend(RouteContext routeContext, ResponseSpec responseSpec, List<ServiceSpec> serviceSpecList) {
		ClientRequestResolver<Mono<Cq>, Cq> requestResolver = this.clientResolverFactory.getClientRequestResolver(routeContext.getClientRequestType(), routeContext.getRequestAggregateType(), false);
		ClientResponseResolver<Flux<ServiceResult<Sr>>, Sr> responseResolver = this.clientResolverFactory.getClientResponseResolver(responseSpec.getContentHandling(), routeContext.getResponseType(), true);
		
		String requestResolverTypeName = requestResolver.getReturnTypeName();
		String resolverArgTypeName = responseResolver.getTypeArgument();
		
		this.log.debug("resolverArgTypeName: " + resolverArgTypeName);
		
		MultiServiceBroker<Cq, Sr> serviceBroker = new MultiServiceBroker<>(routeContext, requestResolver, responseResolver);
		
		for(ServiceSpec serviceSpec : serviceSpecList) {
			ServiceDelegator<Mono<Cq>, Cq, Sr> serviceDelegator = this.createServiceDelegator(requestResolverTypeName, resolverArgTypeName, serviceSpec);
			serviceBroker.addServiceDelegator(serviceDelegator);
		}
		
		return (ServiceBroker<P, Cq, Sr>) serviceBroker;
	}
	
	private <P extends Publisher<Cq>, Cq, Sr> ServiceBroker<P, Cq, Sr> buildSingleBackend(RouteContext routeContext, ResponseSpec responseSpec, ServiceSpec serviceSpec) {
		ClientRequestResolver<P, Cq> requestResolver = this.clientResolverFactory.getClientRequestResolver(routeContext.getClientRequestType(), routeContext.getRequestAggregateType());
		String requestResolverTypeName = requestResolver.getReturnTypeName();
		ClientResponseResolver<Mono<ServiceResult<Sr>>, Sr> responseResolver = this.clientResolverFactory.getClientResponseResolver(responseSpec.getContentHandling(), routeContext.getResponseType(), false);
		String resolverArgTypeName = responseResolver.getTypeArgument();
		
		this.log.debug("resolverArgTypeName: " + resolverArgTypeName);
		
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
	
	private void buildMessageStructure(RouteContext routeContext, ResponseSpec responseSpec, List<ServiceSpec> serviceSpecList) {
		// TODO additional context setting
		ContentHandling contentHandling = responseSpec.getContentHandling();
		if(contentHandling != ContentHandling.COMPOSE) {
			return;
		}
		
		String query = responseSpec.getBodyGraph();
		ExecutionInput executionInput = ExecutionInput.newExecutionInput(query).build();
		
		ParseAndValidateResult parseResult = ParseAndValidate.parse(executionInput);
		if(parseResult.isFailure()) {
			throw new IllegalArgumentException(parseResult.getSyntaxException());
		}
		DocumentAndVariables documentAndVariable = parseResult.getDocumentAndVariables();
		
		MessageSchema messageStructure = GraphQLComposerBuilder.buildAndMap(documentAndVariable, serviceSpecList);
		routeContext.setMessageStructure(messageStructure);
	}
	

}
