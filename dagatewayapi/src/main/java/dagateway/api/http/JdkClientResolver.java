package dagateway.api.http;

//import java.net.Authenticator;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.JdkClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;


public class JdkClientResolver implements WebClientResolver {
	
	public JdkClientResolver() {
	}

	@Override
	public WebClient createWebClient() {
		ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
		
		HttpClient.Builder builder = HttpClient.newBuilder();
		builder.connectTimeout(Duration.ofSeconds(30L));
		builder.executor(executorService);
//		builder.authenticator(Authenticator.getDefault());
		builder.followRedirects(HttpClient.Redirect.ALWAYS);
		
		HttpClient httpClient = builder.build();
		
		ClientHttpConnector connector = new JdkClientHttpConnector(httpClient);
		
		WebClient.Builder webClientBuilder = WebClient.builder();
		webClientBuilder = webClientBuilder.clientConnector(connector);
		
		return webClientBuilder.build();
	}

}
