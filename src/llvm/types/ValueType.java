package llvm.types;

public abstract class ValueType {
    public int getBits() {
        return 0;
    }

    public int getLength() {
        return 0;
    }

    public int getBytes() {
        return 0;
    }

    @Override
    public String toString() {
        return "";
    }
}
