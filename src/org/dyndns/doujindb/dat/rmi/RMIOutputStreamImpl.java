package org.dyndns.doujindb.dat.rmi;

import java.io.*;
import java.rmi.*;
import java.rmi.server.*;

@SuppressWarnings("serial")
public class RMIOutputStreamImpl extends UnicastRemoteObject implements RMIOutputStream
{
	private OutputStream out;

	public RMIOutputStreamImpl(OutputStream out) throws RemoteException
	{
		super(1099);
		this.out = out;
	}

	public void close() throws RemoteException {
		try {
			out.close();
		} catch (IOException e) {
			throw new RemoteException("IOError on the Server", e);
		}
	}

	public void flush() throws RemoteException {
		try {
			out.flush();
		} catch (IOException e) {
			throw new RemoteException("IOError on the Server", e);
		}
	}

	public void write(byte[] b, int off, int len) throws RemoteException {
		try {
			out.write(b, off, len);
		} catch (IOException e) {
			throw new RemoteException("IOError on the Server", e);
		}
	}

	public void write(byte[] b) throws RemoteException {
		try {
			out.write(b);
		} catch (IOException e) {
			throw new RemoteException("IOError on the Server", e);
		}
	}

	public void write(int b) throws RemoteException {
		try {
			out.write(b);
		} catch (IOException e) {
			throw new RemoteException("IOError on the Server", e);
		}
	}
}
