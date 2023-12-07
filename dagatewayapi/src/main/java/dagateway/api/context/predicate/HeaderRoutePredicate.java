package dagateway.api.context.predicate;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;


/**
 * @author Dong-il Cho
 */
public class HeaderRoutePredicate implements RoutePredicate {
	private String name;
	private String regexp;

	
	public HeaderRoutePredicate() {
	}
	
	@Override
	public void setValues(String... values) {
		if(values == null || values.length != 2) {
			throw new IllegalArgumentException("size must be 2");
		}
		this.name = values[0];
		this.regexp = values[1];
	}

	@Override
	public boolean test(ServerWebExchange serverWebExchange) {
		ServerHttpRequest serverHttpRequest = serverWebExchange.getRequest();
		HttpHeaders headers = serverHttpRequest.getHeaders();
		List<String> headerList = headers.get(this.name);
		for(String header : headerList) {
			if(header.matches(this.regexp)) {
				return true;
			}
		}

		return false;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRegexp() {
		return regexp;
	}

	public void setRegexp(String regexp) {
		this.regexp = regexp;
	}

	@Override
	public String toString() {
		return "HeaderRoutePredicate [name=" + name + ", regexp=" + regexp + "]";
	}

}
