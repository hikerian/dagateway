package dagateway.api.http;

import java.time.Duration;

import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.logging.AdvancedByteBufFormat;


public class ReactorClientResolver implements WebClientResolver {
	
	public ReactorClientResolver() {
	}

	@Override
	public WebClient createWebClient() {
		HttpClient httpClient = HttpClient.create()
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 4000) // TODO Config
				.doOnConnected(conn ->
					conn.addHandlerLast(new ReadTimeoutHandler(400)) // TODO Config
	                    .addHandlerLast(new WriteTimeoutHandler(400))) // TODO Config
				.responseTimeout(Duration.ofMinutes(15L)) // TODO Config
				.doOnError((req, err) -> {
					System.out.println("# - REQUEST ERR!!!");
					err.printStackTrace();
				}, (res, err) -> {
					System.out.println("# - RESPONSE ERR!!!");
					err.printStackTrace();
				})
				.doAfterResponseSuccess((res, conn) -> {
//					System.out.println("# - RESPONSE SUCCESS!!!");
				})
				.wiretap("reactor.netty", LogLevel.DEBUG, AdvancedByteBufFormat.SIMPLE)
				.compress(true);

	    ClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);
		
		WebClient.Builder builder = WebClient.builder();
		builder = builder.clientConnector(connector);
		
		return builder.build();
	}

}
