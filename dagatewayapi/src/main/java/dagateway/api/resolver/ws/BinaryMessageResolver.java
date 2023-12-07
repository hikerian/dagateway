package dagateway.api.resolver.ws;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;



/**
 * @author Dong-il Cho
 */
public class BinaryMessageResolver extends AbstractMessageResolver<byte[]> {
	
	
	public BinaryMessageResolver() {
	}

	@Override
	public byte[] extract(WebSocketMessage message) {
		DataBuffer payload = message.getPayload();
		
		int count = payload.readableByteCount();
		byte[] source = new byte[count];
		try (InputStream in = payload.asInputStream()) {
			in.read(source);
		} catch(IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
		return source;
	}

	@Override
	public WebSocketMessage build(WebSocketSession session, byte[] message) {
		return session.binaryMessage((factory)-> {
			return factory.wrap(message);
		});
	}

}
