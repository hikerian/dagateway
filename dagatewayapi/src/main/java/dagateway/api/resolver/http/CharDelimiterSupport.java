package dagateway.api.resolver.http;

import java.io.CharArrayWriter;


/**
 * @author Dong-il Cho
 */
public interface CharDelimiterSupport {
	public char[] delimiters();
	public void encode(char c, CharArrayWriter out);
	public boolean stripDelimiter();

}
