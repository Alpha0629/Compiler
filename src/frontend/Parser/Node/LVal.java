package frontend.Parser.Node;

import frontend.Lexer.Token;
import frontend.Lexer.TokenType;
import frontend.Parser.Node.Exp.Exp;
import frontend.Visitor.Symbol.IntSymbol;
import frontend.Visitor.Symbol.Symbol;
import frontend.Visitor.Visitor;
import frontend.Visitor.SymbolTable;

public class LVal {
    // 左值表达式 LVal → Ident ['[' Exp ']'] // k
    private final Token ident;
    private final Exp exp;

    public LVal(Token ident, Exp exp) {
        this.ident = ident;
        this.exp = exp;
    }

    public Token getIdent() {
        return ident;
    }

    public Exp getExp() {
        return exp;
    }

    public boolean isArray() {
        // 对一个左值赋值，能被赋值的左值只有普通变量和数组
        // 要去符号表里面找这个ident
        SymbolTable curTable = Visitor.getSymbolTableStack().top();
        // System.out.println("当前变量名是：" + ident.getKey());
        while (curTable != null) {
            if (curTable.hasSymbol(ident.getKey())) {
                Symbol symbol = curTable.getSymbol(ident.getKey());
                // System.out.println("匹配到的符号：" + symbol.getName());
                if (exp == null) return ((IntSymbol) symbol).isArray();
                else return false;
            }
            curTable = curTable.getFather();
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ident.toString());
        if (exp != null) {
            sb.append(TokenType.LBRACK.toString() + " " + "[" + "\n");
            sb.append(exp.toString());
            sb.append(TokenType.RBRACK.toString() + " " + "]" + "\n");
        }
        sb.append("<LVal>");
        sb.append('\n');
        return sb.toString();
    }
}
