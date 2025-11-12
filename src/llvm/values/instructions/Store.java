package llvm.values.instructions;

import llvm.types.ValueType;
import llvm.values.Value;

import java.util.ArrayList;
import java.util.Arrays;

// 将42这个值存储到ptr指向的地址
// store i32 42, i32* %ptr
public class Store extends Instruction {
    public Store(ValueType valueType, Value parent, Value storedValue, Value pointer) {
        // valueType必须是void
        super(valueType, parent, new ArrayList<>(Arrays.asList(storedValue, pointer)));
    }

    public Value getStoredValue() {
        return super.getOperands().get(0);
    }

    public Value getPointer() {
        return super.getOperands().get(1);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("store");
        sb.append(" ");
        sb.append(this.getStoredValue().getValueType().toString());
        sb.append(" ");
        sb.append(this.getStoredValue().getName());
        sb.append(", ");
        sb.append(this.getPointer().getValueType().toString());
        sb.append(" ");
        sb.append(this.getPointer().getName());
        return sb.toString();
    }
}
