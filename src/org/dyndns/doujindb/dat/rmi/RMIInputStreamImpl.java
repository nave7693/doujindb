package org.dyndns.doujindb.dat.rmi;

import java.io.*;
import java.rmi.*;
import java.rmi.server.*;

import org.dyndns.doujindb.dat.impl.StreamData;

@SuppressWarnings("serial")
public class RMIInputStreamImpl extends UnicastRemoteObject implements RMIInputStream
{
	private InputStream in;

	public RMIInputStreamImpl(InputStream in) throws RemoteException
	{
		this.in = in;
	}

	public int available() throws RemoteException {
		try {
			return in.available();
		} catch (IOException ex) {
			throw new RemoteException("IOException on the server", ex);
		}
	}

	public void close() throws RemoteException {
		try {
			in.close();
		} catch (IOException ex) {
			throw new RemoteException("IOException on the server", ex);
		}
	}

	public void mark(int readlimit) throws RemoteException {
		in.mark(readlimit);
	}

	public boolean markSupported() throws RemoteException {
		return in.markSupported();
	}

	public int read() throws RemoteException {
		try {
			return in.read();
		} catch (IOException ex) {
			ex.printStackTrace();
			throw new RemoteException("IOException on the server", ex);
		}
	}

	public StreamData read(int off, int len) throws RemoteException {
		try {
			byte[] holder = new byte[len + 1];
			int i = in.read(holder, off, len);
			return new StreamData(holder, i);
		} catch (IOException ex) {
			ex.printStackTrace();
			System.exit(0);
			throw new RemoteException("IOException on the server", ex);
		}
	}

	public void reset() throws RemoteException {
		try {
			in.reset();
		} catch (IOException ex) {
			ex.printStackTrace();
			throw new RemoteException("IOException on the server", ex);
		}
	}

	public long skip(long n) throws RemoteException {
		try {
			return in.skip(n);
		} catch (IOException ex) {
			ex.printStackTrace();
			throw new RemoteException("IOException on the server", ex);
		}
	}
}
