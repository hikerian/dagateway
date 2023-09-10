package dagateway.api.handler;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.http.MediaType;

import dagateway.api.context.RouteContext.TransformSpec;
import dagateway.api.transform.DataTransformer;
import dagateway.api.transform.DataTransformerFactory;
import dagateway.api.utils.Utils;



public class ServiceHandlerFactory {
	private final Logger log = LoggerFactory.getLogger(ServiceHandlerFactory.class);
	
	private AutowireCapableBeanFactory autowireCapableBeanFactory;
	private DataTransformerFactory dataTransformerFactory;
	
	private Map<ServiceRequestHandlerId, Class<? extends AbstractServiceRequestHandler<?, ?, ?, ?>>> serviceRequestHandlers = new ConcurrentHashMap<>();
	private Map<ServiceResponseHandlerId, Class<? extends AbstractServiceResponseHandler<?, ?, ?>>> serviceResponseHandlers = new ConcurrentHashMap<>();
	
	
	public ServiceHandlerFactory() {
	}
	
	public void setAutowireCapableBeanFactory(AutowireCapableBeanFactory autowireCapableBeanFactory) {
		this.autowireCapableBeanFactory = autowireCapableBeanFactory;
	}
	
	public void setDataTransformerFactory(DataTransformerFactory dataTransformerFactory) {
		this.dataTransformerFactory = dataTransformerFactory;
	}
	
	public void addServiceRequestHandler(MediaType from, Class<? extends AbstractServiceRequestHandler<?, ?, ?, ?>> handlerClass) {
		ServiceRequestHandlerId handlerId = new ServiceRequestHandlerId(from);
		this.log.warn("ServiceHandler: " + handlerId);
		
		Class<? extends AbstractServiceRequestHandler<?, ?, ?, ?>> oldHandler = this.serviceRequestHandlers.get(handlerId);
		if(oldHandler != null) {
			this.log.warn("Handler: " + handlerId + " is already exists. It will be replaced." + oldHandler);
		}
		this.serviceRequestHandlers.put(handlerId, handlerClass);
	}
	
	public void addServiceResponseHandler(MediaType from, Class<? extends AbstractServiceResponseHandler<?, ?, ?>> handlerClass) {
		ParameterizedType genericSuperClass = (ParameterizedType) handlerClass.getGenericSuperclass();
		Type srType = genericSuperClass.getActualTypeArguments()[0];
		
		ServiceResponseHandlerId handlerId = new ServiceResponseHandlerId(from, srType.getTypeName());
		this.log.warn("ServiceHandler: " + handlerId);
		
		Class<? extends AbstractServiceResponseHandler<?, ?, ?>> oldHandler = this.serviceResponseHandlers.get(handlerId);
		if(oldHandler != null) {
			this.log.warn("Handler: " + handlerId + " is already exists. It will be replaced." + oldHandler);
		}
		this.serviceResponseHandlers.put(handlerId, handlerClass);
	}
	
	/**
	 * 
	 * @param <Cq>
	 * @param <Sq>
	 * @param aggregatedType ClientRequestHandler의 dataType
	 * @param routeRequest ServiceRequest의 ContentType
	 * @return
	 */
	public <P extends Publisher<Cq>, Cq, T, V> ServiceRequestHandler<P, Cq, T, V> getRequestHandler(MediaType aggregatedType, MediaType serviceRequestType, TransformSpec transformRule) {
		// servicerequesthandler
		@SuppressWarnings("unchecked")
		Class<AbstractServiceRequestHandler<P, Cq, T, V>> handlerClass =
				(Class<AbstractServiceRequestHandler<P, Cq, T, V>>) this.searchAcceptableServiceRequestHandler(aggregatedType);

		this.log.debug("ServiceRequestHandler: " + handlerClass);
		
		// transformer
		ParameterizedType handlerType = (ParameterizedType)handlerClass.getGenericSuperclass(); // <<------------------ TODO Interface로 처리할지 Class로 처리할 지 결정 필요!!
		Type[] supportType = handlerType.getActualTypeArguments();
		DataTransformer<T, V> dataTransformer = this.dataTransformerFactory.newDataTransformer(aggregatedType, serviceRequestType, supportType[2], supportType[3], transformRule);

		AbstractServiceRequestHandler<P, Cq, T, V> serviceRequestHandler = Utils.newInstance(handlerClass);
		this.autowireCapableBeanFactory.autowireBean(serviceRequestHandler);
		serviceRequestHandler.init(dataTransformer, aggregatedType, serviceRequestType);
		
		return serviceRequestHandler;
	}
	
	public <Sr, T, V> ServiceResponseHandler<Sr, T, V> getResponseHandler(MediaType serviceResponseType, MediaType responseType, String typeName, TransformSpec transformRule) {
		@SuppressWarnings("unchecked")
		Class<? extends AbstractServiceResponseHandler<Sr, T, V>> handlerClass =
				(Class<? extends AbstractServiceResponseHandler<Sr, T, V>>) this.searchAcceptableServiceResponseHandler(serviceResponseType, typeName);
		
		this.log.debug("ServiceResponseHandler: " + handlerClass);
		
		ParameterizedType handlerType = (ParameterizedType)handlerClass.getGenericSuperclass();
		Type[] supportType = handlerType.getActualTypeArguments();
		DataTransformer<T, V> dataTransformer = this.dataTransformerFactory.newDataTransformer(serviceResponseType, responseType, supportType[1], supportType[2], transformRule);
		
		AbstractServiceResponseHandler<Sr, T, V> serviceResponseHandler = (AbstractServiceResponseHandler<Sr, T, V>) Utils.newInstance(handlerClass);
		this.autowireCapableBeanFactory.autowireBean(serviceResponseHandler);
		serviceResponseHandler.init(dataTransformer, serviceResponseType, responseType);
		
		return serviceResponseHandler;
		
	}
	
	/*
	 * helper methods...
	 */

	private Class<? extends AbstractServiceRequestHandler<?, ?, ?, ?>> searchAcceptableServiceRequestHandler(MediaType from) {
		Set<Map.Entry<ServiceRequestHandlerId, Class<? extends AbstractServiceRequestHandler<?, ?, ?, ?>>>> entrySet = this.serviceRequestHandlers.entrySet();
		
		for(Map.Entry<ServiceRequestHandlerId, Class<? extends AbstractServiceRequestHandler<?, ?, ?, ?>>> entry : entrySet) {
			ServiceRequestHandlerId id = entry.getKey();
			if(id.getFrom().equalsTypeAndSubtype(from)) {
				return entry.getValue();
			}
		}
		
		for(Map.Entry<ServiceRequestHandlerId, Class<? extends AbstractServiceRequestHandler<?, ?, ?, ?>>> entry : entrySet) {
			ServiceRequestHandlerId id = entry.getKey();
			if(id.getFrom().isCompatibleWith(from)) {
				return entry.getValue();
			}
		}
		
		throw new UnsupportedOperationException("FROM: " + from);
	}
	
	private Class<? extends AbstractServiceResponseHandler<?, ?, ?>> searchAcceptableServiceResponseHandler(MediaType from, String typeName) {
		Set<Map.Entry<ServiceResponseHandlerId, Class<? extends AbstractServiceResponseHandler<?, ?, ?>>>> entrySet = this.serviceResponseHandlers.entrySet();
		for(Map.Entry<ServiceResponseHandlerId, Class<? extends AbstractServiceResponseHandler<?, ?, ?>>> entry : entrySet) {
			ServiceResponseHandlerId handlerId = entry.getKey();
			if(handlerId.getFrom().equalsTypeAndSubtype(from) && (handlerId.getTypeName().equals(typeName) || "*".equals(typeName))) {
				return entry.getValue();
			}
		}
		
		for(Map.Entry<ServiceResponseHandlerId, Class<? extends AbstractServiceResponseHandler<?, ?, ?>>> entry : entrySet) {
			ServiceResponseHandlerId handlerId = entry.getKey();
			if(handlerId.getFrom().isCompatibleWith(from) && (handlerId.getTypeName().equals(typeName) || "*".equals(typeName))) {
				return entry.getValue();
			}
		}
		
		throw new UnsupportedOperationException("FROM: " + from + ", TypeName: " + typeName);
	}



}
