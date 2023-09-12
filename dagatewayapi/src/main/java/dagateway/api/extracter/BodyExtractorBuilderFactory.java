package dagateway.api.extracter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.ReactiveHttpInputMessage;
import org.springframework.web.reactive.function.BodyExtractor;

import dagateway.api.utils.Utils;


public class BodyExtractorBuilderFactory {
	private Map<String, BodyExtractorBuilder<?, ?>> bodyExtractorBuilders = new ConcurrentHashMap<>();
	
	
	public BodyExtractorBuilderFactory() {
		this.addBodyExtractorBuilder(FluxDataBufferExtractorBuilder.class);
		this.addBodyExtractorBuilder(JSONObjectExtractorBuilder.class);
		this.addBodyExtractorBuilder(MultipartExtractorBuilder.class);
		this.addBodyExtractorBuilder(MultiValueMapExtractorBuilder.class);
	}
	
	public void addBodyExtractorBuilder(Class<? extends BodyExtractorBuilder<?, ?>> builderClass) {
		ParameterizedType handlerType = (ParameterizedType)builderClass.getGenericSuperclass();
		Type[] types = handlerType.getActualTypeArguments();
		String inserterId = types[0].getTypeName();
		
		this.bodyExtractorBuilders.put(inserterId, Utils.newInstance(builderClass));
	}
	
	public <T, M extends ReactiveHttpInputMessage> BodyExtractor<T, M> getBodyExtractor(String typeName) {
		@SuppressWarnings("unchecked")
		BodyExtractorBuilder<T, M> bodyExtractorBuilder = (BodyExtractorBuilder<T, M>) this.bodyExtractorBuilders.get(typeName);
		if(bodyExtractorBuilder == null) {
			return null;
		}
		return bodyExtractorBuilder.getBodyExtractor();
		
	}
}
