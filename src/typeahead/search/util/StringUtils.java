package typeahead.search.util;

/**
 * Created by nameshkher on 12/21/16.
 */
public class StringUtils {

    /**
     *  Preventing initialization.
     */
    private StringUtils() {
        throw new AssertionError();
    }

    public static String cleanString(String str) {

        str = str.replaceAll("[^\\dA-Za-z ]", "");
        str.trim();
        return str.toLowerCase();
    }
}
