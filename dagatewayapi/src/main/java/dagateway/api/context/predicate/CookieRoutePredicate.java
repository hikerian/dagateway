package dagateway.api.context.predicate;

import java.util.List;

import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;


/**
 * @author Dong-il Cho
 */
public class CookieRoutePredicate implements RoutePredicate {
	private String name;
	private String regexp;
	
	
	public CookieRoutePredicate() {
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
		MultiValueMap<String, HttpCookie> cookieMap = serverHttpRequest.getCookies();
		List<HttpCookie> cookieList = cookieMap.get(this.name);
		if(cookieList == null) {
			return false;
		}
		for(HttpCookie cookie : cookieList) {
			if(cookie.getValue().matches(this.regexp)) {
				return true;
			}
		}
		return false;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRegexp() {
		return this.regexp;
	}

	public void setRegexp(String regexp) {
		this.regexp = regexp;
	}

	@Override
	public String toString() {
		return "CookieRoutePredicate [name=" + name + ", regexp=" + regexp + "]";
	}
	

}
