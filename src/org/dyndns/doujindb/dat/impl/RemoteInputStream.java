package org.dyndns.doujindb.dat.impl;

import java.io.*;
import java.rmi.*;

import org.dyndns.doujindb.dat.rmi.RMIInputStream;

/**  
* RemoteInputStream.java - Remote InputStream.
* @author  nozomu
* @version 1.0
*/
@SuppressWarnings("serial")
public class RemoteInputStream extends InputStream implements Serializable
{
	private RMIInputStream remoteStream;

	public RemoteInputStream(RMIInputStream remoteStream) {
		this.remoteStream = remoteStream;
	}

	public int available() throws IOException {
		try {
			return remoteStream.available();
		} catch (RemoteException ex) {
			throw new IOException("RemoteException " + ex);
		}
	}

	public void close() throws IOException {
		try {
			remoteStream.close();
		} catch (RemoteException ex) {
			throw new IOException("RemoteException " + ex);
		}
	}

	public void mark(int readlimit) {
		try {
			remoteStream.mark(readlimit);
		} catch (RemoteException ex) {
			System.err.println("mark caused RemoteException " + ex);
		}
	}

	public boolean markSupported() {
		try {
			return remoteStream.markSupported();
		} catch (RemoteException ex) {
			System.err.println("markSupported caused RemoteException " + ex);
			return false;
		}
	}

	public int read() throws IOException {
		try {
			return remoteStream.read();
		} catch (RemoteException ex) {
			throw new IOException("RemoteException " + ex);
		}
	}

	public int read(byte[] b) throws IOException {
		try {
			return read(b, 0, b.length);
		} catch (RemoteException ex) {
			throw new IOException("RemoteException " + ex);
		}
	}

	public int read(byte[] b, int off, int len) throws IOException {
		try {
			byte[] holder;
			int result;
			StreamData data = remoteStream.read(off, len);
			holder = data.getData();
			result = data.getResult();
			System.arraycopy(holder, off, b, off, len - off);
			return result;
		} catch (RemoteException ex) {
			throw new IOException("RemoteException " + ex);
		}
	}

	public void reset() throws IOException {
		try {
			remoteStream.reset();
		} catch (RemoteException ex) {
			throw new IOException("RemoteException " + ex);
		}
	}

	public long skip(long n) throws IOException {
		try {
			return remoteStream.skip(n);		
		} catch (RemoteException ex) {
			throw new IOException("RemoteException " + ex);
		}
	}
}
