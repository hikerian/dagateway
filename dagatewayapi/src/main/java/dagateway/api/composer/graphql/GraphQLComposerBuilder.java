package dagateway.api.composer.graphql;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import dagateway.api.composer.DataProxy;
import dagateway.api.composer.MessageNode;
import dagateway.api.composer.MessageSchema;
import dagateway.api.context.RouteRequestContext.ServiceSpec;
import graphql.execution.instrumentation.DocumentAndVariables;
import graphql.language.Argument;
import graphql.language.Document;
import graphql.language.Field;
import graphql.language.OperationDefinition;
import graphql.language.OperationDefinition.Operation;
import graphql.language.Selection;
import graphql.language.SelectionSet;


/**
 * @author Dong-il Cho
 */
public class GraphQLComposerBuilder {
	public static final String CLIENT_RESPONSE_GRAPH_KEY = "CLIENT_RESPONSE_GRAPH";
	public static final String CLIENT_REQUEST_GRAPH_KEY = "CLIENT_REQUEST_GRAPH";
	public static final String TRANSFORM_GRAPH_KEY = "TRANSFORM_GRAPH";
	
	
	private GraphQLComposerBuilder() {
	}
	
	public static MessageSchema build(DocumentAndVariables documentAndVariable) {
		return GraphQLComposerBuilder.buildAndMap(documentAndVariable, Collections.emptyList());
	}
	
	public static MessageSchema buildAndMap(DocumentAndVariables documentAndVariable, List<ServiceSpec> serviceSpecList) {
		Document document = documentAndVariable.getDocument();
		
		// TODO Argument, Variable 처리
		Map<String, Object> variables = documentAndVariable.getVariables();
		
		Optional<OperationDefinition> optionalOperationDefinition =
				document.getFirstDefinitionOfType(OperationDefinition.class);
		OperationDefinition operationDefinition = optionalOperationDefinition.get();
		
		Operation operation = operationDefinition.getOperation();
		if(Operation.QUERY != operation) {
			throw new UnsupportedOperationException("Unsupported GraphQL Operation: " + operation);
		}
		
		String messageName = operationDefinition.getName();
		
		MessageSchema messageStructure = new MessageSchema(messageName);
		if(messageName != null) {
			GraphQLComposerBuilder.mapServiceSpecAndProxy(messageStructure, messageName, serviceSpecList);
		}
		
		SelectionSet selectionSet = operationDefinition.getSelectionSet();
		if(selectionSet != null) {
			List<Selection> selectionList = selectionSet.getSelections();
			for(Selection selection : selectionList) {
				GraphQLComposerBuilder.traversal(messageStructure, selection, variables, serviceSpecList);
			}
		}

		return messageStructure;
	}
	
	private static void traversal(MessageNode parent, Selection selection, Map<String, Object> variables, List<ServiceSpec> serviceSpecList) {
		if(selection instanceof Field == false) {
			return;
		}

		Field field = (Field)selection;
		String alias = field.getAlias();
		String name = field.getName();
		List<Argument> arguments = field.getArguments();
		// TODO support arguments?
		
		MessageNode element = new MessageNode(alias == null ? name : alias);
		parent.addChild(element);
		
		SelectionSet selectionSet = field.getSelectionSet();
		if(selectionSet != null) {
			List<Selection> childList = selectionSet.getSelections();
			for(Selection child : childList) {
				GraphQLComposerBuilder.traversal(element, child, variables, serviceSpecList);
			}
		}
		
		GraphQLComposerBuilder.mapServiceSpecAndProxy(element, name, serviceSpecList);
	}
	
	private static void mapServiceSpecAndProxy(MessageNode element, String name, List<ServiceSpec> serviceSpecList) {
		Optional<ServiceSpec> optionalServiceSpec = GraphQLComposerBuilder.findServiceSpec(name, serviceSpecList);
		if(optionalServiceSpec.isPresent()) {

			DataProxy dataProxy = new DataProxy();
			element.join(dataProxy);
			
			ServiceSpec serviceSpec = optionalServiceSpec.get();
			serviceSpec.setDataProxy(dataProxy);
		}
	}
	
	private static Optional<ServiceSpec> findServiceSpec(String serviceName, List<ServiceSpec> serviceSpecList) {
		for(ServiceSpec spec : serviceSpecList) {
			if(serviceName.equals(spec.getName())) {
				return Optional.of(spec);
			}
		}
		
		return Optional.empty();
	}



}
