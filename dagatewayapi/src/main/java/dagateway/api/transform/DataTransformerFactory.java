package dagateway.api.transform;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import dagateway.api.context.RouteRequestContext.TransformSpec;


public class DataTransformerFactory {
	private final Logger log = LoggerFactory.getLogger(DataTransformerFactory.class);

	private Map<DataTransformerId, Supplier<? extends DataTransformer<?, ?>>> transformers = new ConcurrentHashMap<>();
	
	
	public DataTransformerFactory() {
	}
	
	public void addDataTransformer(MediaType from, MediaType to, String argumentType, String returnType, Supplier<? extends DataTransformer<?, ?>> transformerSupplier) {
		DataTransformerId handlerId = new DataTransformerId(from, to, argumentType, returnType);
		this.log.warn("ServiceResponseHandler: " + handlerId);
		
		Supplier<? extends DataTransformer<?, ?>> oldSupplier = this.transformers.get(handlerId);
		if(oldSupplier != null) {
			this.log.warn("Handler: " + handlerId + " is already exists. It will be replaced." + oldSupplier);
		}
		this.transformers.put(handlerId, transformerSupplier);
	}
	
	public <T, V> DataTransformer<T, V> newDataTransformer(MediaType from, MediaType to, String argumentType, String returnType, TransformSpec transformRule) {
		@SuppressWarnings("unchecked")
		Supplier<? extends DataTransformer<T, V>> transformerSupplier = (Supplier<? extends DataTransformer<T, V>>) this.searchAcceptableTransformer(from, to, argumentType, returnType);
		DataTransformer<T, V> transformer = transformerSupplier.get();
		transformer.init(transformRule);
		
		return transformer;
	}
	
	private Supplier<? extends DataTransformer<?, ?>> searchAcceptableTransformer(MediaType from, MediaType to, String argumentType, String returnType) {
		Set<Map.Entry<DataTransformerId, Supplier<? extends DataTransformer<?, ?>>>> entrySet = this.transformers.entrySet();
		
		for(Map.Entry<DataTransformerId, Supplier<? extends DataTransformer<?, ?>>> entry : entrySet) {
			DataTransformerId id = entry.getKey();
			if(id.getFrom().equalsTypeAndSubtype(from) && id.getTo().equalsTypeAndSubtype(to) && id.getSrcType().equals(argumentType) && id.getRtnType().equals(returnType)) {
				return entry.getValue();
			}
		}
		
		for(Map.Entry<DataTransformerId, Supplier<? extends DataTransformer<?, ?>>> entry : entrySet) {
			DataTransformerId id = entry.getKey();
			if(id.getFrom().isCompatibleWith(from) && id.getTo().isCompatibleWith(to) && id.getSrcType().equals(argumentType) && id.getRtnType().equals(returnType)) {
				return entry.getValue();
			}
		}
		
		throw new UnsupportedOperationException("FROM: " + from + ", TO: " + to + ", SRC: " + argumentType + ", RTN: " + returnType);
	}


}
