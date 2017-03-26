package typeahead.search.model;

import quora.solution.*;

/**
 * Created by nameshkher on 12/21/16.
 */
public class QueryNode {

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
        this.timeInMillis = Solution.START_TIME_IN_MILLIS++;
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
