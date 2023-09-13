package dagateway.api.context;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

import dagateway.api.composer.DataProxy;
import dagateway.api.composer.MessageSchema;
import dagateway.api.utils.ServerWebExchangeUtils;


public class RouteContext {
	private final Logger log = LoggerFactory.getLogger(RouteContext.class);
	
	public static final MediaType NONE = MediaType.valueOf("none/none");
	
	private URI requestURI = null;
	private HttpMethod requestMethod = null;
	private HttpHeaders requestHeaders = null;
	private MediaType requestContentType = null;
	private GatewayRoute gatewayRoute = null;
	private Map<String, String> uriTemplateVars = null;
	private MessageSchema messageStructure = null;
	
	
	public RouteContext(ServerWebExchange serverWebExchange, GatewayRoute gatewayRoute) {
		ServerHttpRequest request = serverWebExchange.getRequest();
		this.requestURI = request.getURI();
		this.requestMethod = request.getMethod();
		this.requestHeaders = request.getHeaders();
		this.requestContentType = request.getHeaders().getContentType();
		if(this.requestContentType == null) {
			this.requestContentType = RouteContext.NONE;
		}
		this.gatewayRoute = gatewayRoute;
		
		this.uriTemplateVars = ServerWebExchangeUtils.getUriTemplateVariables(serverWebExchange);
	}
	
	public Map<String, String> getVariables() {
		return this.uriTemplateVars;
	}
	
	public void setVariable(String name, String value) {
		this.uriTemplateVars.put(name, value);
	}
	
	public HttpHeaders getRequestHeaders() {
		return this.requestHeaders;
	}
	
	public MediaType getClientRequestType() {
		return this.requestContentType;
	}
	
	public MediaType getRequestAggregateType() {		
		ClientRequest request = this.gatewayRoute.getClientRequest();
		if(request == null) {
			return this.requestContentType;
		}
		
		MediaType aggregateType = request.getAggregateType();
		if(MediaType.ALL.equalsTypeAndSubtype(aggregateType)) {
			return this.requestContentType;
		}
		return aggregateType;
	}
	
	public ResponseSpec getResponseSpec() {
		ClientResponse response = this.gatewayRoute.getClientResponse();
		if(response == null) {
			return null;
		}
		
		return new ResponseSpec(response, this);
	}
	
	public MediaType getResponseType() {
		ClientResponse response = this.gatewayRoute.getClientResponse();
		if(response == null) {
			return null;
		}
		
		return response.getContentType();
	}
	
	public List<ServiceSpec> getServiceSpecList() {
		List<ServiceTarget> serviceTargetList = this.gatewayRoute.getServiceTargets();
		if(serviceTargetList == null || serviceTargetList.size() == 0) {
			return Collections.emptyList();
		}
		List<ServiceSpec> serviceSpecs = serviceTargetList.stream()
				.map(target -> new ServiceSpec(target, this))
				.collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
		
		return serviceSpecs;
	}
	
	public void setMessageStructure(MessageSchema messageStructure) {
		this.messageStructure = messageStructure;
	}
	
	public MessageSchema getMessageStructure() {
		return this.messageStructure;
	}
	
	/*
	 * SubClasses...
	 */	
	public static class ResponseSpec {
		private ClientResponse clientResponse;
		@SuppressWarnings("unused")
		private RouteContext routeContext;
		
		
		ResponseSpec(ClientResponse clientResponse, RouteContext routeContext) {
			this.clientResponse = clientResponse;
			this.routeContext = routeContext;
		}
		
		public MediaType getContentType() {
			return this.clientResponse.getContentType();
		}
		
		public ContentHandling getContentHandling() {
			return this.clientResponse.getContentHandling();
		}
		
		public HeaderSpec getResponseHeaders() {
			return new HeaderSpec(this.clientResponse.getHeader());
		}
		
		public String getBodyGraph() {
			return this.clientResponse.getBodyGraph();
		}
	}
	
	public static class HeaderSpec {
		private HeaderProperties headerSpec;
		
		public HeaderSpec(HeaderProperties responseHeader) {
			this.headerSpec = responseHeader == null ? new HeaderProperties() : responseHeader;
		}

		public List<HeaderProperties.HeaderEntry> getAdd() {
			return this.unmodifiableList(this.headerSpec.getAdd());
		}
		
		public List<HeaderProperties.HeaderEntry> getSet() {
			return this.unmodifiableList(this.headerSpec.getSet());
		}
		
		public List<String> getRetain() {
			return this.unmodifiableList(this.headerSpec.getRetain());
		}

		public List<HeaderProperties.HeaderEntry> getRename() {
			return this.unmodifiableList(this.headerSpec.getRename());
		}
		
		private <T> List<T> unmodifiableList(List<T> list) {
			if(list == null) {
				return Collections.emptyList();
			}
			return Collections.unmodifiableList(list);
		}
		
	}
	
	public static class ServiceSpec {
		private ServiceTarget target;
		private RouteContext routeContext;
		private DataProxy dataProxy;
		
		
		ServiceSpec(ServiceTarget target, RouteContext routeContext) {
			this.target = target;
			this.routeContext = routeContext;
		}
		
		public String getName() {
			return this.target.getName();
		}
		
		public Map<String, String> getVariables() {
			return this.routeContext.uriTemplateVars;
		}
		
		public void setDataProxy(DataProxy dataProxy) {
			this.dataProxy = dataProxy;
		}
		
		public DataProxy getDataProxy() {
			return this.dataProxy;
		}
		
		/*
		 * Endpoint
		 */
		
		public EndpointType getEndpointType() {
			return this.target.getEndpoint().getType();
		}
		
		public String getEndpointUri() {
			return this.target.getEndpoint().getUri();
		}
		
		public String getEndpointPath() {
			return this.target.getEndpoint().getPath();
		}
		
		/*
		 * Request
		 */
		
		public HttpMethod getMethod() {
			ServiceRequest request = this.target.getRequest();
			if(request == null) {
				return this.routeContext.requestMethod;
			}
			HttpMethod requestMethod = request.getMethod();
			return requestMethod == null ? this.routeContext.requestMethod : requestMethod;
		}

		public URI createBackendURI() {
			ServiceEndpoint endpoint = this.target.getEndpoint();
			String backendUri = endpoint.getUri();
			String pathPattern = endpoint.getPath();
			pathPattern = pathPattern.replaceAll("[$][{]", "{");
			
			Map<String, String> uriTemplateVars = this.routeContext.uriTemplateVars;
			UriTemplate pathTemplate = new UriTemplate(pathPattern);
			URI pathuri = pathTemplate.expand(uriTemplateVars);
			
			URI backendRequestURI = UriComponentsBuilder.fromUriString(backendUri)
					.path(pathuri.getRawPath().replaceAll("[/]+", "/"))
					.query(this.routeContext.requestURI.getQuery())
					.build().toUri();
			
			this.routeContext.log.debug(backendRequestURI.toString());
			
			return backendRequestURI;
		}
		
		public MediaType getAggregateType() {
			return this.routeContext.getRequestAggregateType();
		}

		public MediaType getServiceRequestType() {
			MediaType serviceRequestType = null;
			ServiceRequest serviceRequest = this.target.getRequest();
			if(serviceRequest != null) {
				ServiceRequestBody serviceRequestBody = serviceRequest.getBody();
				if(serviceRequestBody != null) {
					TransformRule transformRule = serviceRequestBody.getTransform();
					if(transformRule != null) {
						serviceRequestType = transformRule.getContentType();
					}
				}
			}
			
			return serviceRequestType == null || MediaType.ALL.equalsTypeAndSubtype(serviceRequestType) ? this.getAggregateType() : serviceRequestType;
		}
		
		public TransformSpec getServiceRequestTransformSpec() {
			ServiceRequest serviceRequest = this.target.getRequest();
			if(serviceRequest != null) {
				ServiceRequestBody serviceRequestBody = serviceRequest.getBody();
				if(serviceRequestBody != null) {
					TransformRule transform = serviceRequestBody.getTransform();
					return new TransformSpec(transform);
				}
			}
			
			return null;
		}
		
		public HttpHeaders getClientRequestHeaders() {
			return this.routeContext.getRequestHeaders();
		}
		
		/*
		 * Response
		 */
		
		public MediaType getClientResponseType() {
			return this.routeContext.getResponseType();
		}
		
		public MediaType getServiceResponseType() {
			ServiceResponse serviceResponse = this.target.getResponse();
			if(serviceResponse != null) {
				ServiceResponseBody serviceResponseBody = serviceResponse.getBody();
				if(serviceResponseBody != null) {
					List<TransformRule> transforms = serviceResponseBody.getTransform();
					if(transforms != null && transforms.size() > 0) {
						return transforms.get(0).getContentType();
					}
				}
			}
			return null;
		}
		
		/**
		 * 
		 * @param contentType ServiceResponse ContentType
		 * @return
		 */
		public TransformSpec getServiceResponseTransformSpec(MediaType contentType) {
			ServiceResponse serviceResponse = this.target.getResponse();
			if(serviceResponse != null) {
				ServiceResponseBody serviceResponseBody = serviceResponse.getBody();
				if(serviceResponseBody != null) {
					List<TransformRule> transforms = serviceResponseBody.getTransform();
					for(TransformRule transform : transforms) {
						MediaType transformContentType = transform.getContentType();
						if(transformContentType.equalsTypeAndSubtype(contentType)) {
							return new TransformSpec(transform);
						}
					}
					for(TransformRule transform : transforms) {
						MediaType transformContentType = transform.getContentType();
						if(transformContentType.isCompatibleWith(contentType)) {
							return new TransformSpec(transform);
						}
					}
				}
			}
			return null;
		}
		
		public HeaderSpec getServiceRequestHeaderSpec() {
			ServiceRequest serviceRequest = this.target.getRequest();
			if(serviceRequest != null) {
				HeaderProperties serviceHeader = serviceRequest.getHeader();
				if(serviceHeader != null) {
					return new HeaderSpec(serviceHeader);
				}
			}
			
			return null;
		}
		
	}
	
	public static class TransformSpec {
		private TransformRule transformRule;
		
		public TransformSpec(TransformRule transformRule) {
			this.transformRule = transformRule;
		}
		
		public MediaType getContentType() {
			return this.transformRule.getContentType();
		}
		
		public String getBodyGraph() {
			return this.transformRule.getBodyGraph();
		}
	}

}
