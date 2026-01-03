package optimization;

import backend.MipsModule;
import llvm.IrModule;

import java.io.IOException;
import java.io.PrintWriter;

public class Opt {
    private final IrModule irModule;
    private final MipsModule mipsModule;
    private final String llvmOutputPath;
    private final String mipsOutputPath;
    private final DeadCodeDelete deadCodeDelete;
    private final DominationAnalysis dominationAnalysis;
    private final Mem2Reg mem2Reg;
    private final RemovePhi removePhi;
    private final PeepHole peepHole;

    public Opt(IrModule irModule, MipsModule mipsModule, String llvmOutputPath, String mipsOutputPath) {
        this.irModule = irModule;
        this.mipsModule = mipsModule;
        this.llvmOutputPath = llvmOutputPath;
        this.mipsOutputPath = mipsOutputPath;
        deadCodeDelete = new DeadCodeDelete(irModule, mipsModule);
        dominationAnalysis = new DominationAnalysis(irModule, mipsModule);
        mem2Reg = new Mem2Reg(irModule, mipsModule);
        removePhi = new RemovePhi(irModule, mipsModule);
        peepHole = new PeepHole(irModule, mipsModule);
    }

    public void deleteDeadCode() {
        deadCodeDelete.pass();
    }

    public void analysisDomination() {
        dominationAnalysis.pass();
    }

    public void mem2Reg() {
        mem2Reg.pass();
        try {
            // 输出到主输出路径
            PrintWriter writer = new PrintWriter("llvm_ir_phi.txt");
            writer.println(irModule.toString());
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void removePhi() {
        removePhi.pass();
    }

    public void peepHole() {
        this.peepHole.pass();
    }

    public void outputInFile() {
        try {
            // 输出到主输出路径
            PrintWriter writer = new PrintWriter(llvmOutputPath);
            writer.println(irModule.toString());
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
