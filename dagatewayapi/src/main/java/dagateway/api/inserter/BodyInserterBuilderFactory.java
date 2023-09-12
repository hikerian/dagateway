package dagateway.api.inserter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.web.reactive.function.BodyInserter;

import dagateway.api.utils.Utils;


public class BodyInserterBuilderFactory {
	private Map<String, BodyInserterBuilder<?, ?>> bodyInserterBuilders = new ConcurrentHashMap<>();
	
	
	public BodyInserterBuilderFactory() {
		// initialize
		this.addBodyInserterBuilder(DoubleFluxDataBufferInserterBuilder.class);
		this.addBodyInserterBuilder(FluxDataBufferInserterBuilder.class);
		this.addBodyInserterBuilder(FluxServerSentEventInserterBuilder.class);
		this.addBodyInserterBuilder(JSONObjectInserterBuilder.class);
		this.addBodyInserterBuilder(MultipartInserterBuilder.class);
		this.addBodyInserterBuilder(MultiValueMapInserterBuilder.class);
	}
	
	public void addBodyInserterBuilder(Class<? extends BodyInserterBuilder<?, ?>> builderClass) {
		ParameterizedType handlerType = (ParameterizedType)builderClass.getGenericSuperclass();
		Type[] types = handlerType.getActualTypeArguments();
		String inserterId = types[0].getTypeName();
		
		this.bodyInserterBuilders.put(inserterId, Utils.newInstance(builderClass));
	}
	
	public <P, M extends ReactiveHttpOutputMessage> BodyInserter<?, M> getBodyInserter(String typeName, P data) {
		@SuppressWarnings("unchecked")
		BodyInserterBuilder<P, M> bodyInserter = (BodyInserterBuilder<P, M>) this.bodyInserterBuilders.get(typeName);
		if(bodyInserter == null) {
			return null;
		}
		return bodyInserter.getBodyInserter(data);
	}
	
}
