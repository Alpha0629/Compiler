package backend.MipsItem;

import backend.instructions.MipsInstruction;

import java.util.ArrayList;

public class MipsBlock {
    private final String blockName;
    private final ArrayList<MipsInstruction> instructions;

    public MipsBlock(String blockName) {
        this.blockName = blockName;
        this.instructions = new ArrayList<>();
    }

    public String getBlockName() {
        return blockName;
    }

    public ArrayList<MipsInstruction> getInstructions() {
        return instructions;
    }

    public void addInstructionToTail(MipsInstruction instruction) {
        instructions.add(instruction);
    }

    public void addInstructionToHead(MipsInstruction instruction) {
        instructions.add(0, instruction);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(blockName);
        sb.append(": \n");
        for (MipsInstruction instruction : instructions) {
            sb.append("\t");
            sb.append(instruction.toString());
            sb.append("\n");
        }
        return sb.toString();
    }
}
