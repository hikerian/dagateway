package dagateway.api.context.route;

import org.springframework.http.MediaType;


/**
 * @author Dong-il Cho
 */
public class TransformRule {
	private MediaType contentType;
	private String bodyGraph;
	
	
	public TransformRule() {
	}

	public MediaType getContentType() {
		return this.contentType;
	}
	
	public void setContentType(String contentType) {
		this.contentType = MediaType.parseMediaType(contentType);
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
