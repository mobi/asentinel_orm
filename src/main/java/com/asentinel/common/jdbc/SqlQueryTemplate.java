package com.asentinel.common.jdbc;

import static com.asentinel.common.util.Assert.assertNotNull;

import java.math.BigDecimal;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.KeyHolder;

import com.asentinel.common.jdbc.flavors.JdbcFlavor;
import com.asentinel.common.jdbc.flavors.JdbcFlavorConfig;
import com.asentinel.common.util.Assert;
import com.asentinel.common.util.Utils;

/**
 * Implementation for the {@link SqlQuery} interface. 
 * This should be used as an effectively immutable object. It should be configured,
 * published and after publishing no state changes should be allowed.
 * 
 * @see SqlQuery
 * 
 * @author Razvan Popian
 */
public class SqlQueryTemplate implements SqlQuery {
	private final static Logger DEFAULT_LOG = LoggerFactory.getLogger(SqlQueryTemplate.class);

	private final JdbcFlavor jdbcFlavor;
	private final JdbcOperations jdbcOperations;
	private final RowMapperFactory rowMapperFactory;
	private final BooleanParameterConverter<?> booleanParameterConverter;
	
	private Logger log = DEFAULT_LOG;
	
	
	// DataSource constructors
	
	public SqlQueryTemplate(JdbcFlavor jdbcFlavor, DataSource dataSource) {
		this(jdbcFlavor, dataSource, new DefaultRowMapperFactory());
	}
	
	public SqlQueryTemplate(JdbcFlavor jdbcFlavor, 
				DataSource dataSource, 
				RowMapperFactory rowMapperFactory) {
		this(jdbcFlavor, dataSource, rowMapperFactory, new DefaultBooleanParameterConverter());
	}

	
	public SqlQueryTemplate(JdbcFlavor jdbcFlavor, 
			DataSource dataSource, 
			RowMapperFactory rowMapperFactory,
			BooleanParameterConverter<?> booleanParameterConverter) {
		this(jdbcFlavor, new JdbcTemplate(dataSource), rowMapperFactory, booleanParameterConverter);
	}
	
	// JdbcOperations constructors

	public SqlQueryTemplate(JdbcFlavor jdbcFlavor, JdbcOperations jdbcOperations) {
		this(jdbcFlavor, jdbcOperations, new DefaultRowMapperFactory());
	}
	
	public SqlQueryTemplate(JdbcFlavor jdbcFlavor,
				JdbcOperations jdbcOperations, 
				RowMapperFactory rowMapperFactory) {
		this(jdbcFlavor, jdbcOperations, rowMapperFactory, new DefaultBooleanParameterConverter());
	}
	
	public SqlQueryTemplate(JdbcFlavor jdbcFlavor, 
						JdbcOperations jdbcOperations, 
						RowMapperFactory rowMapperFactory,
						BooleanParameterConverter<?> booleanParameterConverter) {
		assertNotNull(jdbcFlavor, "jdbcFlavor");
		assertNotNull(jdbcOperations, "jdbcOperations");
		assertNotNull(rowMapperFactory, "rowMapperFactory");
		assertNotNull(booleanParameterConverter, "booleanParameterConverter");
		this.jdbcFlavor = jdbcFlavor;
		this.jdbcOperations = jdbcOperations;
		this.rowMapperFactory = rowMapperFactory;
		this.booleanParameterConverter = booleanParameterConverter;
	}
	
	// Deprecated DataSource constructors

	@Deprecated
	public SqlQueryTemplate(DataSource dataSource) {
		this(JdbcFlavorConfig.getJdbcFlavor(), dataSource);
	}
	
	@Deprecated
	public SqlQueryTemplate(DataSource dataSource, RowMapperFactory rowMapperFactory) {
		this(JdbcFlavorConfig.getJdbcFlavor(), dataSource, rowMapperFactory);
	}

	@Deprecated
	public SqlQueryTemplate(DataSource dataSource, RowMapperFactory rowMapperFactory,
			BooleanParameterConverter<?> booleanParameterConverter) {
		this(JdbcFlavorConfig.getJdbcFlavor(), dataSource, rowMapperFactory, booleanParameterConverter); 
	}
	
	// Deprecated JdbcOperations constructors

	@Deprecated
	public SqlQueryTemplate(JdbcOperations jdbcOperations) {
		this(JdbcFlavorConfig.getJdbcFlavor(), jdbcOperations);
	}
	
	@Deprecated
	public SqlQueryTemplate(JdbcOperations jdbcOperations, RowMapperFactory rowMapperFactory) {
		this(JdbcFlavorConfig.getJdbcFlavor(), jdbcOperations, rowMapperFactory);
	}
	
	@Deprecated
	public SqlQueryTemplate(JdbcOperations jdbcOperations, RowMapperFactory rowMapperFactory,
			BooleanParameterConverter<?> booleanParameterConverter) {
		this(JdbcFlavorConfig.getJdbcFlavor(), jdbcOperations, rowMapperFactory, booleanParameterConverter); 
	}

	public Logger getLogger() {
		return log;
	}

	public void setLogger(Logger log) {
		Assert.assertNotNull(log, "log");
		this.log = log;
	}
	
	private void logBefore(String sql, final Object rsProcessor, Object ... inParams) {
		if (log.isDebugEnabled()) {
        	Object userId = -1;
        	String userName = "UNKNOWN";
        	SimpleUser user = ThreadLocalUser.getThreadLocalUser().get();
        	if (user != null) {
    			userId = user.getUserId();
    			userName = user.getUsername();
        	}
        	if (user != null) {
        		log.debug("query - user: " + userName + " (" + userId + "); sql: " + sql);
        	} else {        	
        		log.debug("query - sql: " + sql);
        	}
			if (inParams.length > 0) {
				log.debug("query - with parameters: " +  JdbcUtils.parametersToString(true, inParams));
			}
		}
		if (log.isTraceEnabled()) {
			if (rsProcessor instanceof RowMapper) {
				log.trace("query - mapper: " + rsProcessor);
			} else if (rsProcessor instanceof RowCallbackHandler) {
				log.trace("query - handler: " + rsProcessor);
			} else {
				log.trace("query - Unknown resultset processor: " + rsProcessor);
			}
		}
	}
	
	private void logAfter(int size, long t0, long t1) {
		if (log.isTraceEnabled()) {
			int fetchSize = -1;
			if (this.jdbcOperations instanceof JdbcTemplate) {
				fetchSize = ((JdbcTemplate) this.jdbcOperations).getFetchSize();
			}
			String logStr = "query - Resultset size: " + size + ", query executed in " + ((t1 - t0)/1000000) + " ms";
			if (fetchSize > 0) {
				logStr += " / fetch: " + fetchSize;
			} else {
				logStr += " / fetch: default";
			}
			log.trace(logStr);
		}
	}
	
	
	@Override
	public <T> List<T> query(String sql, final RowMapper<T> mapper, Object ... inParams) throws DataAccessException {
		Assert.assertNotNull(sql, "sql");
		Assert.assertNotNull(mapper, "mapper");
		inParams = preprocessInParams(inParams);
		logBefore(sql, mapper, inParams);
		long t0 = System.nanoTime();
		List<T> list = jdbcOperations.query(sql, mapper, inParams);
		long t1 = System.nanoTime();
		logAfter(list.size(), t0, t1);
		return list;
	}
	
	@Override
	public void query(String sql, RowCallbackHandler handler, Object ... inParams) throws DataAccessException {
		Assert.assertNotNull(sql, "sql");
		Assert.assertNotNull(handler, "handler");
		inParams = preprocessInParams(inParams);
		logBefore(sql, handler, inParams);
		long t0 = System.nanoTime();
		RowCallbackHandlerDecorator handlerDecorator = new RowCallbackHandlerDecorator(handler);
		jdbcOperations.query(sql, inParams, handlerDecorator);
		long t1 = System.nanoTime();
		logAfter(handlerDecorator.size(), t0, t1);
	}
	
	@Override
	public <T> List<T> query(String sql, Class<T> clasz, Object ... inParams) throws DataAccessException {
		return query(sql, rowMapperFactory.getInstance(clasz), inParams);
	}
	
	@Override
	public <T> T queryForObject(String sql, RowMapper<T> mapper, Object ... inParams) 
		throws EmptyResultDataAccessException, DataAccessException {
		List<T> objects = query(sql, mapper, inParams);
		if (objects.isEmpty()) {
			throw new EmptyResultDataAccessException(1);
		}
		if (objects.size() > 1) {
			throw new IncorrectResultSizeDataAccessException(1, objects.size());
		}
		return objects.get(0);
	}
	
	@Override
	public <T> T queryForObject(String sql, Class<T> clasz, Object ... inParams) 
		throws EmptyResultDataAccessException, DataAccessException {
		return (T) queryForObject(sql, rowMapperFactory.getInstance(clasz), inParams);
	}
	
	@Override
	public int queryForInt(String sql, Object ... inParams) 
		throws EmptyResultDataAccessException, DataAccessException {
		Integer i = queryForObject(sql, Integer.class, inParams);
		return (i == null) ? 0 : i;
	}

	@Override
	public long queryForLong(String sql, Object ... inParams) 
			throws EmptyResultDataAccessException, DataAccessException {
		Long l = queryForObject(sql, Long.class, inParams);
		return (l == null) ? 0 : l;
	}
	
	
	@Override
	public String queryForString(String sql, Object ... inParams) 
		throws EmptyResultDataAccessException, DataAccessException {	
		String s = queryForObject(sql, String.class, inParams);
		return (s == null) ? "" : s;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Object> queryForMap(String sql, Object ... inParams)
			throws EmptyResultDataAccessException, DataAccessException {
		return queryForObject(sql, Map.class, inParams);
	}
	
	@Override
	public int update(String sql, Object ... inParamsUpdate) throws DataAccessException {
		return update(sql, null, null, inParamsUpdate);
	}
	
	@Override
	public int update(String sql, String[] keyColumnNames, KeyHolder keyHolder, Object ... inParamsUpdate) throws DataAccessException {
		Assert.assertNotNull(sql, "sql");
		final Object[] inParams;
		if (inParamsUpdate == null) {
			inParams = new Object[0];
		} else {
			inParams = preprocessInParams(inParamsUpdate);
		}
		if (log.isDebugEnabled()) {
        	Object userId = -1;
        	String userName = "UNKNOWN";
        	SimpleUser user = ThreadLocalUser.getThreadLocalUser().get();
        	if (user != null) {
    			userId = user.getUserId();
    			userName = user.getUsername();
        	}
        	if (user != null) {
        		log.debug("update - user: {} ({}); sql: {}", userName, userId, sql);
        	} else {        	
        		log.debug("update - sql: {}", sql);
        	}
			if (inParams.length > 0) {
				log.debug("update - with parameters: {}", JdbcUtils.parametersToString(true, inParams));
			}
		}
		
		long t0 = System.nanoTime();
		int count;
		if (isExpectKeys(keyColumnNames, keyHolder)) {
			count = jdbcOperations.update(
					new CustomPreparedStatementCreator(jdbcFlavor,
							sql, jdbcFlavor.preprocessKeyColumnNames(keyColumnNames), 
							inParams), 
					keyHolder
			); 
		} else {
			count = jdbcOperations.update(
					new CustomPreparedStatementCreator(jdbcFlavor, sql, inParams)
			); 
		}
		long t1 = System.nanoTime();
		if (log.isTraceEnabled()){
			log.trace("update - Affected rows: {}, update executed in {} ms", count, ((t1 - t0)/1000000));
		}
		return count;
	}
	
	private static boolean isExpectKeys(String[] keyColumnNames, KeyHolder keyHolder) {
		return !(keyColumnNames == null
				|| keyColumnNames.length == 0 
				|| keyHolder == null);
	}
	
	@Override
	public JdbcOperations getJdbcOperations() {
		return jdbcOperations;
	}

	public RowMapperFactory getRowMapperFactory() {
		return rowMapperFactory;
	}
	
	public BooleanParameterConverter<?> getBooleanParameterConverter() {
		return booleanParameterConverter;
	}

	/**
	 * Performs parameter preprocessing:<br>
	 * <li> {@code Boolean} instances are converted using {@link BooleanParameterConverter#asObject(boolean)};
	 * <li> {@code Temporal} instances are converted to {@code Date} instances;
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
			if (inParams[i] instanceof Boolean) {
				inParamsFinal[i] = booleanParameterConverter.asObject((Boolean) inParams[i]);
			} else if (inParams[i] instanceof Temporal) {
				inParamsFinal[i] = Utils.toDate((Temporal) inParams[i]);
			} else if (inParams[i] instanceof Double
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
