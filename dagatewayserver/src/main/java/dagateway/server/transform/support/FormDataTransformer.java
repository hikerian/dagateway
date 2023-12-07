package dagateway.server.transform.support;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import dagateway.api.service.ServiceFault;
import dagateway.api.transform.AbstractDataTransformer;



public class FormDataTransformer extends AbstractDataTransformer<MultiValueMap<String, String>, MultiValueMap<String, String>> {
	private final Logger log = LoggerFactory.getLogger(FormDataTransformer.class);
	
	public static final String ARGUMENT_TYPE = "org.springframework.util.MultiValueMap<java.lang.String, java.lang.String>";
	public static final String RETURN_TYPE = "org.springframework.util.MultiValueMap<java.lang.String, java.lang.String>";

	
	public FormDataTransformer() {
	}
	
	@Override
	protected void doInit() {
	}

	@Override
	public MultiValueMap<String, String> transform(MultiValueMap<String, String> source) {
		this.log.debug("transform");
		// TODO...
		
		LinkedMultiValueMap<String, String> newMap = new LinkedMultiValueMap<>();
		newMap.addAll(source);
		newMap.add("OptionalParam", "OptionalValue"); // 추가/변경/제거

		return newMap;
	}

	@Override
	public MultiValueMap<String, String> transform(ServiceFault fault) {
		this.log.debug("transform");
		
		// TODO Is this really acceptable?
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		Map<String, Object> faultMap = fault.toMap();
		Set<Map.Entry<String, Object>> entrySet = faultMap.entrySet();
		for(Map.Entry<String, Object> entry : entrySet) {
			map.set(entry.getKey(), entry.getValue().toString());
		}

		return map;
	}


}
