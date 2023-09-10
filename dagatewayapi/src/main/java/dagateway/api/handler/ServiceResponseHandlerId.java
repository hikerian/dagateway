package dagateway.api.handler;

import java.util.Objects;

import org.springframework.http.MediaType;


public class ServiceResponseHandlerId {
	private MediaType from;
	private String typeName;
	
	
	public ServiceResponseHandlerId(MediaType from, String to) {
		this.from = new MediaType(from.getType(), from.getSubtype());
		this.typeName = to;
	}

	public MediaType getFrom() {
		return this.from;
	}

	public String getTypeName() {
		return this.typeName;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.from, this.typeName);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ServiceResponseHandlerId other = (ServiceResponseHandlerId) obj;
		return Objects.equals(this.from, other.from) && Objects.equals(this.typeName, other.typeName);
	}


}
