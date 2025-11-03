package frontend.Visitor.Symbol;

import java.util.ArrayList;

public class FuncSymbol implements Symbol {
    private final String name;
    private final int count;  // 参数个数
    private final int scopeId;
    private final SymbolType type;
    private final ReturnType returnType;
    private final ArrayList<Symbol> params;

    public FuncSymbol(String name, SymbolType type, ReturnType returnType, int scopeId, ArrayList<Symbol> params) {
        this.name = name;
        this.scopeId = scopeId;
        this.type = type;
        this.returnType = returnType;
        this.params = params;
        this.count = params.size();
    }

    public ReturnType getReturnType() {
        return returnType;
    }

    public ArrayList<Symbol> getParams() {
        return params;
    }

    public int getCount() {
        return count;
    }

    @Override
    public SymbolType getType() {
        return type;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int getScopeId() {
        return this.scopeId;
    }

    @Override
    public String toString() {
        return name + " " +  returnType.toString() + type.toString();
    }
}
