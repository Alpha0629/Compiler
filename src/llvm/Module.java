package llvm;

import llvm.values.Function;
import llvm.values.GlobalVar;
import llvm.values.GlobalString;

import java.util.ArrayList;

public class Module {
    private final ArrayList<GlobalString> globalStrings;
    private final ArrayList<GlobalVar> globalVars;
    private final ArrayList<Function> functions;

    public Module() {
        this.globalStrings = new ArrayList<>();
        this.globalVars = new ArrayList<>();
        this.functions = new ArrayList<>();
    }

    public void addConstString(GlobalString constString) {
        this.globalStrings.add(constString);
    }

    public void addGlobalVar(GlobalVar globalVar) {
        this.globalVars.add(globalVar);
    }

    public void addFunction(Function function) {
        this.functions.add(function);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (GlobalString constString : globalStrings) {
            sb.append(constString.toString());
            sb.append("\n");
        }
        for (GlobalVar globalVar : globalVars) {
            sb.append(globalVar.toString());
            sb.append("\n");
        }
        for (Function function : functions) {
            sb.append(function.toString());
            sb.append("\n");
        }
        sb.deleteCharAt(sb.length() - 1);   // 删除最后的换行符
        return sb.toString();
    }
}
