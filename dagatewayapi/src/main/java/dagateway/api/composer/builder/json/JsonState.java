package dagateway.api.composer.builder.json;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonToken;

import dagateway.api.composer.MessageNode;


/**
 * @author Dong-il Cho
 */
public class JsonState {
	private final Logger log = LoggerFactory.getLogger(JsonState.class);
	
	private MessageNode topology;
	private ArrayDeque<Stage> stageStack; // use for stack
	

	private static class Stage {
		private JsonToken stage;
		private MessageNode messageElement;
		private int sequence;

		
		public Stage(JsonToken stage) {
			this(stage, null);
		}
		
		public Stage(JsonToken stage, MessageNode messageElement) {
			this.stage = stage;
			this.messageElement = messageElement;
			this.sequence = 0;
		}
		
		public JsonToken getStage() {
			return this.stage;
		}
		
		public MessageNode getElement() {
			return this.messageElement;
		}
		
		public int getSequence() {
			return this.sequence;
		}
		
		public int add() {
			return ++this.sequence;
		}

		@Override
		public String toString() {
			return "Stage [stage=" + stage + ", messageElement=" + messageElement
					+ ", sequence=" + sequence + "]";
		}
	}
	
	
	public JsonState(MessageNode topology) {
		this.topology = topology;
		this.stageStack = new ArrayDeque<>();
	}
	
	public int currentDepth() {
		return this.stageStack.size();
	}
	
	public boolean isFirstObject() {
		return this.isFirstContainer();
	}
	
	public void newObject() {
		this.addNewContainer(JsonToken.START_OBJECT);
	}
	
	public void endObject() {
		this.endContainer(JsonToken.START_OBJECT);
	}
	
	public boolean isFirstArray() {
		return this.isFirstContainer();
	}
	
	public void newArray() {
		this.addNewContainer(JsonToken.START_ARRAY);
	}
	
	public void endArray() {
		this.endContainer(JsonToken.START_ARRAY);
	}
	
	public boolean isFirstField() {
		if(this.stageStack.size() == 0) {
			throw new IllegalStateException("Parent Not Found");
		}

		Stage parent = this.stageStack.getFirst();
		JsonToken parentToken = parent.getStage();
		if(parentToken == JsonToken.START_OBJECT) {
			return parent.getSequence() == 0;
		} else {
			throw new IllegalStateException("Illegal Parent Stage: " + parentToken);
		}
	}
	
	public void newField(String fieldName, MessageNode messageElement) {
		if(this.stageStack.size() > 0) {

			Stage parent = this.stageStack.getFirst();
			JsonToken parentToken = parent.getStage();
			if(parentToken == JsonToken.START_OBJECT) {
				parent.add();
				this.stageStack.addFirst(new Stage(JsonToken.FIELD_NAME, messageElement));
			} else {
				throw new IllegalStateException("Illegal Parent Stage: " + parentToken);
			}
		} else {
			throw new IllegalStateException("Parent Not Found");
		}
	}
	
	public boolean isFirstValue() {
		if(this.stageStack.size() > 0) {
			Stage parent = this.stageStack.getFirst();
			JsonToken parentToken = parent.getStage();
			if(parentToken == JsonToken.START_ARRAY) {
				return parent.getSequence() == 0;
			} else if(parentToken == JsonToken.FIELD_NAME) {
				return true;
			} else {
				throw new IllegalStateException("Illegal Parent Stage: " + parentToken);
			}
		} else {
			throw new IllegalStateException("Field Not Found");
		}
	}
	
	public void newValue() {
		if(this.stageStack.size() > 0) {
			Stage parent = this.stageStack.getFirst();
			JsonToken parentToken = parent.getStage();
			if(parentToken == JsonToken.START_ARRAY) {
				parent.add();
			} else if(parentToken == JsonToken.FIELD_NAME) {
				this.stageStack.removeFirst();
			} else {
				throw new IllegalStateException("Illegal Parent Stage: " + parentToken);
			}
		} else {
			throw new IllegalStateException("Field Not Found");
		}
	}
	
	public MessageNode getAcceptanceField(String fieldName) {
		MessageNode messageElement = this.nearestElement();
		List<MessageNode> children = messageElement.getChildren();
		if(children != null) {
			for(MessageNode child : children) {
				if(fieldName.equals(child.getName())) {
					this.log.debug("Field: " + fieldName + " is child of " + messageElement.getName());
					return child;
				}
			}
		}
		this.log.debug("Field: " + fieldName + " is not child of " + (messageElement == null ? null : messageElement.getName()));
		this.log.debug(this.stageStack.toString());
		return null;
	}
	
	public MessageNode nearestElement() {
		Iterator<Stage> stageIterator = this.stageStack.iterator();
		while(stageIterator.hasNext()) {
			Stage stage = stageIterator.next();
			if(stage.getStage() == JsonToken.FIELD_NAME) {
				return stage.getElement();
			}
		}
		
		return this.topology;
	}
	
	/*
	 * helper methods
	 */

	private boolean isFirstContainer() {
		if(this.stageStack.size() == 0) {
			return true;
		}
		Stage parent = this.stageStack.getFirst();
		JsonToken parentToken = parent.getStage();
		if(parentToken == JsonToken.START_ARRAY) {
			return parent.getSequence() == 0;
		} else if(parentToken == JsonToken.FIELD_NAME) {
			return true;
		} else {
			throw new IllegalStateException("Illegal Parent Stage: " + parentToken);
		}
	}
	
	private void addNewContainer(JsonToken container) {
		if(this.stageStack.size() > 0) {
			Stage parent = this.stageStack.getFirst();
			JsonToken parentToken = parent.getStage();
			if(parentToken == JsonToken.START_ARRAY) {
				parent.add();
				this.stageStack.addFirst(new Stage(container));
			} else if(parentToken == JsonToken.FIELD_NAME) {
				this.stageStack.addFirst(new Stage(container));
			} else {
				throw new IllegalStateException("Illegal Parent Stage: " + parentToken);
			}
		} else {
			this.stageStack.addFirst(new Stage(container));
		}
	}
	
	private void endContainer(JsonToken startToken) {
		Stage stage = this.stageStack.removeFirst();
		JsonToken token = stage.getStage();
		if(token != startToken) {
			throw new IllegalStateException("Illegal Current Token: " + token + " assumed: " + startToken);
		}
		if(this.stageStack.size() > 0) {
			Stage parent = this.stageStack.getFirst();
			if(parent.getStage() == JsonToken.FIELD_NAME) {
				this.stageStack.removeFirst();
			}
		}
	}








}
