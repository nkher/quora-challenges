package typeahead.search.service;

import typeahead.search.model.Command;
import typeahead.search.model.ItemType;
import typeahead.search.model.QueryNode;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Created by nameshkher on 12/21/16.
 */
public class TypeAheadSearch {

    private static StringBuffer FINAL_RESULT = new StringBuffer();

    public static long START_TIME_IN_MILLIS = new Date().getTime();

    private final static String SPACE_STRING = " ";

    private final static String COLLEN = ":";

    private final static int COMMAND_INDEX = 0;

    private final static int ADD_COMMAND_TYPE_INDEX = 1;

    private final static int ADD_COMMAND_UID_INDEX = 2;

    private final static int DEL_COMMAND_UID_INDEX = 1;

    private final static int ADD_COMMAND_SCORE_INDEX = 3;

    private final static int QUERY_COMMAND_NUMBER_OF_RESULTS_INDEX = 1;

    private final static int WQUERY_COMMAND_NUMBER_OF_RESULTS_INDEX = 1;

    private final static int WQUERY_COMMAND_NUMBER_OF_BOOSTS_INDEX = 2;

    private final static int MAX_RESULTS = 20;

    private QueryStore queryStore;

    public TypeAheadSearch() {

        queryStore = new QueryStore();
    }

    public int size() {
        return this.queryStore.getSize();
    }

    public String process(final String filePath, final String fileName) {

        BufferedReader bufferedReader = null;

        try {

            bufferedReader = new BufferedReader(new FileReader(filePath + "/" + fileName));

            String input = bufferedReader.readLine(); // reading the line for the number of input rows

            while ( (input = bufferedReader.readLine()) != null ) {

                // We terminate the program here intentionally as we assume the tc1 has to be good.
                // A real service wouldn't terminate this ways on a bad request
                if (input == null || input.length() == 0) {
                    throw new IllegalArgumentException("HTTP 400. Bad Input. Null or Empty Input.");
                }
                parseInput(input);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return FINAL_RESULT.toString();
    }

    private void parseInput(final String input) {

        String[] splitInput = input.split(SPACE_STRING);
        Command command = Command.valueOf(splitInput[COMMAND_INDEX]);

        switch (command) {

            case ADD:
                add(splitInput);
                break;

            case DEL:
                delete(splitInput);
                break;

            case QUERY:
                query(splitInput);
                break;

            case WQUERY:
                wQuery(splitInput);
                break;
        }

    }

    private void add(final String[] input) {

        ItemType type = ItemType.getItemTypeByName(input[ADD_COMMAND_TYPE_INDEX]);
        String uid = input[ADD_COMMAND_UID_INDEX];
        float score = Float.parseFloat(input[ADD_COMMAND_SCORE_INDEX]);

        String rawQuery = generateRawQuery(4, input);

        this.queryStore.insert(rawQuery, uid, score, type);
    }

    private void delete(final String[] input) {

        String uid = input[DEL_COMMAND_UID_INDEX];

        this.queryStore.delete(uid);
    }

    private void query(final String[] input) {

        String queryString = generateRawQuery(2, input);
        int numberOfResults = Integer.parseInt(input[QUERY_COMMAND_NUMBER_OF_RESULTS_INDEX]);

        numberOfResults = adjustNumberOfResults(numberOfResults);

        PriorityQueue<QueryNode> pQueue = this.queryStore.search(queryString, numberOfResults);

        String result = buildResultFromQueue(pQueue, numberOfResults);
        appendResult(result);
    }

    private void wQuery(final String[] input) {

        int numberOfResults = Integer.parseInt(input[WQUERY_COMMAND_NUMBER_OF_RESULTS_INDEX]);
        int numberOfBoosts = Integer.parseInt(input[WQUERY_COMMAND_NUMBER_OF_BOOSTS_INDEX]);

        numberOfResults = adjustNumberOfResults(numberOfResults);

        int start = 3;
        int end = start+numberOfBoosts;
        Map<String, Float> boostMap = buildBoostMap(input, numberOfBoosts, start, end);

        String queryString = generateRawQuery(end, input);

        PriorityQueue<QueryNode> pQueue = this.queryStore.wSearch(queryString, numberOfResults, boostMap);

        String result = buildResultFromQueue(pQueue, numberOfResults);
        appendResult(result);
    }

    private void appendResult(String result) {

        if (result != null || !result.isEmpty()) {
            FINAL_RESULT.append(result);
        }
        FINAL_RESULT.append("\n");
    }

    private Map<String, Float> buildBoostMap(final String input[], final int numberOfBoosts, final int start, final int end) {

        Map<String, Float> boostMap = new HashMap<>();

        if (numberOfBoosts > 0) {

            for (int i=start; i<end; i++) {

                String[] splitBoostInfo = input[i].split(COLLEN);

                if (splitBoostInfo != null && splitBoostInfo.length == 2) {

                    String key = splitBoostInfo[0].toLowerCase();
                    float score = Float.parseFloat(splitBoostInfo[1]);

                    boostMap.put(key, score);
                }
            }
        }

        return boostMap;
    }

    private String generateRawQuery(final int offset, final String[] input) {

        StringBuilder builder = new StringBuilder();

        for (int i=offset; i<input.length-1; i++) {
            builder.append(input[i]);
            builder.append(SPACE_STRING);
        }

        builder.append(input[input.length-1]);

        return builder.toString();
    }

    private String buildResultFromQueue(final PriorityQueue<QueryNode> pQueue, final int numberOfResults) {

        StringBuilder builder = new StringBuilder();
        int count = 0;

        while (pQueue.size() > 1 && count < numberOfResults) {

            QueryNode top = pQueue.remove();

            builder.append(top.getUid());
            builder.append(" ");

            count++;
        }

        if (pQueue.size() == 1 && count < numberOfResults) {
            builder.append(pQueue.remove().getUid());
        }

        return builder.toString();
    }

    private int adjustNumberOfResults(int numberOfResults) {

        return (numberOfResults > MAX_RESULTS) ? MAX_RESULTS : numberOfResults;
    }

}
