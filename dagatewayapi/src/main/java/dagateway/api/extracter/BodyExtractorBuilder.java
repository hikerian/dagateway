package dagateway.api.extracter;

import org.springframework.http.ReactiveHttpInputMessage;
import org.springframework.web.reactive.function.BodyExtractor;


public interface BodyExtractorBuilder<T, M extends ReactiveHttpInputMessage> {
	public BodyExtractor<T, M> getBodyExtractor();

}
