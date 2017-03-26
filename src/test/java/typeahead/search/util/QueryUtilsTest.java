package typeahead.search.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by nameshkher on 3/26/17.
 */
@RunWith(Parameterized.class)
public class QueryUtilsTest {

    private String inputStr;

    private String expectedStr;

    public QueryUtilsTest(String inputStr, String expectedStr) {
        this.inputStr = inputStr;
        this.expectedStr = expectedStr;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {

        return Arrays.asList(new Object[][] {

                { "COR", "cor" },
                { "general of the Volscians. (AUFIDIUS:)", "cor" },
                { "Lieutenant to Aufidius. (Lieutenant:)", "cor" },
        });
    }

    private static String[] expected = {};

    @Test
    public void testCleanString() {

        String actual = QueryUtils.cleanString(inputStr);

        System.out.println(actual);
    }

}
