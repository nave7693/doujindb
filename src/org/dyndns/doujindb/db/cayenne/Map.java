package org.dyndns.doujindb.db.cayenne;

import org.dyndns.doujindb.db.cayenne.auto._Map;

public class Map extends _Map {

    private static Map instance;

    private Map() {}

    public static Map getInstance() {
        if(instance == null) {
            instance = new Map();
        }

        return instance;
    }
}
