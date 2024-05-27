package com.asentinel.common.jdbc;

import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.util.FileCopyUtils;

/**
 * Simple mapper to extract a BLOB from a 1 column resultset as an InputStream. It is
 * the responsibility of the client to close the streams resulted from this class.
 * After obtaining the list of InputStreams the client can use {@link FileCopyUtils#copy(InputStream, java.io.OutputStream)}
 * method to copy the bytes somewhere else.
 * <br><br>
 * Instances of this class should be used as effectively immutable objects.
 * 
 * @author Razvan Popian
 */
public class InputStreamRowMapper extends LobHandlerSupport implements RowMapper<InputStream> {
	
	public InputStreamRowMapper(LobHandler lobHandler) {
		super.setLobHandler(lobHandler);
	}

	/**
	 * @deprecated in favor of {@link #InputStreamRowMapper()}.
	 */
	@Deprecated
	public InputStreamRowMapper() {
		
	}
	
	@Override
	public InputStream mapRow(ResultSet rs, int rowNum) throws SQLException {
		return ResultSetUtils.getBlobAsInputStream(rs, 1, getLobHandler());
	}

	@Override
	public String toString() {
		return "InputStreamRowMapper";
	}

}
