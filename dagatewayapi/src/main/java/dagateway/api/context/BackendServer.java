package dagateway.api.context;


/**
 * @author Dong-il Cho
 */
public class BackendServer {
	private String name;
	private String url;
	private String apiDocs;
	
	
	public BackendServer() {
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getApiDocs() {
		return this.apiDocs;
	}

	public void setApiDocs(String apiDocs) {
		this.apiDocs = apiDocs;
	}

	@Override
	public String toString() {
		return "BackendServer [name=" + this.name + ", url=" + this.url + ", apiDocs=" + this.apiDocs + "]";
	}


}
