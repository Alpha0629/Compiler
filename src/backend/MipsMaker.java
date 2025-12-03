package backend;

import llvm.IrModule;
import llvm.values.Function;
import llvm.values.GlobalString;
import llvm.values.GlobalVar;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class MipsMaker {
    private final MipsModule mipsModule;
    private final IrModule irModule;
    private final String outputPath;
    private final MipsUtils mipsUtils;

    public MipsMaker(MipsModule mipsModule, IrModule irModule, String outputPath) {
        this.mipsModule = mipsModule;
        this.irModule = irModule;
        this.outputPath = outputPath;
        this.mipsUtils = new MipsUtils(mipsModule);
    }

    public void buildMips() {
        ArrayList<GlobalString> globalStrings = irModule.getGlobalStrings();
        for (GlobalString globalString : globalStrings) {
            buildGlobalStringMips(globalString);
        }
        ArrayList<GlobalVar> globalVars = irModule.getGlobalVars();
        for (GlobalVar globalVar : globalVars) {
            buildGlobalVarMips(globalVar);
        }
        ArrayList<Function> functions = irModule.getFunctions();
        Function mainFunction = functions.get(functions.size() - 1);    // 先拿到主函数
        buildMainFunction(mainFunction);
        int i = 0;
        for (Function function : functions) {
            if (i == functions.size() - 1) break;
            buildFunction(function);
            i++;
        }
    }

    public void buildGlobalStringMips(GlobalString globalString) {
        mipsUtils.makeMipsGlobalString(globalString);
    }

    public void buildGlobalVarMips(GlobalVar globalVar) {
        if (globalVar.isZeroInitializer()) mipsUtils.makeMipsZeroInitializer(globalVar);
        else if (globalVar.isArray()) mipsUtils.makeMipsGlobalArray(globalVar);
        else mipsUtils.makeMipsGlobalInteger(globalVar);
    }

    public void buildMainFunction(Function mainFunction) {

    }

    public void buildFunction(Function function) {

    }

    public void outputInFile() {
        try {
            // 输出到主输出路径
            PrintWriter writer = new PrintWriter(outputPath);
            writer.println(mipsModule.toString());
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
