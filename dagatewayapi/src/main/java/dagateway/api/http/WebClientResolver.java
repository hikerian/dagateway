package dagateway.api.http;

import org.springframework.web.reactive.function.client.WebClient;


public interface WebClientResolver {
	public WebClient createWebClient();

}
