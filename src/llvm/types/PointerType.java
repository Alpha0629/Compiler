package llvm.types;

/**
 * {@code @Description} 指针变量
 */
public class PointerType extends ValueType {
    private final ValueType pointedType;    // 指针指向的类型

    public PointerType(IntType pointedType) {
        this.pointedType = pointedType;
    }

//    public PointerType(ValueType pointedType) {
//        this.pointedType = pointedType;
//    }

    public ValueType getPointedType() {
        return this.pointedType;
    }

    @Override
    public int getBytes() {
        return 4;
    }

    @Override
    public int getBits() {
        return 32;
    }

    @Override
    public String toString() {
        return pointedType.toString() + "*";
    }
}
