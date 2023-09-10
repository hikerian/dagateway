package dagateway.api.handler;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.server.ServerResponse;

import dagateway.api.context.RouteContext;
import dagateway.api.service.ServiceResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;



/**
 * 
 * @author chodo
 *
 * @param <T> ServiceResult의 결과 타입, Transform 동작시 아웃풋 타입
 * @param <V> Transform 동작시 인풋 타입
 */
public interface ServiceResponseHandler<Sr, T, V> {
	public ServiceResult<Sr> resolve(ResponseEntity<Flux<DataBuffer>> responseEntity, RouteContext.ServiceSpec serviceSpec);
	public Mono<ServerResponse> buildBody(ServerResponse.BodyBuilder builder, Sr body);

}
