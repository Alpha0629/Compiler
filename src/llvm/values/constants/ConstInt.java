package llvm.values.constants;

import llvm.types.IntType;
import llvm.types.ValueType;

public class ConstInt extends Constant {
    private int val;

    public ConstInt(IntType intType, int val) {
        // intType类型，可能是i32，也可能是i8等
        super(intType);
        this.val = val;
    }

    public int getVal() {
        return val;
    }

    @Override
    public String toString() {
        return String.valueOf(val);
    }
}
