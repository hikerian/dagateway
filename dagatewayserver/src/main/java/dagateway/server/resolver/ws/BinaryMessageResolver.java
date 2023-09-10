package dagateway.server.resolver.ws;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;

import dagateway.api.resolver.ws.AbstractWebSocketMessageResolver;



public class BinaryMessageResolver extends AbstractWebSocketMessageResolver<byte[]> {
	private final Logger log = LoggerFactory.getLogger(BinaryMessageResolver.class);
	
	
	public BinaryMessageResolver() {
	}

	@Override
	public WebSocketMessage resolve(WebSocketSession session, WebSocketMessage message) {
		this.log.debug("resolve");
		DataBuffer payload = message.getPayload();
		
		int count = payload.readableByteCount();
		byte[] source = new byte[count];
		try (InputStream in = payload.asInputStream()) {
			in.read(source);
		} catch(IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
		// transform
		byte[] target = this.transformer.transform(source);
		
		return session.binaryMessage(dataBufferFactory -> dataBufferFactory.wrap(target));
	}

}
