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
        for (int i = 0; i < 4; i++) {
            sb.append(functions.get(i).toString());
            sb.append("\n");
        }
        sb.append("\n\n");
        for (GlobalString constString : globalStrings) {
            sb.append(constString.toString());
            sb.append("\n");
        }
        if (!globalStrings.isEmpty()) sb.append("\n");
        for (GlobalVar globalVar : globalVars) {
            sb.append(globalVar.toString());
            sb.append("\n");
        }
        if (!globalVars.isEmpty()) sb.append("\n");
        int i = 0;
        for (Function function : functions) {
            i++;
            if (i <= 4) continue;
            sb.append(function.toString());
            sb.append("\n\n");
        }
        sb.deleteCharAt(sb.length() - 1);   // 删除最后的换行符
        return sb.toString();
    }
}
