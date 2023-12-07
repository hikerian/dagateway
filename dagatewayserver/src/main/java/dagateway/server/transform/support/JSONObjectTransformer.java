package dagateway.server.transform.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dagateway.api.service.ServiceFault;
import dagateway.api.transform.AbstractDataTransformer;
import net.minidev.json.JSONObject;



public class JSONObjectTransformer extends AbstractDataTransformer<JSONObject, JSONObject> {
	private final Logger log = LoggerFactory.getLogger(JSONObjectTransformer.class);
	
	
	public JSONObjectTransformer() {
	}
	
	@Override
	protected void doInit() {
	}

	@Override
	public JSONObject transform(JSONObject source) {
		this.log.debug("transform");
		return source;
	}

	@Override
	public JSONObject transform(ServiceFault fault) {
		JSONObject jsonObj = new JSONObject(fault.toMap());
		return jsonObj;
	}


}
