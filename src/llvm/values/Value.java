package llvm.values;

import java.util.ArrayList;

/**
 * {@code @Description}
 */
public class Value {
    public static int idCount = 0;
    private final int id;
    private final String name;
    private final ArrayList<User> userList;

    public Value(String name) {
        this.id = idCount++;
        this.name = name;
        this.userList = new ArrayList<>();
    }
}
