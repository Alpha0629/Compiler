package llvm.values.instructions;

import llvm.types.ValueType;
import llvm.values.Value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

// <result> = load <ty>, <ty>* <pointer>
public class Load extends Instruction {
    // valueType表示指针指向的类型
    public Load(String name, ValueType valueType, Value parent, Value pointer) {
        super(name, valueType, parent, new ArrayList<>(Collections.singletonList(pointer)));
    }

    public Value getPointer() {
        return super.getOperands().get(0);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.getName());
        sb.append(" = ");
        sb.append("load");
        sb.append(" ");
        sb.append(super.getValueType().toString());
        sb.append(", ");
        sb.append(this.getPointer().getValueType().toString());
        sb.append(" ");
        sb.append(this.getPointer().getName());
        return sb.toString();
    }
}
