package dagateway.api.composer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dagateway.api.composer.builder.StreamBuilder;


/**
 * 매핑된 노드의 계층구조를 나타내는 Class.
 * @author Dong-il Cho
 */
public class MessageNode {
	private final Logger log = LoggerFactory.getLogger(MessageNode.class);
	
	private String name;
	private MessageNode parent;
	protected List<MessageNode> children;
	
	protected DataProxy provider;

	protected Runnable buildFunc = () -> this.buildFirst();

	protected boolean isDone = false;
	protected int childIdx = 0;
	
	protected MessageSerializer messageSerializer;
	
	
	public MessageNode(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public MessageNode setParent(MessageNode parent) {
		this.parent = parent;
		return this;
	}
	
	public MessageNode getParent() {
		return this.parent;
	}
	
	public List<MessageNode> getChildren() {
		return this.children == null ? Collections.emptyList() : Collections.unmodifiableList(this.children);
	}
	
	public MessageNode addChild(MessageNode child) {
		if(this.children == null) {
			this.children = new ArrayList<>();
		}
		child.setParent(this);
		this.children.add(child);
		
		return this;
	}
	
	public MessageNode getNamedChild(String name) {
		if(this.children != null) {
			for(MessageNode element : this.children) {
				if(name.equals(element.getName())) {
					return element;
				}
			}
		}
		return null;
	}
	
	public boolean hasPreviousSibling() {
		if(this.parent != null) {
			List<MessageNode> sibling = this.parent.getChildren();
			return sibling.indexOf(this) > 0;
		}
		return false;
	}

	public void join(DataProxy provider) {
		this.provider = provider;
	}
	
	public DataProxy getProvider() {
		return this.provider;
	}
	
	public boolean hasProvider() {
		return this.provider != null;
	}

	public void setSerializer(MessageSerializer serializer) {
		this.messageSerializer = serializer;
		if(this.provider != null) {
			StreamBuilder streamBuilder = this.messageSerializer.getDataProxyStreamBuilder(this);
			this.provider.setStreamBuilder(streamBuilder);
		}
		if(this.children != null) {
			for(MessageNode element : this.children) {
				element.setSerializer(serializer);
			}
		}
	}
	
	public boolean isDone() {
		if(this.provider != null) {
			return this.provider.isDone();
		}
		return this.isDone;
	}

	public void buildNext() {
//		this.log.debug(this.name + " buildNext()");
		this.buildFunc.run();
	}

	/*
	 * helper methods
	 */
	protected void buildFirst() {
//		this.log.debug(this.name + " buildFirst()");
		
		this.messageSerializer.nodeName(this, this.name);
		if(this.provider != null) {
			this.buildFunc = () -> this.buildDataProxy();
		} else {
			this.messageSerializer.startObject();
			this.buildFunc = () -> this.buildChildren();
		}
		this.buildFunc.run();
	}
	
	protected void buildDataProxy() {
//		this.log.debug(this.name + " buildDataProxy()");
		
		this.provider.buildNext();
	}
	
	protected void buildChildren() {
//		this.log.debug(this.name + " buildChildren()");

		if(this.children == null || this.children.size() == 0) {
			this.isDone = true;
			this.messageSerializer.endObject();
			return;
		}
		
		MessageNode offset = this.children.get(this.childIdx);
		if(offset.isDone()) {
			if(this.childIdx + 1 < this.children.size()) {
				offset = this.children.get(++this.childIdx);
			} else {
				this.isDone = true;
				this.messageSerializer.endObject();
				return;
			}
		}
		
		offset.buildNext();
		
		if(offset.isDone()) {
			this.buildChildren();
		}
	}


}
