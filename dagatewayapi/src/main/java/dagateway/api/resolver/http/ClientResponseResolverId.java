package dagateway.api.resolver.http;

import java.util.Objects;

import org.springframework.http.MediaType;

import dagateway.api.context.ContentHandling;



public class ClientResponseResolverId {
	private MediaType mediaType;
	private ContentHandling contentHandling;
	private boolean multiple;
	
	
	public ClientResponseResolverId(MediaType mediaType, ContentHandling contentHandling, boolean multiple) {
		this.mediaType = mediaType;
		this.contentHandling = contentHandling;
		this.multiple = multiple;
	}
	
	public MediaType getMediaType() {
		return this.mediaType;
	}
	
	public ContentHandling getContentHandling() {
		return this.contentHandling;
	}
	
	public boolean isMultiple() {
		return this.multiple;
	}

	@Override
	public int hashCode() {
		return Objects.hash(contentHandling, mediaType, multiple);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClientResponseResolverId other = (ClientResponseResolverId) obj;
		return contentHandling == other.contentHandling && Objects.equals(mediaType, other.mediaType)
				&& multiple == other.multiple;
	}


}
