package org.dyndns.doujindb.plug.impl.dataimport;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

final class MugiMugiProvider extends MetadataProvider {

	private static Pattern mURLPattern = Pattern.compile("(http://(www\\.)?doujinshi\\.org/book/)(?<id>[0-9]+)(/.*)");
	
	private static final Logger LOG = (Logger) LoggerFactory.getLogger(MugiMugiProvider.class);
	
	@XmlRootElement
	@XmlType(namespace="org.mugimugi.doujinshi", name="Metadata")
	public static final class Metadata extends org.dyndns.doujindb.plug.impl.dataimport.Metadata {
		Metadata() { }
		
		@Override
		public String provider() {
			return "mugimugi";
		}
	}
	
	@Override
	public Metadata query(File image) {
		try {
			// Check API key
			APIClient.checkAPI();
			// Query API
			URLConnection urlc = new URL("http://www.doujinshi.org/api/" + Configuration.provider_mugimugi_apikey.get() + "/?S=imageSearch").openConnection();
			urlc.setRequestProperty("User-Agent", Configuration.options_http_useragent.get());
			urlc.setConnectTimeout(Configuration.options_http_timeout.get());
			InputStream is = new ClientHttpRequest(urlc).post(new Object[] {
				"img", image
			});
			// Parse XML response
			APIClient.XML_List list = APIClient.parseList(is);
			// Check XML from errors
			if(list.ERROR != null) {
				throw new TaskException("Server Error : " + list.ERROR.EXACT + " (" + list.ERROR.CODE + ")");
			}
			// Find best match
			double bestMatch = 0;
			APIClient.XML_Book book = null;
			for(APIClient.XML_Book b : list.Books) {
				double match = Double.parseDouble(b.search.replaceAll("%", "").replaceAll(",", "."));
				if(match > bestMatch) {
					bestMatch = match;
					book = b;
				}
			}
			// Throw exception if no matching book was found
			if(book == null)
				throw new TaskException("Response books did not match the threshold");
			// Generate Metadata
			Metadata md = toMetadata(book);
			// Set 'score' field
			md.score = (int) bestMatch;
			// Generate Warning in case of poor similarity results
			if(bestMatch < Configuration.provider_mugimugi_threshold.get()) {
				String message = "Response book did not match the threshold (" + bestMatch + "%)";
				md.message = message;
				md.exception(new TaskException(message));
			}
			// Return Metadata object
			return md;
		} catch (TaskException te) {
			Metadata md = new Metadata();
			md.message = te.getMessage();
			md.exception(te);
			return md;
		} catch (IOException ioe) {
			Metadata md = new Metadata();
			md.message = "Error querying MugiMugi with input Image";
			md.exception(ioe);
			return md;
		} catch (JAXBException jaxbe) {
			Metadata md = new Metadata();
			md.message = "Error parsing MugiMugi XML response with input Image";
			md.exception(jaxbe);
			return md;
		}
	}

	@Override
	public Metadata query(String name) {
		try {
			// FIXME Implement MugiMugiProvider.query(String)
			throw new TaskException("Method not implemented");
		} catch (TaskException te) {
			Metadata md = new Metadata();
			md.message = te.getMessage();
			md.exception(te);
			return md;
		}
	}

	@Override
	public Metadata query(URI uri) {
		try {
			// Check API key
			APIClient.checkAPI();
			// Extract MugiMugi Book Id from URI
			Matcher matcher = mURLPattern.matcher(uri.toString());
			if(!matcher.find())
				throw new TaskException("Invalid MugiMugi URI " + uri);
			String bookId = matcher.group("id");
			// Query API
			URLConnection urlc = new URL("http://www.doujinshi.org/api/" + Configuration.provider_mugimugi_apikey.get() + "/?S=getID&ID=B" + bookId + "").openConnection();
			urlc.setRequestProperty("User-Agent", Configuration.options_http_useragent.get());
			urlc.setConnectTimeout(Configuration.options_http_timeout.get());
			// Parse XML response
			APIClient.XML_List list = APIClient.parseList(urlc.getInputStream());
			// Check XML from errors
			if(list.ERROR != null) {
				throw new TaskException("Server Error : " + list.ERROR.EXACT + " (" + list.ERROR.CODE + ")");
			}
			APIClient.XML_Book book = null;
			for(APIClient.XML_Book b : list.Books) {
				book = b;
			}
			// Generate Metadata
			Metadata md = toMetadata(book);
			// Set 'score' field
			md.score = Integer.MAX_VALUE;
			// Return Metadata object
			return md;
		} catch (TaskException te) {
			Metadata md = new Metadata();
			md.message = te.getMessage();
			md.exception(te);
			return md;
		} catch (IOException ioe) {
			Metadata md = new Metadata();
			md.message = "Error querying MugiMugi with input URI " + uri;
			md.exception(ioe);
			return md;
		} catch (JAXBException jaxbe) {
			Metadata md = new Metadata();
			md.message = "Error parsing MugiMugi XML response with input URI " + uri;
			md.exception(jaxbe);
			return md;
		}
	}
	
	private static Metadata toMetadata(APIClient.XML_Book book) {
		Metadata md = new Metadata();
		md.uri = "http://www.doujinshi.org/book/" + book.ID.substring(1);
		int bookid = Integer.parseInt(book.ID.substring(1));
		md.thumbnail = "http://img.doujinshi.org/tn/" + (int) Math.floor((double)bookid / (double)2000) + "/" + bookid + ".jpg";
		md.name = book.NAME_JP;
		md.translation = book.NAME_EN;
		md.alias.add(book.NAME_R);
		for(String name_alt : book.NAME_ALT)
			md.alias.add(name_alt);
		md.timestamp = book.DATE_RELEASED.getTime();
		md.pages = book.DATA_PAGES;
		md.adult = book.DATA_AGE == 1;
		md.info = book.DATA_INFO;
		for(APIClient.XML_Item item : book.LINKS.Items) {
			switch(item.TYPE) {
			case type:
				md.type = item.NAME_JP;
				break;
			case author:
				md.artist.add(new Metadata.Artist(pick(item.NAME_EN, item.NAME_JP, item.NAME_R)));
				break;
			case circle:
				md.circle.add(new Metadata.Circle(pick(item.NAME_EN, item.NAME_JP, item.NAME_R)));
				break;
			case contents:
				md.content.add(new Metadata.Content(pick(item.NAME_EN, item.NAME_JP, item.NAME_R)));
				break;
			case convention:
				md.convention = new Metadata.Convention(pick(item.NAME_EN, item.NAME_JP, item.NAME_R));
				break;
			case parody:
				md.parody.add(new Metadata.Parody(pick(item.NAME_EN, item.NAME_JP, item.NAME_R)));
				break;
			case character:
				break;
			case collections:
				break;
			case genre:
				break;
			case imprint:
				break;
			case publisher:
				break;
			}
		}
		return md;
	}
	
	/**
	 * Pick the best String match that is not null or and empty String
	 */
	private static String pick(String... args) {
		for (String arg : args) {
			if(arg != null && arg.length() > 0)
				return arg;
		}
		return "";
	}

	/**
	 * MugiMugi API Client
	 * @author loli10K
	 * @see http://www.doujinshi.org/API_MANUAL.txt
	 */
	private static final class APIClient
	{
		private static XML_User User = new XML_User();

		static {
			try {
				URLConnection urlc = new URL("http://www.doujinshi.org/api/" + Configuration.provider_mugimugi_apikey.get() + "/").openConnection();
				urlc.setRequestProperty("User-Agent", Configuration.options_http_useragent.get());
				urlc.setConnectTimeout(Configuration.options_http_timeout.get());
				InputStream is = urlc.getInputStream();
				// Parse XML response
				APIClient.XML_List list = APIClient.parseList(is);
				// Check XML from errors
				if(list.ERROR != null) {
					throw new RuntimeException("Server Error : " + list.ERROR.EXACT + " (" + list.ERROR.CODE + ")");
				}
				// Update user data
				User = (list.USER == null ? User : list.USER);
			} catch (Exception e) {
				LOG.error("Error loading API info", e);
			}
		}
		
		private static void checkAPI() throws TaskException {
			if(!Configuration.provider_mugimugi_apikey.get().matches("[0-9a-f]{20}")) {
				if(Configuration.provider_mugimugi_apikey.get().length() == 0)
					throw new TaskException("No API key provided");
				else
					throw new TaskException("Invalid API key provided : " + Configuration.provider_mugimugi_apikey.get());
			}
			if(User.Queries < 1) {
				throw new TaskException("Not enough API queries : " + User.Image_Queries);
			}
		}
		
		private static XML_List parseList(InputStream in) throws JAXBException
		{
			// parse XML_List
			XML_List list = (XML_List) parseObject(in, XML_List.class);
			// update user data
			User = (list.USER == null ? User : list.USER);
			// return list
			return list;
		}
		
		@XmlRootElement(namespace = "", name="LIST")
		static final class XML_List
		{
			@XmlElements({
			    @XmlElement(name="BOOK", type=XML_Book.class)
			  })
			List<XML_Book> Books = new Vector<XML_Book>();
			@XmlElement(name="USER", required=false)
			XML_User USER;
			@XmlElement(name="ERROR", required=false)
			XML_Error ERROR;
		}
		
		@XmlRootElement(namespace = "", name="BOOK")
		static final class XML_Book
		{
			@XmlAttribute(name="ID", required=true)
			String ID = null;
			@XmlAttribute(name="VER", required=true)
			int VER;
			@XmlAttribute(name="search", required=false)
			String search;
			
			@XmlElement(name="NAME_EN", required=false)
			String NAME_EN;
			@XmlElement(name="NAME_JP", required=false)
			String NAME_JP;
			@XmlElement(name="NAME_R", required=false)
			String NAME_R;
			@XmlElements({
			    @XmlElement(name="NAME_ALT", type=String.class)
			  })
			List<String> NAME_ALT = new Vector<String>();
			@XmlElement(name="DATE_RELEASED", required=false)
			Date DATE_RELEASED;
			@XmlElement(name="DATA_ISBN", required=false)
			String DATA_ISBN;
			@XmlElement(name="DATA_PAGES", required=false)
			int DATA_PAGES;
			@XmlElement(name="DATA_AGE", required=false)
			int DATA_AGE;
			@XmlElement(name="DATA_ANTHOLOGY", required=false)
			int DATA_ANTHOLOGY;
			@XmlElement(name="DATA_LANGUAGE", required=false)
			int DATA_LANGUAGE;
			@XmlElement(name="DATA_COPYSHI", required=false)
			int DATA_COPYSHI;
			@XmlElement(name="DATA_MAGAZINE", required=false)
			int DATA_MAGAZINE;
			@XmlElement(name="DATA_INFO", required=false)
			String DATA_INFO;
			
			@XmlElement(required=true)
			XML_Links LINKS;		
		}
		
		@XmlRootElement(namespace = "", name="LINKS")
		static final class XML_Links
		{
			@XmlElements({
			    @XmlElement(name="ITEM", type=XML_Item.class)
			  })
			List<XML_Item> Items = new Vector<XML_Item>();
		}
		
		@XmlRootElement(namespace = "", name="USER")
		static final class XML_User
		{
			@XmlAttribute(name="id", required=true)
			String id = "";
			@XmlElement(name="User", required=true)
			String User = "";
			@XmlElement(name="Queries", required=true)
			int Queries = 0;
			@XmlElement(name="Image_Queries", required=true)
			int Image_Queries = 0;
		}
		
		@XmlRootElement(namespace = "", name="ITEM")
		static final class XML_Item
		{
			@XmlAttribute(name="ID", required=true)
			String ID;
			@XmlAttribute(name="VER", required=true)
			int VER;
			@XmlAttribute(name="TYPE", required=true)
			XML_Type TYPE;
			@XmlAttribute(name="PARENT", required=false)
			String PARENT;
			@XmlAttribute(name="FRQ", required=true)
			int FRQ;
			
			@XmlElement(name="NAME_EN", required=false)
			String NAME_EN;
			@XmlElement(name="NAME_JP", required=false)
			String NAME_JP;
			@XmlElement(name="NAME_R", required=false)
			String NAME_R;
			@XmlElement(name="OBJECTS", required=false)
			int OBJECTS;
			@XmlElements({
			    @XmlElement(name="NAME_ALT", type=String.class)
			  })
			List<String> NAME_ALT = new Vector<String>();
			@XmlElement(name="DATE_START", required=false)
			Date DATE_START;
			@XmlElement(name="DATE_END", required=false)
			Date DATE_END;
			@XmlElement(name="DATA_AGE", required=false)
			int DATA_SEX;
			@XmlElement(name="DATA_AGE", required=false)
			String DATA_AGE;
		}
		
		enum XML_Type
		{
			type, // UNDOCUMENTED
			circle,
			author,
			parody,
			character,
			contents,
			genre,
			convention,
			collections,
			publisher,
			imprint
		}
		
		@XmlRootElement(namespace = "", name="ERROR")
		static final class XML_Error
		{
			@XmlAttribute(name="code", required=true)
			int code;
			
			@XmlElement(name="TYPE", required=false)
			String TYPE;
			@XmlElement(name="EXACT", required=false)
			String EXACT;
			@XmlElement(name="CODE", required=false)
			int CODE;
		}
		
		@SuppressWarnings("unchecked")
		private static <T> T parseObject(InputStream in, Class<T> clazz) throws JAXBException
		{
			T parsed;
			JAXBContext context = JAXBContext.newInstance(clazz);
			Unmarshaller um = context.createUnmarshaller();
			parsed = (T) um.unmarshal(in);
			return parsed;
		}
	}

	@Override
	public boolean isEnabled() {
		return Configuration.provider_mugimugi_enable.get();
	}
}
