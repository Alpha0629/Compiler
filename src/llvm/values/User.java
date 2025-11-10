package llvm.values;

import llvm.types.ValueType;

import java.util.ArrayList;

/**
 * {@code @Description} User 类是 Value 的一个特殊的子类, 特殊之处在于它可以使用其他的Value，同时自己也是Value。
 * 例如在%v5 = add i32 %v3, %v4，这里的v5是一个User，同时可以做为Value供后续使用，同时有操作数v3和v4
 */
public class User extends Value {
//    父类Value的属性
//    public static int idCount = 0;
//    private final int id;
//    private final String name;
//    private final ValueType valueType;
//    private final Value parent;
//    private final ArrayList<User> users;

    private final ArrayList<Value> operands;

    public User(String name, ValueType valueType, Value parent, ArrayList<Value> operands) {
        super(name, valueType, parent);
        this.operands = operands;
    }

    public User(String name, ValueType valueType, Value parent) {
        super("%v" + name, valueType, parent);
        this.operands = new ArrayList<>();
    }

    public User(String name, ValueType valueType, ArrayList<Value> operands) {
        super(name, valueType);
        this.operands = operands;
    }

    public User(String name, ValueType valueType) {
        super(name, valueType);
        this.operands = new ArrayList<>();
    }

    public User(ValueType valueType, Value parent, ArrayList<Value> operands) {
        super(valueType, parent);
        this.operands = operands;
    }

    public User(ValueType valueType, Value parent) {
        super(valueType, parent);
        this.operands = new ArrayList<>();
    }

    public User(ValueType valueType) {
        super(valueType);
        this.operands = new ArrayList<>();
    }

    public User(ValueType valueType, ArrayList<Value> operands) {
        super(valueType);
        this.operands = operands;
    }

    public ArrayList<Value> getOperands() {
        return operands;
    }

    public void addOperand(Value operand) {
        operands.add(operand);
    }
}
