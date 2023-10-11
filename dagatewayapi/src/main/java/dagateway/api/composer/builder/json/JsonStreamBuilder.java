package dagateway.api.composer.builder.json;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.async.ByteArrayFeeder;
import com.fasterxml.jackson.core.io.JsonStringEncoder;

import dagateway.api.composer.MessageNode;
import dagateway.api.composer.builder.AbstractStreamBuilder;
import dagateway.api.composer.stream.StreamBuffer;


/**
 * @author Dong-il Cho
 */
public class JsonStreamBuilder extends AbstractStreamBuilder {
	private final Logger log = LoggerFactory.getLogger(JsonStreamBuilder.class);
	
	private JsonParser jsonParser;
	private ByteArrayFeeder byteArrayFeeder;
	private JsonStringEncoder encoder;
	private JsonState jsonState;
	
	private boolean fieldSkipped = false;

	
	public JsonStreamBuilder(StreamBuffer buffer) {
		super(buffer);
	}
	
	@Override
	protected void doInit() {
		// SAX Style JSONParser
		JsonFactory jsonFactory = new JsonFactory();
		try {
			this.jsonParser = jsonFactory.createNonBlockingByteArrayParser();
		} catch (IOException e) {
			throw new IllegalStateException(e); // TODO Exception 정리
		}
		this.byteArrayFeeder = (ByteArrayFeeder)this.jsonParser.getNonBlockingInputFeeder();
		this.encoder = JsonStringEncoder.getInstance();
		
		this.jsonState = new JsonState(this.element());
	}

	@Override
	public void feed(byte[] buffer, int offset, int size) {
		this.log.debug("MessageNode: " + this.element().getName() + " Feed: " + size);
		try {
			this.byteArrayFeeder.feedInput(buffer, offset, size);
			this.build();
		} catch (IOException e) {
			throw new IllegalStateException(e); // TODO Exception 정리
		}
	}

	@Override
	public void startMessage() {
		this.startObject();
	}
	
	@Override
	public void endMessage() {
		this.endObject();
	}
	
	@Override
	public void nodeName(MessageNode element, String name) {
		this.jsonState.sanitizeForNextField();
		if(this.jsonState.isFirstField() == false) {
			this.write(',');
		}
		this.write('"')
			.write(this.encoder.encodeAsUTF8(name))
			.write('"').write(':');
		this.jsonState.newField(name, element);
	}
	
	@Override
	public void startObject() {
		this.write('{');
		this.jsonState.newObject();
	}
	
	@Override
	public void endObject() {
		this.write('}');
		this.jsonState.endObject();
	}
	
	@Override
	public void startArray() {
		this.write('[');
		this.jsonState.newArray();
	}
	
	@Override
	public void endArray() {
		this.write(']');
		this.jsonState.endArray();
	}
	
	/*
	 * helper methods
	 */
	private void build() throws IOException {
		JsonToken token = null;
		checkLoop: while(true) {
			token = this.jsonParser.nextToken();
			
			switch(token) {
			case NOT_AVAILABLE: {
//				this.log.debug("NOT_AVAILABLE: " + this.jsonState.currentDepth());
				break checkLoop;
			}
			case START_OBJECT: {
//				this.log.debug("START_OBJECT: " + this.jsonState.currentDepth());
				if(this.fieldSkipped) {
					this.jsonParser.skipChildren();
					this.fieldSkipped = false;
					break;
				}
				if(this.jsonState.isFirstObject() == false) {
					this.write(',');
				}
				this.write('{');
				this.jsonState.newObject();
				break;
			}
			case END_OBJECT: {
//				this.log.debug("END_OBJECT: " + this.jsonState.currentDepth());
				this.write('}');
				this.jsonState.endObject();
				break;
			}
			case START_ARRAY: {
//				this.log.debug("START_ARRAY: " + this.jsonState.currentDepth());
				if(this.fieldSkipped) {
					this.jsonParser.skipChildren();
					this.fieldSkipped = false;
					break;
				}
				if(this.jsonState.isFirstArray() == false) {
					this.write(',');
				}
				this.write('[');
				this.jsonState.newArray();
				break;
			}
			case END_ARRAY: {
//				this.log.debug("END_ARRAY: " + this.jsonState.currentDepth());
				this.write(']');
				this.jsonState.endArray();
				break;
			}
			case FIELD_NAME: {
//				this.log.debug("FIELD_NAME: " + this.jsonState.currentDepth());
				String fieldName = this.jsonParser.getCurrentName();

				// check acceptance
				MessageNode element = this.jsonState.getAcceptanceField(fieldName);
				if(element == null) {
					this.fieldSkipped = true;
					break;
				}
				
				if(this.jsonState.isFirstField() == false) {
					this.write(',');
				}
				this.write('"')
					.write(this.encoder.quoteAsUTF8(fieldName))
					.write('"')
					.write(':');
				this.jsonState.newField(fieldName, element);
				break;
			}
			case VALUE_STRING: {
				if(this.fieldSkipped) {
					this.fieldSkipped = false;
					break;
				}
//				this.log.debug("VALUE_STRING: " + this.jsonState.currentDepth());
				if(this.jsonState.isFirstValue() == false) {
					this.write(',');
				}
				this.write('"')
					.write(this.encoder.quoteAsUTF8(this.jsonParser.getValueAsString()))
					.write('"');
				
				this.jsonState.newValue();
				break;
			}
			case VALUE_EMBEDDED_OBJECT: {
//				this.log.debug("VALUE_EMBEDDED_OBJECT: " + this.jsonState.currentDepth());
				throw new UnsupportedOperationException("VALUE_EMBEDDED_OBJECT: " + this.jsonState.currentDepth());
			}
			case VALUE_NULL:
			case VALUE_TRUE:
			case VALUE_FALSE:
			case VALUE_NUMBER_INT:
			case VALUE_NUMBER_FLOAT:
				if(this.fieldSkipped) {
					this.fieldSkipped = false;
					break;
				}
//				this.log.debug("VALUE_ETC: " + this.jsonState.currentDepth());
				if(this.jsonState.isFirstValue() == false) {
					this.write(',');
				}
				this.write(this.jsonParser.getValueAsString().getBytes());
				this.jsonState.newValue();

				break;
			}
		}
	}


}
