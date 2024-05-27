package com.asentinel.common.resource;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.Cleaner;
import java.lang.ref.Cleaner.Cleanable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.asentinel.common.util.CleanerHolder;

/**
 * {@link TempResource} implementation that stores the data in a file created in
 * the system temporary directory or in a file provided by the client. After the
 * temporary resource is not needed anymore the user should call the
 * {@link #cleanup()} method to delete the file (this should usually happen in a
 * finally block). try-with-resources is also supported.<br>
 * Instances of this class can be created in thread A and read/cleaned up in
 * thread B, because this class is designed to ensure visibility of all the
 * mutable members. <br>
 * 
 * After {@link #cleanup()} is called the temporary resource instance should be
 * discarded.
 *
 * @see File#createTempFile(String, String)
 * @see CleanerHolder
 * @see Cleaner
 * 
 * @author Razvan Popian
 */
public class FileTempResource extends FileSystemResource implements TempResource {
	private static final Logger log = LoggerFactory.getLogger(FileTempResource.class);
	
	private static final String PREFIX = "tims_";
	private static final String SUFIX = ".tmp";
	
	private static final Cleaner cleaner = CleanerHolder.CLEANER;

	private static final AtomicLong fileId = new AtomicLong();
			
	private final State state;
	private final Cleanable cleanable;
	
	/**
	 * Constructor that creates a new temporary resource for
	 * which the user is required to call {@link #cleanup()} after the
	 * resource is no longer needed. The file this temporary resource is 
	 * associated with will be created in the system temporary directory.
	 * 
	 * @see File#createTempFile(String, String)
	 */
	public FileTempResource() {
		this(createTempFile());
	}

	/**
	 * Constructor that takes a {@link File} object as parameter. The file
	 * must exist on disk, otherwise this constructor 
	 * will throw an <code>IllegalArgumentException</code>. The user is required to call 
	 * {@link #cleanup()} after the resource is no longer needed.
	 *  
	 * @param file the file to associate this temporary resource with.
	 * @throws IllegalArgumentException if the file does not exist or is a directory.
	 */
	public FileTempResource(File file) {
		super(file);
		if (!file.exists()) {
			throw new IllegalArgumentException("File " + file + " does not exist.");
		}
		if (file.isDirectory()) {
			throw new IllegalArgumentException("File " + file + " is a directory.");
		}
		this.state = new State(file);
		this.cleanable = cleaner.register(this, state);
	}
	
	/**
	 * Constructor that takes the path to an existing file as parameter. The file denoted by
	 * the <code>path</code> parameter  must exist on disk, 
	 * otherwise this constructor will throw an <code>IllegalArgumentException</code>. The user is 
	 * required to call {@link #cleanup()} after the resource is no longer needed.
	 * 
	 * @param path the file path to associate this temporary resource with.
	 * @throws IllegalArgumentException if the file denoted by the path parameter 
	 * does not exist or is a directory.
	 */
	public FileTempResource(String path) {
		this(new File(path));
	}
	
	
	private static long nextFileId() {
		return fileId.incrementAndGet();
	}
	
	private static File createTempFile() {
		try {
			File f = File.createTempFile(PREFIX + nextFileId() + "_", SUFIX);
			f.deleteOnExit();
			return f;
		} catch (IOException e) {
			throw new RuntimeException("Failed to create temporary resource.", e);
		}
	}
	
	boolean isCleaned() {
		return !state.isOpen();
	}

	@Override
	public boolean isOpen() {
		// return false because we need this to work with MimeMessageHelper
		return false;
	}

	@Override
	public String getDescription() {
		return "FileTempResource [" + getFile().getAbsolutePath() + "]";
	}
	
	
	@Override
	public InputStream getInputStream() throws IOException {
		if (!isReadable()) {
			throw new IOException("Temporary resource " + getDescription() + " is not readable. It was probably already deleted.");
		}
		InputStream in = super.getInputStream();
		state.addCloseable(in);
		return in;
	}


	@Override
	public OutputStream getOutputStream() throws IOException {
		if (!isWritable()) {
			throw new IOException("Temporary resource " + getDescription() + " is not readable. It was probably already deleted.");
		}
		OutputStream out = super.getOutputStream();
		state.addCloseable(out);
		return out;
	}
	
	@Override
	public Resource createRelative(String relativePath) {
		throw new UnsupportedOperationException("This is a temporary file. Can not create anything relative to it.");
	}

	
	/**
	 * Deletes the temporary resource and closes any streams associated with it.
	 * 
	 * @see TempResource#cleanup()
	 */
	@Override
	public void cleanup() {
		cleanable.clean();
	}

	
	private static class State implements Runnable {
		
		private final List<Closeable> streams = new ArrayList<>();
		private final File file;
		
		private boolean open = true;

		State(File file) {
			this.file = file;
		}

		@Override
		public void run() {
			cleanup();
		}
		
		private synchronized void cleanup() {
			// make sure all streams associated with this resource are closed
			for (Closeable stream:streams) {
				try {
					stream.close();
				} catch (IOException e) {
					log.debug("cleanup - Failed to close stream for temporary resource file " + file + ". Maybe it is already closed ?");
				}
			}
			streams.clear();
			
			if (file.exists()) {
				// delete the file
				if (!file.delete()) {
					log.warn("cleanup - Failed to delete temporary resource file " + file + ".");
				} else {
					open = false;
					log.debug("cleanup - Deleted temporary resource file " + file);
				}
			} else {
				open = false;
				log.debug("cleanup - Temporary resource file " + file + " does not exist.");
			}
		}

		synchronized boolean isOpen() {
			return open;
		}

		synchronized void addCloseable(Closeable closeable) throws IOException {
			if (!open) {
				try {
					closeable.close();
				} catch (IOException e) {
					log.debug("addCloseable - Failed to close stream for temporary resource file " + file + ".");
				}
				throw new IOException("Temporary resource is closed (" + file + ").");
			}
			streams.add(closeable);
		}

	}

}
