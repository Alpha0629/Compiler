package llvm.values;

import llvm.types.ValueType;

import java.util.ArrayList;

public class Value {
    private final String name;
    private final ValueType valueType;
    private Value parent;
    private final ArrayList<User> users;    // 使用这个Value的User

    public Value(String name, ValueType valueType, Value parent) {
        this.name = name;
        this.valueType = valueType;
        this.parent = parent;
        this.users = new ArrayList<>();
    }

    public Value(String name, ValueType valueType) {
        this.name = name;
        this.valueType = valueType;
        this.parent = null;
        this.users = new ArrayList<>();
    }

    public Value(ValueType valueType, Value parent) {
        this.name = "";
        this.valueType = valueType;
        this.parent = parent;
        this.users = new ArrayList<>();
    }

    public Value(ValueType valueType) {
        this.name = "";
        this.valueType = valueType;
        this.parent = null;
        this.users = new ArrayList<>();
    }

    public boolean withoutName() {
        return this.name.isEmpty();
    }

    public String getName() {
        return name;
    }

    public ValueType getValueType() {
        return valueType;
    }

    public void addUser(User user) {
        this.users.add(user);
    }

    public void removeUser(User user) {
        this.users.remove(user);
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public Value getParent() {
        return parent;
    }

    public void resetParent(Value value) {
        this.parent = value;
    }
}
