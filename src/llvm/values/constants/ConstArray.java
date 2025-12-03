package llvm.values.constants;

import llvm.types.ArrayType;

import java.util.ArrayList;

public class ConstArray extends Constant {
    private final ArrayList<Constant> constants;

    public ConstArray(ArrayType arrayType, ArrayList<Constant> constants) {
        // 这里的arrayType是数组类型，该类当中定义了数组元素的类型
        super(arrayType);
        this.constants = constants;
    }

    public ArrayList<Constant> getConstants() {
        return constants;
    }

    public Boolean isAllZero() {
        for (Constant c : constants) {
            if (c instanceof ConstInt) {
                if (((ConstInt) c).getVal() != 0) return false;
            } else {
                return false;
            }
        }
        return true;
    }

    public int getSize() {
        return constants.size();
    }

    @Override
    public String toString() {
        // [i32 12, i32 0, i32 0, i32 0, i32 0]
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < constants.size(); i++) {
            if (i >= 1) sb.append(", ");
            Constant constant = constants.get(i);
            sb.append(constant.getValueType().toString());
            sb.append(" ");
            if (constant instanceof ConstInt) {
                sb.append(constant.toString());
            }
        }
        sb.append(']');
        return sb.toString();
    }
}
