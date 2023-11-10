package dagateway.api.context;


public interface AttributeOwner {
	public Object getAttribute(String name);
	public void setAttribute(String name, Object value);
}
