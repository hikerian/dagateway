package dagateway.server;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.ReflectionHints;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;



/**
 * @author Dong-il Cho
 */
public class GatewayRuntimeHints implements RuntimeHintsRegistrar {

	@Override
	public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
		hints.resources().registerPattern("backend/*")
			.registerPattern("route/*");
		
		ReflectionHints reflection = hints.reflection();
		reflection.registerType(net.minidev.json.JSONObject.class, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
		
		reflection.registerType(dagateway.api.context.BackendServer.class, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
		reflection.registerType(dagateway.api.context.BackendServers.class, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
		reflection.registerType(dagateway.api.context.GatewayContext.class, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
		reflection.registerType(dagateway.api.context.GatewayRouteContext.class, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
		reflection.registerType(dagateway.api.context.RouteRequestContext.class, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
		reflection.registerType(dagateway.api.context.predicate.CookieRoutePredicate.class, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
		reflection.registerType(dagateway.api.context.predicate.HeaderRoutePredicate.class, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
		reflection.registerType(dagateway.api.context.predicate.HostRoutePredicate.class, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
		reflection.registerType(dagateway.api.context.predicate.MethodRoutePredicate.class, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
		reflection.registerType(dagateway.api.context.predicate.PathRoutePredicate.class, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
		reflection.registerType(dagateway.api.context.predicate.RoutePredicateBuilder.class, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
		reflection.registerType(dagateway.api.context.route.ClientRequest.class, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
		reflection.registerType(dagateway.api.context.route.ClientResponse.class, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
		reflection.registerType(dagateway.api.context.route.ContentHandling.class, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
		reflection.registerType(dagateway.api.context.route.EndpointType.class, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
		reflection.registerType(dagateway.api.context.route.GatewayRoute.class, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
		reflection.registerType(dagateway.api.context.route.GatewayRoutes.class, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
		reflection.registerType(dagateway.api.context.route.HeaderProperties.class, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
		reflection.registerType(dagateway.api.context.route.ServiceEndpoint.class, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
		reflection.registerType(dagateway.api.context.route.ServiceRequest.class, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
		reflection.registerType(dagateway.api.context.route.ServiceRequestBody.class, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
		reflection.registerType(dagateway.api.context.route.ServiceResponse.class, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
		reflection.registerType(dagateway.api.context.route.ServiceResponseBody.class, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
		reflection.registerType(dagateway.api.context.route.ServiceTarget.class, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
		reflection.registerType(dagateway.api.context.route.TransformRule.class, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
		
	}
	
//	public static void main(String[] args) {
//		String codebase = "C:/work/dev/workspaces/bff/nativegateway/src/main/java/";
//		
//		String[] paths = {
//			"C:/work/dev/workspaces/bff/nativegateway/src/main/java/dagateway/api/context"
//			, "C:/work/dev/workspaces/bff/nativegateway/src/main/java/dagateway/api/context/predicate"
//			, "C:/work/dev/workspaces/bff/nativegateway/src/main/java/dagateway/api/context/route"
//		};
//		
//		String[] exclude = {
//			"dagateway.api.context.AttributeOwner.class"
//			, "dagateway.api.context.predicate.RoutePredicate.class"
//		};
//		
//		StringBuilder builder = new StringBuilder();
//		for(String path : paths) {
//			java.io.File dir = new java.io.File(path);
//			java.io.File[] files = dir.listFiles();
//			
//			fileloop: for(java.io.File file : files) {
//				if(file.isDirectory()) {
//					continue fileloop;
//				}
//				String filePath = file.getAbsolutePath();
//				filePath = filePath.substring(codebase.length());
//				filePath = filePath.replaceAll("[\\\\]", ".");
//				filePath = filePath.substring(0, filePath.lastIndexOf(".")) + ".class";
//				
//				for(String ex : exclude) {
//					if(filePath.equals(ex)) {
//						continue fileloop;
//					}
//				}
//				
//				builder.append("reflection.registerType(");
//				builder.append(filePath);
//				builder.append(", MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);\n");
//			}
//		}
//		
//		System.out.println(builder.toString());
//	}

}
