package org.dyndns.doujindb.dat.impl;

import java.io.Serializable;

/**  
* StreamData.java - Used in RMI streams.
* @author  nozomu
* @version 1.0
*/
@SuppressWarnings("serial")
public final class StreamData implements Serializable
{
	private byte[] data;
	private int i;

	public StreamData(byte[] data, int i)
	{
		this.data = data;
		this.i = i;
	}

	public byte[] getData()
	{
		return data;
	}

	public int getResult()
	{
		return i;
	}
}
