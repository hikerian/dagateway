package dagateway.api.transform;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.springframework.util.MimeType;


public interface StringDelimiterSupport {
	public static final List<String> DEFAULT_DELIMITERS = Arrays.asList("\r\n", "\n");
	public static final MimeType DEFAULT_MIMETYPE = new MimeType("text", "plain", StandardCharsets.UTF_8);
	
	public List<String> delimiters();
	public boolean stripDelimiter();

}
