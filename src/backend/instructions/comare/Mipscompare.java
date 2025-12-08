package backend.instructions.comare;

import backend.instructions.MipsInstruction;
import backend.operand.Immediate;
import backend.operand.Operand;
import backend.operand.Register;

public class Mipscompare extends MipsInstruction {
    private final CompareType compareType;
    private final Register dst;
    private final Register src1;
    private final Operand src2;

    public Mipscompare(CompareType compareType, Register dst, Register src1, Register src2) {
        this.compareType = compareType;
        this.dst = dst;
        this.src1 = src1;
        this.src2 = src2;
    }

    public Mipscompare(CompareType compareType, Register dst, Register src1, Immediate src2) {
        this.compareType = compareType;
        this.dst = dst;
        this.src1 = src1;
        this.src2 = src2;
    }

    @Override
    public String toString() {
        return compareType.toString().toLowerCase() + " " + dst.toString() + ", " + src1.toString() + ", " + src2.toString();
    }
}
