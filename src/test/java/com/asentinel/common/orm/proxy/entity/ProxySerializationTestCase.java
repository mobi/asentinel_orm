package com.asentinel.common.orm.proxy.entity;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.junit.Test;

import com.asentinel.common.orm.EntityUtils;
import com.asentinel.common.orm.mappers.PkColumn;
import com.asentinel.common.orm.mappers.Table;

public class ProxySerializationTestCase {
	
	@Test
	public void serializationOfLoadedProxyAndDeserialization() throws IOException, ClassNotFoundException {
		Target t = ProxyFactory.getInstance().newProxy(Target.class, id -> {
			return new Target(1, "Alfa");
		});
		
		// force load proxy, if not loaded the state of the object will not be the expected one
		// but serialization would still work
		t.getName();
		assertTrue(EntityUtils.isLoadedProxy(t));
		
		// write
		byte[] bytes;
		try (
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			ObjectOutputStream objectOut = new ObjectOutputStream(byteOut);
		) {
			objectOut.writeObject(t);
			bytes = byteOut.toByteArray();
		}
		
		// the proxy class is already loaded, on a different jvm the deserialization
		// code should first load the proxy class by calling ProxyFactory.getInstance().getProxyObjectFactory(Target.class);
		
		// read
		try (
			ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
			ObjectInputStream objectIn = new ObjectInputStream(byteIn);
		) {
			Target t2 = (Target) objectIn.readObject();
			assertEquals(t.getId(), t2.getId());
			assertEquals(t.getName(), t2.getName());
			assertTrue(EntityUtils.isLoadedProxy(t));
		}
		
	}
	
	@Test
	public void serializationOfNotLoadedProxyAndDeserialization() throws IOException, ClassNotFoundException {
		Target t = ProxyFactory.getInstance().newProxy(Target.class, id -> {
			return new Target(10, "Beta");
		});
		t.setId(10);
		assertFalse(EntityUtils.isLoadedProxy(t));
		
		// write
		byte[] bytes;
		try (
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			ObjectOutputStream objectOut = new ObjectOutputStream(byteOut);
		) {
			objectOut.writeObject(t);
			bytes = byteOut.toByteArray();
		}
		
		// the proxy class is already loaded, on a different jvm the deserialization
		// code should first load the proxy class by calling ProxyFactory.getInstance().getProxyObjectFactory(Target.class)
		// if the proxy is not loaded like it is the case here the deserialization code should set a proper loader function
		// in the deserialized object using reflection. This is why it is recommended to force load any proxy before serialization
		
		// read
		try (
			ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
			ObjectInputStream objectIn = new ObjectInputStream(byteIn);
		) {
			Target t2 = (Target) objectIn.readObject();
			assertEquals(10, t2.getId());
			assertNull(t2.getName());
			assertTrue(EntityUtils.isLoadedProxy(t2)); // the loader member is transient, false positive
		}
		
	}

	// FYI: The following classes need to be public for ByteBuddy 1.9.2 if the class loading strategy is not ClassLoadingStrategy.Default.INJECTION. 
	// For 1.5.10 they can be declared as package private (this is ideal, because I don't want them to be visible outside the package). 
	// I opened a stackoverflow question on this issue: 
	// https://stackoverflow.com/questions/52829988/behavior-change-on-bytebuddy-version-1-9-1-compared-to-1-5-10

	@Table("target")
	public static class Target implements Serializable {
		
		private static final long serialVersionUID = 1L;

		@PkColumn("id")
		private int id;
		
		private String name;
		
		public Target() {
			
		}
		
		public Target(int id, String name) {
			this.id = id;
			this.name = name;
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return "Target [id=" + id + ", name=" + name + "]";
		}
		
		
	}
}

