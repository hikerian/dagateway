package dagateway.api.resolver.http;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.reactivestreams.Publisher;
import org.springframework.http.MediaType;

import dagateway.api.context.route.ContentHandling;
import dagateway.api.service.ServiceResult;


public class ClientResolverFactory {
	private Map<ClientRequestResolverId, Supplier<? extends AbstractClientRequestResolver<?, ?>>> clientRequestResolvers = new ConcurrentHashMap<>();
	private Map<ClientResponseResolverId, Supplier<? extends AbstractClientResponseResolver<?, ?>>> clientResponseResolvers = new ConcurrentHashMap<>();
	
	
	public ClientResolverFactory() {
	}
	
	public void addRequestResolver(ClientRequestResolverId resolverId, Supplier<? extends AbstractClientRequestResolver<?, ?>> resolverClass) {
		this.clientRequestResolvers.put(resolverId, resolverClass);
	}
	
	public void addResponseResolver(ClientResponseResolverId resolverId, Supplier<? extends AbstractClientResponseResolver<?, ?>> resolverClass) {
		this.clientResponseResolvers.put(resolverId, resolverClass);
	}
	
	@SuppressWarnings("unchecked")
	public <Cq, P extends Publisher<Cq>> AbstractClientRequestResolver<Cq, P> getClientRequestResolver(MediaType requestType, MediaType aggregateType) {
		return (AbstractClientRequestResolver<Cq, P>) this.getClientRequestResolver(requestType, aggregateType, false, false);
	}
	
	@SuppressWarnings("unchecked")
	public <Cq, P extends Publisher<Cq>> AbstractClientRequestResolver<Cq, P> getClientRequestResolver(MediaType requestType, MediaType aggregateType, boolean divided) {
		return (AbstractClientRequestResolver<Cq, P>) this.getClientRequestResolver(requestType, aggregateType, true, divided);
	}
	
	public AbstractClientRequestResolver<?, ?> getClientRequestResolver(MediaType requestType, MediaType aggregateType, boolean checkMultiple, boolean divided) {
		// "formdata|raw|string -> raw, string 인 경우 서비스의 개수에 따라 divided 이거나 통합이거나 결정"
		Supplier<? extends AbstractClientRequestResolver<?, ?>> resolverSupplier = null;
		
		Set<Map.Entry<ClientRequestResolverId, Supplier<? extends AbstractClientRequestResolver<?, ?>>>> entrySet = this.clientRequestResolvers.entrySet();
		for(Map.Entry<ClientRequestResolverId, Supplier<? extends AbstractClientRequestResolver<?, ?>>> entry : entrySet) {
			ClientRequestResolverId resolverId = entry.getKey();
			if(resolverId.getFrom().equalsTypeAndSubtype(requestType)
					&& resolverId.getTo().equalsTypeAndSubtype(aggregateType)
					&& (checkMultiple ? resolverId.isDivided() == divided : true)) {
					
				resolverSupplier = entry.getValue();
				break;
			}
		}
		if(resolverSupplier == null) {
			for(Map.Entry<ClientRequestResolverId, Supplier<? extends AbstractClientRequestResolver<?, ?>>> entry : entrySet) {
				ClientRequestResolverId resolverId = entry.getKey();
				if(resolverId.getFrom().isCompatibleWith(requestType)
						&& resolverId.getTo().isCompatibleWith(aggregateType)
						&& (checkMultiple ? resolverId.isDivided() == divided : true)) {
					resolverSupplier = entry.getValue();
					break;
				}
			}
		}
		
		if(resolverSupplier == null) {
			throw new UnsupportedOperationException(aggregateType + " " + requestType);
		}
		
		AbstractClientRequestResolver<?, ?> resolver = resolverSupplier.get();
		resolver.init(requestType, aggregateType);
		
		return resolver;
	}
	
	public <Sr, P extends Publisher<ServiceResult<Sr>>> AbstractClientResponseResolver<Sr, P> getClientResponseResolver(ContentHandling contentHandling, MediaType responseType, boolean multiple) {
		
		Supplier<? extends AbstractClientResponseResolver<?, ?>> resolverSupplier = null;
		
		for(Map.Entry<ClientResponseResolverId, Supplier<? extends AbstractClientResponseResolver<?, ?>>> entry : this.clientResponseResolvers.entrySet()) {
			ClientResponseResolverId resolverId = entry.getKey();
			if(contentHandling == resolverId.getContentHandling()
					&& resolverId.getMediaType().equalsTypeAndSubtype(responseType)
					&& resolverId.isMultiple() == multiple) {
				resolverSupplier = entry.getValue();
				break;
			}
		}
		if(resolverSupplier == null) {
			for(Map.Entry<ClientResponseResolverId, Supplier<? extends AbstractClientResponseResolver<?, ?>>> entry : this.clientResponseResolvers.entrySet()) {
				ClientResponseResolverId resolverId = entry.getKey();
				if(resolverId.getContentHandling() == contentHandling
						&& resolverId.getMediaType().isCompatibleWith(responseType)
						&& resolverId.isMultiple() == multiple) {
					resolverSupplier = entry.getValue();
					break;
				}
			}
		}
		
		if(resolverSupplier == null) {
			throw new UnsupportedOperationException("Response MediaType " + responseType + " is not supported.");
		}
		
		@SuppressWarnings("unchecked")
		AbstractClientResponseResolver<Sr, P> resolver = (AbstractClientResponseResolver<Sr, P>) resolverSupplier.get();
		resolver.init(responseType);
		
		return resolver;
	}


}
