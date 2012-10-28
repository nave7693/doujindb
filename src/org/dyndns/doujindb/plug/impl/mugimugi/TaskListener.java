package org.dyndns.doujindb.plug.impl.mugimugi;

public interface TaskListener
{
	public void statusChanged(Task.State status);
	public void stepChanged(Task.Step step);
}