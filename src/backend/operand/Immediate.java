package backend.operand;

public record Immediate(int value) implements Operand {

    public final static Immediate Imm0 = new Immediate(0);
    public final static Immediate Imm1 = new Immediate(1);

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
