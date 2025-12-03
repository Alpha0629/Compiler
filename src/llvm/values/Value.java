package llvm.values;

import llvm.types.ValueType;

import java.util.ArrayList;

public class Value {
    private final String name;
    private final ValueType valueType;
    private final Value parent;

    public Value(String name, ValueType valueType, Value parent) {
        this.name = name;
        this.valueType = valueType;
        this.parent = parent;
    }

    public Value(String name, ValueType valueType) {
        this.name = name;
        this.valueType = valueType;
        this.parent = null;
    }

    public Value(ValueType valueType, Value parent) {
        this.name = "";
        this.valueType = valueType;
        this.parent = parent;
    }

    public Value(ValueType valueType) {
        this.name = "";
        this.valueType = valueType;
        this.parent = null;
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
}
