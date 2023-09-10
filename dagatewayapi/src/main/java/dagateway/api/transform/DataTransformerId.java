package dagateway.api.transform;

import java.lang.reflect.Type;
import java.util.Objects;

import org.springframework.http.MediaType;


public class DataTransformerId {
	private MediaType from;
	private MediaType to;
	private String srcType;
	private String rtnType;
	
	
	public DataTransformerId(MediaType from, MediaType to, Type srcType, Type rtnType) {
		this(from, to, srcType.getTypeName(), rtnType.getTypeName());
	}
	
	public DataTransformerId(MediaType from, MediaType to, String srcType, String rtnType) {
		this.from  = new MediaType(from.getType(), from.getSubtype());
		this.to = new MediaType(to.getType(), to.getSubtype());
		this.srcType = srcType;
		this.rtnType = rtnType;
	}

	public MediaType getFrom() {
		return this.from;
	}

	public MediaType getTo() {
		return this.to;
	}

	public String getSrcType() {
		return this.srcType;
	}

	public String getRtnType() {
		return this.rtnType;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.from, this.rtnType, this.srcType, this.to);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DataTransformerId other = (DataTransformerId) obj;
		return this.from.compareTo(other.from) >= 0
				&& this.to.compareTo(other.to) >= 0
				&& Objects.equals(this.rtnType, other.rtnType)
				&& Objects.equals(this.srcType, other.srcType);
	}


}
