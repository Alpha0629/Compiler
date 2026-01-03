package llvm.values.instructions;

import llvm.types.ValueType;
import llvm.values.BasicBlock;
import llvm.values.User;
import llvm.values.Value;

import java.util.ArrayList;

public class Instruction extends User {
    // 与User功能一致，针对各个指令
    public Instruction(String name, ValueType valueType, Value parent, ArrayList<Value> operands) {
        super("%var" + name, valueType, parent, operands);
    }

    public Instruction(String name, ValueType valueType, Value parent) {
        super("%var" + name, valueType, parent);
    }

    public Instruction(String p, String name, ValueType valueType, Value parent) {
        super(p + name, valueType, parent);
    }

    public Instruction(String name, ValueType valueType, ArrayList<Value> operands) {
        super("%var" + name, valueType, operands);
    }

    public Instruction(String name, ValueType valueType) {
        super("%var" + name, valueType);
    }

    public Instruction(ValueType valueType, Value parent, ArrayList<Value> operands) {
        super(valueType, parent, operands);
    }

    public Instruction(ValueType valueType, Value parent) {
        super(valueType, parent);
    }

}
