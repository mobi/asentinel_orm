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
	public void free() throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public InputStream getBinaryStream() throws SQLException {
		return new ByteArrayInputStream(bytes);
	}

	@Override
	public InputStream getBinaryStream(long pos, long length)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getBytes(long pos, int length) throws SQLException {
		return bytes;
	}

	@Override
	public long length() throws SQLException {
		return bytes.length;
	}

	@Override
	public long position(byte[] pattern, long start) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long position(Blob pattern, long start) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public OutputStream setBinaryStream(long pos) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int setBytes(long pos, byte[] bytes) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int setBytes(long pos, byte[] bytes, int offset, int len)
			throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void truncate(long len) throws SQLException {
		// TODO Auto-generated method stub
		
	}

}
