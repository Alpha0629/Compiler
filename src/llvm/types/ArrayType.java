package llvm.types;

/**
 * {@code @Description} 数组变量
 */
public class ArrayType extends ValueType {
    private final IntType elementType;
    private final int length;
    private final int bytes;

    public ArrayType(IntType elementType, int length) {
        this.elementType = elementType;
        this.length = length;
        this.bytes = length << 3;
    }

    @Override
    public int getLength() {
        return this.length;
    }

    @Override
    public int getBytes() {
        return this.bytes;
    }

    @Override
    public String toString() {
        return "[" + this.length + " x " + this.elementType.toString() + "]";
    }
}
