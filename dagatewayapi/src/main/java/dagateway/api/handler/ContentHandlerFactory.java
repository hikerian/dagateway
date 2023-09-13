package dagateway.api.handler;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.http.MediaType;

import dagateway.api.context.RouteContext.TransformSpec;
import dagateway.api.transform.DataTransformer;
import dagateway.api.transform.DataTransformerFactory;
import dagateway.api.utils.Utils;



public class ContentHandlerFactory {
	private final Logger log = LoggerFactory.getLogger(ContentHandlerFactory.class);
	
	private AutowireCapableBeanFactory autowireCapableBeanFactory;
	private DataTransformerFactory dataTransformerFactory;
	
	private Map<ContentHandlerId, Class<? extends AbstractContentHandler<?, ?, ?, ?, ?>>> contentHandlers =
			new ConcurrentHashMap<>();
	
	
	public ContentHandlerFactory() {
	}
	
	public void init(AutowireCapableBeanFactory autowireCapableBeanFactory, DataTransformerFactory dataTransformerFactory) {
		this.autowireCapableBeanFactory = autowireCapableBeanFactory;
		this.dataTransformerFactory = dataTransformerFactory;
	}
	
	public void addServiceRequestHandler(MediaType from, Class<? extends AbstractContentHandler<?, ?, ?, ?, ?>> handlerClass) {
		ParameterizedType genericSuperClass = (ParameterizedType) handlerClass.getGenericSuperclass();
		Type[] types = genericSuperClass.getActualTypeArguments();
		
		ContentHandlerId handlerId = new ContentHandlerId(from
				, types[0].getTypeName()
				, types[4].getTypeName());
		this.log.warn("ContentHandler: " + handlerId);
		
		Class<? extends AbstractContentHandler<?, ?, ?, ?, ?>> oldHandler = this.contentHandlers.get(handlerId);
		if(oldHandler != null) {
			this.log.warn("Handler: " + handlerId + " is already exists. It will be replaced." + oldHandler);
		}
		this.contentHandlers.put(handlerId, handlerClass);
	}
	
	public <P extends Publisher<Cq>, Cq, T, V, R> ContentHandler<P, Cq, T, V, R> getContentHandler(String argumentTypeName, MediaType fromType, MediaType toType, TransformSpec transformRule) {
		// servicerequesthandler
		@SuppressWarnings("unchecked")
		Class<AbstractContentHandler<P, Cq, T, V, R>> handlerClass =
				(Class<AbstractContentHandler<P, Cq, T, V, R>>) this.searchAcceptableContentHandler((id) -> id.getFrom().equalsTypeAndSubtype(fromType) && (id.getArgumentType().equals(argumentTypeName) || "*".equals(argumentTypeName))
						, (id) -> id.getFrom().isCompatibleWith(fromType) && (id.getArgumentType().equals(argumentTypeName) || "*".equals(argumentTypeName)));

		if(handlerClass == null) {
			throw new UnsupportedOperationException("FROM: " + fromType);
		}
		
		this.log.debug("ContentHandler: " + handlerClass);
		
		return this.newInstance(handlerClass, fromType, toType, transformRule);
	}
	
	public <P extends Publisher<Cq>, Cq, T, V, R> ContentHandler<P, Cq, T, V, R> getContentHandler(MediaType fromType, MediaType toType, String returnTypeName, TransformSpec transformRule) {
		@SuppressWarnings("unchecked")
		Class<AbstractContentHandler<P, Cq, T, V, R>> handlerClass =
		(Class<AbstractContentHandler<P, Cq, T, V, R>>) this.searchAcceptableContentHandler((id) -> id.getFrom().equalsTypeAndSubtype(fromType) && (id.getReturnTypeName().equals(returnTypeName) || "*".equals(returnTypeName))
				, (id) -> id.getFrom().isCompatibleWith(fromType) && (id.getReturnTypeName().equals(returnTypeName) || "*".equals(returnTypeName)));
		
		if(handlerClass == null) {
			throw new UnsupportedOperationException("FROM: " + fromType + ", ReturnTypeName: " + returnTypeName);
		}
		
		this.log.debug("ServiceResponseHandler: " + handlerClass);
		
		return this.newInstance(handlerClass, fromType, toType, transformRule);
	}
	
	/*
	 * helper methods...
	 */
	private Class<? extends AbstractContentHandler<?, ?, ?, ?, ?>> searchAcceptableContentHandler(Predicate<ContentHandlerId> firstPredicate, Predicate<ContentHandlerId> secondPredicate) {
		Set<Map.Entry<ContentHandlerId, Class<? extends AbstractContentHandler<?, ?, ?, ?, ?>>>> entrySet = this.contentHandlers.entrySet();
		for(Map.Entry<ContentHandlerId, Class<? extends AbstractContentHandler<?, ?, ?, ?, ?>>> entry : entrySet) {
			ContentHandlerId handlerId = entry.getKey();
			if(firstPredicate.test(handlerId) == true) {
				return entry.getValue();
			}
		}
		
		for(Map.Entry<ContentHandlerId, Class<? extends AbstractContentHandler<?, ?, ?, ?, ?>>> entry : entrySet) {
			ContentHandlerId handlerId = entry.getKey();
			if(secondPredicate.test(handlerId) == true) {
				return entry.getValue();
			}
		}
		return null;
	}
	
	private <P extends Publisher<Cq>, Cq, T, V, R> ContentHandler<P, Cq, T, V, R> newInstance(Class<AbstractContentHandler<P, Cq, T, V, R>> handlerClass, MediaType fromType, MediaType toType, TransformSpec transformRule) {
		// transformer
		ParameterizedType handlerType = (ParameterizedType)handlerClass.getGenericSuperclass();
		Type[] supportType = handlerType.getActualTypeArguments();
		DataTransformer<T, V> dataTransformer = this.dataTransformerFactory.newDataTransformer(fromType, toType, supportType[2], supportType[3], transformRule);

		AbstractContentHandler<P, Cq, T, V, R> contentHandler = Utils.newInstance(handlerClass);
		this.autowireCapableBeanFactory.autowireBean(contentHandler);
		contentHandler.init(dataTransformer, fromType, toType);
		
		return contentHandler;
	}



}
