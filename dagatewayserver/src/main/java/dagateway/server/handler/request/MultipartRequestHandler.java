package dagateway.server.handler.request;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.Part;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;

import dagateway.api.context.RouteContext;
import dagateway.api.handler.AbstractServiceRequestHandler;
import dagateway.api.resolver.ModifiablePart;
import reactor.core.publisher.Mono;


public class MultipartRequestHandler extends AbstractServiceRequestHandler<Mono<MultiValueMap<String, Part>>, MultiValueMap<String, Part>, Part, Part> {
	
	
	public MultipartRequestHandler() {
	}

	@Override
	public RequestHeadersSpec<?> resolveBody(Mono<MultiValueMap<String, Part>> requestBody, RequestBodySpec requestBodySpec, RouteContext.ServiceSpec serviceSpec) {
		// transform
		Mono<MultiValueMap<String, HttpEntity<?>>> transformed = requestBody.map(multiValueMap -> {
			MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
			Set<Entry<String, List<Part>>> entrySet = multiValueMap.entrySet();
			for(Entry<String, List<Part>> entry : entrySet) {
				List<Part> partList = entry.getValue();
				
				for(Part part : partList) {
					MediaType partType = part.headers().getContentType();
					ModifiablePart newPart = new ModifiablePart(part);
					
					// call transformer
					newPart = (ModifiablePart) this.transformer.transform(newPart);
					
					if(newPart != null) {
						bodyBuilder.part(newPart.name(), newPart, partType);
					}
				}
			}
			
			MultiValueMap<String, HttpEntity<?>> newBody = bodyBuilder.build();
			return newBody;
		});
		
		
		return requestBodySpec.body(BodyInserters.fromProducer(transformed, MultiValueMap.class));
	}


}
