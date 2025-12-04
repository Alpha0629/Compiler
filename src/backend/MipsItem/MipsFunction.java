package backend.MipsItem;

import java.util.ArrayList;

public class MipsFunction {
    private final String funcName;
    private final Boolean isDeclare;
    private final ArrayList<MipsBlock> blocks;

    public MipsFunction(String funcName, Boolean isDeclare) {
        this.funcName = funcName;
        this.isDeclare = isDeclare;
        this.blocks = new ArrayList<>();
    }

    public String getFuncName() {
        return funcName;
    }

    public Boolean isDeclare() {
        return isDeclare;
    }

    public ArrayList<MipsBlock> getBlocks() {
        return blocks;
    }

    public void addBlock(MipsBlock block) {
        blocks.add(block);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(funcName);
        sb.append(": \n");
        for (MipsBlock block : blocks) {
            sb.append(block.toString());
            sb.append("\n");
        }
        return sb.toString();
    }
}
