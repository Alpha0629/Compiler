package backend.instructions.jump;

import backend.instructions.MipsInstruction;
import backend.operand.Label;
import backend.operand.Operand;
import backend.operand.Register;

public class MipsJump extends MipsInstruction {
    // j label  无条件跳转
    // jal function_name    跳转并保存下一步地址到$ra
    // jr $rs       跳转到寄存器$rs中存储的地址
    private final JumpType jumpType;
    private final Operand goal;

    public MipsJump(JumpType jumpType, Label goal) {
        this.jumpType = jumpType;
        this.goal = goal;
    }

    public MipsJump(JumpType jumpType, Register goal) {
        this.jumpType = jumpType;
        this.goal = goal;
    }

    public JumpType getJumpType() {
        return jumpType;
    }

    public Operand getGoal() {
        return goal;
    }

    @Override
    public String toString() {
        return jumpType.toString().toLowerCase() + " " + goal.toString();
    }
}
