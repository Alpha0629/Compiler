package backend.instructions.hilo;

import backend.instructions.MipsInstruction;
import backend.operand.Register;

public class MipsHiLo extends MipsInstruction {
    private final HiLoType hiLoType;
    private final Register dst;

    public MipsHiLo(HiLoType hiLoType, Register dst) {
        this.hiLoType = hiLoType;
        this.dst = dst;
    }

    @Override
    public String toString() {
        return hiLoType.toString().toLowerCase() + " " + dst.toString();
    }
}
