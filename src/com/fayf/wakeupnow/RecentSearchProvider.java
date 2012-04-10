package com.fayf.wakeupnow;

import android.content.SearchRecentSuggestionsProvider;

public class RecentSearchProvider extends SearchRecentSuggestionsProvider{
    public final static String AUTHORITY = "com.fayf.wakeupnow.RecentSearchProvider";
    public final static int MODE = DATABASE_MODE_QUERIES | DATABASE_MODE_2LINES;

    public RecentSearchProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}
