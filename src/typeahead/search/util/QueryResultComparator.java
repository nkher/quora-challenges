package typeahead.search.util;

import typeahead.search.model.QueryNode;

import java.util.Comparator;

/**
 * Created by nameshkher on 12/21/16.
 */
public class QueryResultComparator implements Comparator<QueryNode> {

    @Override
    public int compare(QueryNode o1, QueryNode o2) {

        if (o1.getScore() > o2.getScore()) {
            return -1;
        } else if (o1.getScore() < o2.getScore()) {
            return 1;
        } else {

            if (o1.getTimeInMillis() > o2.getTimeInMillis()) {
                return -1;
            } else if (o1.getTimeInMillis() < o2.getTimeInMillis()) {
                return +1;
            }

        }

        return 0;
    }
}
