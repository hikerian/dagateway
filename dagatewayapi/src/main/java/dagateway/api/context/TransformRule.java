package dagateway.api.context;

import org.springframework.http.MediaType;


public class TransformRule {
	private MediaType contentType;
	private String bodyGraph;
	
	
	public TransformRule() {
	}

	public MediaType getContentType() {
		return this.contentType;
	}

	public void setContentType(MediaType contentType) {
		this.contentType = contentType;
	}

	public String getBodyGraph() {
		return this.bodyGraph;
	}

	public void setBodyGraph(String bodyGraph) {
		this.bodyGraph = bodyGraph;
	}

	@Override
	public String toString() {
		return "TransformRule [contentType=" + contentType + ", bodyGraph=" + bodyGraph + "]";
	}


}
