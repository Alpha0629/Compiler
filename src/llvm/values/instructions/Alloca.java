package llvm.values.instructions;

import llvm.types.ValueType;
import llvm.values.Value;
import llvm.values.constants.ConstArray;

public class Alloca extends Instruction {
    // %v7 = alloca i32*
    private final ConstArray initArray;

    public Alloca(String name, ValueType valueType, Value parent) {
        super(name, valueType, parent);
        this.initArray = null;
    }

    public Alloca(String name, ValueType valueType, Value parent, ConstArray constArray) {
        super(name, valueType, parent);
        this.initArray = constArray;
    }

    public ConstArray getInitArray() {
        return initArray;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.getName());
        sb.append(" = ");
        sb.append("alloca");
        sb.append(" ");
        sb.append(super.getValueType().toString());
        return sb.toString();
    }
}
