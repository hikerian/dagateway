package dagateway.server.transform;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;

import dagateway.api.composer.DataProxy;
import dagateway.api.composer.MessageSchema;
import dagateway.api.composer.MessageSerializer;
import dagateway.api.composer.builder.json.JsonStreamBuilder;
import dagateway.api.composer.graphql.GraphQLComposerBuilder;
import dagateway.api.composer.stream.StreamBuffer;
import dagateway.api.context.RouteRequestContext.TransformSpec;
import dagateway.api.service.ServiceFault;
import dagateway.api.transform.AbstractDataTransformer;



/**
 * @author Dong-il Cho
 */
public class JSONGraphTransformer extends AbstractDataTransformer<DataBuffer, DataBuffer> {
	private final Logger log = LoggerFactory.getLogger(JSONGraphTransformer.class);
	
	public static final String ARGUMENT_TYPE = "org.springframework.core.io.buffer.DataBuffer";
	public static final String RETURN_TYPE = "org.springframework.core.io.buffer.DataBuffer";
	
	private DataProxy dataProxy;
	private MessageSerializer serializer;
	
	
	public JSONGraphTransformer() {
	}
	
	@Override
	protected void doInit() {
		TransformSpec transformSpec = this.getTransformSpec();
		String query = transformSpec.getBodyGraph();
		
		MessageSchema messageStructure = GraphQLComposerBuilder.build(query, transformSpec, GraphQLComposerBuilder.TRANSFORM_GRAPH_KEY, Collections.emptyList());
		DataProxy dataProxy = new DataProxy();
		messageStructure.join(dataProxy);
		this.dataProxy = dataProxy;
		
		MessageSerializer serializer = new MessageSerializer(messageStructure, () -> {
			return new JsonStreamBuilder(StreamBuffer.newDefaultStreamBuffer());
		});
		this.serializer = serializer;
	}

	@Override
	public DataBuffer transform(DataBuffer source) {
		this.log.debug("transform");
		
		this.dataProxy.push(source, true);
		
		byte[] resBuffer = this.serializer.buildNext();
		if(resBuffer != null && resBuffer.length > 0) {
			return source.factory().wrap(resBuffer);
		}
		
		return null;
	}

	@Override
	public DataBuffer transform(ServiceFault fault) {
		String json = fault.toString();
		return DefaultDataBufferFactory.sharedInstance.wrap(json.getBytes(StandardCharsets.UTF_8));
	}


}
