package backend.operand;

public class Immediate implements Operand {
    private final int value;

    public Immediate(int value) {
        this.value = value;
    }

    public final static Immediate Imm0 = new Immediate(0);
    public final static Immediate Imm1 = new Immediate(1);

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
