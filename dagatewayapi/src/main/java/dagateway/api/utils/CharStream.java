package dagateway.api.utils;

import java.util.Arrays;


public class CharStream {
	private int increase = 32;
	private char buf[];
	private int count;

	private int pos;
	private int markedPos = -1;

	
	public CharStream() {
		this(32, 32);
	}
	
	public CharStream(int initialSize, int increase) {
		if(initialSize < 0) {
			throw new IllegalArgumentException("Negative initial size: " + initialSize);
		}
		this.buf = new char[initialSize];
		this.increase = increase;
		this.pos = 0;
		this.count = 0;
	}
	
	public void write(int c) {
		int newcount = this.count + 1;
		if(newcount > this.buf.length) {
			this.buf = Arrays.copyOf(this.buf, Math.max(this.buf.length + this.increase, newcount));
		}
		this.buf[this.count] = (char)c;
		this.count = newcount;
	}
	
	public void write(char c[], int off, int len) {
		if((off < 0) || (off > c.length) || (len < 0) ||
				((off | len) > c.length) || ((off + len) < 0)) {
			throw new IndexOutOfBoundsException();
		} else if(len == 0) {
			return;
		}
		
		int newcount = this.count + len;
		if(newcount > this.buf.length) {
			this.buf = Arrays.copyOf(this.buf, Math.max(this.buf.length + this.increase, newcount));
		}
		System.arraycopy(c, off, this.buf, this.count, len);
		this.count = newcount;
	}
	
	public char[] toArray() {
		return Arrays.copyOfRange(this.buf, 0, this.count);
	}
	
	public int read() {
		if(this.pos >= this.count) {
			return -1;
		} else {
			return this.buf[this.pos++];
		}
	}
	
	public int read(char b[], int off, int len) {
		if((off < 0) || (off > b.length) || (len < 0) ||
				((off + len) > b.length) || ((off + len) < 0)) {
			throw new IndexOutOfBoundsException();
		} else if(len == 0) {
			return 0;
		}
		
		if(this.pos >= this.count) {
			return -1;
		}
		
		int avail = this.count - this.pos;
		if(len > avail) {
			len = avail;
		}
		if(len <= 0) {
			return 0;
		}
		System.arraycopy(this.buf, this.pos, b, off, len);
		this.pos += len;

		return len;
	}
	
	public void trim() {
		this.buf = Arrays.copyOfRange(this.buf, this.pos, this.count);
		
		this.pos = 0;
		this.count = this.buf.length;
	}
	
	public int position() {
		return this.pos;
	}
	
	public void position(int position) {
		this.pos = position;
	}
	
	public int mark() {
		this.markedPos = this.pos;
		return this.markedPos;
	}
	
	public void reset() {
		this.pos = this.markedPos;
	}


}
