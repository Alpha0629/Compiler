package backend.MipsItem.MipsGV;

public class MipsGlobalInteger extends MipsGlobalVariable {
    // int a = 1;
    // int b;
    private final String name;
    private final int value;

    public MipsGlobalInteger(String name, int value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append(": .word ");
        sb.append(value);
        return sb.toString();
    }
}
