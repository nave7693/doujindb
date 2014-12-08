package org.dyndns.doujindb.plug.impl.dataimport;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;

import javax.xml.bind.*;
import javax.xml.bind.annotation.*;

final class MugiMugiProvider implements MetadataProvider {

	private static APIClient.XML_User userData = new APIClient.XML_User();
	private static Pattern pattern = Pattern.compile("(http://(www\\.)?doujinshi\\.org/book/)?([0-9]+)(/)?");
	
	@XmlRootElement
	@XmlType(namespace="org.mugimugi.doujinshi", name="Metadata")
	public static final class Metadata extends org.dyndns.doujindb.plug.impl.dataimport.Metadata {
		Metadata() { }
	}
	
	@Override
	public Metadata query(File image) throws TaskException {
		// Check API key
		checkAPI();
		try {
			// Query API
			URLConnection urlc = new URL("http://www.doujinshi.org/api/" + Configuration.provider_mugimugi_apikey + "/?S=imageSearch").openConnection();
			urlc.setRequestProperty("User-Agent", Configuration.options_http_useragent.get());
			urlc.setConnectTimeout(Configuration.options_http_timeout.get());
			InputStream is = new ClientHttpRequest(urlc).post(new Object[] {
				"img", image
			});
			// Parse XML response
			APIClient.XML_List list = APIClient.parseList(is);
			// Update user data
			userData = (list.USER == null ? userData : list.USER);
			// Find best match
			double bestMatch = 0;
			APIClient.XML_Book book = null;
			for(APIClient.XML_Book b : list.Books) {
				double match = Double.parseDouble(b.search.replaceAll("%", "").replaceAll(",", "."));
				if(match > bestMatch) {
					bestMatch = match;
					book = b;
				}
				//TODO keep MugiMugi books reference so we can inspect them manually
				Integer bid = Integer.parseInt(b.ID.substring(1)); // Remove leading 'B' char
			}
			if(bestMatch < Configuration.provider_mugimugi_threshold.get() || book == null)
				throw new TaskException("Response books did not match the threshold (" + Configuration.provider_mugimugi_threshold + ")");
			// Return Metadata object
			return toMetadata(book);
		} catch (IOException ioe) {
			throw new TaskException("Error querying MugiMugi with input Image", ioe);
		} catch (JAXBException jaxbe) {
			throw new TaskException("Error parsing MugiMugi XML response with input Image", jaxbe);
		}
	}

	@Override
	public Metadata query(String string) throws TaskException {
		// TODO Auto-generated method stub
		throw new TaskException("Method not implemented");
	}

	@Override
	public Metadata query(URI uri) throws TaskException {
		// Check API key
		checkAPI();
		try {
			// Extract MugiMugi Book Id from URI
			Matcher matcher = pattern.matcher(uri.toString());
			if(!matcher.find())
				throw new TaskException("Invalid MugiMugi URI " + uri);
			String bookId = matcher.group(3);
			// Query API
			URLConnection urlc = new URL("http://www.doujinshi.org/api/" + Configuration.provider_mugimugi_apikey + "/?S=getID&ID=B" + bookId + "").openConnection();
			urlc.setRequestProperty("User-Agent", Configuration.options_http_useragent.get());
			urlc.setConnectTimeout(Configuration.options_http_timeout.get());
			// Parse XML response
			APIClient.XML_List list = APIClient.parseList(urlc.getInputStream());
			// Update user data
			userData = (list.USER == null ? userData : list.USER);
			APIClient.XML_Book book = null;
			for(APIClient.XML_Book b : list.Books) {
				book = b;
				//TODO keep MugiMugi book reference so we can inspect them manually
				Integer bid = Integer.parseInt(b.ID.substring(1)); // Remove leading 'B' char
			}
			// Return Metadata object
			return toMetadata(book);
		} catch (IOException ioe) {
			throw new TaskException("Error querying MugiMugi with input URI " + uri, ioe);
		} catch (JAXBException jaxbe) {
			throw new TaskException("Error parsing MugiMugi XML response with input URI " + uri, jaxbe);
		}
	}
	
	private static void checkAPI() throws TaskException {
		if(!Configuration.provider_mugimugi_apikey.get().matches("[0-9a-f]{20}")) {
			throw new TaskException("Invalid API Key provided : " + Configuration.provider_mugimugi_apikey);
		}
	}
	
	private static Metadata toMetadata(APIClient.XML_Book book) {
		Metadata md = new Metadata();
		md.name = book.NAME_JP;
		md.translation = book.NAME_EN;
		md.alias.add(book.NAME_R);
		for(String name_alt : book.NAME_ALT)
			md.alias.add(name_alt);
		md.timestamp = book.DATE_RELEASED.getTime() / 1000;
		md.pages = book.DATA_PAGES;
		md.adult = book.DATA_AGE == 1;
		md.info = book.DATA_INFO;
		for(APIClient.XML_Item item : book.LINKS.Items) {
			switch(item.TYPE) {
			case type:
				md.type = item.NAME_JP;
				break;
			case author:
				md.artist.add(item.NAME_JP);
				break;
			case circle:
				md.circle.add(item.NAME_JP);
				break;
			case contents:
				md.content.add(item.NAME_JP);
				break;
			case convention:
				md.convention = item.NAME_JP;
				break;
			case parody:
				md.parody.add(item.NAME_JP);
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
	 * MugiMugi API Client
	 * @author loli10K
	 * @see http://www.doujinshi.org/API_MANUAL.txt
	 */
	private static final class APIClient
	{
		private static XML_User parseUser(InputStream in) throws JAXBException
		{
			return parseObject(in, XML_List.class).USER;
		}
		
		private static XML_List parseList(InputStream in) throws JAXBException
		{
			return parseObject(in, XML_List.class);
		}
		
		private static XML_Book parseBook(InputStream in) throws JAXBException
		{
			return parseObject(in, XML_Book.class);
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
}
