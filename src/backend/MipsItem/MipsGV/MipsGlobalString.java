package backend.MipsItem.MipsGV;

public class MipsGlobalString extends MipsGlobalVariable {
    private final String name;
    private final String content;

    public MipsGlobalString(String name, String content) {
        this.name = name;
        this.content = content;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append(": .asciiz ");
        sb.append('\"');
        sb.append(content);
        sb.append('\"');
        return sb.toString();
    }
}
