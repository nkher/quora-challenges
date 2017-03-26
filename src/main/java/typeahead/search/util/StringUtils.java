package typeahead.search.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
