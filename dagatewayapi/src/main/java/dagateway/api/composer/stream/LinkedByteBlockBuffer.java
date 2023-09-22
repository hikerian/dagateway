package dagateway.api.composer.stream;


/**
 * LinkedList Style ByteStream
 * @author Dong-il Cho
 *
 */
public class LinkedByteBlockBuffer implements StreamBuffer {
	private static class ByteBufferNode {
		private byte[] buffer;
		private ByteBufferNode link;
		private int pos;
		
		
		ByteBufferNode(int capacity) {
			this.buffer = new byte[capacity];
			this.pos = 0;
		}
		
		ByteBufferNode write(byte data) {
			if(this.pos < this.buffer.length) {
				this.buffer[this.pos] = data;
				this.pos++;

				return this;
			}
			
			if(this.link == null) {
				ByteBufferNode link = new ByteBufferNode(this.buffer.length);
				this.link = link;
			}
			
			return link.write(data);
		}
		
		ByteBufferNode write(byte[] datas, int offset, int length) {
			int remainSize = this.buffer.length - this.pos;
			if(remainSize > 0) {
				int writeSize = length < remainSize ? length : remainSize;
				System.arraycopy(datas, offset, this.buffer, this.pos, writeSize);
				this.pos += writeSize;
				offset += writeSize;
				length -= writeSize;
			}
			if(length == 0) {
				return this;
			}
			if(this.link == null) {
				ByteBufferNode link = new ByteBufferNode(this.buffer.length);
				this.link = link;
			}
			
			return this.link.write(datas, offset, length);
		}
		
		void load(byte[] data, int offset) {
			if(this.pos == 0) {
				return;
			}
			if(this.pos > 0) {
				System.arraycopy(this.buffer, 0, data, offset, this.pos);
				offset += this.pos;
				if(this.pos == this.buffer.length && this.link != null) {
					this.link.load(data, offset);
				}
			}
		}
		
		void reset() {
			this.pos = 0;
			if(this.link != null) {
				this.link.reset();
			}
		}
		
		void close() {
			this.buffer = null;
			if(this.link != null) {
				this.link.close();
				this.link = null;
			}
		}
	}
	
	private int blockSize;
	private int writePos;

	private ByteBufferNode first;
	private ByteBufferNode last;

	
	public LinkedByteBlockBuffer() {
		this(1024);
	}
	
	public LinkedByteBlockBuffer(int blockSize) {
		this.blockSize = blockSize;
		this.first = new ByteBufferNode(this.blockSize);
		this.last = this.first;
		
		this.writePos = 0;
	}
	
	public int blockSize() {
		return this.blockSize;
	}
	
	public StreamBuffer write(char data) {
		return this.write((byte)data);
	}
	
	public StreamBuffer write(byte data) {
		this.last = this.last.write(data);
		this.writePos++;
		return this;
	}
	
	public StreamBuffer write(byte[] datas) {
		return this.write(datas, 0, datas.length);
	}
	
	public StreamBuffer write(byte[] datas, int offset, int length) {
		this.last = this.last.write(datas, offset, length);
		this.writePos += length;
		
		return this;
	}
	
	public int size() {
		return this.writePos;
	}
	
	public byte[] flush() {
		byte[] buffered = this.toByteArray();
		this.reset();

		return buffered;
	}
	
	public byte[] toByteArray() {
		byte[] ret = new byte[this.writePos];
		this.first.load(ret, 0);
		
		return ret;
	}
	
	public void reset() {
		this.first.reset();
		this.last = this.first;
		this.writePos = 0;
	}
	
	public void close() {
		this.first.close();
		this.first = null;
		this.last = null;
	}

}
