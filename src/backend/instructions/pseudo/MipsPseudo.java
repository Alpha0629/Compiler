package backend.instructions.pseudo;

import backend.instructions.MipsInstruction;
import backend.operand.Immediate;
import backend.operand.Label;
import backend.operand.Operand;
import backend.operand.Register;

public class MipsPseudo extends MipsInstruction {
    // li $rd, immediate
    // la $rd, label
    private final PseudoType pseudoType;
    private final Register dst;
    private final Operand src;

    public MipsPseudo(PseudoType pseudoType, Register dst, Immediate immediate) {
        this.pseudoType = pseudoType;
        this.dst = dst;
        this.src = immediate;
    }

    public MipsPseudo(PseudoType pseudoType, Register dst, Label label) {
        this.pseudoType = pseudoType;
        this.dst = dst;
        this.src = label;
    }

    @Override
    public String toString() {
        return pseudoType.toString().toLowerCase() + " " + dst.toString() + ", " + src.toString();
    }
}
