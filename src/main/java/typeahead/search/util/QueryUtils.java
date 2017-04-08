package typeahead.search.util;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nameshkher on 12/21/16.
 */
public class QueryUtils {

    private final static String ONLY_ALPHANUMERIC_CHARS = "[^A-Za-z0-9',?\\[\\]\\-*&%^():;|.! ]";

    /**
     *  Preventing initialization.
     */
    private QueryUtils() {
        throw new AssertionError();
    }

    public static String cleanString(String str) {

        if (StringUtils.isEmpty(str)) {
            return str;
        }

        return str.replaceAll(ONLY_ALPHANUMERIC_CHARS, StringUtils.EMPTY).trim().toLowerCase();
    }
}
