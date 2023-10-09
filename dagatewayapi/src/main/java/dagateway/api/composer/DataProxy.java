package dagateway.api.composer;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dagateway.api.composer.builder.StreamBuilder;
import dagateway.api.composer.stream.StreamBuffer;


/**
 * @author Dong-il Cho
 */
public class DataProxy {
	private final Logger log = LoggerFactory.getLogger(DataProxy.class);
	
	private StreamBuilder streamBuilder;
	
	private boolean isFeedEnd = false;


	public DataProxy() {
	}
	
	public void setStreamBuilder(StreamBuilder streamBuilder) {
		this.streamBuilder = streamBuilder;
	}

	public void push(ByteBuffer buffer) {
		this.streamBuilder.feed(buffer);
	}
	
	public void buildNext() {
//		this.log.debug("Proxy moveToParent");

		StreamBuffer buffer = (StreamBuffer)this.streamBuilder.buffer();
		StreamBuilder parentBuilder = this.streamBuilder.getParentBuilder();
		if(buffer.size() == 0 || parentBuilder == null) {
			return;
		}
		byte[] data = buffer.flush();
		StreamBuffer stream = parentBuilder.buffer();
		stream.write(data);
	}
	
	public boolean isBuffered() {
		return this.streamBuilder.bufferedSize() > 0;
	}
	
	public void finish() {
//		this.log.debug("Finish Func Called");
		this.isFeedEnd = true;
	}
	
	public boolean isDone() {
//		this.log.debug("## isFeedEnd: " + this.isFeedEnd + ", bufferedSize: " + this.streamBuilder.bufferedSize());

		// feed가 finished 되었고 buffer에 사이즈가 없으면 done
		return this.isFeedEnd && this.streamBuilder.bufferedSize() == 0;
	}


}
