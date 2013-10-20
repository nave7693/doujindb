package org.dyndns.doujindb.db.cayenne.auto;

import org.apache.cayenne.CayenneDataObject;
import org.dyndns.doujindb.db.cayenne.Artist;
import org.dyndns.doujindb.db.cayenne.Circle;

/**
 * Class _CircleOf was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
@SuppressWarnings("serial")
public abstract class _CircleOf extends CayenneDataObject {

    public static final String BOOK_MEMBEROF_PROPERTY = "bookMemberof";
    public static final String CIRCLE_MEMBEROF_PROPERTY = "circleMemberof";

    public static final String BOOK_ID_PK_COLUMN = "BOOK_ID";
    public static final String CIRCLE_ID_PK_COLUMN = "CIRCLE_ID";

    public void setArtistMemberof(Artist artistMemberof) {
        setToOneTarget("bookMemberof", artistMemberof, true);
    }

    public Artist getArtistMemberof() {
        return (Artist)readProperty("bookMemberof");
    }


    public void setCircleMemberof(Circle circleMemberof) {
        setToOneTarget("circleMemberof", circleMemberof, true);
    }

    public Circle getCircleMemberof() {
        return (Circle)readProperty("circleMemberof");
    }
}