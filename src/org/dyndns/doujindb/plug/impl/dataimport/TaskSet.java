package org.dyndns.doujindb.plug.impl.dataimport;

import java.util.HashSet;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace="org.dyndns.doujindb.plug.impl.dataimport", name="TaskSet")
final class TaskSet
{
	@XmlElements({
	    @XmlElement(name="Task", type=Task.class)
	  })
	HashSet<Task> tasks = new HashSet<Task>();
}
