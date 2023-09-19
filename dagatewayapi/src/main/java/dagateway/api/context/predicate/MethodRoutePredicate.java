package dagateway.api.context.predicate;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;


public class MethodRoutePredicate implements RoutePredicate {
	private List<HttpMethod> methods = new ArrayList<>();
	
	
	public MethodRoutePredicate() {
	}

	@Override
	public void setValues(String... values) {
		if(values == null || values.length == 0) {
			throw new IllegalArgumentException("value must be not empty");
		}
		
		for(String value : values) {
			HttpMethod method = HttpMethod.resolve(value);
			if(method == null) {
				throw new IllegalArgumentException("Unsupported method: " + value);
			}
			this.methods.add(method);
		}
	}

	@Override
	public boolean test(ServerWebExchange serverWebExchange) {
		ServerHttpRequest serverHttpRequest = serverWebExchange.getRequest();
		HttpMethod method = serverHttpRequest.getMethod();
		
		for(HttpMethod required : this.methods) {
			if(required == method) {
				return true;
			}
		}

		return false;
	}

	public List<HttpMethod> getMethods() {
		return this.methods;
	}

	public void setMethods(List<HttpMethod> methods) {
		this.methods = methods;
	}

	@Override
	public String toString() {
		return "MethodRoutePredicate [methods=" + methods + "]";
	}


}
