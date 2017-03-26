package typeahead.search.main;

import typeahead.search.service.TypeAheadSearch;

/**
 * Created by nameshkher on 12/21/16.
 */
public class Starter {

    public static void main(String[] args) throws Exception {

        final String filePath = "inputfiles";
        final String fileName = "tc1";

        TypeAheadSearch typeAheadSearch = new TypeAheadSearch();

        String result = typeAheadSearch.process(filePath, fileName);

        System.out.println(result);
    }
}
