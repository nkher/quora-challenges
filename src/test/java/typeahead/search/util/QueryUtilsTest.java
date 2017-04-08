package typeahead.search.util;

import org.junit.Assert;
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

    private String input;

    private String expected;

    public QueryUtilsTest(String inputStr, String expectedStr) {
        this.input = inputStr;
        this.expected = expectedStr;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {

        return Arrays.asList(new Object[][] {

                 { "COR", "cor" },
                 { "general of the Volscians. (AUFIDIUS:)", "general of the volscians. (aufidius:)" },
                 { "Lieutenant to Aufidius. (Lieutenant:)", "lieutenant to aufidius. (lieutenant:)" },
                 { "kill him, and we'll have.", "kill him, and we'll have."},
                 { "wife to Coriolanus", "wife to coriolanus" },
                 { "[They fight, and", "[they fight, and" },
                 { "so: they are", "so: they are" },
                 { "They are worn, lord consul, so,", "they are worn, lord consul, so," },
                 { "them: on the", "them: on the" },
                 { "for a ?hen!", "for a ?hen!" }
        });
    }

    @Test
    public void testCleanString() {

        String actual = QueryUtils.cleanString(input);

        Assert.assertEquals(expected, actual);
    }

}
