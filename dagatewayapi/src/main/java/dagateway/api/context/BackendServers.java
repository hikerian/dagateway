package dagateway.api.context;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Dong-il Cho
 */
public class BackendServers {
	private List<BackendServer> backends = new ArrayList<>();
	
	
	public BackendServers() {
	}

	public List<BackendServer> getBackends() {
		return this.backends;
	}

	public void setBackends(List<BackendServer> backends) {
		this.backends = backends;
	}

	@Override
	public String toString() {
		return "BackendServers [backends=" + this.backends + "]";
	}


}
