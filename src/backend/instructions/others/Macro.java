package backend.instructions.others;

import backend.instructions.MipsInstruction;

public class Macro extends MipsInstruction {
    private final String macroName;

    public Macro(String macroName) {
        this.macroName = macroName;
    }

    @Override
    public String toString() {
        return macroName;
    }
}
