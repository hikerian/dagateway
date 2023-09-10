package dagateway.api.composer.stream;


/**
 * LinkedList Style ByteStream
 * @author chodo
 *
 */
public class LinkedByteBuffer implements StreamBuffer {
	private static class ByteBufferNode {
		private byte[] buffer;
		private ByteBufferNode link;
		
		
		ByteBufferNode(int capacity) {
			this.buffer = new byte[capacity];
		}
		
		void write(int pos, byte data) {
			this.buffer[pos] = data;
		}
		
		void setLink(ByteBufferNode child) {
			this.link = child;
		}
		
		ByteBufferNode getLink() {
			return this.link;
		}
		
		byte[] getBuffer() {
			return this.buffer;
		}
		
		void close() {
			this.buffer = null;
		}
	}
	
	private int blockSize;
	private int writePos;

	private int linkCnt;
	private ByteBufferNode first;
	private ByteBufferNode last;

	
	public LinkedByteBuffer() {
		this(1024);
	}
	
	public LinkedByteBuffer(int blockSize) {
		this.blockSize = blockSize;
		this.first = new ByteBufferNode(this.blockSize);
		this.last = this.first;
		
		this.linkCnt = 1;
		this.writePos = 0;
	}
	
	public int blockSize() {
		return this.blockSize;
	}
	
	public StreamBuffer write(char data) {
		return this.write((byte)data);
	}
	
	public StreamBuffer write(byte data) {
		this.checkExpand();
		
		int linkPos = this.writePos % this.blockSize;
		this.last.write(linkPos, data);
		this.writePos++;
		
		return this;
	}
	
	public StreamBuffer write(byte[] datas) {
		return this.write(datas, 0, datas.length);
	}
	
	public StreamBuffer write(byte[] datas, int offset, int length) {
		this.checkExpand();
		
		int remainLength = length;
		int dataOffset = offset;
		
		int lastRemain = this.blockSize - (this.writePos % this.blockSize);
		lastRemain = lastRemain > remainLength ? length : lastRemain;
		int linkPos = this.writePos % this.blockSize;
		
		byte[] buffer = this.last.getBuffer();
		if(lastRemain > 0) {
			System.out.println("datas.length: " + datas.length + ", dataOffset: " + dataOffset + ", linkPos: " + linkPos + ", lastRemain: " + lastRemain);
			System.arraycopy(datas, dataOffset, buffer, linkPos, lastRemain);
		}
		
		remainLength -= lastRemain;
		this.writePos += lastRemain;
		dataOffset += lastRemain;
		
		if(remainLength > 0) {
			int linkCnt = remainLength / this.blockSize;
			for(int i = 0; i < linkCnt; i++) {
				this.expand();
				buffer = this.last.getBuffer();
				System.arraycopy(datas, dataOffset, buffer, 0, this.blockSize);
				
				remainLength -= this.blockSize;
				this.writePos += this.blockSize;
				dataOffset += lastRemain;
			}
			
			if(remainLength > 0) {
				this.expand();
				
				buffer = this.last.getBuffer();
				System.arraycopy(datas, dataOffset, buffer, 0, remainLength);
				
				this.writePos += remainLength;
			}
		}
		
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
		byte[] buffer = null;
		
		int linkCnt = this.writePos / this.blockSize;
		
		ByteBufferNode link = this.first;
		for(int i = 0; i < linkCnt; i++) {
			buffer = link.getBuffer();
			System.arraycopy(buffer, 0, ret, i * this.blockSize, this.blockSize);
			link = link.getLink();
		}
		int remain = this.writePos % this.blockSize;
		if(remain > 0) {
			buffer = link.getBuffer();
			System.arraycopy(buffer, 0, ret, (linkCnt * this.blockSize), remain);
		}
		
		return ret;
	}
	
	public void reset() {
		this.last = this.first;
		this.linkCnt = 1;
		this.writePos = 0;
	}
	
	public void close() {
		ByteBufferNode link = this.first;
		while(link != null) {
			link.close();
			ByteBufferNode child = link.getLink();
			link.setLink(null);
			link = child;
		}
	}
	
	private void checkExpand() {
		if(this.writePos >= this.blockSize * this.linkCnt) {
			this.expand();
		}
	}
	
	private void expand() {
		ByteBufferNode child = this.last.getLink();
		if(child == null) {
			child = new ByteBufferNode(this.blockSize);
			this.last.setLink(child);
		}
		this.last = child;
		this.linkCnt++;
	}

}
