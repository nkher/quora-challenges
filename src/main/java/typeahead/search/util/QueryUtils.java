package typeahead.search.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

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

        if (str == null) {
            throw new IllegalArgumentException("Bad Input. Cannot clean a null String.");
        }

        if (StringUtils) {
            return str;
        }

        Matcher matcher = Pattern.compile("\\((.*?)\\)").matcher(str);

        while (matcher.find()) {
            StringBuilder sb = new StringBuilder();
            sb.append("(").append(matcher.group(1)).append(")");
            str = str.replaceAll(sb.toString(), "");
        }

        str = str.replaceAll("[^0-9a-zA-Z'\\-!,:;\\[\\].? *]", "").trim();

        return str.toLowerCase();
    }
}
