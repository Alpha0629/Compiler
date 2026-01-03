package optimization;

import backend.MipsModule;
import llvm.IrModule;
import llvm.values.BasicBlock;
import llvm.values.Function;
import llvm.values.instructions.Instruction;
import llvm.values.instructions.Branch;

import java.util.ArrayList;
import java.util.HashMap;

public class PeepHole {
    private final IrModule irModule;
    private final MipsModule mipsModule;

    public PeepHole(IrModule irModule, MipsModule mipsModule) {
        this.irModule = irModule;
        this.mipsModule = mipsModule;
    }

    public void pass() {
        ArrayList<Function> functions = irModule.getFunctions();
        for (Function function : functions) {
            deleteUselessBranch(function);
        }
    }

    public void deleteUselessBranch(Function function) {
        ArrayList<BasicBlock> basicBlocks = function.getBlocks();
        for (int i = 0; i < basicBlocks.size() - 1; i++) {
            BasicBlock curBlock = basicBlocks.get(i);
            BasicBlock nextBlock = basicBlocks.get(i + 1);
            Instruction lastInstruction = curBlock.getLastInstruction();
            if (lastInstruction instanceof Branch branch) {
                if (branch.getDest() == nextBlock) {
                    curBlock.getInstructions().remove(lastInstruction);
                }
            }
        }
    }
}
