package llvm.values.instructions;

import llvm.types.ValueType;
import llvm.values.Value;

import java.util.ArrayList;
import java.util.Collections;

public class Trunc extends Instruction {
    // <result> = trunc <ty> <value> to <ty2>
    // 将 ty 的 value 的 type 缩减为 ty2（truncate）
    public Trunc(String name, ValueType valueType, Value parent, Value operand) {
        // 此处的valueType表示要转换成的类型，operand表示被操作是Value
        super(name, valueType, parent, new ArrayList<>(Collections.singletonList(operand)));
    }

    public Value getOperand() {
        return super.getOperands().get(0);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.getName());
        sb.append(" = ");
        sb.append("trunc");
        sb.append(" ");
        sb.append(this.getOperand().getValueType().toString());
        sb.append(" ");
        sb.append(this.getOperand().getName());
        sb.append(" to ");
        sb.append(super.getValueType().toString());
        return sb.toString();
    }
}
