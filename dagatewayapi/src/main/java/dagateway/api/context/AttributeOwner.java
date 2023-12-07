package dagateway.api.context;


/**
 * @author Dong-il Cho
 */
public interface AttributeOwner {
	public Object getAttribute(String name);
	public void setAttribute(String name, Object value);
}
