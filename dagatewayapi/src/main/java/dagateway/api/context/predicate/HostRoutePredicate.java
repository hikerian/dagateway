package dagateway.api.context.predicate;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;

import dagateway.api.utils.ServerWebExchangeUtils;


/**
 * @author Dong-il Cho
 */
public class HostRoutePredicate implements RoutePredicate {
	private AntPathMatcher antPathMatcher = new AntPathMatcher(".");

	private List<String> hosts = new ArrayList<>();
	
	
	public HostRoutePredicate() {
	}

	@Override
	public void setValues(String... values) {
		if(values == null || values.length == 0) {
			throw new IllegalArgumentException("value must be not empty");
		}
		for(String host : values) {
			this.hosts.add(host);
		}
	}

	@Override
	public boolean test(ServerWebExchange serverWebExchange) {
		ServerHttpRequest serverHttpRequest = serverWebExchange.getRequest();
		HttpHeaders headers = serverHttpRequest.getHeaders();
		InetSocketAddress hostheader = headers.getHost();
		String hostName = hostheader.getHostString();
		for(String hostPattern : this.hosts) {
			if(this.antPathMatcher.match(hostPattern, hostName)) {
				Map<String, String> variables = this.antPathMatcher.extractUriTemplateVariables(hostPattern, hostName);
				ServerWebExchangeUtils.putUriTemplateVariables(serverWebExchange, variables);
				return true;
			}
		}
		
		return false;
	}
	
	public List<String> getHosts() {
		return this.hosts;
	}

	public void setHosts(List<String> hosts) {
		this.hosts = hosts;
	}

	@Override
	public String toString() {
		return "HostRoutePredicate [hosts=" + hosts + "]";
	}

}
