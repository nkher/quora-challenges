package typeahead.search.service;


import typeahead.search.model.ItemType;
import typeahead.search.model.QueryNode;
import typeahead.search.model.TrieNode;
import typeahead.search.util.*;

import java.util.*;

/***
 *
 * This class represents the Trie made of up of {@link TrieNode}'s. This class is shown below.
 * It contains appropriate add, delete, search and wSearch methods to perform the required operarions.
 * This class also stores the query data strings and the search tokens in a hashmap for the purpose of
 * quick optimization where ever possible.
 *
 */
class QueryStore {

    private TrieNode root;

    private Map<String, QueryNode> queryMap;

    private Map<String, TrieNode> tokenNodeMap;

    private int size = 0;

    public QueryStore() {
        queryMap = new HashMap<>();
        tokenNodeMap = new HashMap<>();
        root = new TrieNode('$');
    }

    public int getSize() {
        return this.size;
    }

    /**
     *
     * Method to perform the search() functionality as per the details given in the challenge assignment.
     *
     * @param query
     */
    public PriorityQueue<QueryNode> search(final String query, final int numberOfResults) {

        validateQuery(query);

        if (numberOfResults == 0) {
            return new PriorityQueue<>();
        }

        List<QueryNode> unweightedResults = optimizedSearch(query);

        PriorityQueue<QueryNode> pQueue = new PriorityQueue<>(numberOfResults, new QueryResultComparator());
        pQueue.addAll(unweightedResults);

        return pQueue;
    }

    /**
     *
     * Method to perform the wSearch() functionality as per the details given in the challenge assignment.
     *
     * @param query
     */
    public PriorityQueue<QueryNode> wSearch(final String query, final int numberOfResults, final Map<String, Float> boostMap) {

        validateQuery(query);

        if (numberOfResults == 0) {
            return new PriorityQueue<>();
        }

        List<QueryNode> unweightedResults = optimizedSearch(query); // perform basic search
        List<QueryNode> boostedResults = applyBoosts(unweightedResults, boostMap); // applying boosts to results

        PriorityQueue<QueryNode> pQueue = new PriorityQueue<>(numberOfResults, new QueryResultComparator());
        pQueue.addAll(boostedResults);

        return pQueue;
    }

    /**
     *
     * Method to perform the insert() functionality as per the details given in the challenge assignment.
     * It inserts into the Trie as well as the queryMap and tokenNodeMap (HashMap and HashSet for optimization).
     *
     * @param query
     */
    public void insert(final String query, final String uid, final float score, final ItemType type) {

        validateQuery(query);

        queryMap.put(uid, new QueryNode(uid, query, score, type));

        // enter each token from the data string into the trie
        String cleanedQuery = StringUtils.cleanString(query);
        String tokens[] = cleanedQuery.split("\\s+");


        for (String token : tokens) {

            if (!tokenNodeMap.containsKey(token)) {

                insertIntoTrie(token, uid);

            } else { // optimization !!

                TrieNode node = tokenNodeMap.get(token);
                node.addToUidList(uid);
                tokenNodeMap.put(token, node);
            }
        }

        size++;
    }

    /**
     *
     * Method to perform the delete() functionality as per the details given in the challenge assignment.
     * It delete's from the queryMap and tokenNodeMap (HashMap and HashSet for optimization).
     * It does not remove the data from the Trie completely (as a part of optimization).
     *
     * @param uid
     */
    public void delete(final String uid) {

        if (queryMap.containsKey(uid)) {

            String rawQuery = queryMap.get(uid).getRawQuery();
            String cleanedRawQuery = StringUtils.cleanString(rawQuery);


            for (String token : cleanedRawQuery.split("\\s+")) {

                deleteFromTrie(token, uid);
            }

            queryMap.remove(uid); // Remove from the query map
            size--;
        }
    }

    private void insertIntoTrie(String token, String uid) {

        int level = token.length();
        TrieNode curr = this.root;

        for (int i=0; i<level; i++) {

            char ch = token.charAt(i);

            if (!curr.doesChildExist(ch)) {

                TrieNode node = new TrieNode(ch);
                curr.addChild(ch, node);
            }

            curr = curr.getChildNodeByLetter(ch);
        }

        curr.setIsLast(true);
        curr.addToUidList(uid);

        tokenNodeMap.put(token, curr);
    }

    /***
     *
     * This delete just removes the uid from the list of uids associated to the last character of this token
     *
     * @param token
     * @param uid
     */
    private void deleteFromTrie(String token, String uid) {

        int level = token.length();
        TrieNode curr = this.root;

        int i = 0;

        while (i < level) {

            char ch = token.charAt(i);
            curr = curr.getChildren().get(ch);
            i++;
        }

        curr.removeFromUidList(uid);

        if (curr.isUidListEmpty()) {
            curr.setIsLast(false);
            tokenNodeMap.remove(token);
        }

    }

    private void validateQuery(String query) {

        if (query == null || query.length() == 0) {
            throw new IllegalArgumentException("HTTP 400. Bad Input. Null or Empty Query passed.");
        }
    }

    private List<QueryNode> optimizedSearch(String query) {

        Set<QueryNode> unweightedResults = new HashSet<>();
        Map<String, Integer> uidFreqMap = new HashMap<>();

        int tokensMatchingSearchCriteria = 0;

        String cleanedQuery = StringUtils.cleanString(query);
        String tokens[] = cleanedQuery.split("\\s+");

        for (String token : tokens) {

            Set<String> result = searchInTrie(token);

            if (!result.isEmpty()) {
                addTokenResultsToMap(uidFreqMap, result);
                tokensMatchingSearchCriteria++;
            }
        }

        Set<String> uidList = buildIntersectionSet(uidFreqMap, tokens.length);

        if (tokensMatchingSearchCriteria == tokens.length) {

            for (String uid : uidList) {
                unweightedResults.add(this.queryMap.get(uid));
            }
        }

        return new ArrayList<>(unweightedResults);
    }

    private void addTokenResultsToMap(Map<String, Integer> uidFreqMap, Set<String> uidSet) {

        for (String uid : uidSet) {

            if (!uidFreqMap.containsKey(uid)) {
                uidFreqMap.put(uid, 0);
            }
            uidFreqMap.put(uid, (uidFreqMap.get(uid) + 1));
        }
    }

    private Set<String> buildIntersectionSet(Map<String, Integer> uidFreqMap, int minCount) {

        Set<String> result = new HashSet<>();

        for (String uid : uidFreqMap.keySet()) {

            if (uidFreqMap.get(uid) >= minCount) {
                result.add(uid);
            }
        }

        return result;
    }

    private Set<String> searchInTrie(String token) {

        Set<String> uidList = new HashSet<>();

        int level = token.length();
        TrieNode curr = this.root;
        char ch = '$'; // dummy initialization

        // Quick optimization
        if (!curr.doesChildExist(token.charAt(0))) {
            return new HashSet<>();
        }

        int i = 0;

        for (i=0; i<level; i++) {

            ch = token.charAt(i);

            if (curr.getChildren() == null || !curr.getChildren().containsKey(ch)) {
                break;
            }

            curr = curr.getChildren().get(ch);
        }

        if (i < level-1) {
            return uidList;
        }

        if (ch == token.charAt(level-1) && curr.getUidList() != null) { // reached last character
            uidList.addAll(curr.getUidList());
        }

        uidList.addAll(performDFSOnTrie(curr));

        return uidList;
    }

    /***
     *
     * This method performs a DFS on the trie from the node that is passed to it.
     * It returns a list of results associated to it.
     *
     * @param curr
     * @return
     */
    private Set<String> performDFSOnTrie(TrieNode curr) {

        if (curr == null) {
            return new HashSet<>();
        }

        Set<String> result = new HashSet<>();
        Queue<TrieNode> queue = new LinkedList<>();
        queue.add(curr);

        TrieNode temp = null;

        while (!queue.isEmpty()) {

            temp = queue.poll();

            if (temp != null && temp.getChildren() != null) {
                queue.addAll(temp.getChildren().values());
            }

            if (temp != null && temp.isLast() && temp.getUidList() != null) {
                result.addAll(temp.getUidList());
            }
        }

        return result;
    }

    /***
     *
     * This performs a basic search over the existing state of the queryMap.
     * The search algorithm is inefficient and is polynomial time in nature.
     *
     * @param query
     * @return
     */
    private List<QueryNode> performBasicSearch(String query) {

        Set<QueryNode> unweightedResults = new HashSet<>();
        String lowerCaseSearchQuery[] = query.split("\\s+");

        for (Map.Entry<String, QueryNode> entry : queryMap.entrySet()) {

            int numberOfTokensFound = 0;
            String dataStrings[] = entry.getValue().getRawQuery().toLowerCase().split("\\s+");

            for (String token : lowerCaseSearchQuery) {

                for (String dataStr : dataStrings) {

                    if (dataStr.startsWith(token)) {
                        numberOfTokensFound++;
                        break;
                    }
                }
            }

            // each token must be found within the data string
            if (numberOfTokensFound == lowerCaseSearchQuery.length) {
                unweightedResults.add(entry.getValue()); // add the id of the corresponding value
            }
        }

        return new ArrayList<>(unweightedResults);
    }

    private List<QueryNode> applyBoosts(List<QueryNode> unweightedResults, Map<String, Float> boostMap) {

        if (boostMap.isEmpty()) return unweightedResults;

        List<QueryNode> updatedResults = new ArrayList<>();

        // applying the boost
        for (QueryNode node : unweightedResults) {

            float rawScore = node.getScore();
            String typeName = node.getItemType().getName();
            String uid = node.getUid();

            if (boostMap.containsKey(typeName) || boostMap.containsKey(uid)) {

                float boostValue = 0;
                float boostedScore = rawScore;

                if (boostMap.containsKey(typeName)) {

                    boostValue = boostMap.get(typeName);
                    boostedScore *= boostValue;

                } if (boostMap.containsKey(uid)) {

                    boostValue = boostMap.get(uid);
                    boostedScore *= boostValue;
                }

                QueryNode boostedNode = node.clone();
                boostedNode.setScore(boostedScore);
                updatedResults.add(boostedNode);

            } else {

                updatedResults.add(node);
            }

        }

        return updatedResults;
    }

}