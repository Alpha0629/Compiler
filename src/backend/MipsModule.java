package backend;

import backend.MipsItem.MipsFunction;
import backend.MipsItem.MipsGV.MipsGlobalArray;
import backend.MipsItem.MipsGV.MipsGlobalInteger;
import backend.MipsItem.MipsGV.MipsGlobalString;
import backend.MipsItem.MipsGV.MipsGlobalVariable;
import backend.MipsItem.MipsGV.MipsZeroInitializer;
import backend.instructions.alu.MipsAlu;
import backend.instructions.alu.AluType;
import backend.instructions.branch.BranchType;
import backend.instructions.branch.MipsBranch;
import backend.operand.Immediate;
import backend.operand.Label;
import backend.operand.Register;

import java.util.ArrayList;

public class MipsModule {
    private final ArrayList<MipsGlobalVariable> mipsGlobalVariables;
    private final ArrayList<MipsFunction> mipsFunctions;

    public MipsModule() {
        this.mipsGlobalVariables = new ArrayList<>();
        this.mipsFunctions = new ArrayList<>();
    }

    public void addMipsGlobalVariable(MipsGlobalVariable mipsGlobalVariable) {
        this.mipsGlobalVariables.add(mipsGlobalVariable);
    }

    public void addMipsFunction(MipsFunction mipsFunction) {
        this.mipsFunctions.add(mipsFunction);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("# Ljm 23371007 Mips\n");
        // 处理宏定义
        // getint
        sb.append(".macro getint\n");
        sb.append("\tli $v0, 5\n");
        sb.append("\tsyscall\n");
        sb.append(".end_macro\n");
        sb.append("\n");
        // putint
        sb.append(".macro putint\n");
        sb.append("\tli $v0, 1\n");
        sb.append("\tsyscall\n");
        sb.append(".end_macro\n");
        sb.append("\n");
        // putch
        sb.append(".macro putch\n");
        sb.append("\tli $v0, 11\n");
        sb.append("\tsyscall\n");
        sb.append(".end_macro\n");
        sb.append("\n");
        // putstr
        sb.append(".macro putstr\n");
        sb.append("\tli $v0, 4\n");
        sb.append("\tsyscall\n");
        sb.append(".end_macro\n");
        sb.append("\n");
        // 处理全局变量
        sb.append(".data\n");
        for (MipsGlobalVariable mipsGlobalVariable : mipsGlobalVariables) {
            if (mipsGlobalVariable instanceof MipsZeroInitializer) {
                // 先处理未初始化的全局数组
                sb.append(mipsGlobalVariable.toString());
                sb.append("\n");
            }
        }
        sb.append("\n\n");
        for (MipsGlobalVariable mipsGlobalVariable : mipsGlobalVariables) {
            if (mipsGlobalVariable instanceof MipsGlobalInteger || mipsGlobalVariable instanceof MipsGlobalArray) {
                // 然后处理int类型的整数变量和数组, 按顺序输出即可
                sb.append(mipsGlobalVariable.toString());
                sb.append("\n");
            }
        }
        sb.append("\n\n");
        for (MipsGlobalVariable mipsGlobalVariable : mipsGlobalVariables) {
            if (mipsGlobalVariable instanceof MipsGlobalString) {
                // 最后输出字符串常量
                sb.append(mipsGlobalVariable.toString());
                sb.append("\n");
            }
        }
        sb.append("\n\n");
        sb.append(".text\n");
        for (MipsFunction mipsFunction : mipsFunctions) {
            sb.append(mipsFunction.toString());
            sb.append("\n");
        }
        return sb.toString();
    }
}
