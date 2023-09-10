package dagateway.api.handler;

import java.util.Objects;

import org.springframework.http.MediaType;


public class ServiceRequestHandlerId {
	private MediaType from;
	
	
	public ServiceRequestHandlerId(MediaType from) {
		this.from = new MediaType(from.getType(), from.getSubtype());
	}

	public MediaType getFrom() {
		return this.from;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.from);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ServiceRequestHandlerId other = (ServiceRequestHandlerId) obj;
		return this.from.equalsTypeAndSubtype(other.from);
	}


}
