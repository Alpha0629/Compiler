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

        MipsAlu mipsAlu0 = new MipsAlu(AluType.SUBU, Register.K0, Register.A3, Register.V0);
        MipsAlu mipsAlu1 = new MipsAlu(AluType.ADDIU, Register.T0, Register.A1, Immediate.Imm0);
        sb.append("\t" + mipsAlu0.toString());
        sb.append("\n");
        sb.append("\t" + mipsAlu1.toString());
        sb.append("\n");
        MipsBranch mipsBranch = new MipsBranch(BranchType.BEQ, Register.S1, Register.A1, new Label("b0"));
        sb.append("\t" + mipsBranch.toString());
        return sb.toString();
    }
}
