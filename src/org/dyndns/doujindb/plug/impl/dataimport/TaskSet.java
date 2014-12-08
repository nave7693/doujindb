package org.dyndns.doujindb.plug.impl.dataimport;

import java.util.*;

import javax.xml.bind.annotation.*;

@XmlRootElement(namespace="org.dyndns.doujindb.plug.impl.dataimport", name="TaskSet")
final class TaskSet
{
	@XmlElements({
	    @XmlElement(name="Task", type=Task.class)
	  })
	HashSet<Task> tasks = new HashSet<Task>();
}
