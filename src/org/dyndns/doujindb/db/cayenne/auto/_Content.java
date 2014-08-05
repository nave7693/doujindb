package org.dyndns.doujindb.db.cayenne.auto;

import java.util.Set;

import org.apache.cayenne.CayenneDataObject;
import org.dyndns.doujindb.db.cayenne.Book;
import org.dyndns.doujindb.db.cayenne.ContentAlias;

/**
 * Class _Content was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _Content extends CayenneDataObject {

    public static final String INFO_PROPERTY = "info";
    public static final String RECYCLED_PROPERTY = "recycled";
    public static final String TAG_NAME_PROPERTY = "tagName";
    public static final String ALIASES_PROPERTY = "aliases";
    public static final String BOOKS_PROPERTY = "books";

    public static final String ID_PK_COLUMN = "ID";

    public void setInfo(String info) {
        writeProperty("info", info);
    }
    public String getInfo() {
        return (String)readProperty("info");
    }

    public void setRecycled(Boolean recycled) {
        writeProperty("recycled", recycled);
    }
    public Boolean getRecycled() {
        return (Boolean)readProperty("recycled");
    }

    public void setTagName(String tagName) {
        writeProperty("tagName", tagName);
    }
    public String getTagName() {
        return (String)readProperty("tagName");
    }

    public void addToAliases(ContentAlias obj) {
        addToManyTarget("aliases", obj, true);
    }
    public void removeFromAliases(ContentAlias obj) {
        removeToManyTarget("aliases", obj, true);
    }
    @SuppressWarnings("unchecked")
    public Set<ContentAlias> getAliases() {
        return (Set<ContentAlias>)readProperty("aliases");
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
