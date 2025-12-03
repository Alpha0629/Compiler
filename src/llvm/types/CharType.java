package llvm.types;

public class CharType extends ValueType {
    private final int bits;
    private final int bytes;

    public CharType(int bits) {
        this.bits = bits;
        this.bytes = bits >> 3;
    }

    @Override
    public int getBytes() {
        return this.bytes;
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
