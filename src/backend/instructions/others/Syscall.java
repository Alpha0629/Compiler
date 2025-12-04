package backend.instructions.others;

import backend.instructions.MipsInstruction;

public class Syscall extends MipsInstruction {
    private final String syscall;

    public Syscall() {
        this.syscall = "syscall";
    }

    @Override
    public String toString() {
        return syscall;
    }
}
