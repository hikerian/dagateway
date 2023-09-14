package dagateway.api.resolver.http;

import java.io.CharArrayWriter;


public interface CharDelimiterSupport {
	public char[] delimiters();
	public void encode(char c, CharArrayWriter out);
	public boolean stripDelimiter();

}
