package dagateway.api.composer.builder;

import dagateway.api.composer.MessageNode;
import dagateway.api.composer.stream.StreamBuffer;


/**
 * @author Dong-il Cho
 * 
 */
public abstract class AbstractStreamBuilder implements StreamBuilder {
	private MessageNode element;
	private StreamBuilder parentBuilder;
	
	private StreamBuffer buffer;
	
	
	public AbstractStreamBuilder(StreamBuffer buffer) {
		this.buffer = buffer;
	}
	
	@Override
	public void init(MessageNode element, StreamBuilder parentBuilder) {
		this.element = element;
		this.parentBuilder = parentBuilder;
		
		this.doInit();
	}
	
	@Override
	public StreamBuilder getParentBuilder() {
		return this.parentBuilder;
	}
	
	/**
	 * initialize StreamBuilder
	 */
	protected abstract void doInit();
	

	public StreamBuffer buffer() {
		return this.buffer;
	}
	
	protected StreamBuffer write(char b) {
		this.buffer.write(b);
		return this.buffer;
	}
	
	protected StreamBuffer write(byte[] bytes) {
		this.buffer.write(bytes);
		return this.buffer;
	}
	
	@Override
	public int bufferedSize() {
		return this.buffer.size();
	}
	
	@Override
	public MessageNode element() {
		return this.element;
	}
	
	@Override
	public void dispose() {
		this.buffer.close();
		this.buffer = null;
	}

}
