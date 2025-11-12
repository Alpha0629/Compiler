package llvm.values.instructions;

import llvm.types.PointerType;
import llvm.types.ValueType;
import llvm.values.Value;

import java.util.ArrayList;
import java.util.Arrays;

public class Gep extends Instruction {
    // <result> = getelementptr <ty>, ptr <ptrval>{, <ty> <idx>}*
    // %v26 = getelementptr inbounds [15 x i8], [15 x i8]* @str.1, i32 0, i32 0
    // %v3 = getelementptr inbounds i32, i32* %v2, i32 1
    private final ValueType elementType;    // 指针所指向的元素的类型，例如[15 x i8]，i32

    // 传入的valueType的表示的是result的类型
    public Gep(String name, ValueType valueType, Value parent, Value pointer, Value left, Value right) {
        // (ArrayIrTy)(((PointerType) (pointer.getValueType())).getPointedType());
        // elementType是指向的元素类型，在此处是一个数组，然后需要获取数组的元素类型，即i8，然后再次转换成i8*
        super(name, valueType, parent, new ArrayList<>(Arrays.asList(pointer, left, right)));
        // pointer一定是一个指针类型，现在要获取指针所指向的类型
        this.elementType = ((PointerType)(pointer.getValueType())).getPointedType();
    }

    // 传入的valueType的表示的是result的类型
    public Gep(String name, ValueType valueType, Value parent, Value pointer, Value index) {
        // 这里的valueType代表指针类型是：i32*还是i8*, 也就是result的类型
        super(name, valueType, parent, new ArrayList<>(Arrays.asList(pointer, index)));
        // pointer一定是一个指针类型，现在要获取指针所指向的类型
        this.elementType = ((PointerType)(pointer.getValueType())).getPointedType();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.getName());
        sb.append(" = ");
        sb.append("getelementptr");
        sb.append(" ");
        sb.append(this.elementType.toString());
        sb.append(", ");
        for (int i = 0; i < super.getOperands().size(); i++) {
            if (i >= 1) sb.append(", ");
            sb.append(super.getOperands().get(i).getValueType().toString());
            sb.append(" ");
            sb.append(super.getOperands().get(i).getName());
        }
        return sb.toString();
    }
}
