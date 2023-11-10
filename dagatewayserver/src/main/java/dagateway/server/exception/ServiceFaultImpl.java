package dagateway.server.exception;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

import dagateway.api.service.ServiceFault;


public class ServiceFaultImpl implements ServiceFault {
	private static final long serialVersionUID = -1866362952264018817L;

	private String code;
	private String message;
	private String cause;

	
	public ServiceFaultImpl() {
	}

	public String getCode() {
		return this.code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getCause() {
		return this.cause;
	}

	public void setCause(String cause) {
		this.cause = cause;
	}
	
	@Override
	public Map<String, Object> toMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("code", this.getCode());
		map.put("message", this.getMessage());
		map.put("cause", this.getCause());
		
		return map;
	}
	
	@Override
	public String toString() {
		try {
			JsonMapper mapper = JsonMapper
					.builder()
					.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
					.build();
			return mapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
}
