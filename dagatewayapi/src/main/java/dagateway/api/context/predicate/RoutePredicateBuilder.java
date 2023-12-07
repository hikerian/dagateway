package dagateway.api.context.predicate;

import java.util.Map;
import java.util.function.Supplier;

import dagateway.api.utils.InsensitiveKeyMap;
import dagateway.api.utils.Utils;


/**
 * Predicate는 ava.util.function.Predicate 를 사용하는 형태로 변경 필요.
 * @author Dong-il Cho
 *
 */
public class RoutePredicateBuilder {
	private final Map<String, Supplier<? extends RoutePredicate>> predicateMap = new InsensitiveKeyMap<>();
	
	
	public RoutePredicateBuilder() {
		this.init();
	}
	
	private void init() {
		this.addPredicate("cookie", () -> new CookieRoutePredicate());
		this.addPredicate("header", () -> new HeaderRoutePredicate());
		this.addPredicate("method", () -> new MethodRoutePredicate());
		this.addPredicate("host", () -> new HostRoutePredicate());
		this.addPredicate("path", () -> new PathRoutePredicate());
	}
	
	public void addPredicate(String shortcut, Supplier<? extends RoutePredicate> predicateClass) {
		this.predicateMap.put(shortcut, predicateClass);
	}
	
	public RoutePredicate build(String shortcut) {
		String[] keyValue = Utils.splitAndTrim(shortcut, "=");
		if(keyValue.length != 2) {
			throw new IllegalArgumentException(shortcut);
		}
		String[] values = Utils.splitAndTrim(keyValue[1], ",");
		
		Supplier<? extends RoutePredicate> predicateSupplier = this.predicateMap.get(keyValue[0]);
		if(predicateSupplier == null) {
			throw new UnsupportedOperationException("name: " + keyValue[0]);
		}
		
		RoutePredicate predicate = predicateSupplier.get();
		predicate.setValues(values);

		return predicate;
	}
	

}
