package dagateway.api.resolver.http;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.http.MediaType;

import dagateway.api.context.ContentHandling;
import dagateway.api.service.ServiceResult;
import dagateway.api.utils.Utils;



public class ClientResolverFactory {
	private final Logger log = LoggerFactory.getLogger(ClientResolverFactory.class);
	
	private AutowireCapableBeanFactory autowireCapableBeanFactory;
	
	private Map<ClientRequestResolverId, Class<? extends AbstractClientRequestResolver<?, ?>>> clientRequestResolvers = new ConcurrentHashMap<>();
	private Map<ClientResponseResolverId, Class<? extends AbstractClientResponseResolver>> clientResponseResolvers = new ConcurrentHashMap<>();
	
	
	public ClientResolverFactory() {
	}
	
	public void setAutowireCapableBeanFactory(AutowireCapableBeanFactory autowireCapableBeanFactory) {
		this.autowireCapableBeanFactory = autowireCapableBeanFactory;
	}
	
	public void addRequestResolver(ClientRequestResolverId resolverId, Class<? extends AbstractClientRequestResolver<?, ?>> resolverClass) {
		this.clientRequestResolvers.put(resolverId, resolverClass);
	}
	
	public void addResponseResolver(ClientResponseResolverId resolverId, Class<? extends AbstractClientResponseResolver> resolverClass) {
		this.clientResponseResolvers.put(resolverId, resolverClass);
	}
	
	public <Cq, P extends Publisher<Cq>> AbstractClientRequestResolver<Cq, P> getClientRequestResolver(MediaType requestType, MediaType aggregateType) {
		return this.getClientRequestResolver(requestType, aggregateType, false, false);
	}
	
	public <Cq, P extends Publisher<Cq>> AbstractClientRequestResolver<Cq, P> getClientRequestResolver(MediaType requestType, MediaType aggregateType, boolean divided) {
		return this.getClientRequestResolver(requestType, aggregateType, true, divided);
	}
	
	public <Cq, P extends Publisher<Cq>> AbstractClientRequestResolver<Cq, P> getClientRequestResolver(MediaType requestType, MediaType aggregateType, boolean checkMultiple, boolean divided) {
		// "formdata|raw|string -> raw, string인 경우 서비스의 개수에 따라 divided 이거나 통합이거나 결정"
		Class<? extends ClientRequestResolver<?, ?>> resolverClass = null;
		
		Set<Map.Entry<ClientRequestResolverId, Class<? extends AbstractClientRequestResolver<?, ?>>>> entrySet = this.clientRequestResolvers.entrySet();
		for(Map.Entry<ClientRequestResolverId, Class<? extends AbstractClientRequestResolver<?, ?>>> entry : entrySet) {
			ClientRequestResolverId resolverId = entry.getKey();
			if(resolverId.getFrom().equalsTypeAndSubtype(requestType)
					&& resolverId.getTo().equalsTypeAndSubtype(aggregateType)
					&& (checkMultiple ? resolverId.isDivided() == divided : true)) {
					
				resolverClass = entry.getValue();
				break;
			}
		}
		if(resolverClass == null) {
			for(Map.Entry<ClientRequestResolverId, Class<? extends AbstractClientRequestResolver<?, ?>>> entry : entrySet) {
				ClientRequestResolverId resolverId = entry.getKey();
				if(resolverId.getFrom().isCompatibleWith(requestType)
						&& resolverId.getTo().isCompatibleWith(aggregateType)
						&& (checkMultiple ? resolverId.isDivided() == divided : true)) {
					resolverClass = entry.getValue();
					break;
				}
			}
		}
		
		if(resolverClass == null) {
			throw new UnsupportedOperationException(aggregateType + " " + requestType);
		}
		
		this.log.debug("ClientRequestResolver: " + resolverClass);
		
		@SuppressWarnings("unchecked")
		AbstractClientRequestResolver<Cq, P> resolver = (AbstractClientRequestResolver<Cq, P>) Utils.newInstance(resolverClass);
		this.autowireCapableBeanFactory.autowireBean(resolver);
		resolver.init(requestType, aggregateType);
		
		return resolver;
	}
	
	public <Sr, P extends Publisher<ServiceResult<Sr>>> AbstractClientResponseResolver<Sr, P> getClientResponseResolver(ContentHandling contentHandling, MediaType responseType, boolean multiple) {
		
		Class<? extends AbstractClientResponseResolver> resolverClass = null;
		
		for(Map.Entry<ClientResponseResolverId, Class<? extends AbstractClientResponseResolver>> entry : this.clientResponseResolvers.entrySet()) {
			ClientResponseResolverId resolverId = entry.getKey();
			if(contentHandling == resolverId.getContentHandling()
					&& resolverId.getMediaType().equalsTypeAndSubtype(responseType)
					&& resolverId.isMultiple() == multiple) {
				resolverClass = entry.getValue();
				break;
			}
		}
		if(resolverClass == null) {
			for(Map.Entry<ClientResponseResolverId, Class<? extends AbstractClientResponseResolver>> entry : this.clientResponseResolvers.entrySet()) {
				ClientResponseResolverId resolverId = entry.getKey();
				if(resolverId.getContentHandling() == contentHandling
						&& resolverId.getMediaType().isCompatibleWith(responseType)
						&& resolverId.isMultiple() == multiple) {
					resolverClass = entry.getValue();
					break;
				}
			}
		}
		
		if(resolverClass == null) {
			throw new UnsupportedOperationException("Response MediaType " + responseType + " is not supported.");
		}
		
		this.log.debug("ClientResponseResolver: " + resolverClass);
		
		@SuppressWarnings("unchecked")
		AbstractClientResponseResolver<Sr, P> resolver = (AbstractClientResponseResolver<Sr, P>) Utils.newInstance(resolverClass);
		this.autowireCapableBeanFactory.autowireBean(resolver);
		resolver.init(responseType);
		
		return resolver;
	}

}
