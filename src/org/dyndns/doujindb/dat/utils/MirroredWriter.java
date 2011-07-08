package org.dyndns.doujindb.dat.utils;

import java.io.*;
import java.util.Set;

/**  
* MirroredWriter.java - Mirror every write operation on multiple writers
* @author  nozomu
* @version 1.0
*/
public final class MirroredWriter extends Writer
{
	private Set<Writer> wrs;
	
	public MirroredWriter(Iterable<Writer> writers)
	{
		for(Writer wr : writers)
			wrs.add(wr);
	}
	
	public MirroredWriter(Set<Writer> writers)
	{
		for(Writer wr : writers)
			wrs.add(wr);
	}
	
	public MirroredWriter(Writer wr0, Writer wr1)
	{
		wrs.add(wr0);
		wrs.add(wr1);
	}

	@Override
	public void close() throws IOException
	{
		for(Writer wr : wrs)
			try{wr.close();}catch(NullPointerException npe){}
	}

	@Override
	public void flush() throws IOException
	{
		for(Writer wr : wrs)
			try{wr.flush();}catch(NullPointerException npe){}
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException
	{
		for(Writer wr : wrs)
			try{wr.write(cbuf, off, len);}catch(NullPointerException npe){}
	}
	
	@Override
	public void write(char[] cbuf) throws IOException
	{
		for(Writer wr : wrs)
			try{wr.write(cbuf);}catch(NullPointerException npe){}
	}
	
	@Override
	public void write(int c) throws IOException
	{
		for(Writer wr : wrs)
			try{wr.write(c);}catch(NullPointerException npe){}
	}
	
	@Override
	public void write(String str) throws IOException
	{
		for(Writer wr : wrs)
			try{wr.write(str);}catch(NullPointerException npe){}
	}
	
	@Override
	public void write(String str, int off, int len) throws IOException
	{
		for(Writer wr : wrs)
			try{wr.write(str, off, len);}catch(NullPointerException npe){}
	}
}
