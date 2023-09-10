package dagateway.server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;


@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {
	
	
	/**
	 * https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter
	 * @param http
	 * @return
	 */
	@Bean
	SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
		return http.authorizeExchange()
				.pathMatchers("/admin") // 관리 화면을 위해 권한 부여
				.hasAuthority("Admin") // 관리 화면을 위해 권한 부여
				.anyExchange()
				.permitAll()
				.and()
				.csrf() // 기본적으로 post 등 body를 포함하는 요청에 대해 csrf가 활성화 되어있음.
				.disable()
				.build();
	}

}
