package org.dyndns.doujindb.db.cayenne;

import java.net.URL;

import org.apache.cayenne.conf.*;
import org.apache.cayenne.util.*;

public class EmbeddedConfiguration extends DefaultConfiguration
{

	@Override
	protected void setResourceLocator(ResourceLocator locator)
	{
		super.setResourceLocator(new EmbeddedResourceLocator());
	}

	private final class EmbeddedResourceLocator extends ResourceLocator
	{
		@Override
		public URL getResource(String name)
		{
			return getClass().getResource(name);
		}
	}	
}
