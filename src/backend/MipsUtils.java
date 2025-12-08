package backend;

import backend.MipsItem.MipsFunction;
import backend.MipsItem.MipsGV.MipsGlobalArray;
import backend.MipsItem.MipsGV.MipsGlobalInteger;
import backend.MipsItem.MipsGV.MipsGlobalString;
import backend.MipsItem.MipsGV.MipsZeroInitializer;
import backend.instructions.alu.AluType;
import backend.instructions.alu.MipsAlu;
import backend.instructions.branch.BranchType;
import backend.instructions.branch.MipsBranch;
import backend.instructions.comare.CompareType;
import backend.instructions.comare.Mipscompare;
import backend.instructions.hilo.HiLoType;
import backend.instructions.hilo.MipsHiLo;
import backend.instructions.jump.JumpType;
import backend.instructions.jump.MipsJump;
import backend.instructions.mem.MemoryType;
import backend.instructions.mem.Mipsmemory;
import backend.instructions.others.Annotation;
import backend.instructions.others.Macro;
import backend.instructions.others.Syscall;
import backend.instructions.pseudo.MipsPseudo;
import backend.instructions.pseudo.PseudoType;
import backend.operand.Immediate;
import backend.operand.Label;
import backend.operand.Register;
import llvm.types.ValueType;
import llvm.values.GlobalString;
import llvm.values.GlobalVar;
import llvm.values.Value;
import llvm.values.constants.ConstArray;
import llvm.values.constants.ConstInt;
import llvm.values.constants.Constant;
import llvm.values.instructions.Alloca;
import llvm.values.instructions.Instruction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class MipsUtils {
    private final MipsModule mipsModule;
    private final Queue<Register> freeRegisters;

    public MipsUtils(MipsModule mipsModule) {
        this.mipsModule = mipsModule;
        this.freeRegisters = initFreeRegisters();
    }

    public void allocateArgs(MipsFunction mipsFunction) {
        ArrayList<Value> args = mipsFunction.getArgs();
        ArrayList<ValueType> argTypes = mipsFunction.getArgTypes();
        for (int i = 0; i < args.size(); i++) {
            Value arg = args.get(i);
            ValueType argType = argTypes.get(i);
            // 先确定每个参数的偏移量, 即便是前四个参数也要预留栈空间
            int bytes = argType.getBytes();
            mipsFunction.addCurOffset(bytes);
            mipsFunction.getValue2Offset().put(arg, mipsFunction.getCurOffset());
            System.out.println("给第" + i + "个参数分配栈空间, 其相对于$sp($sp表示当前函数体的起始栈空间)的偏移为" + mipsFunction.getCurOffset());
            if (i == 0) {
                mipsFunction.getValue2Reg().put(arg, Register.A0);
            } else if (i == 1) {
                mipsFunction.getValue2Reg().put(arg, Register.A1);
            } else if (i == 2) {
                mipsFunction.getValue2Reg().put(arg, Register.A2);
            } else if (i == 3) {
                mipsFunction.getValue2Reg().put(arg, Register.A3);
            }
        }
    }

    public void allocateLeftValues(MipsFunction mipsFunction) {
        ArrayList<Instruction> instructions = mipsFunction.getAllIrInstructions();
        for (Instruction instruction : instructions) {
            if (!instruction.withoutName()) {
                // 给每个有var的指令分配4个字节的空间
                int bytes = instruction.getValueType().getBytes();
                // System.out.println(instruction.getValueType());
                // System.out.println(bytes);
                mipsFunction.addCurOffset(bytes);
                mipsFunction.getValue2Offset().put(instruction, mipsFunction.getCurOffset());
                System.out.println("The cur instrution is: " + instruction + " with offset: " + mipsFunction.getCurOffset());
                this.makeAnnotation(instruction.getName() + " is allocated at -" + (mipsFunction.getCurOffset()) + "($sp)");
            }
        }
    }

    public void allocateObject(int bytes) {
        MipsMaker.curFunction.addCurOffset(bytes);
    }

    public MipsPseudo makePseudo(PseudoType pseudoType, Register dst, int val) {
        Immediate imm = new Immediate(val);
        MipsPseudo li = new MipsPseudo(pseudoType, dst, imm);
        MipsMaker.curBlock.addInstructionToTail(li);
        return li;
    }

    public MipsPseudo makePseudo(PseudoType pseudoType, Register dst, String labelName) {
        Label label = new Label(labelName);
        MipsPseudo La = new MipsPseudo(pseudoType, dst, label);
        MipsMaker.curBlock.addInstructionToTail(La);
        return La;
    }

    public Mipsmemory makeLoad(Register dst, int offset, Register src) {
        Immediate imm = new Immediate(offset);
        Mipsmemory load = new Mipsmemory(MemoryType.LW, dst, imm, src);
        MipsMaker.curBlock.addInstructionToTail(load);
        return load;
    }

    public Mipsmemory makeStore(Register src, int offset, Register dst) {
        Immediate imm = new Immediate(offset);
        Mipsmemory store = new Mipsmemory(MemoryType.SW, dst, imm, src);
        MipsMaker.curBlock.addInstructionToTail(store);
        return store;
    }

    public MipsAlu makeAlu(AluType aluType, Register dst, Register src1, Register src2) {
        MipsAlu alu = new MipsAlu(aluType, dst, src1, src2);
        MipsMaker.curBlock.addInstructionToTail(alu);
        return alu;
    }

    public MipsAlu makeAlu(AluType aluType, Register dst, Register src, int val) {
        Immediate imm = new Immediate(val);
        MipsAlu alu = new MipsAlu(aluType, dst, src, imm);
        MipsMaker.curBlock.addInstructionToTail(alu);
        return alu;
    }

    public MipsHiLo makeHiLo(HiLoType hiLoType, Register dst) {
        MipsHiLo hiLo = new MipsHiLo(hiLoType, dst);
        MipsMaker.curBlock.addInstructionToTail(hiLo);
        return hiLo;
    }

    public MipsJump makeJump(JumpType jumpType, Register goal) {
        MipsJump jump = new MipsJump(jumpType, goal);
        MipsMaker.curBlock.addInstructionToTail(jump);
        return jump;
    }

    public MipsJump makeJump(JumpType jumpType, String goalName) {
        Label goal = new Label(goalName);
        MipsJump jump = new MipsJump(jumpType, goal);
        MipsMaker.curBlock.addInstructionToTail(jump);
        return jump;
    }

    public Mipscompare makeCompare(CompareType compareType, Register dst, Register src1, Register src2) {
        Mipscompare compare = new Mipscompare(compareType, dst, src1, src2);
        MipsMaker.curBlock.addInstructionToTail(compare);
        return compare;
    }

    public Mipscompare makeCompare(CompareType compareType, Register dst, Register src, int val) {
        Immediate imm = new Immediate(val);
        Mipscompare compare = new Mipscompare(compareType, dst, src, imm);
        MipsMaker.curBlock.addInstructionToTail(compare);
        return compare;
    }

    public MipsBranch makeBranch(BranchType branchType, Register src1, Register src2, String labelName) {
        Label label = new Label(labelName);
        MipsBranch branch = new MipsBranch(branchType, src1, src2, label);
        MipsMaker.curBlock.addInstructionToTail(branch);
        return branch;
    }

    public Syscall makeSyscall() {
        Syscall syscall = new Syscall();
        MipsMaker.curBlock.addInstructionToTail(syscall);
        return syscall;
    }

    public void makeAnnotation(Instruction instruction) {
        Annotation annotation = new Annotation(instruction.toString());
        MipsMaker.curBlock.addInstructionToTail(annotation);
    }

    public void makeAnnotation(String content) {
        Annotation annotation = new Annotation(content);
        MipsMaker.curFunction.addHeadInstructions(annotation);
    }

    public void makeMacro(String content) {
        Macro macro = new Macro(content);
        MipsMaker.curBlock.addInstructionToTail(macro);
    }

    public HashMap<Value, Integer> getValue2Offset() {
        return MipsMaker.curFunction.getValue2Offset();
    }

    public HashMap<Value, Register> getValue2Reg() {
        return MipsMaker.curFunction.getValue2Reg();
    }

    public int calTotalOffset(MipsFunction mipsFunction) {
        int totalOffset = 0;
        ArrayList<Instruction> instructions = mipsFunction.getAllIrInstructions();
        System.out.println(instructions.size());
        for (Instruction instruction : instructions) {
            if (!instruction.withoutName()) {
                totalOffset += 4;   // 给每个有var的指令分配4个字节的空间
            }
        }
        for (Instruction instruction : instructions) {
            if (instruction instanceof Alloca) {
                totalOffset += 4;   // 给每个要alloca的变量分配4个字节的空间
            }
        }
        mipsFunction.setTotalOffset(totalOffset);
        return totalOffset;
    }

    public void makeMipsGlobalString(GlobalString globalString) {
        String name = globalString.getName().substring(1);
        String content = globalString.getConstString().toString();
        content = content.replace("\\0A", "\\n");
        MipsGlobalString mipsGlobalString = new MipsGlobalString(name, content);
        mipsModule.addMipsGlobalVariable(mipsGlobalString);
    }

    public void makeMipsZeroInitializer(GlobalVar globalVar) {
        String name = globalVar.getName().substring(1);
        int length = ((ConstArray) globalVar.getConstInit()).getSize();
        MipsZeroInitializer zeroInitializer = new MipsZeroInitializer(name, length);
        mipsModule.addMipsGlobalVariable(zeroInitializer);
    }

    public void makeMipsGlobalArray(GlobalVar globalVar) {
        String name = globalVar.getName().substring(1);
        ConstArray constArray = (ConstArray) globalVar.getConstInit();
        ArrayList<Integer> inits = new ArrayList<>();
        for (Constant constant : constArray.getConstants()) {
            assert constant instanceof ConstInt;
            inits.add(((ConstInt) constant).getVal());
        }
        MipsGlobalArray mipsGlobalArray = new MipsGlobalArray(name, inits);
        mipsModule.addMipsGlobalVariable(mipsGlobalArray);
    }

    public void makeMipsGlobalInteger(GlobalVar globalVar) {
        String name = globalVar.getName().substring(1);
        int value = ((ConstInt) globalVar.getConstInit()).getVal();
        MipsGlobalInteger mipsGlobalInteger = new MipsGlobalInteger(name, value);
        mipsModule.addMipsGlobalVariable(mipsGlobalInteger);
    }

    public Queue<Register> initFreeRegisters() {
        Queue<Register> freeRegisters = new LinkedList<>();
        freeRegisters.add(Register.T0);
        freeRegisters.add(Register.T1);
        freeRegisters.add(Register.T2);
        freeRegisters.add(Register.T3);
        freeRegisters.add(Register.T4);
        freeRegisters.add(Register.T5);
        freeRegisters.add(Register.T6);
        freeRegisters.add(Register.T7);
        freeRegisters.add(Register.T8);
        freeRegisters.add(Register.T9);
        return freeRegisters;
    }

    public Register getFreeRegister() {
        Register freeRegister = freeRegisters.poll();
        if (freeRegister == null) {
            throw new RuntimeException("There is no free register");
        }
        freeRegister.setOccupied();
        return freeRegister;
    }

    public void freeRegister(Register... registers) {
        for (int i = 0; i < registers.length; i++) {
            Register register = registers[i];
            if (register != null) {
                register.setFree();
                freeRegisters.offer(register);
            }
        }
    }
}
