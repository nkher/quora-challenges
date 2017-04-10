package quora.solution;

import javafx.util.Pair;

import java.io.*;
import java.util.*;

/**
 * Created by nameshkher on 4/8/17.
 */
public class RelatedQuestions {

    private BufferedReader reader;

    private BufferedWriter writer;

    private FileReader fileReader;

    private FileWriter fileWriter;

    private List<Integer> readingTimes;

    private Map<Integer, List<Integer>> adj;

    // Stores the total time for the path that goes down
    // from a node to its immediate next neighbour.
    // Key would be the node and the value map contains its immediate neighbours.
    // The value map's key is a its immediate neighbours and it's value Pair is
    // a Pair of Total time taken down that path and the number of edges down that path
    private Map<Integer, Map<Integer, List<Integer>>> dp;

    private long N;

    public RelatedQuestions() {
        readingTimes = new ArrayList<>();
        adj = new HashMap<>();
        dp = new HashMap<>();
    }

    void run() throws IOException {

        fileReader = new FileReader("/Users/nameshkher/Documents/repositories/algorithms-ds/input");
        fileWriter = new FileWriter("/Users/nameshkher/Documents/repositories/algorithms-ds/output");

        reader = new BufferedReader(fileReader);
        writer = new BufferedWriter(fileWriter);

        String line = reader.readLine();
        N = Long.parseLong(line);

        adj = new HashMap<>((int)N+1);

        // Add the graph
        line = reader.readLine();
        String[] arr = line.split("\\s+");
        readingTimes.add(0, 0);
        for (int i=0; i<arr.length; i++) {
            readingTimes.add(i+1, Integer.parseInt(arr[i]));
        }

        // Add the relations
        while ( (line = reader.readLine()) != null ) {

            String[] pair = line.split("\\s+");
            int n1 = Integer.parseInt(pair[0]);
            int n2 = Integer.parseInt(pair[1]);

            if (adj.get(n1) == null) {
                adj.put(n1, new ArrayList<>());
            }

            if (adj.get(n2) == null) {
                adj.put(n2, new ArrayList<>());
            }

            adj.get(n1).add(n2);
            adj.get(n2).add(n1);
        }

        solve();
    }

    private void solve() {

        int time = Integer.MAX_VALUE;
        long bestNode = -1;
        Set<Integer> visited = new HashSet<>();

        // Go over each node once
        for (int i=0; i<readingTimes.size()-1; i++) {

            int curr = i+1;
            visited.add(curr);

            dfs(curr, visited);

            visited.clear();

            System.out.println("Source : " + curr);
            System.out.println(dp);
        }

        // System.out.println(String.valueOf(bestNode));
    }

    private void dfs(int from, Set<Integer> visited) {

        List<Integer> neighbours = new ArrayList<>(adj.get(from));

        if (neighbours != null) {
            neighbours.removeAll(visited);
        }

        if (neighbours.isEmpty()) { // we have seen all the nodes
            return;
        }

        for (int n=0; n<neighbours.size(); n++) {

            // from = 3, to = 2
            // from = 3, to = 4

            int to = neighbours.get(n);

            visited.add(to); // add to visited

            Map<Integer, List<Integer>> val = new HashMap<>();

            if (dp.containsKey(from)) {
                val = dp.get(from);
            }

            if (val.containsKey(to)) { // we have gone down this path
                continue;
            }

            // recurse one path till the bottom
            dfs(to, visited);

            if (!val.containsKey(to)) {

                int fromTime = readingTimes.get(from);

                if (!dp.containsKey(to)) {

                    int toTime = readingTimes.get(to);

                    List<Integer> intermediateTimes = new ArrayList<>();

                    intermediateTimes.add(fromTime + toTime);

                    val.put(to, intermediateTimes);
                    dp.put(from, val);

                } else { // case we have previously dene dfs on a node in the graph and hence we should have the result in the dp map

                    List<Integer> intermediateTimes = new ArrayList<>();
                    Map<Integer, List<Integer>> intermediateMap = dp.get(to);

                    for (Integer key : intermediateMap.keySet()) {

                        // Why this check ? - Since the edges are undirected, the grand child of the the current node's child
                        // can be the current node itself. Say if 2 is the current node and it's child is 1. When traversing
                        // over the children of 1 we see 2 again. We don't wanna go in that route !!

                        if (key != from) {
                            List<Integer> subInterTimes = intermediateMap.get(key);
                            for (Integer t : subInterTimes) {
                                intermediateTimes.add(t + fromTime);
                            }
                        } else {

                            // intermediateTimes.add(readingTimes.get(to) + fromTime);
                        }

                    }

                    if (!intermediateTimes.isEmpty()) {
                        val.put(to, intermediateTimes);
                        dp.put(from, val);
                    }
                }
            }
        }
    }

    public static void main(String args[]) throws IOException {

        new RelatedQuestions().run();
    }

}
