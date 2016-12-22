package typeahead.search.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by nameshkher on 12/21/16.
 */
public class TrieNode {

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
