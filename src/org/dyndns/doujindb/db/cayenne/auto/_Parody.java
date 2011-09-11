package org.dyndns.doujindb.db.cayenne.auto;

import java.util.Set;

import org.apache.cayenne.CayenneDataObject;
import org.dyndns.doujindb.db.cayenne.Book;

/**
 * Class _Parody was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
@SuppressWarnings("serial")
public abstract class _Parody extends CayenneDataObject {

    public static final String JAPANESE_NAME_PROPERTY = "japaneseName";
    public static final String ROMANJI_NAME_PROPERTY = "romanjiName";
    public static final String TRANSLATED_NAME_PROPERTY = "translatedName";
    public static final String WEBLINK_PROPERTY = "weblink";
    public static final String BOOKS_PROPERTY = "books";

    public static final String ID_PK_COLUMN = "ID";

    public void setJapaneseName(String japaneseName) {
        writeProperty("japaneseName", japaneseName);
    }
    public String getJapaneseName() {
        return (String)readProperty("japaneseName");
    }

    public void setRomanjiName(String romanjiName) {
        writeProperty("romanjiName", romanjiName);
    }
    public String getRomanjiName() {
        return (String)readProperty("romanjiName");
    }

    public void setTranslatedName(String translatedName) {
        writeProperty("translatedName", translatedName);
    }
    public String getTranslatedName() {
        return (String)readProperty("translatedName");
    }

    public void setWeblink(String weblink) {
        writeProperty("weblink", weblink);
    }
    public String getWeblink() {
        return (String)readProperty("weblink");
    }

    public void addToBooks(Book obj) {
        addToManyTarget("books", obj, true);
    }
    public void removeFromBooks(Book obj) {
        removeToManyTarget("books", obj, true);
    }
    @SuppressWarnings("unchecked")
    public Set<Book> getBooks() {
        return (Set<Book>)readProperty("books");
    }


}
