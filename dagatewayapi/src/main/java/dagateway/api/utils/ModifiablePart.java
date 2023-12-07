package dagateway.api.utils;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.codec.multipart.Part;

import reactor.core.publisher.Flux;


/**
 * @author Dong-il Cho
 */
public class ModifiablePart implements Part {
	private String name;
	private HttpHeaders httpHeaders;
	private Flux<DataBuffer> content;
	
	
	public ModifiablePart(Part part) {
		this.name = part.name();
		this.httpHeaders = new HttpHeaders();
		HttpHeaders reqHeaders = part.headers();
		if(reqHeaders.size() > 0) {
			this.httpHeaders.putAll(reqHeaders);
		}
		this.content = part.content();
	}
	
	public ModifiablePart(String name, HttpHeaders httpHeaders, Flux<DataBuffer> content) {
		this.name = name;
		this.httpHeaders = httpHeaders;
		this.content = content;
	}

	@Override
	public String name() {
		return this.name;
	}

	@Override
	public HttpHeaders headers() {
		return this.httpHeaders;
	}

	@Override
	public Flux<DataBuffer> content() {
		return this.content;
	}
	
	public void setContent(Flux<DataBuffer> newContent) {
		this.content = newContent;
	}


}
