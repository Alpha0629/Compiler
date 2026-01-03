package optimization;

import backend.MipsModule;
import llvm.IrModule;
import llvm.values.BasicBlock;
import llvm.values.Function;
import llvm.values.instructions.Branch;
import llvm.values.instructions.Instruction;
import llvm.values.instructions.Ret;

import java.util.ArrayList;
import java.util.Iterator;

public class DeadCodeDelete {
    private final IrModule irModule;
    // private final MipsModule mipsModule;

    public DeadCodeDelete(IrModule irModule, MipsModule mipsModule) {
        this.irModule = irModule;
        // this.mipsModule = mipsModule;
    }

    public void pass() {
        ArrayList<Function> functions = irModule.getFunctions();
        for (Function function : functions) {
            ArrayList<BasicBlock> basicBlocks = function.getBlocks();
            for (BasicBlock basicBlock : basicBlocks) {
                ArrayList<Instruction> instructions = basicBlock.getInstructions();
                boolean dead = false;
                Iterator<Instruction> iter = instructions.iterator();
                while (iter.hasNext()) {
                    Instruction instruction = iter.next();
                    if (dead) {
                        // 这个指令是无用的
                        // 先要删除这个指令所使用的其他Value指向的User（自己）
                        instruction.deleteAllUsageRelationship();
                        iter.remove();  // 使用迭代器安全删除
                        continue;
                    }
                    if (instruction instanceof Branch || instruction instanceof Ret) {
                        dead = true;
                    }
                }
            }
        }
    }
}
