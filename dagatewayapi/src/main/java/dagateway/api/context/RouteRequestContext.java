package dagateway.api.context;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
import dagateway.api.context.route.ClientRequest;
import dagateway.api.context.route.ClientResponse;
import dagateway.api.context.route.ContentHandling;
import dagateway.api.context.route.EndpointType;
import dagateway.api.context.route.HeaderProperties;
import dagateway.api.context.route.ServiceEndpoint;
import dagateway.api.context.route.ServiceRequest;
import dagateway.api.context.route.ServiceRequestBody;
import dagateway.api.context.route.ServiceResponse;
import dagateway.api.context.route.ServiceResponseBody;
import dagateway.api.context.route.ServiceTarget;
import dagateway.api.context.route.TransformRule;
import dagateway.api.utils.ServerWebExchangeUtils;



/**
 * @author Dong-il Cho
 */
public class RouteRequestContext implements AttributeOwner {
	private final Logger log = LoggerFactory.getLogger(RouteRequestContext.class);
	
	public static final MediaType NONE = MediaType.valueOf("none/none");
	
	private URI requestURI = null;
	private HttpMethod requestMethod = null;
	private HttpHeaders requestHeaders = null;
	private MediaType requestContentType = null;
	private GatewayRouteContext gatewayRoute = null;
	private Map<String, BackendServer> backendServers = null;
	private Map<String, String> uriTemplateVars = null;
	private MessageSchema messageStructure = null;
	
	
	public RouteRequestContext(ServerWebExchange serverWebExchange, GatewayRouteContext gatewayRoute, Map<String, BackendServer> backendServers) {
		ServerHttpRequest request = serverWebExchange.getRequest();
		this.requestURI = request.getURI();
		this.requestMethod = request.getMethod();
		this.requestHeaders = request.getHeaders();
		this.requestContentType = request.getHeaders().getContentType();
		if(this.requestContentType == null) {
			this.requestContentType = RouteRequestContext.NONE;
		}
		this.gatewayRoute = gatewayRoute;
		this.backendServers = new ConcurrentHashMap<>(backendServers);
		
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
				.map(target -> {
					ServiceEndpoint endpoint = target.getEndpoint();
					String backendName = endpoint.getBackendName();
					BackendServer backendServer = this.backendServers.get(backendName);
					return new ServiceSpec(target, backendServer, this);
				})
				.collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
		
		return serviceSpecs;
	}
	
	public void setMessageStructure(MessageSchema messageStructure) {
		this.messageStructure = messageStructure;
	}
	
	public MessageSchema getMessageStructure() {
		return this.messageStructure;
	}
	
	public Object getAttribute(String name) {
		return this.gatewayRoute.getAttribute(name);
	}
	
	public void setAttribute(String name, Object value) {
		this.gatewayRoute.setAttribute(name, value);
	}
	
	/*
	 * SubClasses...
	 */	
	public static class ResponseSpec {
		private ClientResponse clientResponse;
		@SuppressWarnings("unused")
		private RouteRequestContext routeContext;
		
		
		ResponseSpec(ClientResponse clientResponse, RouteRequestContext routeContext) {
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
	
	public static class ServiceSpec implements AttributeOwner {
		private String path;
		private ServiceTarget target;
		private BackendServer backendServer;
		private RouteRequestContext routeContext;
		private DataProxy dataProxy;
		
		
		ServiceSpec(ServiceTarget target, BackendServer backendServer, RouteRequestContext routeContext) {
			this.path = "service(" + target.getName() + ")";
			this.target = target;
			this.backendServer = backendServer;
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
			return this.backendServer.getUrl();
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
			String backendUri = this.getEndpointUri();
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
			
			return serviceRequestType == null || MediaType.ALL.equalsTypeAndSubtype(serviceRequestType) 
					? this.getAggregateType() : serviceRequestType;
		}
		
		public TransformSpec getServiceRequestTransformSpec() {
			ServiceRequest serviceRequest = this.target.getRequest();
			if(serviceRequest != null) {
				ServiceRequestBody serviceRequestBody = serviceRequest.getBody();
				if(serviceRequestBody != null) {
					TransformRule transform = serviceRequestBody.getTransform();
					return new TransformSpec("request", this, transform);
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
		
		public TransformSpec getServiceResponseTransformSpec() {
			ServiceResponse serviceResponse = this.target.getResponse();
			if(serviceResponse != null) {
				ServiceResponseBody serviceResponseBody = serviceResponse.getBody();
				if(serviceResponseBody != null) {
					List<TransformRule> transforms = serviceResponseBody.getTransform();
					for(TransformRule transform : transforms) {
						return new TransformSpec("response", this, transform);
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
							return new TransformSpec("response", this, transform);
						}
					}
					for(TransformRule transform : transforms) {
						MediaType transformContentType = transform.getContentType();
						if(transformContentType.isCompatibleWith(contentType)) {
							return new TransformSpec("response", this, transform);
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
		
		public Object getAttribute(String name) {
			return this.routeContext.getAttribute(this.path + "." + name);
		}
		
		public void setAttribute(String name, Object value) {
			this.routeContext.setAttribute(this.path + "." + name, value);
		}

		@Override
		public String toString() {
			return "ServiceSpec [path=" + this.path + "]";
		}
		
	}
	
	public static class TransformSpec implements AttributeOwner {
		private String path;
		private ServiceSpec serviceSpec;
		private TransformRule transformRule;
		
		public TransformSpec(String parentPath, ServiceSpec serviceSpec, TransformRule transformRule) {
			this.path = parentPath + ".tranform";
			this.serviceSpec = serviceSpec;
			this.transformRule = transformRule;
		}
		
		public MediaType getContentType() {
			return this.transformRule.getContentType();
		}
		
		public String getBodyGraph() {
			return this.transformRule.getBodyGraph();
		}
		
		public Object getAttribute(String name) {
			return this.serviceSpec.getAttribute(this.path + "." + name);
		}
		
		public void setAttribute(String name, Object value) {
			this.serviceSpec.setAttribute(this.path + "." + name, value);
		}

	}


}
