package frontend.Parser.Node;

import java.util.ArrayList;

import frontend.Lexer.TokenType;

public class FuncFParams {
    // 函数形参表 FuncFParams → FuncFParam { ',' FuncFParam }
    private final ArrayList<FuncFParam> funcFParams;

    public FuncFParams(ArrayList<FuncFParam> funcFParams) {
        this.funcFParams = funcFParams;
    }

    public ArrayList<FuncFParam> getFuncFParams() {
        return funcFParams;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(funcFParams.get(0).toString());
        for (int i = 1; i < funcFParams.size(); i++) {
            sb.append(TokenType.COMMA.toString() + " " + "," + "\n");
            sb.append(funcFParams.get(i).toString());
        }
        sb.append("<FuncFParams>");
        sb.append('\n');
        return sb.toString();
    }
}
