package llvm.values;

import llvm.types.ValueType;

import java.util.ArrayList;

/**
 * {@code @Description}
 */
public class Value {
    public static int idCount = 0;
    private final int id;
    private final String name;
    private final ValueType valueType;
    private final Value parent;
    private final ArrayList<User> users;

    public Value(String name, ValueType valueType, Value parent) {
        this.id = idCount++;
        this.name = name;
        this.valueType = valueType;
        this.parent = parent;
        this.users = new ArrayList<>();
    }

    public Value(String name, ValueType valueType) {
        this.id = idCount++;
        this.name = name;
        this.valueType = valueType;
        this.parent = null;
        this.users = new ArrayList<>();
    }

    public Value(ValueType valueType, Value parent) {
        this.id = idCount++;
        this.name = "";
        this.valueType = valueType;
        this.parent = parent;
        this.users = new ArrayList<>();
    }

    public Value(ValueType valueType) {
        this.id = idCount++;
        this.name = "";
        this.valueType = valueType;
        this.parent = null;
        this.users = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ValueType getValueType() {
        return valueType;
    }

    public Value getParent() {
        return parent;
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public void addUser(User user) {
        users.add(user);
    }

    public void removeUser(User user) {
        users.remove(user);
    }
}
