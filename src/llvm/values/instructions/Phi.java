package llvm.values.instructions;

import llvm.types.ValueType;
import llvm.values.BasicBlock;
import llvm.values.Value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Phi extends Instruction {
    private final HashMap<BasicBlock, Value> sourceBlock2sourceValue = new HashMap<>();
    private final ArrayList<BasicBlock> sourceBlocks = new ArrayList<>();

    public Phi(String name, ValueType pointedType, BasicBlock phiBlock) {
        super("%p", name, pointedType, phiBlock);
        sourceBlocks.addAll(phiBlock.getPrecursors());
        for (BasicBlock sourceBlock : sourceBlocks) {
            sourceBlock2sourceValue.put(sourceBlock, null);
        }
    }

    public void buildOperand(Value sourceValue, BasicBlock sourceBlock) {
        if (sourceBlock2sourceValue.containsKey(sourceBlock)) {
            sourceBlock2sourceValue.put(sourceBlock, sourceValue);  // 来自于sourceBlock的数据，使用sourceValue
            sourceValue.addUser(this);
            sourceBlock.addUser(this);
            super.addOperand(sourceValue);
        }
    }

    public HashMap<BasicBlock, Value> getSourceBlock2sourceValue() {
        return sourceBlock2sourceValue;
    }

    public ArrayList<BasicBlock> getSourceBlocks() {
        return sourceBlocks;
    }

    public void updateSourceBlock2sourceValue(Value oldOp, Value newOp) {
        sourceBlock2sourceValue.replaceAll((block, value) ->
                value == oldOp ? newOp : value
        );
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.getName());
        sb.append(" = ");
        sb.append(this.getClass().getSimpleName().toLowerCase());
        sb.append(" ");
        sb.append(super.getValueType());
        sb.append(" ");
        for (BasicBlock sourceBlock : sourceBlocks) {
            if (sourceBlock2sourceValue.containsKey(sourceBlock)) {
                Value sourceValue = sourceBlock2sourceValue.get(sourceBlock);
                sb.append("[").append(sourceValue.getName()).append(", ").append(sourceBlock.getName()).append("], ");
            }
        }
        if (sb.charAt(sb.length() - 2) == ',') {
            return sb.substring(0, sb.length() - 2);
        }
        return sb.toString();
    }
}
