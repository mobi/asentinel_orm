package com.asentinel.common.orm.proxy.collection;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.asentinel.common.orm.EntityUtils;

public class CollectionProxySerializationTestCase {

	@Test
	public void serializationOfLoadedProxyAndDeserialization() throws IOException, ClassNotFoundException {
		@SuppressWarnings("unchecked")
		List<Integer> list = CollectionProxyFactory.getInstance().newProxy(ArrayList.class, id -> {
			return Arrays.asList(1, 2, 3);
		}, 17);
		
		// force load proxy, if not loaded the state of the object will not be the expected one
		// but serialization would still work
		list.size();
		assertTrue(EntityUtils.isLoadedProxy(list));
		
		// write
		byte[] bytes;
		try (
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			ObjectOutputStream objectOut = new ObjectOutputStream(byteOut);
		) {
			objectOut.writeObject(list);
			bytes = byteOut.toByteArray();
		}
		
		// the proxy class is already loaded, on a different jvm the deserialization
		// code should first load the proxy class by calling CollectionProxyFactory.getInstance().getProxyObjectFactory(ArrayList.class);
		
		// read
		try (
			ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
			ObjectInputStream objectIn = new ObjectInputStream(byteIn);
		) {
			@SuppressWarnings("unchecked")
			List<Integer> list2 = (List<Integer>) objectIn.readObject();
			assertEquals(list, list2);
			assertTrue(EntityUtils.isLoadedProxy(list2));
		}
	}
	
	@Test
	public void serializationOfNotLoadedProxyAndDeserialization() throws IOException, ClassNotFoundException {
		@SuppressWarnings("unchecked")
		List<Integer> list = CollectionProxyFactory.getInstance().newProxy(ArrayList.class, id -> {
			return Arrays.asList(1, 2, 3);
		}, 17);
		
		assertFalse(EntityUtils.isLoadedProxy(list));
		
		// write
		byte[] bytes;
		try (
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			ObjectOutputStream objectOut = new ObjectOutputStream(byteOut);
		) {
			objectOut.writeObject(list);
			bytes = byteOut.toByteArray();
		}
		
		// the proxy class is already loaded, on a different jvm the deserialization
		// code should first load the proxy class by calling CollectionProxyFactory.getInstance().getProxyObjectFactory(ArrayList.class);
		// if the proxy is not loaded like it is the case here the deserialization code should set a proper loader function and the parent id
		// in the deserialized object using reflection. This is why it is recommended to force load any proxy before serialization

		// read
		try (
			ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
			ObjectInputStream objectIn = new ObjectInputStream(byteIn);
		) {
			@SuppressWarnings("unchecked")
			List<Integer> list2 = (List<Integer>) objectIn.readObject();
			assertEquals(0, list2.size());
			assertTrue(EntityUtils.isLoadedProxy(list2)); // false positive 
		}
	}
}
