package backend.instructions.branch;

import backend.instructions.MipsInstruction;
import backend.operand.Label;
import backend.operand.Register;

public class MipsBranch extends MipsInstruction {
    private final BranchType branchType;
    private final Register src1;
    private final Register src2;
    private final Label label;

    public MipsBranch(BranchType branchType, Register src1, Register src2, Label label) {
        this.branchType = branchType;
        this.src1 = src1;
        this.src2 = src2;
        this.label = label;
    }

    @Override
    public String toString() {
        return branchType.toString().toLowerCase() + " " + src1.toString() + ", " + src2.toString() + ", " + label.toString();
    }
}
