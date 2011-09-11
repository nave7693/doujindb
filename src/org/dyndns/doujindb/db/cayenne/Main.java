package org.dyndns.doujindb.db.cayenne;

import java.net.URL;
import java.util.*;

import org.apache.cayenne.*;
import org.apache.cayenne.access.*;
import org.apache.cayenne.query.*;
import org.apache.cayenne.util.ResourceLocator;
import org.apache.cayenne.conf.*;
import org.apache.cayenne.dba.JdbcAdapter;

@SuppressWarnings("unused")
public class Main {

	public static void start() {
		
//		ResourceLocator lock = new ResourceLocator()
//		{
//
//			@Override
//			public URL getResource(String id) {
//				System.out.println("getResource(" + id + ")");
//				return getClass().getResource(id);
//			}
//
//			@Override
//			public Collection<URL> getResources(String id) {
//				System.out.println("getResources(" + id + ")");
//				Collection<URL> buff = new Vector<URL>();
//				buff.add(getClass().getResource(id));
//				return buff;
//			}
//			
//		};
//		
//		DefaultConfiguration conf = new DefaultConfiguration("asasdcayenne.xml", lock);
//		conf.setR
//		{
//
//			@Override
//			protected ResourceFinder getResourceFinder()
//			{
//				return new ResourceFinder()
//				{
//
//					@Override
//					public URL getResource(String id) {
//						System.out.println("getResource(" + id + ")");
//						return getClass().getResource(id);
//					}
//
//					@Override
//					public Collection<URL> getResources(String id) {
//						System.out.println("getResources(" + id + ")");
//						Collection<URL> buff = new Vector<URL>();
//						buff.add(getClass().getResource(id));
//						return buff;
//					}
//					
//				};
//			}
//			
//		};
		
		DefaultConfiguration conf = new DefaultConfiguration();			
		conf.addClassPath("org/dyndns/doujindb/db/cayenne/");
		Configuration.initializeSharedConfiguration(conf);

		JdbcAdapter adapter = (org.apache.cayenne.dba.JdbcAdapter) conf.getDomain("doujindb").getNode("mysql").getAdapter();
		adapter.setSupportsGeneratedKeys(true);
		
//		System.out.println("" + conf.getDomainConfigurationName());
//		
//		System.out.println("Listing domains ...");
//		Collection<DataDomain> doms = conf.getDomains();
//		for(DataDomain dom : doms)
//			System.out.println(dom.getName());
		
		ObjectContext context = conf.getDomain("doujindb").createDataContext();

		Artist shimakaze = context.newObject(Artist.class);
		shimakaze.setJapaneseName("Shimakaze");

		Circle sob = context.newObject(Circle.class);
		sob.setJapaneseName("Soundz of Bell");
		
		shimakaze.addToCircles(sob);
		
		System.out.println("Artists : " + context.performQuery(new org.apache.cayenne.query.SelectQuery(org.dyndns.doujindb.db.cayenne.Artist.class)).size());
		System.out.println("Circles : " + context.performQuery(new org.apache.cayenne.query.SelectQuery(org.dyndns.doujindb.db.cayenne.Circle.class)).size());
		for(Artist a : sob.getArtists())
			System.out.println("@ " + a);
		
		context.commitChanges();
		
		//context.deleteObject(sob);
		
		System.out.println("Artists : " + context.performQuery(new org.apache.cayenne.query.SelectQuery(org.dyndns.doujindb.db.cayenne.Artist.class)).size());
		System.out.println("Circles : " + context.performQuery(new org.apache.cayenne.query.SelectQuery(org.dyndns.doujindb.db.cayenne.Circle.class)).size());
		for(Artist a : sob.getArtists())
			System.out.println("@ " + a);
		
		context.commitChanges();
		
		System.out.println("Artists : " + context.performQuery(new org.apache.cayenne.query.SelectQuery(org.dyndns.doujindb.db.cayenne.Artist.class)).size());
		System.out.println("Circles : " + context.performQuery(new org.apache.cayenne.query.SelectQuery(org.dyndns.doujindb.db.cayenne.Circle.class)).size());
		for(Artist a : sob.getArtists())
			System.out.println("@ " + a);
		
		shimakaze.removeFromCircles(sob);
		context.deleteObject(sob);
		context.commitChanges();
		
		System.out.println("Artists : " + context.performQuery(new org.apache.cayenne.query.SelectQuery(org.dyndns.doujindb.db.cayenne.Artist.class)).size());
		System.out.println("Circles : " + context.performQuery(new org.apache.cayenne.query.SelectQuery(org.dyndns.doujindb.db.cayenne.Circle.class)).size());
		for(Artist a : sob.getArtists())
			System.out.println("@ " + a);
		
		
//		ObjectContext context = DataContext.createDataContext();
//		
//		Artist picasso = context.newObject(Artist.class);
//		picasso.setName("Pablo Picasso");
//		picasso.setDateOfBirthString("18811025");
//
//		
//		Gallery metropolitan = context.newObject(Gallery.class);
//		metropolitan.setName("Metropolitan Museum of Art"); 
//
//		Painting girl = context.newObject(Painting.class);
//		girl.setName("Girl Reading at a Table");
//		        
//		Painting stein = context.newObject(Painting.class);
//		stein.setName("Gertrude Stein");
//
//		picasso.addToPaintings(girl);
//		picasso.addToPaintings(stein);
//		        
//		girl.setGallery(metropolitan);
//		stein.setGallery(metropolitan);
//
//
//		context.commitChanges();
//
//
//		SelectQuery select1 = new SelectQuery(Painting.class);
//		List paintings1 = context.performQuery(select1);
	}
}