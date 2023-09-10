package dagateway.api.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;

import dagateway.api.context.RouteContext;


public class ServerWebExchangeUtils {
	private static final Logger log = LoggerFactory.getLogger(ServerWebExchangeUtils.class);
	
	public static final String ROUTECONTEXT_ATTRIBUTE = qualify("routeContext");
	public static final String URI_TEMPLATE_VARIABLES_ATTRIBUTE = qualify("uriTemplateVariables");
	public static final String GATEWAY_ALREADY_ROUTED_ATTRIBUTE = qualify("gatewayAlreadyRouted");
	
	private static final AntPathMatcher antPathMatcher = new AntPathMatcher();

	
	private ServerWebExchangeUtils() {
	}
	
	public static AntPathMatcher getAntPathMatcher() {
		return ServerWebExchangeUtils.antPathMatcher;
	}
	
	public static void putUriTemplateVariables(ServerWebExchange serverWebExchange, Map<String, String> uriVars) {
		Map<String, Object> attributes = serverWebExchange.getAttributes();
		if(attributes.containsKey(ServerWebExchangeUtils.URI_TEMPLATE_VARIABLES_ATTRIBUTE)) {
			@SuppressWarnings("unchecked")
			Map<String, Object> existingVariables = (Map<String, Object>) attributes.get(URI_TEMPLATE_VARIABLES_ATTRIBUTE);
			
			Map<String, Object> newVariables = new HashMap<>();
			newVariables.putAll(existingVariables);
			newVariables.putAll(uriVars);
			
			System.out.println(newVariables);
			attributes.put(ServerWebExchangeUtils.URI_TEMPLATE_VARIABLES_ATTRIBUTE, newVariables);
		} else {
			attributes.put(ServerWebExchangeUtils.URI_TEMPLATE_VARIABLES_ATTRIBUTE, uriVars);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String, String> getUriTemplateVariables(ServerWebExchange serverWebExchange) {
		Map<String, Object> attributes = serverWebExchange.getAttributes();
		return (Map<String, String>) attributes.getOrDefault(ServerWebExchangeUtils.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Collections.emptyMap());
	}
	
	public static void putRouteContext(ServerWebExchange serverWebExchange, RouteContext routeContext) {
		Map<String, Object> attributes = serverWebExchange.getAttributes();
		attributes.put(ServerWebExchangeUtils.ROUTECONTEXT_ATTRIBUTE, routeContext);
	}
	
	public static RouteContext getRouteContext(ServerWebExchange serverWebExchange) {
		Map<String, Object> attributes = serverWebExchange.getAttributes();
		return (RouteContext) attributes.get(ServerWebExchangeUtils.ROUTECONTEXT_ATTRIBUTE);
	}
	
	public static RouteContext getRouteContext(ServerRequest serverRequest) {
		Optional<Object> routeContextOptional = serverRequest.attribute(ServerWebExchangeUtils.ROUTECONTEXT_ATTRIBUTE);
		if(routeContextOptional.isEmpty()) {
			ServerWebExchangeUtils.log.debug("RouteContext is not found in ServerRequest");
			return ServerWebExchangeUtils.getRouteContext(serverRequest.exchange());
		}
		return (RouteContext)routeContextOptional.get();
	}
	
	private static String qualify(String attr) {
		return ServerWebExchangeUtils.class.getName() + "." + attr;
	}



}
