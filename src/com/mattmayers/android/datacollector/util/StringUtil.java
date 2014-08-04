package com.mattmayers.android.datacollector.util;

/**
 * Created by matt on 6/11/14.
 */
public class StringUtil {
    public static final String EMPTY = "";

    public static boolean hasText(CharSequence charSequence) {
        return (charSequence != null) && !charSequence.equals(EMPTY);
    }
}
