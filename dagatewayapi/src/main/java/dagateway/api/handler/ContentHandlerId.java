package dagateway.api.handler;

import java.util.Objects;

import org.springframework.http.MediaType;



/**
 * @author Dong-il Cho
 */
public class ContentHandlerId {
	private MediaType from;
	private String argumentTypeName;
	private String returnTypeName;


	public ContentHandlerId(MediaType from, String argumentTypeName, String returnTypeName) {
		this.from = new MediaType(from.getType(), from.getSubtype());
		this.argumentTypeName = argumentTypeName;
		this.returnTypeName = returnTypeName;
	}

	public MediaType getFrom() {
		return this.from;
	}
	
	public String getArgumentType() {
		return this.argumentTypeName;
	}

	public String getReturnTypeName() {
		return this.returnTypeName;
	}

	@Override
	public int hashCode() {
		return Objects.hash(argumentTypeName, from, returnTypeName);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ContentHandlerId other = (ContentHandlerId) obj;
		return Objects.equals(argumentTypeName, other.argumentTypeName) && Objects.equals(from, other.from)
				&& Objects.equals(returnTypeName, other.returnTypeName);
	}


}
