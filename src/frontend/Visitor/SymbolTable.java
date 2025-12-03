package frontend.Visitor;

import frontend.Visitor.Symbol.Symbol;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class SymbolTable {
    // 每个SymbolTable对应一个作用域（一个大括号）
    // SymbolTable存储多个Symbol（在当前作用域下定义的变量）
    private final int scopeId;  // 当前SymbolTable的作用域id
    private final SymbolTable father;   // 当前大括号的上一层大括号
    private final ArrayList<SymbolTable> children;  // 当前层的所有下一层大括号
    private final LinkedHashMap<String, Symbol> symbols;  // 当前作用域内的所有变量，用变量名做key
    // 本质上是树的一个节点，并且同时拥有双向指针，一个指向父节点，剩下的指向所有下一层的子节点

    public SymbolTable(int scopeId, SymbolTable father) {
        this.scopeId = scopeId;
        this.father = father;   // 父指针指向father
        this.father.addChild(this); // 自身作为父节点的孩子
        this.children = new ArrayList<>();
        this.symbols = new LinkedHashMap<>();
    }

    public SymbolTable(int scopeId) {
        this.scopeId = scopeId;
        this.father = null;
        this.children = new ArrayList<>();
        this.symbols = new LinkedHashMap<>();
    }

    public LinkedHashMap<String, Symbol> getSymbols() {
        return symbols;
    }

    public int getScopeId() {
        return scopeId;
    }

    public SymbolTable getFather() {
        return father;
    }

    public ArrayList<SymbolTable> getChildren() {
        return children;
    }

    public void addChild(SymbolTable child) {
        children.add(child);
    }

    public boolean hasSymbol(String symbol) {
        return symbols.containsKey(symbol);
    }

    public Symbol getSymbol(String symbolName) {
        return symbols.get(symbolName);
    }

    public void putSymbol(Symbol symbol) {
        symbols.put(symbol.getName(), symbol);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Symbol> entry : symbols.entrySet()) {
            String key = entry.getKey();
            if (key.equals("getint")) continue;
            Symbol value = entry.getValue();
            sb.append(this.getScopeId() + " " + value.toString() + "\n");
        }
        for (SymbolTable child : children) {
            sb.append(child.toString());
        }
        return sb.toString();
    }
}
