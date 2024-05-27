package com.asentinel.common.jdbc;

import static com.asentinel.common.util.Assert.assertNotNull;

import java.math.BigDecimal;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.support.TransactionOperations;

import com.asentinel.common.jdbc.flavors.JdbcFlavor;
import com.asentinel.common.jdbc.flavors.JdbcFlavorConfig;
import com.asentinel.common.util.Assert;

/**
 * Implementation for the {@link InOutCall} interface. 
 * This should be used as an effectively immutable object. It should be configured,
 * published and after publishing no state changes should be allowed.
 * 
 * @see InOutCall
 * 
 * @author Razvan Popian
 */
public class InOutCallTemplate implements InOutCall {
	
	private final JdbcFlavor jdbcFlavor;
	private final JdbcOperations jdbcOperations;
	private final RowMapperFactory rowMapperFactory;
	
	private TransactionOperations transactionOperations = null;
	
	private Logger log = null;
	
	public InOutCallTemplate(JdbcFlavor jdbcFlavor, DataSource dataSource) {
		this(jdbcFlavor, dataSource, new DefaultRowMapperFactory());
	}
	
	public InOutCallTemplate(JdbcFlavor jdbcFlavor, JdbcOperations jdbcOperations) {
		this(jdbcFlavor, jdbcOperations, new DefaultRowMapperFactory());
	}
	
	
	public InOutCallTemplate(JdbcFlavor jdbcFlavor, DataSource dataSource, RowMapperFactory rowMapperFactory) {
		this(jdbcFlavor, new JdbcTemplate(dataSource), rowMapperFactory);
		
	}
	
	public InOutCallTemplate(JdbcFlavor jdbcFlavor, JdbcOperations jdbcOperations, RowMapperFactory rowMapperFactory) {
		assertNotNull(jdbcFlavor, "jdbcFlavor");
		assertNotNull(jdbcOperations, "jdbcOperations");
		assertNotNull(rowMapperFactory, "rowMapperFactory");
		this.jdbcFlavor = jdbcFlavor;
		this.jdbcOperations = jdbcOperations;
		this.rowMapperFactory = rowMapperFactory;
	}

	// Deprecated constructors
	
	@Deprecated
	public InOutCallTemplate(DataSource dataSource) {
		this(JdbcFlavorConfig.getJdbcFlavor(), dataSource);
	}
	
	@Deprecated
	public InOutCallTemplate(JdbcOperations jdbcOperations) {
		this(JdbcFlavorConfig.getJdbcFlavor(), jdbcOperations);
	}
	
	@Deprecated
	public InOutCallTemplate(DataSource dataSource, RowMapperFactory rowMapperFactory) {
		this(JdbcFlavorConfig.getJdbcFlavor(), dataSource, rowMapperFactory);
	}
	
	@Deprecated
	public InOutCallTemplate(JdbcOperations jdbcOperations, RowMapperFactory rowMapperFactory) {
		this(JdbcFlavorConfig.getJdbcFlavor(), jdbcOperations, rowMapperFactory);
	}

	
	/**
	 * @see #setTransactionOperations(TransactionOperations)
	 */
	public TransactionOperations getTransactionOperations() {
		return transactionOperations;
	}

	/**
	 * Injects a {@link TransactionOperations} instance so that all stored procedures/functions calls
	 * will be executed inside an explicit transaction. The default value of this member is <code>null</code>
	 * which means that the calls will be executed inside an implicit transaction.
	 * @param transactionOperations  the {@link TransactionOperations} instance.
	 */
	public void setTransactionOperations(TransactionOperations transactionOperations) {
		this.transactionOperations = transactionOperations;
	}

	
	public Logger getLogger() {
		return log;
	}

	public void setLogger(Logger log) {
		this.log = log;
	}
	
	private Object callInternal(String spName, CallableStatementCallback<?> action, int resultCount, Object ... inParams) throws DataAccessException {
		Assert.assertNotEmpty(spName, "spName");
		Assert.assertNotNull(action, "action");
		Assert.assertPositive(resultCount, "resultCount");
		if (inParams == null) {
			inParams = new Object[0];
		}
		CallableStatementCreator csc = jdbcFlavor.buildCallableStatementCreator(spName, resultCount, log, preprocessInParams(inParams));
		return jdbcOperations.execute(csc, action);
	}

	
	@Override
	public Object call(String spName, CallableStatementCallback<?> action, int resultCount, Object ... inParams) throws DataAccessException {
		if (transactionOperations == null) {
			return callInternal(spName, action, resultCount, inParams);
		} else {
			return transactionOperations.execute(
				status -> callInternal(spName, action, resultCount, inParams)
			);
		}
	}
	
	
	// ---------  Calls with RowMapper parameters ---------------- //	
	
	@SuppressWarnings("unchecked")
	@Override
	public List<List<?>> call(String spName, RowMapper<?>[] rowMappers, Object ... inParams) throws DataAccessException {
		if (rowMappers == null) {
			rowMappers = new RowMapper<?>[0];
		}
		return (List<List<?>>) call(spName, 
				jdbcFlavor.buildCallableStatementCallback(
						ResultSetUtils.toResultSetSqlParameters(rowMappers), 
						inParams), 
				rowMappers.length, 
				inParams);
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> call(String spName, RowMapper<T> rowMapper, Object ... inParams) throws DataAccessException {
		return (List<T>)call(spName, new RowMapper<?>[]{rowMapper}, inParams).get(0);
	}

	
	@Override
	public <T> T callForObject(String spName, RowMapper<T> rowMapper, Object ... inParams) throws DataAccessException {
		Assert.assertNotEmpty(spName, "spName");
		Assert.assertNotNull(rowMapper, "rowMapper");
		List<T> objects = call(spName, rowMapper, inParams);
		if (objects.size() == 0) {
			throw new EmptyResultDataAccessException(1);
		}
		if (objects.size() > 1) {
			throw new IncorrectResultSizeDataAccessException(1, objects.size());
		}
		return (T) objects.get(0);
	}
	
	
	// ---------  Calls with Class parameters ---------------- //
	
	
	@Override
	public List<List<?>> call(String spName, Class<?>[] classes, Object ... inParams) throws DataAccessException {
		RowMapper<?>[] rowMappers;		
		if (classes == null) {
			rowMappers = new RowMapper<?>[0];
		} else {
			rowMappers = new RowMapper<?>[classes.length];
			for (int i=0; i<classes.length; i++) {
				rowMappers[i] = rowMapperFactory.getInstance(classes[i]); 
			}
		}
		return call(spName, rowMappers, inParams);
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> call(String spName, Class<T> clasz, Object ... inParams) throws DataAccessException {
		return (List<T>) call(spName, new Class<?>[]{clasz}, inParams).get(0);
	}
	
	@Override
	public <T> T callForObject(String spName, Class<T> clasz, Object ... inParams) throws DataAccessException {
		Assert.assertNotEmpty(spName, "spName");
		Assert.assertNotNull(clasz, "clasz");
		List<T> objects = call(spName, clasz, inParams);
		if (objects.size() == 0) {
			throw new EmptyResultDataAccessException(1);
		}
		if (objects.size() > 1) {
			throw new IncorrectResultSizeDataAccessException(1, objects.size());
		}
		return (T) objects.get(0);
	}
	
	
	// -------------------------------------------------- //
	
	
	@Override
	public void callWithoutResult(String spName, Object ... inParams) throws DataAccessException {
		call(spName, new RowMapper<?>[0], inParams);
	}
	
	// -------------------------------------------------- //	
	
	
	@SuppressWarnings("unchecked")
	@Override
	public List<List<?>> call(String spName, ResultSetSqlParameter[] rsParams, Object ... inParams) throws DataAccessException {
		if (rsParams == null) {
			rsParams = new ResultSetSqlParameter[0];
		}
		return (List<List<?>>) call(spName, 
				jdbcFlavor.buildCallableStatementCallback(rsParams, inParams), 
				rsParams.length, 
				inParams);
	}

	@Override
	public void call(String spName, RowCallbackHandler handler, Object ... inParams) throws DataAccessException {
		call(spName, new ResultSetSqlParameter[]{new ResultSetSqlParameter(handler)}, inParams);
	}
	
	
	// -------------------------------------------------- //


	public JdbcOperations getJdbcOperations() {
		return jdbcOperations;
	}

	public RowMapperFactory getRowMapperFactory() {
		return rowMapperFactory;
	}
	
	
	// -------------------------------------------------- //
	
	
	/**
	 * Performs parameters preprocessing:<br>
	 * <li> {@code Double} instances are converted to {@code BigDecimal} if {@link JdbcFlavor#isWrapDoubleParameterWithBigDecimal()}
	 * 		returns {@code true}.
	 * <li> {@code Float} instances are converted to {@code BigDecimal} if {@link JdbcFlavor#isWrapFloatParameterWithBigDecimal()}
	 * 		returns {@code true}.
	 * 
	 * @param inParams the parameters as received from the client code.
	 * @return the final parameters array that will be passed to Spring JDBC. 
	 * 
	 * @see BooleanParameterConverter
	 */
	private Object[] preprocessInParams(Object ... inParams) {
		Object[] inParamsFinal;
		if (inParams == null) {
			inParamsFinal = new Object[0];
		} else {
			inParamsFinal = new Object[inParams.length];
		}
		for (int i=0; i < inParamsFinal.length; i++) {
			if (inParams[i] instanceof Double
						&& jdbcFlavor.isWrapDoubleParameterWithBigDecimal()) {
				inParamsFinal[i] = BigDecimal.valueOf((Double) inParams[i]);
			} else if (inParams[i] instanceof Float
					&& jdbcFlavor.isWrapFloatParameterWithBigDecimal()) {
				inParamsFinal[i] = BigDecimal.valueOf((Float) inParams[i]);
			} else {
				inParamsFinal[i] = inParams[i];
			}
		}
		return inParamsFinal;
	}

}
