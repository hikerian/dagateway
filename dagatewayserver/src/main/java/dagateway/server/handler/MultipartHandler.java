package dagateway.server.handler;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.Part;
import org.springframework.util.MultiValueMap;

import dagateway.api.handler.AbstractContentHandler;
import dagateway.api.utils.ModifiablePart;
import reactor.core.publisher.Mono;



/**
 * @author Dong-il Cho
 */
public class MultipartHandler extends AbstractContentHandler<Mono<MultiValueMap<String, Part>>, MultiValueMap<String, Part>, Part, Part, Mono<MultiValueMap<String, HttpEntity<?>>>> {
	public static final String ARGUMENT_TYPE = "reactor.core.publisher.Mono<org.springframework.util.MultiValueMap<java.lang.String, org.springframework.http.codec.multipart.Part>>";
	public static final String RETURN_TYPE = "reactor.core.publisher.Mono<org.springframework.util.MultiValueMap<java.lang.String, org.springframework.http.HttpEntity<?>>>";
	public static final String TRANS_ARGUMENT_TYPE = "org.springframework.http.codec.multipart.Part";
	public static final String TRANS_RETURN_TYPE = "org.springframework.http.codec.multipart.Part";
	
	
	public MultipartHandler() {
	}

	@Override
	public String getArgumentTypeName() {
		return ARGUMENT_TYPE;
	}

	@Override
	public String getReturnTypeName() {
		return RETURN_TYPE;
	}

	@Override
	public String getTransArgumentTypeName() {
		return TRANS_ARGUMENT_TYPE;
	}

	@Override
	public String getTransReturnTypeName() {
		return TRANS_RETURN_TYPE;
	}

	@Override
	public Mono<MultiValueMap<String, HttpEntity<?>>> handle(Mono<MultiValueMap<String, Part>> requestBody) {
		
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
		
		return transformed;
	}

	@Override
	protected Mono<MultiValueMap<String, HttpEntity<?>>> wrapSingle(Part value) {
		MediaType partType = value.headers().getContentType();
		MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
		bodyBuilder.part(value.name(), value, partType);
		
		MultiValueMap<String, HttpEntity<?>> newBody = bodyBuilder.build();
		
		return Mono.just(newBody);
	}


}
