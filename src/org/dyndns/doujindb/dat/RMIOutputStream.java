package org.dyndns.doujindb.dat;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIOutputStream extends Remote
{
	public void close() throws RemoteException;
	public void flush() throws RemoteException;
	public void write(byte[] b, int off, int len) throws RemoteException;
	public void write(byte[] b) throws RemoteException;
	public void write(int b) throws RemoteException;
}
