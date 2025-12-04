package backend.instructions.alu;

import backend.instructions.MipsInstruction;
import backend.operand.Immediate;
import backend.operand.Operand;
import backend.operand.Register;

public class MipsAlu extends MipsInstruction {
    private final AluType aluType;
    private final Register dst;
    private final Register src1;
    private final Operand src2;

    public MipsAlu(AluType aluType, Register dst, Register src1, Register src2) {
        this.aluType = aluType;
        this.dst = dst;
        this.src1 = src1;
        this.src2 = src2;
    }

    public MipsAlu(AluType aluType, Register dst, Register src1, Immediate immediate) {
        this.aluType = aluType;
        this.dst = dst;
        this.src1 = src1;
        this.src2 = immediate;
    }

    @Override
    public String toString() {
        return aluType.toString().toLowerCase() + " " + dst.toString() + ", " + src1.toString() + ", " + src2.toString();
    }
}
