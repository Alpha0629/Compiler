package backend;

import backend.MipsItem.MipsBlock;
import backend.MipsItem.MipsFunction;
import backend.instructions.alu.AluType;
import backend.instructions.branch.BranchType;
import backend.instructions.comare.CompareType;
import backend.instructions.hilo.HiLoType;
import backend.instructions.jump.JumpType;
import backend.instructions.pseudo.PseudoType;
import backend.operand.Register;
import llvm.IrModule;
import llvm.types.PointerType;
import llvm.types.ValueType;
import llvm.types.VoidType;
import llvm.values.BasicBlock;
import llvm.values.Function;
import llvm.values.GlobalString;
import llvm.values.GlobalVar;
import llvm.values.Value;
import llvm.values.constants.ConstInt;
import llvm.values.instructions.Add;
import llvm.values.instructions.Alloca;
import llvm.values.instructions.Branch;
import llvm.values.instructions.Call;
import llvm.values.instructions.Copy;
import llvm.values.instructions.Gep;
import llvm.values.instructions.Icmp;
import llvm.values.instructions.Icmp.Cmp;
import llvm.values.instructions.Instruction;
import llvm.values.instructions.Load;
import llvm.values.instructions.Mul;
import llvm.values.instructions.Ret;
import llvm.values.instructions.Sdiv;
import llvm.values.instructions.Srem;
import llvm.values.instructions.Store;
import llvm.values.instructions.Sub;
import llvm.values.instructions.Zext;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.stream.Collectors;

public class MipsMaker {
    private final MipsModule mipsModule;
    private final IrModule irModule;
    private final String outputPath;
    private final MipsUtils mipsUtils;
    public static MipsFunction curFunction;
    public static MipsBlock curBlock;
    private int curAddress;

    public MipsMaker(MipsModule mipsModule, IrModule irModule, String outputPath) {
        this.mipsModule = mipsModule;
        this.irModule = irModule;
        this.outputPath = outputPath;
        this.mipsUtils = new MipsUtils(mipsModule);
        this.curAddress = 0x7fffeffc;
    }

    public void buildMips() {
        ArrayList<GlobalString> globalStrings = irModule.getGlobalStrings();
        for (GlobalString globalString : globalStrings) {
            buildGlobalStringMips(globalString);
        }
        ArrayList<GlobalVar> globalVars = irModule.getGlobalVars();
        for (GlobalVar globalVar : globalVars) {
            buildGlobalVarMips(globalVar);
        }
        ArrayList<Function> functions = irModule.getFunctions();
        Function mainFunction = functions.get(functions.size() - 1);    // 先拿到主函数
        buildMipsMainFunction(mainFunction);
        int i = 0;
        for (Function function : functions) {
            if (i < 4) {
                i++;
                continue;
            }
            if (i == functions.size() - 1) break;
            buildMipsFunction(function);
            i++;
        }
    }

    public void buildMipsMainFunction(Function mainFunction) {
        curFunction = new MipsFunction(mainFunction.getName().substring(1), mainFunction, new ArrayList<>(), 0x7fffeffc);
        mipsUtils.allocateLeftValues(curFunction);
        mipsModule.addMipsFunction(curFunction);
        for (BasicBlock basicBlock : mainFunction.getBlocks()) {
            buildBasicBlock(basicBlock);
        }
    }

    public void buildBasicBlock(BasicBlock basicBlock) {
        curBlock = new MipsBlock(basicBlock.getName().substring(1), new ArrayList<>(), basicBlock.getInstructions());
        curFunction.addBlock(curBlock);
        for (Instruction instruction : basicBlock.getInstructions()) {
            buildMipsInstruction(instruction);
        }
    }

    public void buildMipsInstruction(Instruction instruction) {
        mipsUtils.makeAnnotation(instruction);
        if (instruction instanceof Add) {
            mapAdd2Mips((Add) instruction);
        } else if (instruction instanceof Sub) {
            mapSub2Mips((Sub) instruction);
        } else if (instruction instanceof Mul) {
            mapMul2Mips((Mul) instruction);
        } else if (instruction instanceof Sdiv) {
            mapSdiv2Mips((Sdiv) instruction);
        } else if (instruction instanceof Srem) {
            mapSrem2Mips((Srem) instruction);
        } else if (instruction instanceof Alloca) {
            mapAlloca2Mips((Alloca) instruction);
        } else if (instruction instanceof Branch) {
            mapBranch2Mips((Branch) instruction);
        } else if (instruction instanceof Call) {
            mapCall2Mips((Call) instruction);
        } else if (instruction instanceof Gep) {
            mapGep2Mips((Gep) instruction);
        } else if (instruction instanceof Icmp) {
            mapIcmp2Mips((Icmp) instruction);
        } else if (instruction instanceof Load) {
            mapLoad2Mips((Load) instruction);
        } else if (instruction instanceof Ret) {
            mapRet2Mips((Ret) instruction);
        } else if (instruction instanceof Store) {
            mapStore2Mips((Store) instruction);
        } else if (instruction instanceof Zext) {
            mapZext2Mips((Zext) instruction);
        } else if (instruction instanceof Copy) {
            mapCopy2Mips((Copy) instruction);
        } else {
            System.out.println("Unknown instruction: " + instruction);
        }
    }

    public void mapCopy2Mips(Copy copy) {
        Value phiNode = copy.getPhiNode();
        Value value = copy.getValue();
        // 先处理value
        Register reg = mipsUtils.getFreeRegister();
        if (value instanceof ConstInt) {
            int val = ((ConstInt) value).getVal();
            mipsUtils.makePseudo(PseudoType.LI, reg, val);
        } else if (mipsUtils.getValue2Reg().containsKey(value)) {
            mipsUtils.makeAlu(AluType.ADDIU, reg, mipsUtils.getValue2Reg().get(value), 0);
        } else {
            System.out.println(copy.toString());
            // 从栈上加载
            int offset = mipsUtils.getValue2Offset().get(value);   // 要加载到的栈地址相对于sp的偏移
            mipsUtils.makeLoad(reg, -offset, Register.SP);
        }
        // 把拿出来的值存放都phiNode对应的栈空间上
        int offset = mipsUtils.getValue2Offset().get(phiNode);
        mipsUtils.makeStore(reg, -offset, Register.SP);
        mipsUtils.freeRegister(reg);
    }

    public void mapAdd2Mips(Add add) {
        // 左操作数是常数 or Value
        // 右操作数是常数 or Value
        Value leftOp = add.getLeftOperand();
        Value rightOp = add.getRightOperand();

        if (leftOp instanceof ConstInt && rightOp instanceof ConstInt) {
            // 左右都是常数, 常数先相加然后用li, 最后给add对应的栈空间赋值即可
            int total = ((ConstInt) leftOp).getVal() + ((ConstInt) rightOp).getVal();
            Register reg = mipsUtils.getFreeRegister();
            mipsUtils.makePseudo(PseudoType.LI, reg, total);
            int offset = mipsUtils.getValue2Offset().get(add);
            mipsUtils.makeStore(reg, -offset, Register.SP);
            // 存储完了, 现在把这里用过的寄存器释放掉
            mipsUtils.freeRegister(reg);
        } else if (leftOp instanceof ConstInt) {
            // 左边的是常数, 右边不是
            int offset = mipsUtils.getValue2Offset().get(rightOp);
            Register right = mipsUtils.getFreeRegister();
            mipsUtils.makeLoad(right, -offset, Register.SP);
            Register dst = mipsUtils.getFreeRegister();
            mipsUtils.makeAlu(AluType.ADDIU, dst, right, ((ConstInt) leftOp).getVal());
            offset = mipsUtils.getValue2Offset().get(add);
            mipsUtils.makeStore(dst, -offset, Register.SP);
            mipsUtils.freeRegister(right, dst);
        } else if (rightOp instanceof ConstInt) {
            // 右边的是常数, 左边不是
            int offset = mipsUtils.getValue2Offset().get(leftOp);   // 要加载到的栈地址相对于sp的偏移
            Register left = mipsUtils.getFreeRegister();
            mipsUtils.makeLoad(left, -offset, Register.SP);
            Register dst = mipsUtils.getFreeRegister();
            mipsUtils.makeAlu(AluType.ADDIU, dst, left, ((ConstInt) rightOp).getVal());
            offset = mipsUtils.getValue2Offset().get(add);
            mipsUtils.makeStore(dst, -offset, Register.SP);
            mipsUtils.freeRegister(left, dst);
        } else {
            int offset = mipsUtils.getValue2Offset().get(leftOp);   // 要加载到的栈地址相对于sp的偏移
            Register left = mipsUtils.getFreeRegister();
            mipsUtils.makeLoad(left, -offset, Register.SP);
            offset = mipsUtils.getValue2Offset().get(rightOp);
            Register right = mipsUtils.getFreeRegister();
            mipsUtils.makeLoad(right, -offset, Register.SP);
            Register dst = mipsUtils.getFreeRegister();
            // System.out.println(dst);
            mipsUtils.makeAlu(AluType.ADDU, dst, left, right);
            offset = mipsUtils.getValue2Offset().get(add);
            mipsUtils.makeStore(dst, -offset, Register.SP);
            mipsUtils.freeRegister(left, right, dst);
        }
    }

    public void mapSub2Mips(Sub sub) {
        Value leftOp = sub.getLeftOperand();
        Value rightOp = sub.getRightOperand();

        if (leftOp instanceof ConstInt && rightOp instanceof ConstInt) {
            // 左右都是常数, 常数先相加然后用li, 最后给add对应的栈空间赋值即可
            int total = ((ConstInt) leftOp).getVal() - ((ConstInt) rightOp).getVal();
            Register reg = mipsUtils.getFreeRegister();
            mipsUtils.makePseudo(PseudoType.LI, reg, total);
            int offset = mipsUtils.getValue2Offset().get(sub);
            mipsUtils.makeStore(reg, -offset, Register.SP);
            // 存储完了, 现在把这里用过的寄存器释放掉
            mipsUtils.freeRegister(reg);
        } else if (leftOp instanceof ConstInt) {
            // 左边的是常数, 右边不是
            Register left = mipsUtils.getFreeRegister();
            mipsUtils.makePseudo(PseudoType.LI, left, ((ConstInt) leftOp).getVal());
            int offset = mipsUtils.getValue2Offset().get(rightOp);
            Register right = mipsUtils.getFreeRegister();
            mipsUtils.makeLoad(right, -offset, Register.SP);
            Register dst = mipsUtils.getFreeRegister();
            mipsUtils.makeAlu(AluType.SUBU, dst, left, right);
            offset = mipsUtils.getValue2Offset().get(sub);
            mipsUtils.makeStore(dst, -offset, Register.SP);
            mipsUtils.freeRegister(left, right, dst);
        } else if (rightOp instanceof ConstInt) {
            // 右边是一个常数
            // 要计算 $t0 = $t1 - 10
            // addiu $t0, $t1, -10
            // $t0 = $t1 + (-10) = $t1 - 10
            int offset = mipsUtils.getValue2Offset().get(leftOp);   // 要加载到的栈地址相对于sp的偏移
            Register left = mipsUtils.getFreeRegister();
            mipsUtils.makeLoad(left, -offset, Register.SP);
            Register dst = mipsUtils.getFreeRegister();
            mipsUtils.makeAlu(AluType.ADDIU, dst, left, -(((ConstInt) rightOp).getVal()));
            offset = mipsUtils.getValue2Offset().get(sub);
            mipsUtils.makeStore(dst, -offset, Register.SP);
            mipsUtils.freeRegister(left, dst);
        } else {
            int offset = mipsUtils.getValue2Offset().get(leftOp);   // 要加载到的栈地址相对于sp的偏移
            Register left = mipsUtils.getFreeRegister();
            mipsUtils.makeLoad(left, -offset, Register.SP);
            offset = mipsUtils.getValue2Offset().get(rightOp);
            Register right = mipsUtils.getFreeRegister();
            mipsUtils.makeLoad(right, -offset, Register.SP);
            Register dst = mipsUtils.getFreeRegister();
            mipsUtils.makeAlu(AluType.SUBU, dst, left, right);
            offset = mipsUtils.getValue2Offset().get(sub);
            mipsUtils.makeStore(dst, -offset, Register.SP);
            mipsUtils.freeRegister(left, right, dst);
        }
    }

    public void mapMul2Mips(Mul mul) {
        Value leftOp = mul.getLeftOperand();
        Value rightOp = mul.getRightOperand();

        if (leftOp instanceof ConstInt && rightOp instanceof ConstInt) {
            // 左右都是常数, 常数先相加然后用li, 最后给add对应的栈空间赋值即可
            int total = ((ConstInt) leftOp).getVal() * ((ConstInt) rightOp).getVal();
            Register reg = mipsUtils.getFreeRegister();
            mipsUtils.makePseudo(PseudoType.LI, reg, total);
            int offset = mipsUtils.getValue2Offset().get(mul);
            mipsUtils.makeStore(reg, -offset, Register.SP);
            // 存储完了, 现在把这里用过的寄存器释放掉
            mipsUtils.freeRegister(reg);
        } else if (leftOp instanceof ConstInt) {
            // 左边的是常数, 右边不是
            Register left = mipsUtils.getFreeRegister();
            mipsUtils.makePseudo(PseudoType.LI, left, ((ConstInt) leftOp).getVal());
            int offset = mipsUtils.getValue2Offset().get(rightOp);
            Register right = mipsUtils.getFreeRegister();
            mipsUtils.makeLoad(right, -offset, Register.SP);
            Register dst = mipsUtils.getFreeRegister();
            mipsUtils.makeAlu(AluType.MUL, dst, left, right);
            offset = mipsUtils.getValue2Offset().get(mul);
            mipsUtils.makeStore(dst, -offset, Register.SP);
            mipsUtils.freeRegister(left, right, dst);
        } else if (rightOp instanceof ConstInt) {
            int offset = mipsUtils.getValue2Offset().get(leftOp);
            Register left = mipsUtils.getFreeRegister();
            mipsUtils.makeLoad(left, -offset, Register.SP);
            Register right = mipsUtils.getFreeRegister();
            mipsUtils.makePseudo(PseudoType.LI, right, ((ConstInt) rightOp).getVal());
            Register dst = mipsUtils.getFreeRegister();
            mipsUtils.makeAlu(AluType.MUL, dst, left, right);
            offset = mipsUtils.getValue2Offset().get(mul);
            mipsUtils.makeStore(dst, -offset, Register.SP);
            mipsUtils.freeRegister(left, right, dst);

        } else {
            int offset = mipsUtils.getValue2Offset().get(leftOp);   // 要加载到的栈地址相对于sp的偏移
            Register left = mipsUtils.getFreeRegister();
            mipsUtils.makeLoad(left, -offset, Register.SP);
            offset = mipsUtils.getValue2Offset().get(rightOp);
            Register right = mipsUtils.getFreeRegister();
            // System.out.println(left + " " + right);
            // System.out.println(left == right);
            mipsUtils.makeLoad(right, -offset, Register.SP);
            Register dst = mipsUtils.getFreeRegister();
            mipsUtils.makeAlu(AluType.MUL, dst, left, right);
            offset = mipsUtils.getValue2Offset().get(mul);
            mipsUtils.makeStore(dst, -offset, Register.SP);
            mipsUtils.freeRegister(left, right, dst);
        }
    }

    public void mapSdiv2Mips(Sdiv sdiv) {
        Value leftOp = sdiv.getLeftOperand();
        Value rightOp = sdiv.getRightOperand();

        if (leftOp instanceof ConstInt && rightOp instanceof ConstInt) {
            // 左右都是常数, 常数先相加然后用li, 最后给add对应的栈空间赋值即可
            int total = ((ConstInt) leftOp).getVal() / ((ConstInt) rightOp).getVal();
            Register reg = mipsUtils.getFreeRegister();
            mipsUtils.makePseudo(PseudoType.LI, reg, total);
            int offset = mipsUtils.getValue2Offset().get(sdiv);
            mipsUtils.makeStore(reg, -offset, Register.SP);
            // 存储完了, 现在把这里用过的寄存器释放掉
            mipsUtils.freeRegister(reg);
        } else if (leftOp instanceof ConstInt) {
            // 左边的是常数, 右边不是
            Register left = mipsUtils.getFreeRegister();
            mipsUtils.makePseudo(PseudoType.LI, left, ((ConstInt) leftOp).getVal());
            int offset = mipsUtils.getValue2Offset().get(rightOp);
            Register right = mipsUtils.getFreeRegister();
            mipsUtils.makeLoad(right, -offset, Register.SP);
            Register dst = mipsUtils.getFreeRegister();
            mipsUtils.makeAlu(AluType.DIV, dst, left, right);
            offset = mipsUtils.getValue2Offset().get(sdiv);
            mipsUtils.makeStore(dst, -offset, Register.SP);
            mipsUtils.freeRegister(left, right, dst);
        } else if (rightOp instanceof ConstInt) {
            int offset = mipsUtils.getValue2Offset().get(leftOp);
            Register left = mipsUtils.getFreeRegister();
            mipsUtils.makeLoad(left, -offset, Register.SP);
            Register right = mipsUtils.getFreeRegister();
            mipsUtils.makePseudo(PseudoType.LI, right, ((ConstInt) rightOp).getVal());
            Register dst = mipsUtils.getFreeRegister();
            mipsUtils.makeAlu(AluType.DIV, dst, left, right);
            offset = mipsUtils.getValue2Offset().get(sdiv);
            mipsUtils.makeStore(dst, -offset, Register.SP);
            mipsUtils.freeRegister(left, right, dst);

        } else {
            int offset = mipsUtils.getValue2Offset().get(leftOp);   // 要加载到的栈地址相对于sp的偏移
            Register left = mipsUtils.getFreeRegister();
            mipsUtils.makeLoad(left, -offset, Register.SP);
            offset = mipsUtils.getValue2Offset().get(rightOp);
            Register right = mipsUtils.getFreeRegister();
            mipsUtils.makeLoad(right, -offset, Register.SP);
            Register dst = mipsUtils.getFreeRegister();
            mipsUtils.makeAlu(AluType.DIV, dst, left, right);
            offset = mipsUtils.getValue2Offset().get(sdiv);
            mipsUtils.makeStore(dst, -offset, Register.SP);
            mipsUtils.freeRegister(left, right, dst);
        }
    }

    public void mapSrem2Mips(Srem srem) {
        Value leftOp = srem.getLeftOperand();
        Value rightOp = srem.getRightOperand();

        if (leftOp instanceof ConstInt && rightOp instanceof ConstInt) {
            // 左右都是常数, 常数先相加然后用li, 最后给add对应的栈空间赋值即可
            int total = ((ConstInt) leftOp).getVal() % ((ConstInt) rightOp).getVal();
            Register reg = mipsUtils.getFreeRegister();
            mipsUtils.makePseudo(PseudoType.LI, reg, total);
            int offset = mipsUtils.getValue2Offset().get(srem);
            mipsUtils.makeStore(reg, -offset, Register.SP);
            // 存储完了, 现在把这里用过的寄存器释放掉
            mipsUtils.freeRegister(reg);
        } else if (leftOp instanceof ConstInt) {
            // 左边的是常数, 右边不是
            Register left = mipsUtils.getFreeRegister();
            mipsUtils.makePseudo(PseudoType.LI, left, ((ConstInt) leftOp).getVal());
            int offset = mipsUtils.getValue2Offset().get(rightOp);
            Register right = mipsUtils.getFreeRegister();
            mipsUtils.makeLoad(right, -offset, Register.SP);
            mipsUtils.makeAlu(AluType.DIV, null, left, right);
            Register dst = mipsUtils.getFreeRegister();
            mipsUtils.makeHiLo(HiLoType.MFHI, dst);
            offset = mipsUtils.getValue2Offset().get(srem);
            mipsUtils.makeStore(dst, -offset, Register.SP);
            mipsUtils.freeRegister(left, right, dst);
        } else if (rightOp instanceof ConstInt) {
            int offset = mipsUtils.getValue2Offset().get(leftOp);
            Register left = mipsUtils.getFreeRegister();
            mipsUtils.makeLoad(left, -offset, Register.SP);
            Register right = mipsUtils.getFreeRegister();
            mipsUtils.makePseudo(PseudoType.LI, right, ((ConstInt) rightOp).getVal());
            mipsUtils.makeAlu(AluType.DIV, null, left, right);
            Register dst = mipsUtils.getFreeRegister();
            mipsUtils.makeHiLo(HiLoType.MFHI, dst);
            offset = mipsUtils.getValue2Offset().get(srem);
            mipsUtils.makeStore(dst, -offset, Register.SP);
            mipsUtils.freeRegister(left, right, dst);

        } else {
            int offset = mipsUtils.getValue2Offset().get(leftOp);   // 要加载到的栈地址相对于sp的偏移
            Register left = mipsUtils.getFreeRegister();
            mipsUtils.makeLoad(left, -offset, Register.SP);
            offset = mipsUtils.getValue2Offset().get(rightOp);
            Register right = mipsUtils.getFreeRegister();
            mipsUtils.makeLoad(right, -offset, Register.SP);
            mipsUtils.makeAlu(AluType.DIV, null, left, right);
            Register dst = mipsUtils.getFreeRegister();
            mipsUtils.makeHiLo(HiLoType.MFHI, dst);
            offset = mipsUtils.getValue2Offset().get(srem);
            mipsUtils.makeStore(dst, -offset, Register.SP);
            mipsUtils.freeRegister(left, right, dst);
        }
    }

    public void mapAlloca2Mips(Alloca alloca) {
        ValueType valueType = ((PointerType) alloca.getValueType()).getPointedType();
        int bytes = valueType.getBytes();
        // 只能在栈上
        // %v1 = alloca i32
        // addiu $k0, $sp, -16  (对象的地址)
        // sw $k0, -4($sp)      (把对象的地址赋值给指向它的指针%v1)
        // 现在%v1已经分配了对应的位置, 需要再给其指向的对象分配空间
        mipsUtils.allocateObject(bytes);
        Register reg = mipsUtils.getFreeRegister();
        mipsUtils.makeAlu(AluType.ADDIU, reg, Register.SP, -(curFunction.getCurOffset()));
        int offset = mipsUtils.getValue2Offset().get(alloca);
        mipsUtils.makeStore(reg, -offset, Register.SP);
        mipsUtils.freeRegister(reg);
    }

    public void mapBranch2Mips(Branch branch) {
        if (!branch.isConditionalJump()) {
            // 无条件跳转
            BasicBlock destBlock = branch.getDest();
            String blockName = destBlock.getName().substring(1);
            mipsUtils.makeJump(JumpType.J, blockName);
        } else {
            // 有条件跳转
            // 如果cond != 0, 则跳转
            Value cond = branch.getCond();
            // # br i1 %v5, label %b1, label %b2
            // lb $k0, -12($sp)
            // beq $k0, $zero, b2
            // j b1
            Register condReg = mipsUtils.getFreeRegister();
            int offset = mipsUtils.getValue2Offset().get(cond);
            mipsUtils.makeLoad(condReg, -offset, Register.SP);
            String trueBlockName = branch.getTrueBlock().getName().substring(1);
            mipsUtils.makeBranch(BranchType.BNE, condReg, Register.ZERO, trueBlockName); // 如果不等于则跳到trueBlock
            // 否则无条件跳转到flaseBlcok
            String falseBlockName = branch.getFalseBlock().getName().substring(1);
            mipsUtils.makeJump(JumpType.J, falseBlockName);
            mipsUtils.freeRegister(condReg);
        }
    }

    public void mapCall2Mips(Call call) {
        Function subFunction = call.getFunction();
        String funcName = subFunction.getName().substring(1);
        ArrayList<Value> args = call.getArguments();
        switch (funcName) {
            case "getint": {
                // mipsUtils.makeMacro("getint");
                mipsUtils.makePseudo(PseudoType.LI, Register.V0, 5);
                mipsUtils.makeSyscall();
                int offset = mipsUtils.getValue2Offset().get(call);
                mipsUtils.makeStore(Register.V0, -offset, Register.SP);
                break;
            }
            case "putint": {
                Value arg = args.get(0);
                if (arg instanceof ConstInt) {
                    mipsUtils.makePseudo(PseudoType.LI, Register.A0, ((ConstInt) arg).getVal());
                } else {
                    // 在栈上
                    int offset = mipsUtils.getValue2Offset().get(arg);
                    mipsUtils.makeLoad(Register.A0, -offset, Register.SP);
                }
                // mipsUtils.makeMacro("putint");
                mipsUtils.makePseudo(PseudoType.LI, Register.V0, 1);
                mipsUtils.makeSyscall();
                break;
            }
            case "putstr": {
                // 肯定在栈上
                Value arg = args.get(0);
                int offset = mipsUtils.getValue2Offset().get(arg);
                mipsUtils.makeLoad(Register.A0, -offset, Register.SP);
                // mipsUtils.makeMacro("putstr");
                mipsUtils.makePseudo(PseudoType.LI, Register.V0, 4);
                mipsUtils.makeSyscall();
                break;
            }
            default: {
                // 普通函数
                // 第一步: 保存寄存器
                // 1.1: 保存该函数使用过的寄存器到栈上, 实际上只需要保存$a0到$a3
                ArrayList<Register> reg2save = curFunction.getValue2Reg().values().stream().sorted(Comparator.comparingInt(Register::getNumber)).collect(Collectors.toCollection(ArrayList::new));
                // 1.2: 保存$ra到栈上
                reg2save.add(Register.RA);  // $ra也要保存
                int increment = 0;
                HashMap<Register, Integer> reg2offset = new HashMap<>();
                for (Register reg : reg2save) {
                    increment += 4;
                    int offset = curFunction.getCurOffset() + increment;
                    reg2offset.put(reg, offset);
                    mipsUtils.makeStore(reg, -offset, Register.SP);
                }
                // 第二步: 传递参数
                // 2.1 把前四个参数存到参数寄存器
                for (int i = 0; i < 4 && i < args.size(); i++) {
                    Value arg = args.get(i);
                    Register reg;
                    if (i == 0) reg = Register.A0;
                    else if (i == 1) reg = Register.A1;
                    else if (i == 2) reg = Register.A2;
                    else reg = Register.A3;
                    if (arg instanceof ConstInt) {
                        mipsUtils.makePseudo(PseudoType.LI, reg, ((ConstInt) arg).getVal());
                    } else {
                        // 参数在栈上
                        int offset = mipsUtils.getValue2Offset().get(arg);
                        mipsUtils.makeLoad(reg, -offset, Register.SP);
                    }
                    mipsUtils.makeAnnotation("看这里", true);
                    int offset = curFunction.getCurOffset() + increment + (i + 1) * 4;
                    mipsUtils.makeStore(reg, -offset, Register.SP);
                }
                // 2.2 后面的参数存到栈上
                for (int i = 4; i < args.size(); i++) {
                    Value arg = args.get(i);
                    Register reg = mipsUtils.getFreeRegister();
                    if (arg instanceof ConstInt) {
                        mipsUtils.makePseudo(PseudoType.LI, reg, ((ConstInt) arg).getVal());
                        int offset = curFunction.getCurOffset() + increment + (i + 1) * 4;
                        mipsUtils.makeStore(reg, -offset, Register.SP);
                    } else {
                        // 在栈上
                        int offset = curFunction.getValue2Offset().get(arg);
                        mipsUtils.makeLoad(reg, -offset, Register.SP);
                        offset = curFunction.getCurOffset() + increment + (i + 1) * 4;
                        mipsUtils.makeStore(reg, -offset, Register.SP);
                    }
                    mipsUtils.freeRegister(reg);
                }
                // 第三步: 跳转
                // 3.1 更新栈指针, 指向新的函数的顶部
                int newOffset = curFunction.getCurOffset() + increment;
                mipsUtils.makeAlu(AluType.ADDIU, Register.SP, Register.SP, -newOffset);
                // 3.2 jal
                mipsUtils.makeJump(JumpType.JAL, funcName);
                // 第四步: 复原
                // 4.1 恢复$ra
                mipsUtils.makeLoad(Register.RA, 0, Register.SP);
                // 4.2 恢复$sp
                mipsUtils.makeAlu(AluType.ADDIU, Register.SP, Register.SP, newOffset);
                // 4.3 恢复所有先前保存的寄存器, 从栈加载到寄存器内
                for (Register reg : reg2save) {
                    if (reg == Register.RA) continue;
                    int offset = reg2offset.get(reg);
                    mipsUtils.makeLoad(reg, -offset, Register.SP);
                }
                // 4.4 如果有返回值, 则要吧$v0加载到栈上
                ValueType returnType = subFunction.getReturnType();
                if (!(returnType instanceof VoidType)) {
                    int offset = curFunction.getValue2Offset().get(call);
                    mipsUtils.makeStore(Register.V0, -offset, Register.SP);
                }
            }
        }
    }

    public void mapGep2Mips(Gep gep) {
        // 函数内定义的数组, 对数组进行操作
        // # %v9 = getelementptr inbounds [5 x i32], [5 x i32]* %v7, i32 0, i32 %v8
        // lw $k0, -8($sp)      // pointer
        // lw $k1, -12($sp)
        // sll $k1, $k1, 2
        // addu $k0, $k0, $k1      // address
        // sw $k0, -16($sp)

        // 函数参数是数组, 对参数进行操作
        // # %v3 = getelementptr inbounds i32, i32* %v2, i32 3
        // lw $k0, -12($sp)
        // li $k1, 12
        // addu $k0, $k0, $k1
        // sw $k0, -16($sp)

        // 先处理索引
        Value index;
        if (gep.getDimension() == 1) index = gep.getIndex();
        else index = gep.getRightIndex();
        // 然后把基指针取出
        Value pointer = gep.getPointer();
        Register pointerReg = mipsUtils.getFreeRegister();
        if (pointer instanceof GlobalVar || pointer instanceof GlobalString) {
            // 全局数组 or 字符串常量
            String labelName = pointer.getName().substring(1);
            mipsUtils.makePseudo(PseudoType.LA, pointerReg, labelName);
        } else {
            // 在栈上
            int offset = mipsUtils.getValue2Offset().get(pointer);
            mipsUtils.makeLoad(pointerReg, -offset, Register.SP);   // lw
        }

        Register address = mipsUtils.getFreeRegister();
        Register offsetReg = mipsUtils.getFreeRegister();
        if (index instanceof ConstInt) {
            // 索引是常数, 直接addiu即可
            mipsUtils.makeAlu(AluType.ADDIU, address, pointerReg, ((ConstInt) index).getVal() * 4);
        } else {
            // 索引位于栈上, 先加载再处理
            int offset = mipsUtils.getValue2Offset().get(index);
            mipsUtils.makeLoad(offsetReg, -offset, Register.SP);
            mipsUtils.makeAlu(AluType.SLL, offsetReg, offsetReg, 2);    // 等价于 *4
            mipsUtils.makeAlu(AluType.ADDU, address, pointerReg, offsetReg);
        }
        int offset = mipsUtils.getValue2Offset().get(gep);
        mipsUtils.makeStore(address, -offset, Register.SP);
        mipsUtils.freeRegister(pointerReg, address, offsetReg);
    }

    public void mapIcmp2Mips(Icmp icmp) {
        // # %v6 = icmp slt i32 %v5, 2
        // lw $k0, -8($sp)
        // li $k1, 2
        // slt $k0, $k0, $k1
        // sb $k0, -12($sp)
        Value left = icmp.getLeftOperand();
        Value right = icmp.getRightOperand();

        if (left instanceof ConstInt && right instanceof ConstInt) {
            int leftVal = ((ConstInt) left).getVal();
            int rightVal = ((ConstInt) right).getVal();
            boolean compare;
            Cmp cmp = icmp.getCmp();
            compare = switch (cmp) {
                case eq -> leftVal == rightVal;
                case ne -> leftVal != rightVal;
                case slt -> leftVal < rightVal;
                case sle -> leftVal <= rightVal;
                case sgt -> leftVal > rightVal;
                case sge -> leftVal >= rightVal;
                default -> false;
            };
            int val = compare ? 1 : 0;
            Register reg = mipsUtils.getFreeRegister();
            mipsUtils.makePseudo(PseudoType.LI, reg, val);
            int offset = mipsUtils.getValue2Offset().get(icmp);
            mipsUtils.makeStore(reg, -offset, Register.SP);
            mipsUtils.freeRegister(reg);
            return;
        } else if (left instanceof ConstInt) {
            // 左边是常数, 需要反转
            int leftval = ((ConstInt) left).getVal();
            Register reg = mipsUtils.getFreeRegister();
            int offset = mipsUtils.getValue2Offset().get(right);
            mipsUtils.makeLoad(reg, -offset, Register.SP);
            Register outcome = mipsUtils.getFreeRegister();
            CompareType compareType = switch (icmp.getCmp()) {
                case eq -> CompareType.SEQ; // 不变
                case ne -> CompareType.SNE; // 不变
                case slt -> CompareType.SGT;
                case sle -> CompareType.SGE;
                case sgt -> CompareType.SLTI;
                case sge -> CompareType.SLE;
                default -> null;
            };
            mipsUtils.makeCompare(compareType, outcome, reg, leftval);
            offset = mipsUtils.getValue2Offset().get(icmp);
            mipsUtils.makeStore(outcome, -offset, Register.SP);
            mipsUtils.freeRegister(reg, outcome);
        } else if (right instanceof ConstInt) {
            // 不用li, 直接比较即可
            int rightval = ((ConstInt) right).getVal();
            Register reg = mipsUtils.getFreeRegister();
            int offset = mipsUtils.getValue2Offset().get(left);
            mipsUtils.makeLoad(reg, -offset, Register.SP);
            Register outcome = mipsUtils.getFreeRegister();
            CompareType compareType = switch (icmp.getCmp()) {
                case eq -> CompareType.SEQ;
                case ne -> CompareType.SNE;
                case slt -> CompareType.SLTI;
                case sle -> CompareType.SLE;
                case sgt -> CompareType.SGT;
                case sge -> CompareType.SGE;
                default -> null;
            };
            mipsUtils.makeCompare(compareType, outcome, reg, rightval);
            offset = mipsUtils.getValue2Offset().get(icmp);
            mipsUtils.makeStore(outcome, -offset, Register.SP);
            mipsUtils.freeRegister(reg, outcome);
        } else {
            // 直接两个load
            int offset = mipsUtils.getValue2Offset().get(left);
            Register src1 = mipsUtils.getFreeRegister();
            mipsUtils.makeLoad(src1, -offset, Register.SP);
            offset = mipsUtils.getValue2Offset().get(right);
            Register src2 = mipsUtils.getFreeRegister();
            mipsUtils.makeLoad(src2, -offset, Register.SP);
            Register outcome = mipsUtils.getFreeRegister();
            CompareType compareType = switch (icmp.getCmp()) {
                case eq -> CompareType.SEQ;
                case ne -> CompareType.SNE;
                case slt -> CompareType.SLT;
                case sle -> CompareType.SLE;
                case sgt -> CompareType.SGT;
                case sge -> CompareType.SGE;
                default -> null;
            };
            mipsUtils.makeCompare(compareType, outcome, src1, src2);
            offset = mipsUtils.getValue2Offset().get(icmp);
            mipsUtils.makeStore(outcome, -offset, Register.SP);
            mipsUtils.freeRegister(src1, src2, outcome);
        }
    }

    public void mapLoad2Mips(Load load) {
        // 流程: 首先拿到指针所指对象的地址, 然后根据地址获取这个对象, 然后再把对象存到对应的栈上
        // 分别是 lw, lw, sw
        // # %v3 = load i32, i32* %v2
        // lw $k0, -8($sp)
        // lw $k0, 0($k0)
        // sw $k0, -12($sp)
        Value pointer = load.getPointer();
        Register address = mipsUtils.getFreeRegister();
        if (pointer instanceof GlobalVar) {
            String globalName = ((GlobalVar) pointer).getName().substring(1);
            mipsUtils.makePseudo(PseudoType.LA, address, globalName);
        } else {
            // 在栈上
            int offset = mipsUtils.getValue2Offset().get(pointer);
            mipsUtils.makeLoad(address, -offset, Register.SP);
        }
        Register value = mipsUtils.getFreeRegister();
        mipsUtils.makeLoad(value, 0, address);
        int offset = mipsUtils.getValue2Offset().get(load);
        mipsUtils.makeStore(value, -offset, Register.SP);
        mipsUtils.freeRegister(address, value);
    }

    public void mapRet2Mips(Ret ret) {
        if (curFunction.isMain()) {
            // li $v0, 10
            // syscall
            mipsUtils.makePseudo(PseudoType.LI, Register.V0, 10);
            mipsUtils.makeSyscall();
        } else {
            ValueType returnValueType = curFunction.getReturnType();
            if (returnValueType instanceof VoidType) {
                // void类型
            } else {
                Value returnValue = ret.getReturnValue();
                if (returnValue instanceof ConstInt) {
                    int val = ((ConstInt) returnValue).getVal();
                    mipsUtils.makePseudo(PseudoType.LI, Register.V0, val);
                } else {
                    // 返回值是%var, 在栈上
                    int offset = mipsUtils.getValue2Offset().get(returnValue);
                    mipsUtils.makeLoad(Register.V0, -offset, Register.SP);
                }
            }
            mipsUtils.makeJump(JumpType.JR, Register.RA);
        }
    }

    public void mapStore2Mips(Store store) {
        // store对应赋值语句
        // # store i32 3, i32* %v2
        // lw $k1, -4($sp)
        // li $k0, 3
        // sw $k0, 0($k1)


        // # store i32 %a1, i32* %v2
        // lw $k1, -28($sp)
        // sw $a1, 0($k1)
        Value pointer = store.getPointer();
        Value storedValue = store.getStoredValue();

        // 处理要存的数据
        Register value = null;
        if (storedValue instanceof ConstInt) {
            value = mipsUtils.getFreeRegister();
            mipsUtils.makePseudo(PseudoType.LI, value, ((ConstInt) storedValue).getVal());
        } else if (mipsUtils.getValue2Reg().containsKey(storedValue)) {
            value = mipsUtils.getValue2Reg().get(storedValue);
        } else {
            // 在栈上
            value = mipsUtils.getFreeRegister();
            int offset = mipsUtils.getValue2Offset().get(storedValue);
            mipsUtils.makeLoad(value, -offset, Register.SP);
        }

        // 处理指针
        Register address = mipsUtils.getFreeRegister();
        if (pointer instanceof GlobalVar) {
            // 指向的是全局变量
            String labelName = pointer.getName().substring(1);
            mipsUtils.makePseudo(PseudoType.LA, address, labelName);
            mipsUtils.makeStore(value, 0, address);
        } else {
            // 在栈上, 直接把value存进去即可
            int offset = mipsUtils.getValue2Offset().get(pointer);
            mipsUtils.makeLoad(address, -offset, Register.SP);
            mipsUtils.makeStore(value, 0, address);
        }
        if (mipsUtils.getValue2Reg().containsKey(storedValue)) mipsUtils.freeRegister(address);
        else mipsUtils.freeRegister(value, address);
    }

    public void mapZext2Mips(Zext zext) {
        // %v4 = zext i1 %v3 to i32
        // lb $k0, -4($sp)
        // sw $k0, -8($sp)
        Value operand = zext.getOperand();
        Register value = mipsUtils.getFreeRegister();
        int offset = mipsUtils.getValue2Offset().get(operand);
        mipsUtils.makeLoad(value, -offset, Register.SP);
        offset = mipsUtils.getValue2Offset().get(zext);
        mipsUtils.makeStore(value, -offset, Register.SP);
        mipsUtils.freeRegister(value);
    }

    public void buildMipsFunction(Function function) {
        curFunction = new MipsFunction(function.getName().substring(1), function, new ArrayList<>(), curAddress);
        mipsUtils.allocateArgs(curFunction);
        mipsUtils.allocateLeftValues(curFunction);
        mipsModule.addMipsFunction(curFunction);
        // System.out.println(curFunction);
        for (BasicBlock basicBlock : function.getBlocks()) {
            buildBasicBlock(basicBlock);
        }
    }

    public void buildGlobalStringMips(GlobalString globalString) {
        mipsUtils.makeMipsGlobalString(globalString);
    }

    public void buildGlobalVarMips(GlobalVar globalVar) {
        if (globalVar.isZeroInitializer()) mipsUtils.makeMipsZeroInitializer(globalVar);
        else if (globalVar.isArray()) mipsUtils.makeMipsGlobalArray(globalVar);
        else mipsUtils.makeMipsGlobalInteger(globalVar);
    }

    public void outputInFile() {
        try {
            // 输出到主输出路径
            PrintWriter writer = new PrintWriter(outputPath);
            writer.println(mipsModule.toString());
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
