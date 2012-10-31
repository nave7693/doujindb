package org.dyndns.doujindb.plug.impl.mugimugi;

public interface TaskListener
{
	public void statusChanged(Task.Step step, Task.State status);
	public void stepChanged(Task.Step step);
}