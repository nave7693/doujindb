package org.dyndns.doujindb.plug.impl.dataimport;

public interface TaskListener {
	public abstract void taskChanged(Task task);
	public abstract void taskmanagerChanged();
}
