package frontend.Visitor.Symbol;

public class IntSymbol implements Symbol {
    private final String name;
    private final boolean isArray;
    private final int scopeId;
    private final boolean isConst;
    private final SymbolType type;

    public IntSymbol(String name, SymbolType type, int scopeId) {
        this.name = name;
        this.isArray = isArrayType(type);
        this.scopeId = scopeId;
        this.isConst = (type == SymbolType.ConstInt || type == SymbolType.ConstIntArray);
        this.type = type;
    }

    public boolean isArrayType(SymbolType type) {
        return type == SymbolType.IntArray || type == SymbolType.ConstIntArray || type == SymbolType.StaticIntArray;
    }

    public boolean isArray() {
        return isArray;
    }

    public boolean isConst() {
        return isConst;
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
        return name + " " + type.toString();
    }
}
