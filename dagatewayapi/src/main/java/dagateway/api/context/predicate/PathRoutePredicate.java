package dagateway.api.context.predicate;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.server.PathContainer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPattern.PathMatchInfo;
import org.springframework.web.util.pattern.PathPatternParser;

import dagateway.api.utils.ServerWebExchangeUtils;


/**
 * @see org.springframework.cloud.gateway.handler.predicate.PathRoutePredicateFactory
 * @author Dong-il Cho
 */
public class PathRoutePredicate implements RoutePredicate {	
	private List<PathPattern> paths = new ArrayList<>();
	
	
	public PathRoutePredicate() {
	}

	@Override
	public void setValues(String... values) {
		if(values == null || values.length == 0) {
			throw new IllegalArgumentException("value must be not empty");
		}
		PathPatternParser pathPatternParser = new PathPatternParser(); // AntPathMatcher
		pathPatternParser.setMatchOptionalTrailingSeparator(false);
		for(String path : values) {
			this.paths.add(pathPatternParser.parse(path));
		}
	}

	@Override
	public boolean test(ServerWebExchange serverWebExchange) {
		ServerHttpRequest serverHttpRequest = serverWebExchange.getRequest();
		PathContainer path = serverHttpRequest.getPath();
		
		for(PathPattern pattern : this.paths) {
			if(pattern.matches(path)) {
				PathMatchInfo pathMatchInfo = pattern.matchAndExtract(path);
				ServerWebExchangeUtils.putUriTemplateVariables(serverWebExchange, pathMatchInfo.getUriVariables());
				
				return true;
			}
		}
		
		return false;
	}

	public List<PathPattern> getPaths() {
		return this.paths;
	}

	public void setPaths(List<PathPattern> paths) {
		this.paths = paths;
	}

	@Override
	public String toString() {
		return "PathRoutePredicate [paths=" + paths + "]";
	}


}
