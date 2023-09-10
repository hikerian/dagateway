package dagateway.server.transform.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;

import dagateway.api.composer.DataProxy;
import dagateway.api.composer.MessageSchema;
import dagateway.api.composer.MessageSerializer;
import dagateway.api.composer.builder.json.JsonStreamBuilder;
import dagateway.api.composer.graphql.GraphQLComposerBuilder;
import dagateway.api.composer.stream.LinkedByteBuffer;
import dagateway.api.context.RouteContext.TransformSpec;
import dagateway.api.transform.AbstractDataTransformer;
import graphql.ExecutionInput;
import graphql.ParseAndValidate;
import graphql.ParseAndValidateResult;
import graphql.execution.instrumentation.DocumentAndVariables;



public class JSONGraphTransformer extends AbstractDataTransformer<DataBuffer, DataBuffer> {
	private final Logger log = LoggerFactory.getLogger(JSONGraphTransformer.class);
	private DataProxy dataProxy;
	private MessageSerializer serializer;
	
	
	public JSONGraphTransformer() {
	}
	
	@Override
	protected void doInit() {
		TransformSpec transformSpec = this.getTransformSpec();
		String query = transformSpec.getBodyGraph();
		
		ExecutionInput executionInput = ExecutionInput.newExecutionInput(query).build();
		ParseAndValidateResult parseResult = ParseAndValidate.parse(executionInput);
		if(parseResult.isFailure()) {
			throw new IllegalArgumentException(parseResult.getSyntaxException());
		}
		DocumentAndVariables documentAndVariable = parseResult.getDocumentAndVariables();
		
		MessageSchema messageStructure = GraphQLComposerBuilder.build(documentAndVariable);
		DataProxy dataProxy = new DataProxy();
		messageStructure.join(dataProxy);
		this.dataProxy = dataProxy;
		
		MessageSerializer serializer = new MessageSerializer(messageStructure, () -> {
			return new JsonStreamBuilder(new LinkedByteBuffer());
		});
		this.serializer = serializer;
	}

	@Override
	public DataBuffer transform(DataBuffer source) {
		this.log.debug("transform");
		
		this.dataProxy.push(source.asByteBuffer());
		
		byte[] resBuffer = this.serializer.buildNext();
		DataBufferUtils.release(source);
		
		if(resBuffer != null && resBuffer.length > 0) {
			return source.factory().wrap(resBuffer);
		}
		
		return null;
	}

}
