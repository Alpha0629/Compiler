package llvm.values.instructions;

import llvm.types.ValueType;
import llvm.types.VoidType;
import llvm.values.Value;

import java.util.ArrayList;
import java.util.Collections;

public class Ret extends Instruction {
    // ret <type> <value> | ret void
    public Ret(ValueType returnValueType, Value parent, Value returnValue) {
        super(returnValueType, parent, new ArrayList<>(Collections.singletonList(returnValue)));
    }

    public Ret(ValueType valueType, Value parent) {
        super(valueType, parent);
    }

    public Value getReturnValue() {
        assert(!super.getOperands().isEmpty());
        return super.getOperands().get(0);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ret");
        sb.append(" ");
        if (super.getValueType() instanceof VoidType) {
            sb.append("void");
            return sb.toString();
        } else {
            sb.append(super.getValueType().toString());
            sb.append(" ");
            sb.append(this.getReturnValue().getName());
            return sb.toString();
        }
    }
}
