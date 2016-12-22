package quora.solution;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.Map.Entry;

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
	
	private void process(final String input) {
								
		// We terminate the program here intentionally as we assume the tc1 has to be good.
		// A real service wouldn't terminate this ways on a bad request
		if (input == null || input.length() == 0) {
			throw new IllegalArgumentException("HTTP 400. Bad Input. Null or Empty Input.");
		}
				
		parseInput(input);
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
	
	public static void main(String[] args) throws Exception {

		final String filePath = "inputfiles";
		final String fileName = "tc5";
		
		TypeAheadSearch typeAheadSearch = new TypeAheadSearch();
		
		BufferedReader bufferedReader = null;
		
		try {
			
			bufferedReader = new BufferedReader(new FileReader(filePath + "/" + fileName));
			
			String line = bufferedReader.readLine(); // reading the line for the number of input rows
			
			while ( (line = bufferedReader.readLine()) != null ) {

				typeAheadSearch.process(line);
			}

		} catch (Exception e) {
			
			System.out.println(e.getMessage());
		}

		System.out.println(FINAL_RESULT.toString());
	}

}

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
		String cleanedQuery = cleanString(query);
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
			String cleanedRawQuery = cleanString(rawQuery);


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

		String cleanedQuery = cleanString(query);
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
			
		for (Entry<String, QueryNode> entry : queryMap.entrySet()) {
			
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

	private String cleanString(String str) {

		str = str.replaceAll("[^\\dA-Za-z ]", "");
		str.trim();
		return str.toLowerCase();
	}
	
}

/***
 *
 * This class represents the data structure of a node within the Trie
 * which we build to store the query data strings.
 *
 */
class TrieNode {

	private char letter;
	private Map<Character, TrieNode> children;
	private boolean isLast;
	private Set<String> uidList;

	public TrieNode(char letter) {
		this.letter = letter;
	}

	public Map<Character, TrieNode> getChildren() {
		return this.children;
	}

	public void addChild(char ch, TrieNode node) {
		if (children == null) {
			children = new HashMap<>();
		}
		children.put(ch, node);
	}

	public TrieNode getChildNodeByLetter(char ch) {
		if (children == null) {
			children = new HashMap<>();
		}
		return this.children.get(ch);
	}

	public boolean doesChildExist(char ch) {
		if (children == null) {
			children = new HashMap<>();
		}
		return this.children.containsKey(ch);
	}

	public void setIsLast(boolean isLast) {
		this.isLast = isLast;
	}

	public boolean isLast() {
		return this.isLast;
	}

	public void addToUidList(String uid) {
		if (uidList == null) {
			uidList = new HashSet<>();
		}
		uidList.add(uid);
	}

	public boolean isUidListEmpty() {

		return (this.uidList != null &&  this.uidList.isEmpty());
	}

	public void removeFromUidList(String uid) {

		this.uidList.remove(uid);
	}

	public Set<String> getUidList() {
		return this.uidList;
	}

}

class QueryNode {

	private float score;
	private String rawQuery;
	private long timeInMillis;
	private String uid;
	private ItemType type;

	public QueryNode(String uid, String rawQuery, float score, ItemType type) {
		this.uid = uid;
		this.rawQuery = rawQuery;
		this.score = score;
		this.type = type;
		this.timeInMillis = TypeAheadSearch.START_TIME_IN_MILLIS++;
	}
	
	public float getScore() {
		return this.score;
	}

	public String getRawQuery() {
		return this.rawQuery;
	}

	public long getTimeInMillis() {
		return this.timeInMillis;
	}
	
	public String getUid() {
		return this.uid;
	}
	
	public ItemType getItemType() {
		return this.type;
	}

	public void setScore(float score) {
		this.score = score;
	}

	@Override
	public QueryNode clone() {

		return new QueryNode(this.uid, this.rawQuery, this.score, this.type);
	}

	@Override
	public String toString() {

		return "[" + this.uid + ", " + this.score + ", " + this.timeInMillis + "]";
	}
	
}

enum ItemType {
	
	User("user"),
	Topic("topic"),
	Question("question"),
	Board("board");
	
	private String name;
	
	private ItemType(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}
	
	public static ItemType getItemTypeByName(final String name) {
		
		for (ItemType type : ItemType.values()) {
			
			if (type.getName().equalsIgnoreCase(name)) {
				return type;
			}
		}
		
		return null;
	}
	
}

enum Command {
	
	ADD, 
	DEL,
	QUERY,
	WQUERY
}

class QueryResultComparator implements Comparator<QueryNode> {

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
