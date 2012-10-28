package org.dyndns.doujindb.plug.impl.mugimugi;

import java.util.*;
import javax.xml.bind.annotation.*;

@XmlRootElement(namespace="org.dyndns.doujindb.plug.impl.mugimugi", name="Tasks")
final class TaskSet
{
	@XmlElements({
	    @XmlElement(name="Task", type=Task.class)
	  })
	public Set<Task> tasks = new HashSet<Task>();
}