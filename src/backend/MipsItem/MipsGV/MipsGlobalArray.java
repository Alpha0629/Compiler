package backend.MipsItem.MipsGV;

import java.util.ArrayList;

public class MipsGlobalArray extends MipsGlobalVariable {
    private final String name;
    private final ArrayList<Integer> inits;

    public MipsGlobalArray(String name, ArrayList<Integer> inits) {
        this.name = name;
        this.inits = inits;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append(": .word ");
        for (int i = 0; i < inits.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(inits.get(i));
        }
        return sb.toString();
    }
}
