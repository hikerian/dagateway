package dagateway.server.handler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;

import dagateway.api.handler.AbstractContentHandler;
import dagateway.api.utils.CharStream;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;



/**
 * @author Dong-il Cho
 */
public class DataBuffer2TextPlainHandler extends AbstractContentHandler<Flux<DataBuffer>, DataBuffer, String, String, Mono<String>> {
	private final Logger log = LoggerFactory.getLogger(DataBuffer2TextPlainHandler.class);
	
	public static final String ARGUMENT_TYPE = "reactor.core.publisher.Flux<org.springframework.core.io.buffer.DataBuffer>";
	public static final String RETURN_TYPE = "reactor.core.publisher.Mono<java.lang.String>";
	public static final String TRANS_ARGUMENT_TYPE = "java.lang.String";
	public static final String TRANS_RETURN_TYPE = "java.lang.String";
	
	private byte[] remainBytes = null;
	
	
	public DataBuffer2TextPlainHandler() {
	}

	@Override
	public String getArgumentTypeName() {
		return ARGUMENT_TYPE;
	}

	@Override
	public String getReturnTypeName() {
		return RETURN_TYPE;
	}

	@Override
	public String getTransArgumentTypeName() {
		return TRANS_ARGUMENT_TYPE;
	}

	@Override
	public String getTransReturnTypeName() {
		return TRANS_RETURN_TYPE;
	}

	@Override
	public Mono<String> handle(Flux<DataBuffer> responseBody) {
		this.log.debug("handle");
		
		CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
		CharBuffer decodeBuffer = CharBuffer.allocate(8192);
		
		Mono<String> strData = responseBody.collect(() -> new CharStream(2048, 64)
				, (collector, dataBuffer)-> {
			int readableCount = dataBuffer.readableByteCount();
			byte[] data = null;
			int offset = 0;
			if(this.remainBytes != null) {
				data = new byte[readableCount + this.remainBytes.length];
				System.arraycopy(this.remainBytes, 0, data, 0, this.remainBytes.length);
				offset = this.remainBytes.length;
			} else {
				data = new byte[readableCount];
				offset = 0;
			}
			
			try(InputStream in = dataBuffer.asInputStream(true)) {
				in.read(data, offset, readableCount);
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
			
			readableCount += offset;
			
			ByteBuffer byteBuffer = ByteBuffer.wrap(data);
			
			char[] buf = decodeBuffer.array();
			boolean loop = true;
			while(loop) {
				CoderResult coderResult = decoder.decode(byteBuffer, decodeBuffer, false);
				int position = decodeBuffer.position();
				collector.write(buf, 0, position);
				
				decodeBuffer.clear();
				if(coderResult.isOverflow() == false) {
					loop = false;
					if(readableCount > byteBuffer.position()) {
						byte[] remainData = new byte[readableCount - byteBuffer.position()];
						byteBuffer.get(remainData);
						
						this.remainBytes = remainData;
					} else {
						this.remainBytes = null;
					}
				}
			}
			decoder.reset();
		}).map(collector -> this.transformer.transform(new String(collector.toArray())));

		return strData;
	}

	@Override
	protected Mono<String> wrapSingle(String value) {
		return Mono.just(value);
	}

	
}
