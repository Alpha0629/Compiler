package llvm.values;

import llvm.types.ValueType;
import llvm.values.constants.ConstString;

public class GlobalString extends User {
    // @str.0 = constant [13 x i8] c"Hello World!\00"
    private final ConstString constString;

    public GlobalString(String name, ValueType valueType, ConstString constString) {
        // 保证ValueType是指针类型，指针指向的是数组类型，数组内的元素类型是i8
        super("@str." + name, valueType);
        this.constString = constString;
    }

    public ConstString getConstString() {
        return constString;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.getName());
        sb.append(" = ");
        sb.append("constant");
        sb.append(" ");
        // constString.getValueType()一定是ArrayType
        sb.append(constString.getValueType().toString());
        sb.append(" ");
        sb.append('c');
        sb.append('\"');
        sb.append(constString);
        sb.append("\\00");
        sb.append('\"');
        return sb.toString();
    }
}
