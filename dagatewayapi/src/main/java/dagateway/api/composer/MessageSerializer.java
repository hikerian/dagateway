package dagateway.api.composer;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dagateway.api.composer.builder.StreamBuilder;


public class MessageSerializer {
	private final Logger log = LoggerFactory.getLogger(MessageSerializer.class);
	
	private final MessageNode schema;
	private Supplier<StreamBuilder> builderFactory;
	private StreamBuilder messageBuilder;
	private Map<DataProxy, StreamBuilder> proxyBuilder = new ConcurrentHashMap<>();
	
	private boolean isDone = false;
	
	
	public MessageSerializer(MessageSchema schema, Supplier<StreamBuilder> builderFactory) {
		this.schema = schema;
		this.builderFactory = builderFactory;
		
		StreamBuilder messageBuilder = this.builderFactory.get();
		messageBuilder.init(this.schema, null);
		this.messageBuilder = messageBuilder;
		
		schema.setSerializer(this);
	}
	
	public StreamBuilder getDataProxyStreamBuilder(MessageNode messageElement) {
		DataProxy dataProxy = messageElement.getProvider();
		if(dataProxy == null) {
			return null;
		}
		
		StreamBuilder streamBuilder = this.proxyBuilder.get(dataProxy);
		if(streamBuilder == null) {
			streamBuilder = this.builderFactory.get();
			streamBuilder.init(messageElement, this.messageBuilder);
			
			this.proxyBuilder.put(dataProxy, streamBuilder);
		}
		return streamBuilder;
	}
	
	public void startMessage() {
		this.messageBuilder.startMessage();
	}
	
	public void endMessage() {
		this.messageBuilder.endMessage();
	}
	
	public void startObject() {
		this.messageBuilder.startObject();
	}
	
	public void endObject() {
		this.messageBuilder.endObject();
	}
	
	public void nodeName(MessageNode node, String name) {
		this.log.debug("nodeName: " + name);
		
		this.messageBuilder.nodeName(node, name);
	}
	
	public byte[] buildNext() {
		this.build();

		byte[] datas = this.messageBuilder.buffer().flush();
		if(this.isDone) {
			this.dispose();
		}
		
		return datas;
	}
	
	public boolean isDone() {
		return this.isDone;
	}
	
	public void dispose() {
		Collection<StreamBuilder> streamBuilders = this.proxyBuilder.values();
		for(StreamBuilder streamBuilder : streamBuilders) {
			streamBuilder.dispose();
		}
	}

	private void build() {
		this.log.debug("build");
		if(this.schema.isDone()) {
			this.isDone = true;
			return;
		}
		
		this.schema.buildNext();
		
		if(this.schema.isDone()) {
			this.isDone = true;
			return;
		}
	}


}