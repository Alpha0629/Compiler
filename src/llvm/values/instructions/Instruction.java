package llvm.values.instructions;

import llvm.types.ValueType;
import llvm.values.User;
import llvm.values.Value;

import java.util.ArrayList;

public class Instruction extends User {
    // 与User功能一致，针对各个指令
    public Instruction(String name, ValueType valueType, Value parent, ArrayList<Value> operands) {
        super("%v" + name, valueType, parent, operands);
    }

    public Instruction(String name, ValueType valueType, Value parent) {
        super("%v" + name, valueType, parent);
    }

    public Instruction(String name, ValueType valueType, ArrayList<Value> operands) {
        super("%v" + name, valueType, operands);
    }

    public Instruction(String name, ValueType valueType) {
        super("%v" + name, valueType);
    }

    public Instruction(ValueType valueType, Value parent, ArrayList<Value> operands) {
        super(valueType, parent, operands);
    }

    public Instruction(ValueType valueType, Value parent) {
        super(valueType, parent);
    }

}
