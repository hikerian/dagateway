package dagateway.api.composer.stream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


/**
 * @author Dong-il Cho
 */
public class ByteArrayStreamBuffer implements StreamBuffer {
	private ByteArrayOutputStream out;
	
	
	public ByteArrayStreamBuffer() {
		this.out = new ByteArrayOutputStream(512);
	}

	@Override
	public StreamBuffer write(char data) {
		this.out.write((int)data);
		return this;
	}

	@Override
	public StreamBuffer write(byte data) {
		this.out.write((int)data);
		return this;
	}

	@Override
	public StreamBuffer write(byte[] datas) {
		try {
			this.out.write(datas);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return this;
	}

	@Override
	public StreamBuffer write(byte[] datas, int offset, int length) {
		this.out.write(datas, offset, length);
		return this;
	}

	@Override
	public int size() {
		return this.out.size();
	}

	@Override
	public byte[] flush() {
		byte[] datas = this.out.toByteArray();
		this.out.reset();
		return datas;
	}

	@Override
	public void close() {
		try {
			this.out.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		this.out = null;
	}

}
