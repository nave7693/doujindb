package org.dyndns.doujindb.dat.impl;

import java.io.*;
import java.rmi.*;

import org.dyndns.doujindb.dat.rmi.RMIOutputStream;

/**  
* RemoteOutputStream.java - Remote OutputStream.
* @author  nozomu
* @version 1.0
*/
@SuppressWarnings("serial")
public class RemoteOutputStream extends OutputStream implements Serializable
{
	
	private RMIOutputStream remoteStream;

	public RemoteOutputStream(RMIOutputStream remoteStream) {
		this.remoteStream = remoteStream;
	}

	public void close() throws IOException {
		try {
			remoteStream.close();
		} catch (RemoteException e) {
			throw new IOException("RemoteException " + e);
		}
	}

	public void flush() throws IOException {
		try {
			remoteStream.flush();
		} catch (RemoteException e) {
			throw new IOException("RemoteException " + e);
		}
	}

	public void write(byte[] b, int off, int len) throws IOException {
		try {
			remoteStream.write(b, off, len);
		} catch (RemoteException e) {
			throw new IOException("RemoteException " + e);
		}
	}

	public void write(byte[] b) throws IOException {
		try {
			remoteStream.write(b);
		} catch (RemoteException e) {
			throw new IOException("RemoteException " + e);
		}
	}

	public void write(int b) throws IOException {
		try {
			remoteStream.write(b);
		} catch (RemoteException e) {
			throw new IOException("RemoteException " + e);
		}
	}
}

