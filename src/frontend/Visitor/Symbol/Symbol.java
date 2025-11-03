package frontend.Visitor.Symbol;

public interface Symbol {
    String getName();

    int getScopeId();

    String toString();

    SymbolType getType();
}
