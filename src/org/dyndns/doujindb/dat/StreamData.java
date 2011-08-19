package org.dyndns.doujindb.dat;

import java.io.Serializable;

@SuppressWarnings("serial")
public class StreamData implements Serializable
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
