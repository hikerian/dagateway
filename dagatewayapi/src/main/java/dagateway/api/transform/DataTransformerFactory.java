package dagateway.api.transform;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.http.MediaType;

import dagateway.api.context.RouteContext.TransformSpec;
import dagateway.api.utils.Utils;


public class DataTransformerFactory {
	private final Logger log = LoggerFactory.getLogger(DataTransformerFactory.class);
	
	private AutowireCapableBeanFactory autowireCapableBeanFactory;
	private Map<DataTransformerId, Class<? extends DataTransformer<?, ?>>> transformers = new ConcurrentHashMap<>();
	
	
	public DataTransformerFactory() {
	}
	
	public void setAutowireCapableBeanFactory(AutowireCapableBeanFactory autowireCapableBeanFactory) {
		this.autowireCapableBeanFactory = autowireCapableBeanFactory;
	}
	
	public void addDataTransformer(MediaType from, MediaType to, Class<? extends DataTransformer<?, ?>> transformerClass) {
		ParameterizedType handlerType = (ParameterizedType)transformerClass.getGenericSuperclass();
		Type[] supportType = handlerType.getActualTypeArguments();
		
		DataTransformerId handlerId = new DataTransformerId(from, to, supportType[0], supportType[1]);
		this.log.warn("ServiceResponseHandler: " + handlerId);
		
		Class<? extends DataTransformer<?, ?>> oldHandler = this.transformers.get(handlerId);
		if(oldHandler != null) {
			this.log.warn("Handler: " + handlerId + " is already exists. It will be replaced." + oldHandler);
		}
		this.transformers.put(handlerId, transformerClass);
	}
	
	public <T, V> DataTransformer<T, V> newDataTransformer(MediaType from, MediaType to, Type argumentType, Type returnType, TransformSpec transformRule) {
		@SuppressWarnings("unchecked")
		Class<? extends DataTransformer<T, V>> transformerClass = (Class<? extends DataTransformer<T, V>>) this.searchAcceptableTransformer(from, to, argumentType, returnType);
		DataTransformer<T, V> transformer = Utils.newInstance(transformerClass);
		
		transformer.init(transformRule);
		
		this.autowireCapableBeanFactory.autowireBean(transformer);
		
		return transformer;
	}
	
	private Class<? extends DataTransformer<?, ?>> searchAcceptableTransformer(MediaType from, MediaType to, Type argumentType, Type returnType) {
		Set<Map.Entry<DataTransformerId, Class<? extends DataTransformer<?, ?>>>> entrySet = this.transformers.entrySet();
		
		String type1 = argumentType.getTypeName();
		String type2 = returnType.getTypeName();
		
		for(Map.Entry<DataTransformerId, Class<? extends DataTransformer<?, ?>>> entry : entrySet) {
			DataTransformerId id = entry.getKey();
			if(id.getFrom().equalsTypeAndSubtype(from) && id.getTo().equalsTypeAndSubtype(to) && id.getSrcType().equals(type1) && id.getRtnType().equals(type2)) {
				return entry.getValue();
			}
		}
		
		for(Map.Entry<DataTransformerId, Class<? extends DataTransformer<?, ?>>> entry : entrySet) {
			DataTransformerId id = entry.getKey();
			if(id.getFrom().isCompatibleWith(from) && id.getTo().isCompatibleWith(to) && id.getSrcType().equals(type1) && id.getRtnType().equals(type2)) {
				return entry.getValue();
			}
		}
		
		throw new UnsupportedOperationException("FROM: " + from + ", TO: " + to + ", SRC: " + type1 + ", RTN: " + type2);
	}
	

}
