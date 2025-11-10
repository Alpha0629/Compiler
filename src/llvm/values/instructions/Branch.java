package llvm.values.instructions;

import llvm.types.ValueType;
import llvm.values.BasicBlock;
import llvm.values.Value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Branch extends Instruction {
    private final boolean isConditionalJump;

    // 第一种（有条件跳转）：br i1 <cond>, label <iftrue>, label <iffalse>
    public Branch(ValueType valueType, Value parent, Value cond, BasicBlock trueBlock, BasicBlock falseBlock) {
        // 一定是voidType
        super(valueType, parent, new ArrayList<>(Arrays.asList(cond, trueBlock, falseBlock)));
        this.isConditionalJump = true;
    }

    // 第二种（无条件跳转）：br label <dest>
    public Branch(ValueType valueType, Value parent, BasicBlock dest) {
        // 一定是voidType
        super(valueType, parent, new ArrayList<>(Collections.singletonList(dest)));
        this.isConditionalJump = false;
    }

    public boolean isConditionalJump() {
        return isConditionalJump;
    }

    public Value getCond() {
        if (isConditionalJump) {
            return super.getOperands().get(0);
        } else {
            return null;
        }
    }

    public BasicBlock getTrueBlock() {
        if (isConditionalJump) {
            return (BasicBlock) super.getOperands().get(1);
        } else {
            return null;
        }
    }

    public BasicBlock getFalseBlock() {
        if (isConditionalJump) {
            return (BasicBlock) super.getOperands().get(2);
        } else {
            return null;
        }
    }

    public BasicBlock getDest() {
        if (isConditionalJump) {
            return null;
        } else {
            return (BasicBlock) super.getOperands().get(0);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("branch");
        sb.append(" ");
        if (isConditionalJump) {
            sb.append(getCond().getValueType().toString());
            sb.append(" ");
            sb.append(getCond().getName());
            sb.append(", ");
            sb.append(getTrueBlock().getValueType().toString());
            sb.append(" ");
            sb.append(getTrueBlock().getName());
            sb.append(", ");
            sb.append(getFalseBlock().getValueType().toString());
            sb.append(" ");
            sb.append(getFalseBlock().getName());
        } else {
            sb.append(getDest().getValueType().toString());
            sb.append(" ");
            sb.append(getDest().getName());
        }
        return sb.toString();
    }
}
