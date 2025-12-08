package backend.MipsItem;

import backend.instructions.MipsInstruction;
import llvm.values.instructions.Instruction;

import java.util.ArrayList;

public class MipsBlock {
    private final String blockName;
    private final ArrayList<MipsInstruction> instructions;
    private final ArrayList<Instruction> irInstructions;

    public MipsBlock(String blockName, ArrayList<MipsInstruction> instructions,ArrayList<Instruction> irInstructions) {
        this.blockName = blockName;
        this.instructions = instructions;
        this.irInstructions = irInstructions;
    }

    public String getBlockName() {
        return blockName;
    }

    public ArrayList<MipsInstruction> getInstructions() {
        return instructions;
    }

    public ArrayList<Instruction> getIrInstructions() {
        return irInstructions;
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
