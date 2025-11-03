package frontend.Lexer;

public enum TokenType {
    IDENFR,     // 变量名
    INTCON,     // 常数
    STRCON,     // 字符串
    CONSTTK,    // const
    INTTK,      // int
    STATICTK,   // static
    BREAKTK,    // break
    CONTINUETK, // continue
    IFTK,       // if
    MAINTK,     // main
    ELSETK,     // else
    NOT,        // not
    AND,        // and
    OR,         // or
    FORTK,      // for
    RETURNTK,   // return
    VOIDTK,     // void
    PLUS,       // +
    MINU,       // -
    PRINTFTK,   // printf
    MULT,       // *
    DIV,        // /
    MOD,        // %
    LSS,        // <
    LEQ,        // <=
    GRE,        // >
    GEQ,        // >=
    EQL,        // ==
    NEQ,        // !=
    SEMICN,     // ;
    COMMA,      // ,
    LPARENT,    // (
    RPARENT,    // )
    LBRACK,     // [
    RBRACK,     // ]
    LBRACE,     // {
    RBRACE,     // }
    ASSIGN,     // =
    EOF,
    PE,
    SE,
    ME,
    DE,
    MODE,
    QUERY,
    COLON,
    PP;

    @Override
    public String toString() {
        switch (this) {
            case IDENFR: return "IDENFR";
            case INTCON: return "INTCON";
            case STRCON: return "STRCON";
            case CONSTTK: return "CONSTTK";
            case INTTK: return "INTTK";
            case STATICTK: return "STATICTK";
            case BREAKTK: return "BREAKTK";
            case CONTINUETK: return "CONTINUETK";
            case IFTK: return "IFTK";
            case MAINTK: return "MAINTK";
            case ELSETK: return "ELSETK";
            case NOT: return "NOT";
            case AND: return "AND";
            case OR: return "OR";
            case FORTK: return "FORTK";
            case RETURNTK: return "RETURNTK";
            case VOIDTK: return "VOIDTK";
            case PLUS: return "PLUS";
            case MINU: return "MINU";
            case PRINTFTK: return "PRINTFTK";
            case MULT: return "MULT";
            case DIV: return "DIV";
            case MOD: return "MOD";
            case LSS: return "LSS";
            case LEQ: return "LEQ";
            case GRE: return "GRE";
            case GEQ: return "GEQ";
            case EQL: return "EQL";
            case NEQ: return "NEQ";
            case SEMICN: return "SEMICN";
            case COMMA: return "COMMA";
            case LPARENT: return "LPARENT";
            case RPARENT: return "RPARENT";
            case LBRACK: return "LBRACK";
            case RBRACK: return "RBRACK";
            case LBRACE: return "LBRACE";
            case RBRACE: return "RBRACE";
            case ASSIGN: return "ASSIGN";
            case EOF: return "EOF";
            case PE: return "PE";
            case SE: return "SE";
            case ME: return "ME";
            case DE: return "DE";
            case MODE: return "MODE";
            case QUERY: return "QUERY";
            case COLON: return "COLON";
            case PP: return "PP";
            default: return "UNKNOWN";
        }
    }
}