package llvm.values.instructions;

import llvm.types.ValueType;
import llvm.values.BasicBlock;
import llvm.values.Value;

import java.util.ArrayList;

public class Phi extends Instruction {
    //  <result> = phi [fast-math-flags] <ty> [<val0>, <label0>], ...
    public Phi(String name, ValueType valueType, Value parent, ArrayList<BasicBlock> blocks) {
        super(name, valueType, parent, new ArrayList<>(blocks));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        return sb.toString();
    }
}
