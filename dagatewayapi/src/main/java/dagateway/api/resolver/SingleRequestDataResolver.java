package dagateway.api.resolver;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.springframework.web.reactive.function.server.ServerRequest;

import reactor.core.publisher.Mono;



public abstract class SingleRequestDataResolver<Cq> extends AbstractClientRequestResolver<Cq, Mono<Cq>> {
	
	
	public SingleRequestDataResolver() {
	}
	
	public Mono<Cq> resolve(ServerRequest serverRequest) {
		Mono<Cq> resultMono = this.doResolve(serverRequest);
		return resultMono.defaultIfEmpty(this.emptyValue());
	}
	
	public abstract Mono<Cq> doResolve(ServerRequest serverRequest);
	public abstract Cq emptyValue();
	
	@Override
	public String getReturnTypeName() {
		ParameterizedType genericParent = (ParameterizedType)this.getClass().getGenericSuperclass();
		Type[] argTypes = genericParent.getActualTypeArguments();
		
		return "reactor.core.publisher.Mono<" + argTypes[0].getTypeName() + ">";
	}

}
