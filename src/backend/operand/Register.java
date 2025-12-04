package backend.operand;

public class Register implements Operand {
    private final RegisterType reg;
    private final String number;

    public Register(RegisterType reg, String number) {
        this.reg = reg;
        this.number = number;
    }

    // Zero register (always contains 0)
    public final static Register ZERO = new Register(RegisterType.ZERO, "0");

    // Assembler temporary registers
    public final static Register AT = new Register(RegisterType.AT, "1");

    // Function return values
    public final static Register V0 = new Register(RegisterType.V0, "2");
    public final static Register V1 = new Register(RegisterType.V1, "3");

    // Function arguments
    public final static Register A0 = new Register(RegisterType.A0, "4");
    public final static Register A1 = new Register(RegisterType.A1, "5");
    public final static Register A2 = new Register(RegisterType.A2, "6");
    public final static Register A3 = new Register(RegisterType.A3, "7");

    // Temporary registers (not preserved across calls)
    public final static Register T0 = new Register(RegisterType.T0, "8");
    public final static Register T1 = new Register(RegisterType.T1, "9");
    public final static Register T2 = new Register(RegisterType.T2, "10");
    public final static Register T3 = new Register(RegisterType.T3, "11");
    public final static Register T4 = new Register(RegisterType.T4, "12");
    public final static Register T5 = new Register(RegisterType.T5, "13");
    public final static Register T6 = new Register(RegisterType.T6, "14");
    public final static Register T7 = new Register(RegisterType.T7, "15");

    // Saved registers (preserved across calls)
    public final static Register S0 = new Register(RegisterType.S0, "16");
    public final static Register S1 = new Register(RegisterType.S1, "17");
    public final static Register S2 = new Register(RegisterType.S2, "18");
    public final static Register S3 = new Register(RegisterType.S3, "19");
    public final static Register S4 = new Register(RegisterType.S4, "20");
    public final static Register S5 = new Register(RegisterType.S5, "21");
    public final static Register S6 = new Register(RegisterType.S6, "22");
    public final static Register S7 = new Register(RegisterType.S7, "23");

    // More temporary registers
    public final static Register T8 = new Register(RegisterType.T8, "24");
    public final static Register T9 = new Register(RegisterType.T9, "25");

    // Kernel registers (OS use)
    public final static Register K0 = new Register(RegisterType.K0, "26");
    public final static Register K1 = new Register(RegisterType.K1, "27");

    // Global pointer
    public final static Register GP = new Register(RegisterType.GP, "28");

    // Stack pointer
    public final static Register SP = new Register(RegisterType.SP, "29");

    // Frame pointer
    public final static Register FP = new Register(RegisterType.FP, "30");

    // Return address
    public final static Register RA = new Register(RegisterType.RA, "31");

    @Override
    public String toString() {
        return "$" + reg.toString().toLowerCase();
    }
}