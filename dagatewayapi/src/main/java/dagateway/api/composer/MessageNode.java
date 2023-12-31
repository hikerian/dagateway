package dagateway.api.composer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dagateway.api.composer.builder.StreamBuilder;


/**
 * Tree Node Mapping Class.
 * @author Dong-il Cho
 */
public class MessageNode {
	private final Logger log = LoggerFactory.getLogger(MessageNode.class);
	
	private String alias;
	private String name;
	private MessageNode parent;
	protected List<MessageNode> children;
	
	protected DataProxy provider;

	protected Runnable buildFunc = () -> {
		this.initBuild();
		this.buildFirst();
	};

	private List<MessageNode> buildableChildren = null;
	private MessageNode offset = null;
	protected boolean isDone = false;
	
	protected MessageSerializer messageSerializer;
	
	
	public MessageNode(String alias, String name) {
		this.alias = alias;
		this.name = name;
	}
	
	public String getAlias() {
		return this.alias;
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
	
	public boolean isBuffered() {
		if(this.provider != null) {
			return this.provider.isBuffered();
		} else if(this.children != null && this.children.size() > 0) {
			// all children is buffered -> true
			for(MessageNode child : this.children) {
				if(child.isBuffered() == false) {
					return false;
				}
			}
			return true;
		}
		return true;
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
	private void initBuild() {
		if(this.children != null) {
			this.buildableChildren = new ArrayList<>(this.children);
		} else {
			this.buildableChildren = Collections.emptyList();
		}
	}
	
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
	
		MessageNode offset = this.getOffsetNode();
		if(offset == null) {
			this.isDone = true;
			this.messageSerializer.endObject();
			return;
		}
		
		offset.buildNext();
		
		if(offset.isDone()) {
			this.buildChildren();
		}
	}
	
	protected MessageNode getOffsetNode() {
		if(this.offset != null) {
			if(this.offset.isDone() == false) {
				return this.offset;
			} else {
				this.buildableChildren.remove(this.offset);
			}
		}
		this.offset = null;
		MessageNode nextOffset = null;
		for(MessageNode offset : this.buildableChildren) {
			if(offset.isDone() == false) {
				if(offset.isBuffered()) {
					this.offset = offset;
					return offset;
				}
				if(nextOffset == null) {
					nextOffset = offset;
				}
			}
		}
		this.offset = nextOffset;

		return nextOffset;
	}


}
