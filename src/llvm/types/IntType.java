package llvm.types;

public class IntType extends ValueType {
    private final int bits;
    private final int bytes;

    public IntType(int bits) {
        this.bits = bits;
        this.bytes = bits >> 3;
    }

    @Override
    public int getBytes() {
        return 4;
    }

    @Override
    public int getBits() {
        return this.bits;
    }

    @Override
    public String toString() {
        return "i" + this.bits;
    }
}
