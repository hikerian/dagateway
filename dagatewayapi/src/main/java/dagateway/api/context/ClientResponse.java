package dagateway.api.context;

import org.springframework.http.MediaType;


public class ClientResponse {
	private MediaType contentType;
	private ContentHandling contentHandling = ContentHandling.PASSTHROUGH; // default
	private HeaderProperties header;
	private String bodyGraph;
	
	
	public ClientResponse() {
	}

	public MediaType getContentType() {
		return this.contentType;
	}

	public void setContentType(MediaType contentType) {
		this.contentType = contentType;
	}

	public ContentHandling getContentHandling() {
		return this.contentHandling;
	}

	public void setContentHandling(String contentHandling) {
		this.contentHandling = ContentHandling.valueOf(contentHandling.toUpperCase());
	}
	
	public void setContentHandling(ContentHandling contentHandling) {
		this.contentHandling = contentHandling;
	}

	public HeaderProperties getHeader() {
		return this.header;
	}

	public void setHeader(HeaderProperties header) {
		this.header = header;
	}

	public String getBodyGraph() {
		return this.bodyGraph;
	}

	public void setBodyGraph(String bodyGraph) {
		System.out.println(bodyGraph);
		this.bodyGraph = bodyGraph;
	}

	@Override
	public String toString() {
		return "ClientResponse [contentType=" + contentType + ", contentHandling=" + contentHandling + ", header="
				+ header + ", bodyGraph=" + bodyGraph + "]";
	}


}
