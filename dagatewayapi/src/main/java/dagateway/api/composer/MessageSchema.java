package dagateway.api.composer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Dong-il Cho
 */
public class MessageSchema extends MessageNode {
	private final Logger log = LoggerFactory.getLogger(MessageSchema.class);
	
	
	public MessageSchema() {
		this("message");
	}
	
	public MessageSchema(String name) {
		super(name);
	}

	protected void buildFirst() {
		this.log.debug("buildFirst()");

		if(this.provider != null) {
			this.buildFunc = () -> this.buildDataProxy();
		} else {
			this.messageSerializer.startMessage();
			
			if(this.children == null || this.children.size() == 0) {
				this.isDone = true;
				
				this.messageSerializer.endMessage();
				return;
			}

			this.buildFunc = () -> this.buildChildren();
		}
		this.buildFunc.run();
	}
	
	protected void buildDataProxy() {
		this.log.debug("buildDataProxy()");

		this.provider.buildNext();

		if(this.provider.isDone()) {
			this.isDone = true;
			this.messageSerializer.endMessage();
		}
	}
	
	protected void buildChildren() {
		this.log.debug("buildChildren()");
		
		MessageNode offset = this.children.get(this.childIdx);
		if(offset.isDone()) {
			if(this.childIdx + 1 < this.children.size()) {
				offset = this.children.get(++this.childIdx);
			} else {
				this.isDone = true;
				this.messageSerializer.endMessage();
				return;
			}
		}
		
		offset.buildNext();
		
		if(offset.isDone()) {
			this.buildChildren();
		}
	}
}
