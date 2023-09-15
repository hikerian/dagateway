package dagateway.api.composer.stream;


/**
 * @author Dong-il Cho
 */
public interface StreamBuffer {
	public StreamBuffer write(char data);
	public StreamBuffer write(byte data);
	public StreamBuffer write(byte[] datas);
	public StreamBuffer write(byte[] datas, int offset, int length);
	public int size();
	public byte[] flush();
	public void close();

}
