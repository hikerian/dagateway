package dagateway.api.context.predicate;

import java.util.Map;

import org.springframework.util.LinkedCaseInsensitiveMap;

import dagateway.api.context.RoutePredicate;
import dagateway.api.utils.Utils;


/**
 * Predicate는 ava.util.function.Predicate 를 사용하는 형태로 변경 필요.
 * @author chodo
 *
 */
public class RoutePredicateBuilder {
	private final Map<String, Class<? extends RoutePredicate>> predicateMap = new LinkedCaseInsensitiveMap<>();
	
	
	public RoutePredicateBuilder() {
		this.init();
	}
	
	private void init() {
		this.addPredicate("cookie", CookieRoutePredicate.class);
		this.addPredicate("header", HeaderRoutePredicate.class);
		this.addPredicate("method", MethodRoutePredicate.class);
		this.addPredicate("host", HostRoutePredicate.class);
		this.addPredicate("path", PathRoutePredicate.class);
	}
	
	public void addPredicate(String shortcut, Class<? extends RoutePredicate> predicateClass) {
		this.predicateMap.put(shortcut, predicateClass);
	}
	
	public RoutePredicate build(String shortcut) {
		String[] keyValue = Utils.splitAndTrim(shortcut, "=");
		if(keyValue.length != 2) {
			throw new IllegalArgumentException(shortcut);
		}
		String[] values = Utils.splitAndTrim(keyValue[1], ",");
		
		Class<? extends RoutePredicate> predicateClass = this.predicateMap.get(keyValue[0]);
		if(predicateClass == null) {
			throw new UnsupportedOperationException("name: " + keyValue[0]);
		}
		
		RoutePredicate predicate = Utils.newInstance(predicateClass);
		predicate.setValues(values);

		return predicate;
	}
	

}
