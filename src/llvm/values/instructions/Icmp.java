package llvm.values.instructions;

import frontend.Lexer.TokenType;
import llvm.types.ValueType;
import llvm.values.Value;

import java.util.ArrayList;
import java.util.Arrays;

public class Icmp extends Instruction {
    // %v18 = icmp sle i32 %v17, 21
    public enum Cmp {
        eq,  // ==
        ne,  // !=
        slt, // <
        sle, // <=
        sgt, // >
        sge, // >=
    }

    private final Cmp cmp;

    public Icmp(String name, ValueType valueType, Value parent, TokenType cond, Value leftOperand, Value rightOperand) {
        super(name, valueType, parent, new ArrayList<>(Arrays.asList(leftOperand, rightOperand)));
        this.cmp = token2Cmp(cond);
    }

    public Value getLeftOperand() {
        return super.getOperands().get(0);
    }

    public Value getRightOperand() {
        return super.getOperands().get(1);
    }

    public Cmp getCmp() {
        return cmp;
    }

    private Cmp token2Cmp(TokenType cond) {
        return switch (cond) {
            case EQL -> Cmp.eq;
            case NEQ -> Cmp.ne;
            case LSS -> Cmp.slt;
            case LEQ -> Cmp.sle;
            case GRE -> Cmp.sgt;
            case GEQ -> Cmp.sge;
            default -> null;
        };
    }



    @Override
    public String toString() {
        // %v18 = icmp sle i32 %v17, 21
        StringBuilder sb = new StringBuilder();
        sb.append(super.getName());
        sb.append(" = ");
        sb.append("icmp");
        sb.append(" ");
        sb.append(cmp.toString());
        sb.append(" ");
        sb.append(this.getLeftOperand().getValueType().toString());
        sb.append(" ");
        sb.append(this.getLeftOperand().getName());
        sb.append(", ");
        // 相同的ValueType比较，只需要获取一次即可
        sb.append(this.getRightOperand().getName());
        return sb.toString();
    }
}
