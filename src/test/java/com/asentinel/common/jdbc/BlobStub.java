package com.asentinel.common.jdbc;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;

import com.asentinel.common.util.Assert;

public class BlobStub implements Blob {
	
	private final byte[] bytes;
	
	public BlobStub(byte[] bytes) {
		Assert.assertNotNull(bytes, "bytes");
		this.bytes = bytes;
	}

	@Override
	public void free() {

	}

	@Override
	public InputStream getBinaryStream() {
		return new ByteArrayInputStream(bytes);
	}

	@Override
	public InputStream getBinaryStream(long pos, long length) {
		return null;
	}

	@Override
	public byte[] getBytes(long pos, int length) {
		return bytes;
	}

	@Override
	public long length() throws SQLException {
		return bytes.length;
	}

	@Override
	public long position(byte[] pattern, long start) {
		return 0;
	}

	@Override
	public long position(Blob pattern, long start) {
		return 0;
	}

	@Override
	public OutputStream setBinaryStream(long pos) {
		return null;
	}

	@Override
	public int setBytes(long pos, byte[] bytes) {
		return 0;
	}

	@Override
	public int setBytes(long pos, byte[] bytes, int offset, int len) {
		return 0;
	}

	@Override
	public void truncate(long len) {

	}
}
