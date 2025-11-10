package llvm.values;

import llvm.types.LabelType;
import llvm.types.ValueType;
import llvm.values.instructions.Instruction;

import java.util.ArrayList;

public class BasicBlock extends Value {
    private final ArrayList<Instruction> instructions;

    public BasicBlock(String name, LabelType labelType, Value parent) {
        // 保证是label型的标签
        super("%b" + name, labelType, parent);
        this.instructions = new ArrayList<>();
    }

    public ArrayList<Instruction> getInstructions() {
        return instructions;
    }

    public void addInstructionToTail(Instruction instruction) {
        instructions.add(instruction);
    }

    public void addInstructionToHead(Instruction instruction) {
        instructions.add(0, instruction);
    }

    @Override
    public String toString() {
        // b9:
        //     %v10 = getelementptr inbounds [6 x i8], [6 x i8]* @str.0, i32 0, i32 0
        //     call void @putstr(i8*  %v10)
        //     ret i32 0
        StringBuilder sb = new StringBuilder();
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
