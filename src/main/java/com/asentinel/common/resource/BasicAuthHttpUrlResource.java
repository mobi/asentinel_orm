package com.asentinel.common.resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.ResourceUtils;

import com.asentinel.common.util.Assert;

/**
 * Extension of {@link UrlResource} that adds support for Basic Authentication.
 * 
 * @see Resource
 * @see UrlResource
 * 
 * @author Razvan Popian
 */
public class BasicAuthHttpUrlResource extends UrlResource {
	private final String user;
	private final String password;
	
	/**
	 * Create a new {@code BasicAuthHttpUrlResource} based on the given URL object
	 * and using the {@code user} and {@code password} for creating the basic authentication 
	 * header.
	 * @param url a URL
	 * @param user the username.
	 * @param password the password.
	 */
	public BasicAuthHttpUrlResource(URL url, String user, String password) {
		super(url);
		Assert.assertNotEmpty(user, "user");
		Assert.assertNotEmpty(password, "password");
		this.user = user;
		this.password = password;
	}

	
	/**
	 * Create a new {@code UrlResource} based on a URL path.
	 * <p>Note: The given path needs to be pre-encoded if necessary.
	 * 
	 * @param path a URL path
	 * @param user the username.
	 * 
	 * @throws MalformedURLException if the given URL path is not valid.
	 */
	public BasicAuthHttpUrlResource(String path, String user, String password) throws MalformedURLException {
		super(path);
		Assert.assertNotEmpty(user, "user");
		Assert.assertNotEmpty(password, "password");
		this.user = user;
		this.password = password;
	}

	private void addAuthenticationHeader(URLConnection con) {
		String userpass = user + ":" + password;
		String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
		con.setRequestProperty ("Authorization", basicAuth);			
	}
	
	@Override
	protected void customizeConnection(HttpURLConnection con) throws IOException {
		super.customizeConnection(con);
		addAuthenticationHeader(con);
	}
	
	
	URLConnection openConnectionInternal() throws IOException {
		return super.getURL().openConnection();
	}

	
	/**
	 * @return an {@code InputStream} for this resource.
	 */
	@Override
	public InputStream getInputStream() throws IOException {
		URLConnection con = openConnectionInternal();
		addAuthenticationHeader(con);
		ResourceUtils.useCachesIfNecessary(con);
		try {
			return con.getInputStream();
		} catch (IOException ex) {
			// Close the HTTP connection (if applicable).
			if (con instanceof HttpURLConnection) {
				((HttpURLConnection) con).disconnect();
			}
			throw ex;
		}
	}
	
	
	
	public String getUser() {
		return user;
	}


	public String getPassword() {
		return password;
	}


	@Override
	public String toString() {
		var url = super.getURL().toString();		
		return "BasicAuthHttpUrlResource ["
				+ "URL=" + url
				+ ", user=" + user 
				+ "]";
	}
}
