package com.asentinel.common.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.lob.LobHandler;

/**
 * Simple mapper to extract a BLOB from a 1 column resultset as byte array.
 * <br>
 * Instances of this class should be used as effectively immutable objects.
 * 
 * @author Razvan Popian
 */
public class ByteArrayRowMapper extends LobHandlerSupport implements RowMapper<byte[]> {
	
	public ByteArrayRowMapper(LobHandler lobHandler) {
		super.setLobHandler(lobHandler);
	}
	
	/**
	 * @deprecated in favor of {@link #ByteArrayRowMapper(LobHandler)}.
	 */
	@Deprecated
	public ByteArrayRowMapper() {
		
	}
	
	
	@Override
	public byte[] mapRow(ResultSet rs, int rowNum) throws SQLException {
		return ResultSetUtils.getBlobAsBytes(rs, 1, getLobHandler());
	}
	
	@Override
	public String toString() {
		return "ByteArrayRowMapper";
	}

}
