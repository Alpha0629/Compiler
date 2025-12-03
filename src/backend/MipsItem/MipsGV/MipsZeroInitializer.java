package backend.MipsItem.MipsGV;

public class MipsZeroInitializer extends MipsGlobalVariable {
    private final String name;
    private final int length;

    public MipsZeroInitializer(String name, int length) {
        this.name = name;
        this.length = length;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append(": .space ");
        sb.append(length * 4);
        return sb.toString();
    }
}
