package dagateway.api.extracter;

import org.springframework.http.ReactiveHttpInputMessage;


public abstract class AbstractBodyExtractorBuilder<T, M extends ReactiveHttpInputMessage> implements BodyExtractorBuilder<T, M> {

	
	protected AbstractBodyExtractorBuilder() {
	}

}
