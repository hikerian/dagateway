package dagateway.api.resolver.http;

import java.util.Objects;

import org.springframework.http.MediaType;



/**
 * @author Dong-il Cho
 */
public class ClientRequestResolverId {
	private MediaType from;
	private MediaType to;
	private boolean divided;
	
	public ClientRequestResolverId(MediaType from, MediaType to, boolean divided) {
		this.from = from;
		this.to = to;
		this.divided = divided;
	}
	
	public MediaType getFrom() {
		return this.from;
	}
	
	public MediaType getTo() {
		return this.to;
	}
	
	public boolean isDivided() {
		return this.divided;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.from, this.to, this.divided);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClientRequestResolverId other = (ClientRequestResolverId) obj;
		return this.from.compareTo(other.from) >= 0
				&& this.to.compareTo(other.to) >= 0
				&& this.divided == other.divided;
	}
}
