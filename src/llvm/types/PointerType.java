package llvm.types;

/**
 * {@code @Description} 指针变量
 */
public class PointerType extends ValueType {
    private final ValueType pointer;    // 指针指向的类型

    public PointerType(IntType pointer) {
        this.pointer = pointer;
    }

//    public PointerType(ValueType pointer) {
//        this.pointer = pointer;
//    }

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
        return pointer.toString() + "*";
    }
}
