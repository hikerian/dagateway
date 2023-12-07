package dagateway.api.service;

import java.io.Serializable;
import java.util.Map;



/**
 * @author Dong-il Cho
 */
public interface ServiceFault extends Serializable {
	
	public Map<String, Object> toMap();
	public String toString();

}
