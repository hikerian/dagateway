package dagateway.api.inserter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.web.reactive.function.BodyInserter;

import dagateway.api.inserter.impl.DoubleFluxDataBufferInserterBuilder;
import dagateway.api.inserter.impl.FluxDataBufferInserterBuilder;
import dagateway.api.inserter.impl.FluxServerSentEventInserterBuilder;
import dagateway.api.inserter.impl.JSONObjectInserterBuilder;
import dagateway.api.inserter.impl.MonoStringInserterBuilder;
import dagateway.api.inserter.impl.MultiValueMapInserterBuilder;
import dagateway.api.inserter.impl.MultipartInserterBuilder;


public class BodyInserterBuilderFactory {
	private final Logger log = LoggerFactory.getLogger(BodyInserterBuilderFactory.class);
	private Map<String, BodyInserterBuilder<?, ?>> bodyInserterBuilders = new ConcurrentHashMap<>();
	
	
	public BodyInserterBuilderFactory() {
		// initialize
		this.addBodyInserterBuilder(new DoubleFluxDataBufferInserterBuilder());
		this.addBodyInserterBuilder(new FluxDataBufferInserterBuilder());
		this.addBodyInserterBuilder(new FluxServerSentEventInserterBuilder());
		this.addBodyInserterBuilder(new JSONObjectInserterBuilder());
		this.addBodyInserterBuilder(new MultipartInserterBuilder());
		this.addBodyInserterBuilder(new MultiValueMapInserterBuilder());
		this.addBodyInserterBuilder(new MonoStringInserterBuilder());
	}
	
	public void addBodyInserterBuilder(BodyInserterBuilder<?, ?> builder) {
		String inserterId = builder.supportType();
		this.bodyInserterBuilders.put(inserterId, builder);
	}
	
	public <P, M extends ReactiveHttpOutputMessage> BodyInserter<?, M> getBodyInserter(String typeName, P data) {
		@SuppressWarnings("unchecked")
		BodyInserterBuilder<P, M> bodyInserter = (BodyInserterBuilder<P, M>) this.bodyInserterBuilders.get(typeName);
		if(bodyInserter == null) {
			this.log.warn("TypeName: " + typeName + " InserterBuilder is not found");
			return null;
		}
		return bodyInserter.getBodyInserter(data);
	}
	

}
