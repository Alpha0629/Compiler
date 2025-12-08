package backend.operand;

public record Label(String label) implements Operand {

    @Override
    public String toString() {
        return label;
    }
}
