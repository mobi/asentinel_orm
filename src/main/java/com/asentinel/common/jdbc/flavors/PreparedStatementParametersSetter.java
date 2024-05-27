package com.asentinel.common.jdbc.flavors;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;

/**
 * Interface containing methods for setting parameters on a
 * {@code PreparedStatement}. It allows database specific implementations.
 * 
 * @see JdbcFlavor#getPreparedStatementParametersSetter()
 * 
 * @author Razvan.Popian
 */
public interface PreparedStatementParametersSetter {

	LobHandler getLobHandler();
	
	LobCreator setParameters(PreparedStatement ps, Object ... inParams) throws SQLException;
	
	LobCreator setParameters(PreparedStatement ps, LobCreator lobCreator, Object ... inParams) throws SQLException;

	LobCreator setParametersWithIndex(PreparedStatement ps, int index, LobCreator lobCreator, Object ... inParams) throws SQLException;
	
	void setParameter(PreparedStatement ps, int paramIndex, int sqlType, Object inValue) throws SQLException;	
	
	void cleanupParameters(Object ... inParams);
	
	
	default LobCreator getLobCreator(LobCreator lobCreator) {
		if (lobCreator == null) {
			lobCreator = getLobHandler().getLobCreator();
		}
		return lobCreator;
	}
	
}
