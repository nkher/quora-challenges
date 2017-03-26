package typeahead.search.model;

/**
 * Created by nameshkher on 12/21/16.
 */
public enum ItemType {

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
