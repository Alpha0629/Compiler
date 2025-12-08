package backend.instructions.mem;

import backend.instructions.MipsInstruction;
import backend.operand.Immediate;
import backend.operand.Operand;
import backend.operand.Register;

public class Mipsmemory extends MipsInstruction {
    // lw $t0, 8($s1)        # 从地址 $s1 + 8 加载字到 $t0
    // sw $t0, 12($s1)       # 将 $t0 的值存储到地址 $s1 + 12
    private final MemoryType memoryType;
    private final Register dst; // 最终去哪里
    private final Immediate offset; // 偏移
    private final Register src; // 从哪里来

    public Mipsmemory(MemoryType memoryType, Register dst, Immediate offset, Register src) {
        this.memoryType = memoryType;
        this.dst = dst;
        this.offset = offset;
        this.src = src;
    }

    public MemoryType getMemoryType() {
        return memoryType;
    }

    public Register getDst() {
        return dst;
    }

    public Operand getSrc() {
        return src;
    }

    public Immediate getOffset() {
        return offset;
    }

    @Override
    public String toString() {
        return switch (memoryType) {
            case LW -> "lw " + dst.toString() + ", " + offset.toString() + "(" + src.toString() + ")";
            case SW -> "sw " + src.toString() + ", " + offset.toString() + "(" + dst.toString() + ")";
        };
    }

}
