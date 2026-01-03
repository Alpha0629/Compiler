package llvm.values;

import llvm.IrMaker;
import llvm.IrUtils;
import llvm.types.LabelType;
import llvm.types.ValueType;
import llvm.values.instructions.Copy;
import llvm.values.instructions.Instruction;
import llvm.values.instructions.Phi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;

public class BasicBlock extends Value {
    private final ArrayList<Instruction> instructions;
    private final HashSet<Instruction> instructionSet;
    private boolean visited;
    private final LinkedHashSet<BasicBlock> precursors;   // 前驱：从前面的块跳转到当前的块
    private final LinkedHashSet<BasicBlock> successors;   // 后继：当前块会跳转到的块
    private final LinkedHashSet<BasicBlock> dominators;
    private final LinkedHashSet<BasicBlock> directDominatedpersons;
    private BasicBlock directDominator;
    private final LinkedHashSet<BasicBlock> dominanceBoundaries;

    public BasicBlock(String name, LabelType labelType, Value parent) {
        // 保证是label型的标签
        super("%b" + name, labelType, parent);
        this.instructions = new ArrayList<>();
        this.instructionSet = new HashSet<>();
        this.visited = false;
        this.precursors = new LinkedHashSet<>();
        this.successors = new LinkedHashSet<>();
        this.dominators = new LinkedHashSet<>();
        this.directDominatedpersons = new LinkedHashSet<>();
        this.directDominator = null;
        this.dominanceBoundaries = new LinkedHashSet<>();
    }

    public ArrayList<Instruction> getInstructions() {
        return instructions;
    }

    public Instruction getLastInstruction() {
        return instructions.get(instructions.size() - 1);
    }

    public void addInstructionToTail(Instruction instruction) {
        // if (IrMaker.currentBlock == IrMaker.garbageBlock) return;
        if (!instructionSet.contains(instruction)) {
            instructionSet.add(instruction);
            instructions.add(instruction);
        } else {
            System.out.println("警告：重复添加相同的指令");
        }
    }

    public void addInstructionsToTail(ArrayList<Instruction> instructions) {
        this.instructions.addAll(instructions);
    }

    public void addInstructionToHead(Instruction instruction) {
        if (IrMaker.currentBlock == IrMaker.garbageBlock) {
            // 如果已经return了, 不要把alloca添加到头部
            // System.out.println(instruction);
            IrUtils.nameCount--;
            return;
        }
        if (!instructionSet.contains(instruction)) {
            instructionSet.add(instruction);
            instructions.add(0, instruction);
        } else {
            System.out.println("警告：重复添加相同的指令");
        }
    }

    public void insertPhiToHead(Phi phi) {
        if (!instructionSet.contains(phi)) {
            instructionSet.add(phi);
            instructions.add(0, phi);
        } else {
            System.out.println("警告：重复添加相同的指令");
        }
    }

    public void addPrecursor(BasicBlock precursor) {
        precursors.add(precursor);
    }

    public void addSuccessor(BasicBlock successor) {
        successors.add(successor);
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public Function getParentFunction() {
        return (Function) this.getParent();
    }

    public LinkedHashSet<BasicBlock> getPrecursors() {
        return precursors;
    }

    public LinkedHashSet<BasicBlock> getSuccessors() {
        return successors;
    }

    public LinkedHashSet<BasicBlock> getDominators() {
        return dominators;
    }

    public void addDominator(BasicBlock dominator) {
        dominators.add(dominator);
    }

    public void setNewDominators(HashSet<BasicBlock> newDominators) {
        this.dominators.clear();
        this.dominators.addAll(newDominators);
    }

    public void setDirectDominator(BasicBlock directDominator) {
        // 当前基本块的直接支配者，当前基本块是被支配的
        this.directDominator = directDominator;
    }

    public void addDirectDominatedPerson(BasicBlock directDominatedPerson) {
        // 当前基本块是哪些基本块的直接支配者，当前基本块是支配者
        this.directDominatedpersons.add(directDominatedPerson);
    }

    public LinkedHashSet<BasicBlock> getDirectDominatedpersons() {
        return directDominatedpersons;
    }

    public void addDonminanceBoundary(BasicBlock donminanceBoundary) {
        this.dominanceBoundaries.add(donminanceBoundary);
    }

    public BasicBlock getDirectDominator() {
        return directDominator;
    }

    public LinkedHashSet<BasicBlock> getDominanceBoundaries() {
        return dominanceBoundaries;
    }

    @Override
    public String toString() {
        // b9:
        //     %v10 = getelementptr inbounds [6 x i8], [6 x i8]* @str.0, i32 0, i32 0
        //     call void @putstr(i8*  %v10)
        //     ret i32 0
        StringBuilder sb = new StringBuilder();
        // sb.append(super.getName());
        sb.append(super.getName().substring(1));
        sb.append(":");
        sb.append('\n');
        for (int i = 0; i < instructions.size(); i++) {
            if (i >= 1) sb.append('\n');
            Instruction instruction = instructions.get(i);
            sb.append('\t');
            sb.append(instruction.toString());
        }
        return sb.toString();
    }
}
