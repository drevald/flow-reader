package com.veve.flowreader.dao.sqlite;

/**
 * Created by ddreval on 4/4/2018.
 */

public class BookStorageSchema {
        public static final class BookTable {
            public static final String NAME = "books";
            public static final class Cols {
                public static final String PATH = "path";
                public static final String NAME = "name";
                public static final String PAGES_COUNT = "pages_count";
                public static final String CURRENT_PAGE = "current_page";
                public static final String ID = "id";
            }
        }
}
