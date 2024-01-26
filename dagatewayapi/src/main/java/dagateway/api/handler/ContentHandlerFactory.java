package dagateway.api.handler;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import dagateway.api.context.RouteRequestContext.TransformSpec;
import dagateway.api.transform.DataTransformer;
import dagateway.api.transform.DataTransformerFactory;



/**
 * @author Dong-il Cho
 */
public class ContentHandlerFactory {
	private final Logger log = LoggerFactory.getLogger(ContentHandlerFactory.class);

	private DataTransformerFactory dataTransformerFactory;
	
	private Map<ContentHandlerId, Supplier<? extends AbstractContentHandler<?, ?, ?, ?, ?>>> contentHandlers = new ConcurrentHashMap<>();
	
	
	
	public ContentHandlerFactory() {
	}
	
	public void init(DataTransformerFactory dataTransformerFactory) {
		this.dataTransformerFactory = dataTransformerFactory;
	}
	
	public void addServiceRequestHandler(MediaType from, String argType, String returnType, Supplier<? extends AbstractContentHandler<?, ?, ?, ?, ?>> supplier) {
		ContentHandlerId handlerId = new ContentHandlerId(from, argType, returnType);
		this.log.warn("ContentHandler: " + handlerId);
		
		Supplier<? extends AbstractContentHandler<?, ?, ?, ?, ?>> oldSupplier = this.contentHandlers.get(handlerId);
		if(oldSupplier != null) {
			this.log.warn("Handler: " + handlerId + " is already exists. It will be replaced." + oldSupplier);
		}
		this.contentHandlers.put(handlerId, supplier);
	}
	
	public <P extends Publisher<Cq>, Cq, T, V, R> ContentHandler<P, Cq, T, V, R> getContentHandler(MediaType fromType
			, MediaType toType, String argumentTypeName, TransformSpec transformRule) {

		// servicerequesthandler
		@SuppressWarnings("unchecked")
		Supplier<? extends AbstractContentHandler<P, Cq, T, V, R>> handlerSupplier =
				(Supplier<? extends AbstractContentHandler<P, Cq, T, V, R>>) this.searchAcceptableContentHandler((id) -> id.getFrom().equalsTypeAndSubtype(fromType)
						&& (id.getArgumentType().equals(argumentTypeName) || "*".equals(argumentTypeName))
						, (id) -> id.getFrom().isCompatibleWith(fromType)
						&& (id.getArgumentType().equals(argumentTypeName) || "*".equals(argumentTypeName)));

		if(handlerSupplier == null) {
			throw new UnsupportedOperationException("FROM: " + fromType);
		}
		
		return this.newInstance(handlerSupplier, fromType, toType, transformRule);
	}
	
	public <P extends Publisher<Cq>, Cq, T, V, R> ContentHandler<P, Cq, T, V, R> getContentHandler(MediaType fromType
			, MediaType toType, String argumentTypeName, String returnTypeName, TransformSpec transformRule) {

		@SuppressWarnings("unchecked")
		Supplier<? extends AbstractContentHandler<P, Cq, T, V, R>> handlerSupplier = 
		(Supplier<? extends AbstractContentHandler<P, Cq, T, V, R>>) this.searchAcceptableContentHandler(
				(id) -> id.getFrom().equalsTypeAndSubtype(fromType)
				&& (id.getArgumentType().equals(argumentTypeName) || "*".equals(argumentTypeName))
				&& (id.getReturnTypeName().equals(returnTypeName) || "*".equals(returnTypeName))
				, (id) -> id.getFrom().isCompatibleWith(fromType)
				&& (id.getArgumentType().equals(argumentTypeName) || "*".equals(argumentTypeName))
				&& (id.getReturnTypeName().equals(returnTypeName) || "*".equals(returnTypeName)));
		
		if(handlerSupplier == null) {
			throw new UnsupportedOperationException("FROM: " + fromType + ", ReturnTypeName: " + returnTypeName);
		}
		
		this.log.debug("ServiceResponseHandler: " + handlerSupplier);
		
		return this.newInstance(handlerSupplier, fromType, toType, transformRule);
	}
	
	/*
	 * helper methods...
	 */
	private Supplier<? extends AbstractContentHandler<?, ?, ?, ?, ?>> searchAcceptableContentHandler(Predicate<ContentHandlerId> firstPredicate, Predicate<ContentHandlerId> secondPredicate) {
		Set<Map.Entry<ContentHandlerId, Supplier<? extends AbstractContentHandler<?, ?, ?, ?, ?>>>> entrySet = this.contentHandlers.entrySet();
		for(Map.Entry<ContentHandlerId, Supplier<? extends AbstractContentHandler<?, ?, ?, ?, ?>>> entry : entrySet) {
			ContentHandlerId handlerId = entry.getKey();
			if(firstPredicate.test(handlerId) == true) {
				return entry.getValue();
			}
		}
		
		for(Map.Entry<ContentHandlerId, Supplier<? extends AbstractContentHandler<?, ?, ?, ?, ?>>> entry : entrySet) {
			ContentHandlerId handlerId = entry.getKey();
			if(secondPredicate.test(handlerId) == true) {
				return entry.getValue();
			}
		}
		return null;
	}
	
	private <P extends Publisher<Cq>, Cq, T, V, R> ContentHandler<P, Cq, T, V, R> newInstance(Supplier<? extends AbstractContentHandler<P, Cq, T, V, R>> handlerSupplier, MediaType fromType, MediaType toType, TransformSpec transformRule) {
		// transformer
		AbstractContentHandler<P, Cq, T, V, R> contentHandler = handlerSupplier.get();
		String transArgType = contentHandler.getTransArgumentTypeName();
		String transRtnType = contentHandler.getTransReturnTypeName();
		
		DataTransformer<T, V> dataTransformer = this.dataTransformerFactory.newDataTransformer(fromType, toType, transArgType, transRtnType, transformRule);

		contentHandler.init(dataTransformer, fromType, toType);
		
		this.log.debug("Contenthandler: " + contentHandler);
		
		return contentHandler;
	}


}
