package dagateway.api.composer.builder;

import java.nio.ByteBuffer;

import dagateway.api.composer.MessageNode;
import dagateway.api.composer.stream.StreamBuffer;


/**
 * @author Dong-il Cho
 */
public interface StreamBuilder {
	public void init(MessageNode element, StreamBuilder parentBuilder);
	public MessageNode element();
	public StreamBuilder getParentBuilder();
	
	public void feed(ByteBuffer datas);
	public StreamBuffer buffer();
	public int bufferedSize();
	
	public void startMessage();
	public void endMessage();
	public void startObject();
	public void endObject();
	public void startArray();
	public void endArray();
	public void nodeName(MessageNode element, String name);

	public void dispose();


}
