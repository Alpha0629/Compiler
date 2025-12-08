package backend.MipsItem;

import backend.instructions.MipsInstruction;
import backend.operand.Register;
import llvm.types.ValueType;
import llvm.values.BasicBlock;
import llvm.values.Function;
import llvm.values.Value;
import llvm.values.instructions.Instruction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class MipsFunction {
    private final String funcName;
    private final Function oriFunction;
    private final ArrayList<MipsBlock> mipsBlocks;
    private ArrayList<MipsInstruction> headInstructions;
    private int totalOffset;
    private int curOffset;
    private int startAddress;
    private int endAddress;
    private int curAddress;
    private final HashMap<Value, Register> value2Reg;
    private final HashMap<Value, Integer> value2Offset;

    public MipsFunction(String funcName, Function oriFunction, int totalOffset, int startAddress, int endAddress) {
        this.funcName = funcName;
        this.oriFunction = oriFunction;
        this.mipsBlocks = new ArrayList<>();
        this.headInstructions = new ArrayList<>();
        this.totalOffset = totalOffset;
        this.startAddress = startAddress;
        this.endAddress = endAddress;
        this.value2Reg = new HashMap<>();
        this.value2Offset = new HashMap<>();
    }

    public MipsFunction(String funcName, Function oriFunction, ArrayList<MipsBlock> mipsBlocks, int startAddress) {
        this.funcName = funcName;
        this.oriFunction = oriFunction;
        this.mipsBlocks = mipsBlocks;
        this.headInstructions = new ArrayList<>();
        this.totalOffset = 0;
        this.curOffset = 0;
        this.startAddress = 0;
        this.endAddress = 0;
        this.value2Reg = new HashMap<>();
        this.value2Offset = new HashMap<>();
    }

    public HashMap<Value, Register> getValue2Reg() {
        return value2Reg;
    }

    public HashMap<Value, Integer> getValue2Offset() {
        return value2Offset;
    }

    public String getFuncName() {
        return funcName;
    }

    public Boolean isDeclare() {
        return oriFunction.isDeclare();
    }

    public void setTotalOffset(int totalOffset) {
        this.totalOffset = totalOffset;
    }

    public ArrayList<MipsBlock> getBlocks() {
        return mipsBlocks;
    }

    public void addBlock(MipsBlock block) {
        mipsBlocks.add(block);
    }

    public void addHeadInstructions(MipsInstruction headInstruction) {
        this.headInstructions.add(headInstruction);
    }

    public int getStartAddress() {
        return startAddress;
    }

    public int getCurOffset() {
        return curOffset;
    }

    public void addCurOffset(int increment) {
        curOffset += increment;
    }

    public boolean isMain() {
        return Objects.equals(this.funcName, "main");
    }

    public ValueType getReturnType() {
        return oriFunction.getReturnType();
    }

    public ArrayList<Instruction> getAllIrInstructions() {
        ArrayList<Instruction> instructions = new ArrayList<>();
        for (BasicBlock basicBlock : oriFunction.getBlocks()) {
            instructions.addAll(basicBlock.getInstructions());
        }
        return instructions;
    }

    public ArrayList<ValueType> getArgTypes() {
        return this.oriFunction.getArgTypes();
    }

    public ArrayList<Value> getArgs() {
        return this.oriFunction.getArgs();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(funcName);
        sb.append(": \n");
        for (MipsInstruction instruction : headInstructions) {
            sb.append("\t");
            sb.append(instruction.toString());
            sb.append("\n");
        }
        for (MipsBlock block : mipsBlocks) {
            sb.append(block.toString());
            sb.append("\n");
        }
        return sb.toString();
    }
}
