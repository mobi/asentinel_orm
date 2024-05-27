package com.asentinel.common.orm.proxy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.util.StreamUtils;

import com.asentinel.common.jdbc.InputStreamRowMapper;
import com.asentinel.common.jdbc.SqlQuery;
import com.asentinel.common.orm.EntityUtils;
import com.asentinel.common.orm.TargetMembers;
import com.asentinel.common.orm.TargetMembersHolder;
import com.asentinel.common.util.Assert;

/**
 * Proxy that defers the loading of database blobs to the time when the bytes
 * are actually requested.
 * 
 * @since 1.61.0
 * @author Razvan Popian
 */
public class InputStreamProxy extends InputStream {
	
	private static final String SQL_TEMPLATE = "select %s from %s where %s = ?";
	
	private final SqlQuery queryEx;
	private final LobHandler lobHandler;
	private final String sql;
	private final Object parentObject;
	
	private InputStream blob;
	
	public InputStreamProxy(SqlQuery queryEx, LobHandler lobHandler, Object parentObject, String column) {
		Assert.assertNotNull(queryEx, "queryEx");
		Assert.assertNotNull(lobHandler, "lobHandler");
		Assert.assertNotEmpty(column, "column");
		Assert.assertNotNull(parentObject, "parentObject");
		this.queryEx = queryEx;
		this.lobHandler = lobHandler;
		this.parentObject = parentObject;
		TargetMembers tm = TargetMembersHolder.getInstance().getTargetMembers(parentObject.getClass());
		String table = tm.getUpdatableTable();
		String pkColumn = tm.getPkColumnMember().getPkColumnAnnotation().value(); 
		sql = String.format(SQL_TEMPLATE, column, table, pkColumn);
	}


	@Override
	public int read() throws IOException {
		/*
		 * synchronized just in case the initialization happens on another thread than the 
		 * one that created the proxy. it ensures only one thread inits the proxy
		 */
		synchronized(this) {
			if (blob == null) {
				try (InputStream in = queryEx.queryForObject(sql, new InputStreamRowMapper(lobHandler), getParentObjectId())) {
					byte[] bytes = StreamUtils.copyToByteArray(in);
					blob = new ByteArrayInputStream(bytes);
				}
			}
		}
		
		return blob.read();
	}
	
	protected Object getParentObjectId() {
		return EntityUtils.getEntityId(parentObject);
	}
	
	public synchronized boolean isLoaded() {
		return blob != null;
	}

}
