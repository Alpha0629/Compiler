package frontend.Lexer;

public enum TokenType {
    IDENFR,
    INTCON,
    STRCON,
    CONSTTK,
    INTTK,
    STATICTK,
    BREAKTK,
    CONTINUETK,
    IFTK,
    MAINTK,
    ELSETK,
    NOT,
    AND,
    OR,
    FORTK,
    RETURNTK,
    VOIDTK,
    PLUS,
    MINU,
    PRINTFTK,
    MULT,
    DIV,
    MOD,
    LSS,
    LEQ,
    GRE,
    GEQ,
    EQL,
    NEQ,
    SEMICN,
    COMMA,
    LPARENT,
    RPARENT,
    LBRACK,
    RBRACK,
    LBRACE,
    RBRACE,
    ASSIGN,
    EOF;

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
            default: return "UNKNOWN";
        }
    }
}