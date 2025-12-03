package llvm.values;

import llvm.types.PointerType;
import llvm.types.ValueType;
import llvm.values.constants.ConstArray;
import llvm.values.constants.ConstInt;
import llvm.values.constants.Constant;

public class GlobalVar extends User {
    // @g_arr = dso_local global [5 x i32] [i32 12, i32 0, i32 0, i32 0, i32 0]
    // @g_ndarray = dso_local constant [4 x i32] zeroinitializer
    // @g_a = dso_local global i32 10
    // @g.temp = internal global i32 9
    private final Boolean isConst;
    private final Boolean isStatic;
    private final Constant constInit;

    public GlobalVar(String name, ValueType valueType, boolean isConst, boolean isStatic, Constant constInit) {
        // 保证ValueType是指针类型，指针指向的内容与consInit的类型保持一致，例如i32，[5 x i32]
        super(name, valueType);
        this.isConst = isConst;
        this.isStatic = isStatic;
        this.constInit = constInit;
    }

    public boolean isConst() {
        return isConst;
    }

    public Constant getConstInit() {
        return constInit;
    }

    public boolean isZeroInitializer() {
        if (constInit instanceof ConstArray) {
            ConstArray array = (ConstArray) constInit;
            return array.isAllZero();
        } else {
            return false;
        }
    }

    public boolean isInteger() {
        return this.constInit instanceof ConstInt;
    }

    public boolean isArray() {
        return !isZeroInitializer() && !isInteger();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.getName());
        sb.append(" = ");
        if (isStatic) {
            sb.append("internal");
        } else {
            sb.append("dso_local");
        }
        sb.append(" ");
        // 如果是数组常量，则输出"constant"
        // 反之，输出"global"
        if (isConst) {
            sb.append("constant");
        } else {
            sb.append("global");
        }
        sb.append(" ");
        sb.append(((PointerType) super.getValueType()).getPointedType().toString());
        sb.append(" ");
        // 如果是赋值的是数组，并且数组是全0的，那么直接输出"zeroinitializer"
        // 反之，正常输出原始内容即可
        if (isZeroInitializer()) {
            sb.append("zeroinitializer");
        } else {
            sb.append(constInit.toString());
        }
        return sb.toString();
    }
}
