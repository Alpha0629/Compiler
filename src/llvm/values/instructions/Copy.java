package llvm.values.instructions;

import llvm.types.ValueType;
import llvm.values.BasicBlock;
import llvm.values.Value;

import java.util.ArrayList;
import java.util.Arrays;

public class Copy extends Instruction {
    public Copy(ValueType valueType, Value parent, Value phiNode, Value value) {
        super(valueType, parent, new ArrayList<>(Arrays.asList(phiNode, value)));
    }

    public Value getPhiNode() {
        return super.getOperands().get(0);
    }

    public Value getValue() {
        return super.getOperands().get(1);
    }

    public void resetValue(Value value) {
        super.getOperands().set(1, value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("copy");
        sb.append(" ");
        sb.append(this.getPhiNode().getName());
        sb.append(", ");
        sb.append(this.getValue().getName());
        return sb.toString();
    }
}
