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

    public MipsAlu(Register src1, Register src2) {
        this.aluType = AluType.DIV;
        this.dst = null;
        this.src1 = src1;
        this.src2 = src2;
    }

    public AluType getAluType() {
        return aluType;
    }

    public Register getDst() {
        return dst;
    }

    public Register getSrc1() {
        return src1;
    }

    public Operand getSrc2() {
        return src2;
    }

    @Override
    public String toString() {
        if (dst != null) return aluType.toString().toLowerCase() + " " + dst.toString() + ", " + src1.toString() + ", " + src2.toString();
        else return "div , " + src1.toString() + ", " + src2.toString();
    }
}
