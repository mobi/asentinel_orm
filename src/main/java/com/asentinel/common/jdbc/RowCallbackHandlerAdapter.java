package com.asentinel.common.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.lob.LobHandler;

import com.asentinel.common.util.Assert;

/**
 * Adapter class that adapts a {@link RowMapper} instance to 
 * a {@link RowCallbackHandler} instance. It uses the mapper to create an
 * object for each row of the resultset and then calls the {@link #processRow(Object, int)}
 * method for the object.
 * <br><br>
 * 
 * This class could be useful if you don't want to store the objects resulted from the 
 * resultset in a list, but in a map for example.  Here is the code:
 * <br><br>
 * <pre>
 *   final Map&lt;Integer, MyObject&gt; map =  ....;
 *   InOutCall spCall = ....;
 *   MyObjectRowMapper mapper = new MyObjectRowMapper();
 *   spCall.call("test", new RowCallbackHandlerAdapter&lt;MyObject&gt;(mapper) {
 *   
 *   	protected void processRow(MyObject object, int rowNum) {
 *   		map.put(object.getId(), object);
 *   	}
 *   });
 *</pre>
 *
 * Subclasses are not reusable and not thread safe. It is common for subclasses
 * to add additional state to this class.
 * <br><br> 
 * 
 * @see RowMapper
 * @see RowCallbackHandler
 * 
 * @author Razvan Popian
 */
public abstract class RowCallbackHandlerAdapter<T> implements RowCallbackHandler {

	private final RowMapper<T> rowMapper;
	
	/**
	 * Constructor. Direct init of the RowMapper instance member.
	 * @param rowMapper
	 */
	public RowCallbackHandlerAdapter(RowMapper<T> rowMapper) {
		Assert.assertNotNull(rowMapper, "rowMapper");
		this.rowMapper = rowMapper;
	}
	
	/**
	 * Constructor that initializes the RowMapper instance member
	 * using the {@link RowMapperFactory} parameter. 
	 * @param clasz class for which to create a mapper.
	 * @param rowMapperFactory
	 * 
	 * @see RowMapperFactory
	 */
	public RowCallbackHandlerAdapter(Class<T> clasz, RowMapperFactory rowMapperFactory){
		Assert.assertNotNull(clasz, "clasz");
		Assert.assertNotNull(rowMapperFactory, "rowMapperFactory");
		rowMapper = rowMapperFactory.getInstance(clasz);
	}
	
	/**
	 * Constructor that initializes the RowMapper instance member
	 * using the {@link ResultSetUtils#rowMapperForClass(Class, LobHandler)} method. 
	 * @param clasz class for which to create a mapper.
	 * @param lobHandler the {@code LobHandler} to use.
	 */
	public RowCallbackHandlerAdapter(Class<T> clasz, LobHandler lobHandler){
		Assert.assertNotNull(clasz, "clasz");
		rowMapper = ResultSetUtils.rowMapperForClass(clasz, lobHandler);
	}
	

	@Override
	public final void processRow(ResultSet rs) throws SQLException {
		T object = rowMapper.mapRow(rs, rs.getRow());
		processRow(object, rs.getRow());
	}
	
	/**
	 * Abstract method where subclasses will do their custom processing.
	 * @param object 
	 * 			the object created by the RowMapper member for the current row.
	 * @param rowNum 
	 * 			current row number, 1 based.
	 */
	protected abstract void processRow(T object, int rowNum);
	
	
	@Override
	public String toString() {
		return "RowCallbackHandlerAdapter [rowMapper=" + rowMapper + "]";
	}


}
