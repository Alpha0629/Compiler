package llvm.values.instructions;

import llvm.types.ValueType;
import llvm.values.Value;

import java.util.ArrayList;
import java.util.Arrays;

public class And extends Instruction {
    // %v5 = and i32 %v3, %v4
    public And(String name, ValueType valueType, Value parent, Value leftOperand, Value rightOperand) {
        super(name, valueType, parent, new ArrayList<>(Arrays.asList(leftOperand, rightOperand)));
    }

    public Value getLeftOperand() {
        return super.getOperands().get(0);
    }

    public Value getRightOperand() {
        return super.getOperands().get(1);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.getName());
        sb.append(" = ");
        sb.append("and");
        sb.append(" ");
        sb.append(super.getValueType().toString());
        sb.append(" ");
        sb.append(this.getLeftOperand());
        sb.append(", ");
        sb.append(this.getRightOperand());
        return sb.toString();
    }
}
