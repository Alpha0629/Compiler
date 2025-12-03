package llvm;

import llvm.values.Value;

import java.util.HashMap;

public class IrSymbolTable {
    private final HashMap<String, Value> symbols;

    public IrSymbolTable() {
        symbols = new HashMap<String, Value>();
    }

    public void putSymbol(String name, Value value) {
        symbols.put(name, value);
    }

    public Value getSymbol(String name) {
        return symbols.get(name);
    }

    public boolean hasSymbol(String name) {
        return symbols.containsKey(name);
    }
}
