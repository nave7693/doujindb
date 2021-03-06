package org.dyndns.doujindb.db.cayenne.auto;

import java.util.Set;

import org.apache.cayenne.CayenneDataObject;
import org.dyndns.doujindb.db.cayenne.Book;
import org.dyndns.doujindb.db.cayenne.ParodyAlias;

/**
 * Class _Parody was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _Parody extends CayenneDataObject {

    public static final String JAPANESE_NAME_PROPERTY = "japaneseName";
    public static final String RECYCLED_PROPERTY = "recycled";
    public static final String ROMAJI_NAME_PROPERTY = "romajiName";
    public static final String TRANSLATED_NAME_PROPERTY = "translatedName";
    public static final String WEBLINK_PROPERTY = "weblink";
    public static final String ALIASES_PROPERTY = "aliases";
    public static final String BOOKS_PROPERTY = "books";

    public static final String ID_PK_COLUMN = "ID";

    public void setJapaneseName(String japaneseName) {
        writeProperty("japaneseName", japaneseName);
    }
    public String getJapaneseName() {
        return (String)readProperty("japaneseName");
    }

    public void setRecycled(Boolean recycled) {
        writeProperty("recycled", recycled);
    }
    public Boolean getRecycled() {
        return (Boolean)readProperty("recycled");
    }

    public void setRomajiName(String romajiName) {
        writeProperty("romajiName", romajiName);
    }
    public String getRomajiName() {
        return (String)readProperty("romajiName");
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

    public void addToAliases(ParodyAlias obj) {
        addToManyTarget("aliases", obj, true);
    }
    public void removeFromAliases(ParodyAlias obj) {
        removeToManyTarget("aliases", obj, true);
    }
    @SuppressWarnings("unchecked")
    public Set<ParodyAlias> getAliases() {
        return (Set<ParodyAlias>)readProperty("aliases");
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


    protected abstract void postAdd();

}
