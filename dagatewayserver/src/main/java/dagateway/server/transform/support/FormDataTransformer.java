package dagateway.server.transform.support;

import org.springframework.util.MultiValueMap;

import dagateway.api.transform.AbstractDataTransformer;



public class FormDataTransformer extends AbstractDataTransformer<MultiValueMap<String, String>, MultiValueMap<String, String>> {

	
	public FormDataTransformer() {
	}
	
	@Override
	protected void doInit() {
	}

	@Override
	public MultiValueMap<String, String> transform(MultiValueMap<String, String> source) {
		// TODO
		source.add("OptionalParam", "OptionalValue"); // 추가/변경/제거

		return source;
	}

}
