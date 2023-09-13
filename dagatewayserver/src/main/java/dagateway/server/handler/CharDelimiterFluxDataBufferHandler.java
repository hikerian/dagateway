package dagateway.server.handler;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;

import dagateway.api.context.RouteContext.ServiceSpec;
import dagateway.api.handler.AbstractContentHandler;
import dagateway.api.resolver.CharDelimiterSupport;
import dagateway.api.transform.DataTransformer;
import dagateway.api.utils.CharStream;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SynchronousSink;


public class CharDelimiterFluxDataBufferHandler extends AbstractContentHandler<Flux<DataBuffer>, DataBuffer, String, String, Flux<DataBuffer>> {
	private final Logger log = LoggerFactory.getLogger(CharDelimiterFluxDataBufferHandler.class);	
	
	private char[] delimiters = null;
	private boolean stripDelimiter = false;
	
	private CharDelimiterSupport charDelimiterSupport;
	
	private CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
	private CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();
	
	private byte[] remainBytes = null;
	private CharBuffer decodeBuffer = CharBuffer.allocate(8192);
	
	private CharStream stream = new CharStream(2048, 64);
	
	private CharArrayWriter sourceBuffer = new CharArrayWriter(8192);
	private CharArrayWriter resultBuffer = new CharArrayWriter(8192);
	
	
	public CharDelimiterFluxDataBufferHandler() {
	}
	
	@Override
	public void init(DataTransformer<String, String> transformer, MediaType backendContentType, MediaType clientContentType) {
		super.init(transformer, backendContentType, clientContentType);
		if(transformer != null && CharDelimiterSupport.class.isAssignableFrom(transformer.getClass())) {
			CharDelimiterSupport charDelimiterSupport = (CharDelimiterSupport)transformer;
			this.delimiters = charDelimiterSupport.delimiters();
			this.stripDelimiter = charDelimiterSupport.stripDelimiter();
			
			if(this.delimiters == null || this.delimiters.length == 0) {
				throw new IllegalArgumentException("delimiters must be one more");
			}
			this.charDelimiterSupport = charDelimiterSupport;
		} else {
			throw new IllegalArgumentException("Trasnformer must be support CharDelimiterSupport: " + transformer);
		}
	}

	@Override
	public Flux<DataBuffer> handle(Flux<DataBuffer> responseBody, ServiceSpec serviceSpec) {
		Flux<DataBuffer> transformedBody = responseBody.handle(this::handle);
		return transformedBody;
	}
	
	private void handle(DataBuffer dataBuffer, SynchronousSink<DataBuffer> sink) {
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
			int readCount = in.read(data, offset, readableCount);
			this.log.debug("READABLECOUNT: " + readableCount + ", OFFSET: " + offset + ", READCOUNT: " + readCount);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
		readableCount += offset;
		
		ByteBuffer byteBuffer = ByteBuffer.wrap(data);
		
		char[] buf = this.decodeBuffer.array();
		boolean loop = true;
		while(loop) {
			CoderResult coderResult = this.decoder.decode(byteBuffer, this.decodeBuffer, false);
			int position = this.decodeBuffer.position();
			
			this.log.debug(coderResult + ", BUFFER WRITTEN POSITION: " + position);
			
			this.stream.write(buf, 0, position); // <-- TODO encode
			
			this.decodeBuffer.clear();
			if(coderResult.isOverflow() == false) {
				loop = false;
				
				this.log.debug("readableCount: " + readableCount + ", byteBuffer.position(): " + byteBuffer.position());
				
				if(readableCount > byteBuffer.position()) {
					byte[] remainData = new byte[readableCount - byteBuffer.position()];
					byteBuffer.get(remainData);
					
					this.remainBytes = remainData;
				} else {
					this.remainBytes = null;
				}
				
				if(coderResult.isMalformed()) {
					this.log.debug("+===================================+\n"
							+ "|!!!!!!!!!!!! Malformed !!!!!!!!!!!!|\n"
							+ "+===================================+");
				}
			}
		}
		this.decoder.reset();
		
		int position = this.stream.position();
		
		for(int ch; (ch = this.stream.read()) != -1;) {
			char cha = (char)ch;
			if(Arrays.binarySearch(this.delimiters, cha) >= 0) {
				if(this.stripDelimiter == false) {
					this.sourceBuffer.append(cha);
				}
				String sourceBlock = this.sourceBuffer.toString();

				// transform
				this.resultBuffer.append(this.transformer.transform(sourceBlock));

				this.sourceBuffer.reset();
				this.stream.trim();
				position = this.stream.position();
			} else {
				this.charDelimiterSupport.encode(cha, this.sourceBuffer);
			}
		}
		
		this.stream.position(position);
		
		char[] transformedData = this.resultBuffer.toCharArray();
		this.sourceBuffer.reset();
		this.resultBuffer.reset();
		
		if(transformedData != null && transformedData.length > 0) {
			try {
				ByteBuffer resultBuffer = this.encoder.encode(CharBuffer.wrap(transformedData));
				this.encoder.reset();
				
				sink.next(DefaultDataBufferFactory.sharedInstance.wrap(resultBuffer));
			} catch (CharacterCodingException e) {
				throw new RuntimeException(e);
			}
		}
	}

}
