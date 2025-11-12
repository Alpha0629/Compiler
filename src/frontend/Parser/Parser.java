package frontend.Parser;

import java.util.ArrayList;
import java.io.PrintWriter;
import java.io.IOException;

import frontend.Lexer.Token;
import frontend.Lexer.TokenList;
import frontend.Lexer.TokenType;
import frontend.Parser.Node.Exp.AddExp;
import frontend.Parser.Node.BType;
import frontend.Parser.Node.Block;
import frontend.Parser.Node.BlockItem;
import frontend.Parser.Node.CompUnit;
import frontend.Parser.Node.Exp.Cond;
import frontend.Parser.Node.ConstDecl;
import frontend.Parser.Node.ConstDef;
import frontend.Parser.Node.Exp.ConstExp;
import frontend.Parser.Node.ConstInitVal;
import frontend.Parser.Node.Decl;
import frontend.Parser.Node.Exp.EqExp;
import frontend.Parser.Node.Exp.Exp;
import frontend.Parser.Node.ForStmt;
import frontend.Parser.Node.FuncDef;
import frontend.Parser.Node.FuncFParam;
import frontend.Parser.Node.FuncFParams;
import frontend.Parser.Node.FuncRParams;
import frontend.Parser.Node.FuncType;
import frontend.Parser.Node.InitVal;
import frontend.Parser.Node.Exp.LAndExp;
import frontend.Parser.Node.Exp.LOrExp;
import frontend.Parser.Node.LVal;
import frontend.Parser.Node.MainFuncDef;
import frontend.Parser.Node.Exp.MulExp;
import frontend.Parser.Node.Number;
import frontend.Parser.Node.Exp.PrimaryExp;
import frontend.Parser.Node.Exp.RelExp;
import frontend.Parser.Node.Exp.UnaryExp;
import frontend.Parser.Node.UnaryOp;
import frontend.Parser.Node.VarDecl;
import frontend.Parser.Node.VarDef;
import frontend.Parser.Node.Statement.AssignmentStmt;
import frontend.Parser.Node.Statement.BlockStmt;
import frontend.Parser.Node.Statement.BreakStmt;
import frontend.Parser.Node.Statement.ContinueStmt;
import frontend.Parser.Node.Statement.ExpStmt;
import frontend.Parser.Node.Statement.ForLoopStmt;
import frontend.Parser.Node.Statement.IfStmt;
import frontend.Parser.Node.Statement.PrintfStmt;
import frontend.Parser.Node.Statement.ReturnStmt;
import frontend.Parser.Node.Statement.Stmt;
import frontend.Error.Error;
import frontend.Error.ErrorType;

public class Parser {
    private final TokenList tokens;
    private final String outputPath;
    private final String errorPath;
    private final String additionalOutputPath;
    private Token now;
    private CompUnit compUnit;
    private final ArrayList<Error> errors;
    private boolean check = true;


    public Parser(TokenList tokens, String outputPath, String errorPath, ArrayList<Error> errors) {
        this.tokens = tokens;
        this.outputPath = outputPath;
        this.errorPath = errorPath;
        this.additionalOutputPath = null;
        this.errors = errors;
    }

    public Parser(TokenList tokens, String outputPath, String errorPath, String additionalOutputPath, ArrayList<Error> errors) {
        this.tokens = tokens;
        this.outputPath = outputPath;
        this.errorPath = errorPath;
        this.additionalOutputPath = additionalOutputPath;
        this.errors = errors;
    }

    public CompUnit parse() {
        now = getNextToken();
        if (now.getValue() != TokenType.EOF) {
            compUnit = parseCompUnit();
        }
        return compUnit;
    }


    // CompUnit → {Decl} {FuncDef} MainFuncDef
    // {Decl} 第一个终结符为const或者int
    // {FuncDef} 第一个终结符为int或者void
    // MainFuncDef 第一个终结符为int
    public CompUnit parseCompUnit() {
        // 现在指向第一个Token
        // 先判断
        // System.out.println("正在解析CompUnit");
        ArrayList<Decl> decls = new ArrayList<>();
        ArrayList<FuncDef> funcDefs = new ArrayList<>();
        MainFuncDef mainFuncDef = null;
        //  CompUnit → {Decl} {FuncDef} MainFuncDef
        while (now.getValue() == TokenType.INTTK || now.getValue() == TokenType.CONSTTK || now.getValue() == TokenType.VOIDTK) {
            if (now.getValue() == TokenType.INTTK) {
                Token preRead = getPreReadToken();
                if (preRead.getValue() == TokenType.MAINTK) {   // preRead == main
                    // 有且仅有一个主函数定义，读到就可以break了
                    mainFuncDef = parseMainFuncDef();
                    break;
                } else if (preRead.getValue() == TokenType.IDENFR) {    // preRead == 变量/函数名
                    Token prePreRead = getPrePreReadToken();
                    if (prePreRead.getValue() == TokenType.LPARENT) {    // prePreRead == '('
                        funcDefs.add(parseFuncDef());
                    } else {
                        decls.add(parseDecl());
                    }
                }
            } else if (now.getValue() == TokenType.CONSTTK) {
                while (now.getValue() == TokenType.CONSTTK) {
                    decls.add(parseDecl());
                }
            } else if (now.getValue() == TokenType.VOIDTK) {
                while (now.getValue() == TokenType.VOIDTK) {
                    funcDefs.add(parseFuncDef());
                }
            }
        }
        // // System.out.println(decls.size());
        return new CompUnit(decls, funcDefs, mainFuncDef);
    }

    public Decl parseDecl() {
        // 声明 Decl → ConstDecl | VarDecl 
        // now == const或int,也有可能是static
        // System.out.println("正在解析Decl");
        ConstDecl constDecl = null;
        VarDecl varDecl = null;

        if (now.getValue() == TokenType.CONSTTK) {
            constDecl = parseConstDecl();
            return new Decl(constDecl);
        } else {
            assert (now.getValue() == TokenType.STATICTK || now.getValue() == TokenType.INTTK);
            varDecl = parseVarDecl();
            // // System.out.println(varDecl.toString());
            return new Decl(varDecl);
        }
    }

    public ConstDecl parseConstDecl() {
        // 常量声明 ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';' // i
        assert (now.getValue() == TokenType.CONSTTK);

        // System.out.println("正在解析ConstDecl");

        BType bType = null;
        ArrayList<ConstDef> constDefs = new ArrayList<>();
        // now == const
        now = getNextToken();   // now == BType即int
        bType = parseBType();
        // now == 变量名
        constDefs.add(parseConstDef());
        // now == ',' 或者 ';' 或者出现错误i
        while (now.getValue() == TokenType.COMMA) {
            now = getNextToken();
            constDefs.add(parseConstDef());
            // 每次parseConstDef后，now要么为','要么为';'或者出现错误i
        }
        // 此时now只能是';'或者出现错误i
        if (now.getValue() != TokenType.SEMICN) {
            addError(new Error(ErrorType.i, getLastToken().getLine()));
        } else {
            now = getNextToken();
        }
        // 现在now一定是下一个Token
        return new ConstDecl(bType, constDefs);
    }

    public BType parseBType() {
        // 基本类型 BType → 'int'
        // now == BType即int
        assert (now.getValue() == TokenType.INTTK);

        // System.out.println("正在解析BType");

        Token token = new Token(now.getKey(), now.getValue(), now.getLine());
        now = getNextToken();   // now 指向了下一个Token
        return new BType(token);
    }

    public ConstDef parseConstDef() {
        // 常量定义 ConstDef → Ident [ '[' ConstExp ']' ] '=' ConstInitVal // k
        // array[2]  = | ch = 
        // now== Ident即变量名
        assert (now.getValue() == TokenType.IDENFR);

        // System.out.println("正在解析ConstDef");

        Token token = new Token(now.getKey(), now.getValue(), now.getLine());
        ConstExp constExp = null;
        ConstInitVal constInitVal = null;

        now = getNextToken();
        if (now.getValue() == TokenType.LBRACK) {
            now = getNextToken();
            constExp = parseConstExp();
            // 现在now == ']'或者出现错误k(即now == '=')
            if (now.getValue() != TokenType.RBRACK) {
                addError(new Error(ErrorType.k, getLastToken().getLine()));
            } else {
                now = getNextToken();
            }
        }

        assert (now.getValue() == TokenType.ASSIGN);
        now = getNextToken();
        constInitVal = parseConstInitVal();

        return new ConstDef(token, constExp, constInitVal);
    }

    public ConstExp parseConstExp() {
        // 常量表达式 ConstExp → AddExp 注：使用的 Ident 必须是常量 
        // System.out.println("正在解析ConstExp");
        return new ConstExp(parseAddExp());
    }

    public ConstInitVal parseConstInitVal() {
        // 常量初值 ConstInitVal → ConstExp | '{' [ ConstExp { ',' ConstExp } ] '}' | StringConst
        ConstExp constExp = null;   // 针对第一种可能
        ArrayList<ConstExp> constExps = new ArrayList<>();  // 针对数组赋值
        Token stringConst = null; // 针对字符串赋值

        // System.out.println("正在解析ConstInitVal");

        if (now.getValue() == TokenType.LBRACE) {
            // '{' [ ConstExp { ',' ConstExp } ] '}'
            // { } 可以为空
            // {1}
            // {1, 2, 3}
            now = getNextToken();   // 往下读，看now是不是右括号
            if (now.getValue() == TokenType.RBRACE) {   // 只是一个大括号{}
                now = getNextToken();
                return new ConstInitVal(constExps);
            } else {    // 有1到多个exp
                constExps.add(parseConstExp());
                while (now.getValue() == TokenType.COMMA) { // 如果当前是逗号，说明有多个exp，继续解析
                    now = getNextToken();   // 读到exp第一个终结符
                    constExps.add(parseConstExp());
                }
                assert (now.getValue() == TokenType.RBRACE);
                now = getNextToken();   // 读入下一个
                return new ConstInitVal(constExps);
            }
        } else if (now.getValue() == TokenType.STRCON) {
            // StringConst是终结符，不需要解析
            stringConst = new Token(now.getKey(), now.getValue(), now.getLine());
            return new ConstInitVal(stringConst);
        } else {
            constExp = parseConstExp();
            return new ConstInitVal(constExp);
        }
    }

    public VarDecl parseVarDecl() {
        // 变量声明 VarDecl → [ 'static' ] BType VarDef { ',' VarDef } ';' // i
        Token staticToken = null;
        BType bType = null;
        ArrayList<VarDef> varDefs = new ArrayList<VarDef>();

        if (now.getValue() == TokenType.STATICTK) {
            staticToken = new Token(now.getKey(), now.getValue(), now.getLine());
            now = getNextToken();
        }

        assert (now.getValue() == TokenType.INTTK);
        bType = parseBType();

        assert (now.getValue() == TokenType.IDENFR);

        varDefs.add(parseVarDef());
        // now == ',' 或者 ';' 或者出现错误i
        while (now.getValue() == TokenType.COMMA) {
            now = getNextToken();
            varDefs.add(parseVarDef());
            // 每次parseConstDef后，now要么为','要么为';'或者出现错误i
        }

        if (now.getValue() != TokenType.SEMICN) {
            addError(new Error(ErrorType.i, getLastToken().getLine()));
        } else {
            // 往后读一个
            now = getNextToken();
        }

        return new VarDecl(staticToken, bType, varDefs);
    }

    public VarDef parseVarDef() {
        // 变量定义 VarDef → Ident [ '[' ConstExp ']' ] | Ident [ '[' ConstExp ']' ] '=' InitVal // k
        assert (now.getValue() == TokenType.IDENFR);
        Token ident = new Token(now.getKey(), now.getValue(), now.getLine());
        ConstExp constExp = null;
        InitVal initVal = null;

        now = getNextToken();
        // // System.out.println("now: " + now.toString());
        if (now.getValue() == TokenType.LBRACK) {   // 如果读到了左中括号，则说明有ConstExp
            now = getNextToken();   // 先读入
            constExp = parseConstExp();     // 解析

            if (now.getValue() != TokenType.RBRACK) {   // 理应返回右中括号，如果不是则发生错误k
                addError(new Error(ErrorType.k, getLastToken().getLine()));
            } else {    // 就是右中括号，读下一个
                now = getNextToken();
            }

            if (now.getValue() == TokenType.ASSIGN) {  // 如果是等号，则说明是右边的那种情况
                now = getNextToken();   // 此时now是InitVal的首终结符
                initVal = parseInitVal();   // 解析
                // 此时now指向了下一个终结符
                return new VarDef(ident, constExp, initVal);
            } else {    // 读到的不是等号， 说明是左边那种情况
                // 就相当于先读入了，不需要处理
                // 此时now指向了下一个终结符
                return new VarDef(ident, constExp);
            }
        } else {    // 没有ConstExp的情况
            if (now.getValue() == TokenType.ASSIGN) {  // 如果是等号，则说明是右边的那种情况
                now = getNextToken();   // 此时now是InitVal的首终结符
                initVal = parseInitVal();   // 解析
                // 此时now指向了下一个终结符
                return new VarDef(ident, initVal);
            } else {    // 读到的不是等号， 说明是左边那种情况
                // 此时now指向了下一个终结符
                return new VarDef(ident);
            }
        }
    }

    public InitVal parseInitVal() {
        // 变量初值 InitVal → Exp | '{' [ Exp { ',' Exp } ] '}' 
        // System.out.println("正在解析InitVal");
        Exp exp = null;
        ArrayList<Exp> exps = new ArrayList<>();

        if (now.getValue() == TokenType.LBRACE) {   // 是右边这种情况
            now = getNextToken();   // 往下读，看now是不是右括号
            // System.out.println(now.toString());
            if (now.getValue() == TokenType.RBRACE) {   // 只是一个大括号{}
                now = getNextToken();
                return new InitVal(exps);
            } else {    // 有1到多个exp
                exps.add(parseExp());
                while (now.getValue() == TokenType.COMMA) { // 如果当前是逗号，说明有多个exp，继续解析
                    now = getNextToken();   // 读到exp第一个终结符
                    exps.add(parseExp());
                }
                // // System.out.println(exps.size());
                assert (now.getValue() == TokenType.RBRACE);
                now = getNextToken();   // 读入下一个
                return new InitVal(exps);
            }
        } else {    // 是左边这种情况
            exp = parseExp();
            return new InitVal(exp);
        }
    }

    public Exp parseExp() {
        // 表达式 Exp → AddExp 
        // System.out.println("正在解析Exp");
        return new Exp(parseAddExp());
    }

    public FuncDef parseFuncDef() {
        // 函数定义 FuncDef → FuncType Ident '(' [FuncFParams] ')' Block // j
        assert (now.getValue() == TokenType.VOIDTK || now.getValue() == TokenType.INTTK);

        // System.out.println("正在解析FuncDef");

        FuncType funcType = null;
        Token ident = null;
        FuncFParams funcFParams = null;
        Block block = null;
        // 当前now指向FuncType的第一个终结符
        funcType = parseFuncType();
        assert (now.getValue() == TokenType.IDENFR); // 现在now应该指向标识符
        ident = new Token(now.getKey(), now.getValue(), now.getLine());
        now = getNextToken();
        assert (now.getValue() == TokenType.LPARENT);    // 现在是左括号
        now = getNextToken();   // 继续读
        if (now.getValue() == TokenType.INTTK) {    // 解析到了函数参数列表
            funcFParams = parseFuncFParams();   // 现在now应该指向右括号
        } else {
            // 没有实际的参数，现在now也应该指向右括号
        }

        if (now.getValue() != TokenType.RPARENT) {
            // 右括号缺失，处理错误j
            addError(new Error(ErrorType.j, getLastToken().getLine()));
        } else {    // 否则，令now已经指向了Block中的第一个终结符即左大括号
            now = getNextToken();
        }

        block = parseBlock();
        return new FuncDef(funcType, ident, funcFParams, block);
    }

    public FuncType parseFuncType() {
        assert (now.getValue() == TokenType.VOIDTK || now.getValue() == TokenType.INTTK);

        // System.out.println("正在解析FuncType");

        Token voidToken = null;
        Token intToken = null;

        if (now.getValue() == TokenType.VOIDTK) {
            voidToken = new Token(now.getKey(), now.getValue(), now.getLine());
        } else {
            intToken = new Token(now.getKey(), now.getValue(), now.getLine());
        }

        now = getNextToken();   // 往后读一个
        return new FuncType(voidToken, intToken);
    }

    public FuncFParams parseFuncFParams() {
        // 函数形参表 FuncFParams → FuncFParam { ',' FuncFParam }
        assert (now.getValue() == TokenType.INTTK);

        // System.out.println("正在解析FuncFParams");

        ArrayList<FuncFParam> funcFParams = new ArrayList<>();

        funcFParams.add(parseFuncFParam());
        while (now.getValue() == TokenType.COMMA) {
            // System.out.println("多个参数");
            now = getNextToken();
            funcFParams.add(parseFuncFParam());
        }
        // 退出循环，此时now指向下一个，正确
        return new FuncFParams(funcFParams);
    }

    public FuncFParam parseFuncFParam() {
        // 函数形参 FuncFParam → BType Ident ['[' ']'] // k
        assert (now.getValue() == TokenType.INTTK);

        // System.out.println("正在解析FuncFParam");

        BType bType = null;
        Token identToken = null;
        Token lbrackToken = null;
        Token rbrackToken = null;
        // 采用Token来标记是不是数组形式的定义

        bType = parseBType();
        identToken = new Token(now.getKey(), now.getValue(), now.getLine());
        now = getNextToken();
        if (now.getValue() == TokenType.LBRACK) {   // 存在数组形式
            lbrackToken = new Token(now.getKey(), now.getValue(), now.getLine());
            rbrackToken = new Token("]", TokenType.RBRACK, now.getLine());
            now = getNextToken();
            if (now.getValue() != TokenType.RBRACK) {
                addError(new Error(ErrorType.k, getLastToken().getLine()));
            } else {
                now = getNextToken();   // 往后读一个
            }
        } else {
            // now已经读到下一个了
        }
        return new FuncFParam(bType, identToken, lbrackToken, rbrackToken);
    }

    public Block parseBlock() {
        // 语句块 Block → '{' { BlockItem } '}' 
        assert (now.getValue() == TokenType.LBRACE);
        Token lBrace = new Token(now.getKey(), now.getValue(), now.getLine());
        // System.out.println("正在解析Block");

        ArrayList<BlockItem> blockItems = new ArrayList<>();
        now = getNextToken();
        while (now.getValue() != TokenType.RBRACE) {
            blockItems.add(parseBlockItem());
        }
        assert (now.getValue() == TokenType.RBRACE);
        Token rBrace = new Token(now.getKey(), now.getValue(), now.getLine());
        now = getNextToken();   // 往后读一个
        return new Block(blockItems, lBrace, rBrace);
    }

    public BlockItem parseBlockItem() {
        // 语句块项 BlockItem → Decl | Stmt
        // 如果是Decl，则第一个终结符是const或者static或者int
        // 如果是Stmt，则第一个终结符是/标识符/运算符/左括号/左大括号/if/for/break/return/printf
        // 交集为空，不需要预读

        // System.out.println("正在解析BlockItem");

        Decl decl = null;
        Stmt stmt = null;
        if (now.getValue() == TokenType.CONSTTK || now.getValue() == TokenType.STATICTK || now.getValue() == TokenType.INTTK) {
            decl = parseDecl();
            // 解析完声明后，now自动指向下一个
            return new BlockItem(decl);
        } else {
            stmt = parseStmt();
            // 解析完声明后，now自动指向下一个
            return new BlockItem(stmt);
        }
    }

    public MainFuncDef parseMainFuncDef() {
        // 主函数定义 MainFuncDef → 'int' 'main' '(' ')' Block // j

        // System.out.println("正在解析MainFuncDef");

        Block block = null;

        assert (now.getValue() == TokenType.INTTK);

        now = getNextToken();
        assert (now.getValue() == TokenType.MAINTK);

        now = getNextToken();
        assert (now.getValue() == TokenType.LPARENT);

        now = getNextToken();
        // assert(now.getStoredValue() == TokenType.RPARENT);
        if (now.getValue() != TokenType.RPARENT) {
            addError(new Error(ErrorType.j, getLastToken().getLine()));
        } else {
            // 现在now指向右括号
            now = getNextToken();
        }
        block = parseBlock();
        // now指向了下一个
        return new MainFuncDef(block);
    }

    public ForStmt parseForStmt() {
        // 语句 ForStmt → LVal '=' Exp { ',' LVal '=' Exp } 

        // System.out.println("正在解析ForStmt");

        ArrayList<LVal> lVals = new ArrayList<>();
        ArrayList<Exp> exps = new ArrayList<>();

        assert (now.getValue() == TokenType.IDENFR); // now指向LVal的第一个终结符，即标识符

        lVals.add(parseLVal());
        // System.out.println("now的值是：" + now.toString());
        assert (now.getValue() == TokenType.ASSIGN);
        now = getNextToken();
        exps.add(parseExp());

        while (now.getValue() == TokenType.COMMA) {
            now = getNextToken();
            lVals.add(parseLVal());
            assert (now.getValue() == TokenType.ASSIGN);
            now = getNextToken();
            exps.add(parseExp());
        }
        // 现在now指向下一个
        return new ForStmt(lVals, exps);
    }

    public LVal parseLVal() {
        // 左值表达式 LVal → Ident ['[' Exp ']'] // k
        assert (now.getValue() == TokenType.IDENFR);

        // System.out.println("正在解析LVal");

        Token ident = new Token(now.getKey(), now.getValue(), now.getLine());
        Exp exp = null;
        now = getNextToken();
        if (now.getValue() == TokenType.LBRACK) {
            now = getNextToken();
            exp = parseExp();
            if (now.getValue() != TokenType.RBRACK) {
                addError(new Error(ErrorType.k, getLastToken().getLine()));
            } else {
                now = getNextToken();
            }
        } else {
            // now已经读到下一个了
        }
        return new LVal(ident, exp);
    }

    public Cond parseCond() {
        // 条件表达式 Cond → LOrExp
        // System.out.println("正在解析Cond");
        return new Cond(parseLOrExp());
    }

    public PrimaryExp parsePrimaryExp() {
        // 基本表达式 PrimaryExp → '(' Exp ')' | LVal | Number // j

        // System.out.println("正在解析PrimaryExp");

        Exp exp = null;
        LVal lVal = null;
        Number number = null;

        if (now.getValue() == TokenType.LPARENT) {
            now = getNextToken();
            exp = parseExp();
            if (now.getValue() != TokenType.RPARENT) {
                addError(new Error(ErrorType.j, getLastToken().getLine()));
            } else {
                now = getNextToken();
            }
            return new PrimaryExp(exp);
        } else if (now.getValue() == TokenType.IDENFR) {
            lVal = parseLVal();
            // 此时now指向下一个
            return new PrimaryExp(lVal);
        } else {
            // // System.out.println(now.toString());
            assert (now.getValue() == TokenType.INTCON);
            number = parseNumber();
            // 此时now指向下一个
            return new PrimaryExp(number);
        }
    }

    public Number parseNumber() {
        // 数值 Number → IntConst 
        assert (now.getValue() == TokenType.INTCON);

        // System.out.println("正在解析Number");

        Token intConst = new Token(now.getKey(), now.getValue(), now.getLine());
        now = getNextToken();   // 往后读一个
        return new Number(intConst);
    }

    public UnaryExp parseUnaryExp() {
        // 一元表达式 UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp // j

        // System.out.println("正在解析UnaryExp");

        PrimaryExp primaryExp = null;
        Token ident = null;
        FuncRParams funcRParams = null;
        UnaryOp unaryOp = null;
        UnaryExp unaryExp = null;

        // PrimaryExp的最左终结符可能是Ident，因此需要预读
        // 优先处理第三种情况，第三种和前两种不构成冲突
        if (now.getValue() == TokenType.PLUS || now.getValue() == TokenType.MINU || now.getValue() == TokenType.NOT) {
            // 说明是第三种情况
            unaryOp = parseUnaryOp();
            unaryExp = parseUnaryExp();  // 递归调用
            return new UnaryExp(unaryOp, unaryExp);
        } else {
            if (now.getValue() == TokenType.IDENFR) {
                // 需要通过预读来判断是第一种还是第二种
                Token preRead = getPreReadToken();  // 仅预读，now指针不移动
                if (preRead.getValue() == TokenType.LPARENT) {
                    // 确定是第二种
                    ident = new Token(now.getKey(), now.getValue(), now.getLine());
                    now = getNextToken();
                    assert (now.getValue() == TokenType.LPARENT);
                    now = getNextToken();
                    // FuncRParams → Exp → Exp → AddExp → MulExp → UnaryExp → '(' or Ident or IntConst or '+' or '-' or '!'
                    if (now.getValue() == TokenType.LPARENT || now.getValue() == TokenType.IDENFR || now.getValue() == TokenType.INTCON || now.getValue() == TokenType.PLUS || now.getValue() == TokenType.MINU || now.getValue() == TokenType.NOT) {
                        // 左括号完了，括号内是有参数，还是缺少了右括号，这需要进行判定
                        // 如果是括号内有参数，则已经判定完成了
                        // 如果缺少了右括号，那么now有哪些可能？
                        funcRParams = parseFuncRParams();
                    }

                    if (now.getValue() != TokenType.RPARENT) {
                        addError(new Error(ErrorType.j, getLastToken().getLine()));
                    } else {
                        // now指向了右括号，因此要向后读一个
                        now = getNextToken();
                    }
                    return new UnaryExp(ident, funcRParams);
                } else {
                    // 是第一种，现在now指针还是指向PrimaryExp的第一个终结符
                    primaryExp = parsePrimaryExp();
                    return new UnaryExp(primaryExp);
                }
            } else {
                // 只可能是第一种了
                primaryExp = parsePrimaryExp();
                return new UnaryExp(primaryExp);
            }
        }
    }

    public UnaryOp parseUnaryOp() {
        // 单目运算符 UnaryOp → '+' | '−' | '!' 注：'!'仅出现在条件表达式中
        assert (now.getValue() == TokenType.PLUS || now.getValue() == TokenType.MINU || now.getValue() == TokenType.NOT);

        // System.out.println("正在解析UnaryOp");

        Token unaryOp = new Token(now.getKey(), now.getValue(), now.getLine());
        now = getNextToken();   // 往后读一个
        return new UnaryOp(unaryOp);
    }

    public FuncRParams parseFuncRParams() {
        // 函数实参表 FuncRParams → Exp { ',' Exp } 

        // System.out.println("正在解析FuncRParams");

        ArrayList<Exp> exps = new ArrayList<>();
        exps.add(parseExp());
        while (now.getValue() == TokenType.COMMA) {
            now = getNextToken();
            exps.add(parseExp());
        }
        return new FuncRParams(exps);
    }

    public AddExp parseAddExp() {
        // 加减表达式 AddExp → MulExp | AddExp ('+' | '−') MulExp
        // 消除左递归 AddExp → MulExp {('+' | '−') MulExp}

        // System.out.println("正在解析AddExp");

        ArrayList<MulExp> mulExps = new ArrayList<>();
        ArrayList<Token> signs = new ArrayList<>();    // 专门记录加号还是减号， 长度永远比mulExps少1
        mulExps.add(parseMulExp());
        while (now.getValue() == TokenType.PLUS || now.getValue() == TokenType.MINU) {
            signs.add(new Token(now.getKey(), now.getValue(), now.getLine()));
            now = getNextToken();
            mulExps.add(parseMulExp());
        }
        return new AddExp(mulExps, signs);
    }

    public MulExp parseMulExp() {
        // 乘除模表达式 MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp 
        // 消除左递归 MulExp → UnaryExp {('*' | '/' | '%') UnaryExp}

        // System.out.println("正在解析MulExp");

        ArrayList<UnaryExp> unaryExps = new ArrayList<>();
        ArrayList<Token> signs = new ArrayList<>();
        unaryExps.add(parseUnaryExp());
        while (now.getValue() == TokenType.MULT || now.getValue() == TokenType.DIV || now.getValue() == TokenType.MOD) {
            signs.add(new Token(now.getKey(), now.getValue(), now.getLine()));
            now = getNextToken();
            unaryExps.add(parseUnaryExp());
        }
        return new MulExp(unaryExps, signs);
    }

    public LOrExp parseLOrExp() {
        // 逻辑或表达式 LOrExp → LAndExp | LOrExp '||' LAndExp
        // 消除左递归 LOrExp → LAndExp {'||' LAndExp} 

        // System.out.println("正在解析LOrExp");

        ArrayList<LAndExp> lAndExps = new ArrayList<>();
        ArrayList<Token> signs = new ArrayList<>();
        lAndExps.add(parseLAndExp());
        while (now.getValue() == TokenType.OR) {
            signs.add(new Token(now.getKey(), now.getValue(), now.getLine()));
            now = getNextToken();
            lAndExps.add(parseLAndExp());
        }
        return new LOrExp(lAndExps, signs);
    }

    public LAndExp parseLAndExp() {
        // 逻辑与表达式 LAndExp → EqExp | LAndExp '&&' EqExp
        // 消除左递归 LAndExp → EqExp {'&&' EqExp}

        // System.out.println("正在解析LAndExp");

        ArrayList<EqExp> eqExps = new ArrayList<>();
        ArrayList<Token> signs = new ArrayList<>();
        eqExps.add(parseEqExp());
        while (now.getValue() == TokenType.AND) {
            signs.add(new Token(now.getKey(), now.getValue(), now.getLine()));
            now = getNextToken();
            eqExps.add(parseEqExp());
        }
        return new LAndExp(eqExps, signs);
    }

    public EqExp parseEqExp() {
        // 相等性表达式 EqExp → RelExp | EqExp ('==' | '!=') RelExp 
        // 消除左递归 EqExp → RelExp {('==' | '!=') RelExp}

        // System.out.println("正在解析EqExp");

        ArrayList<RelExp> relExps = new ArrayList<>();
        ArrayList<Token> signs = new ArrayList<>();
        relExps.add(parseRelExp());
        while (now.getValue() == TokenType.EQL || now.getValue() == TokenType.NEQ) {
            signs.add(new Token(now.getKey(), now.getValue(), now.getLine()));
            now = getNextToken();
            relExps.add(parseRelExp());
        }
        return new EqExp(relExps, signs);
    }

    public RelExp parseRelExp() {
        // 关系表达式 RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp 
        // 消除左递归 RelExp → AddExp {('<' | '>' | '<=' | '>=') AddExp}

        // System.out.println("正在解析RelExp");

        ArrayList<AddExp> addExps = new ArrayList<>();
        ArrayList<Token> signs = new ArrayList<>();
        addExps.add(parseAddExp());
        while (now.getValue() == TokenType.LSS || now.getValue() == TokenType.GRE || now.getValue() == TokenType.LEQ || now.getValue() == TokenType.GEQ) {
            signs.add(new Token(now.getKey(), now.getValue(), now.getLine()));
            now = getNextToken();
            addExps.add(parseAddExp());
        }
        return new RelExp(addExps, signs);
    }

    public Stmt parseStmt() {
        // 语句 Stmt → LVal '=' Exp ';' // i
        // | [Exp] ';' // i
        // | Block
        // | 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // j
        // | 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt 
        // | 'break' ';' | 'continue' ';' // i
        // | 'return' [Exp] ';' // i
        // | 'printf''('StringConst {','Exp}')'';' // i j 

        // 前两种需要特判，后六种可以直接判断
        // 第一种最左终结符的ident
        // 第二种最左终结符有'(' or ident or intConst or '+' or '-' or '!' 
        if (now.getValue() == TokenType.LBRACE) {
            // | Block

            // System.out.println("正在解析BlockStmt");
            Block block = parseBlock();
            return new BlockStmt(block);
        } else if (now.getValue() == TokenType.IFTK) {
            // | 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // j
            now = getNextToken();
            now = getNextToken();

            // System.out.println("正在解析IfStmt");

            Cond cond = parseCond();
            if (now.getValue() != TokenType.RPARENT) {
                addError(new Error(ErrorType.j, getLastToken().getLine()));
            } else {
                now = getNextToken();
            }
            ArrayList<Stmt> stmts = new ArrayList<>();
            stmts.add(parseStmt());
            while (now.getValue() == TokenType.ELSETK) {
                now = getNextToken();
                stmts.add(parseStmt());
            }
            // 此时now指向下一个
            return new IfStmt(cond, stmts);
        } else if (now.getValue() == TokenType.FORTK) {
            // 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt 

            // System.out.println("正在解析ForStmt");  

            ForStmt headForStmt = null;
            Cond cond = null;
            ForStmt rearForStmt = null;
            Stmt stmt = null;

            now = getNextToken();   // 左括号
            now = getNextToken();
            if (now.getValue() != TokenType.SEMICN) {
                headForStmt = parseForStmt();
            }
            now = getNextToken();
            if (now.getValue() != TokenType.SEMICN) {
                cond = parseCond();
            }
            now = getNextToken();
            if (now.getValue() != TokenType.RPARENT) {
                rearForStmt = parseForStmt();
            }
            assert (now.getValue() == TokenType.RPARENT);
            now = getNextToken();
            stmt = parseStmt();
            return new ForLoopStmt(headForStmt, cond, rearForStmt, stmt);
        } else if (now.getValue() == TokenType.BREAKTK) {
            Token Break = new Token(now.getKey(), now.getValue(), now.getLine());
            // 'break' ';' // i
            // System.out.println("正在解析BreakStmt");
            now = getNextToken();
            if (now.getValue() != TokenType.SEMICN) {
                addError(new Error(ErrorType.i, getLastToken().getLine()));
            } else {
                now = getNextToken();
            }
            return new BreakStmt(Break);
        } else if (now.getValue() == TokenType.CONTINUETK) {
            Token Continue = new Token(now.getKey(), now.getValue(), now.getLine());
            // 'continue' ';' // i
            // System.out.println("正在解析ContinueStmt");
            now = getNextToken();
            if (now.getValue() != TokenType.SEMICN) {
                addError(new Error(ErrorType.i, getLastToken().getLine()));
            } else {
                now = getNextToken();
            }
            return new ContinueStmt(Continue);
        } else if (now.getValue() == TokenType.RETURNTK) {
            Token Return = new Token(now.getKey(), now.getValue(), now.getLine());
            // 'return' [Exp] ';' // i
            // System.out.println("正在解析ReturnStmt");
            // 共有四种可能
            // return;
            // return   只能从下一行进行判断，下一个终结符可能是'{' or '}' or 'if' or 'else' or 'for' or 'break' or 'return' or 'printf' or const or int or static
            // return Exp; 
            // return Exp
            Exp exp = null;
            LVal lVal = null;

            now = getNextToken();   // 指向return的下一个
            if (now.getValue() == TokenType.SEMICN) {
                // 第一种
                now = getNextToken();
                return new ReturnStmt(Return, exp);
            } else if (now.getValue() == TokenType.LBRACE || now.getValue() == TokenType.RBRACE || now.getValue() == TokenType.IFTK || now.getValue() == TokenType.ELSETK ||now.getValue() == TokenType.FORTK || now.getValue() == TokenType.BREAKTK || now.getValue() == TokenType.RETURNTK || now.getValue() == TokenType.PRINTFTK || now.getValue() == TokenType.CONSTTK || now.getValue() == TokenType.INTTK || now.getValue() == TokenType.STATICTK) {
                // 第二种的部分情况
                addError(new Error(ErrorType.i, getLastToken().getLine()));
                return new ReturnStmt(Return, exp);
            } else {
                if (now.getValue() == TokenType.LPARENT || now.getValue() == TokenType.INTCON || now.getValue() == TokenType.PLUS || now.getValue() == TokenType.MINU || now.getValue() == TokenType.NOT) {
                    // 是第三种或者第四种，并且exp的最左终结符不是ident(部分情况)
                    exp = parseExp();
                    if (now.getValue() != TokenType.SEMICN) {
                        // 有错
                        addError(new Error(ErrorType.i, getLastToken().getLine()));
                    } else {
                        now = getNextToken();
                    }
                    return new ReturnStmt(Return, exp);
                } else {
                    Token preReadToken = getPreReadToken();
                    if (preReadToken.getValue() == TokenType.LPARENT) {
                        // 说明是Ident '(' [FuncRParams] ')'
                        exp = parseExp();
                        if (now.getValue() != TokenType.SEMICN) {
                            // 有错
                            addError(new Error(ErrorType.i, getLastToken().getLine()));
                        } else {
                            now = getNextToken();
                        }
                        return new ReturnStmt(Return, exp);
                    } else {
                        int p = tokens.getIndex() - 1;  // 当前now的索引
                        check = false;
                        lVal = parseLVal();     // 试探性的解析一边
                        check = true;

                        if (now.getValue() == TokenType.ASSIGN) {
                            now = tokens.setAndGetToken(p);  // 现在now已经恢复
                            // 说明是第二种情况，此时第二种已经穷尽
                            if (now.getValue() != TokenType.SEMICN) {   // 一定成立
                                addError(new Error(ErrorType.i, getLastToken().getLine()));
                            } else {
                                now = getNextToken();
                            }
                            return new ReturnStmt(Return, exp);
                        } else {    // 只有可能是三和四
                            now = tokens.setAndGetToken(p);  // 现在now已经恢复
                            exp = parseExp();
                            if (now.getValue() != TokenType.SEMICN) {
                                addError(new Error(ErrorType.i, getLastToken().getLine()));
                            } else {
                                now = getNextToken();
                            }
                            return new ReturnStmt(Return, exp);
                        }
                    }
                }
            }
        } else if (now.getValue() == TokenType.PRINTFTK) {
            Token Printf = new Token(now.getKey(), now.getValue(), now.getLine());
            // 'printf''('StringConst {','Exp}')'';' // i j 
            // System.out.println("正在解析PrintfStmt");
            Token stringToken = null;
            ArrayList<Exp> exps = new ArrayList<>();

            now = getNextToken();   // 现在指向左括号
            now = getNextToken();   // 现在指向字符串
            stringToken = new Token(now.getKey(), now.getValue(), now.getLine());
            now = getNextToken();
            while (now.getValue() == TokenType.COMMA) {
                now = getNextToken();
                exps.add(parseExp());
            }
            // 现在应该是右括号
            if (now.getValue() != TokenType.RPARENT) {
                addError(new Error(ErrorType.j, getLastToken().getLine()));
                // 如果缺了右括号，那么要判断当前是不是分号
                if (now.getValue() != TokenType.SEMICN) {
                    // 既有i，又有j错误
                    addError(new Error(ErrorType.i, getLastToken().getLine()));
                } else {
                    // 缺了右括号，但是没缺分号
                    now = getNextToken();
                }
            } else {
                // 没有缺右括号
                now = getNextToken();
                if (now.getValue() != TokenType.SEMICN) {
                    // 但是缺了分号
                    addError(new Error(ErrorType.i, getLastToken().getLine()));
                } else {
                    // now指向了分号，什么都没缺
                    now = getNextToken();
                }
            }
            return new PrintfStmt(Printf, stringToken, exps);
        }


        // 现在考虑前两种情况
        // LVal '=' Exp ';' // i
        // [Exp] ';' // i
        // c = getint();
        // System.out.println("正在解析ExpStmt");
        // System.out.println("now的值是：" + now.toString());
        Exp exp = null;
        LVal lVal = null;

        if (now.getValue() == TokenType.LPARENT || now.getValue() == TokenType.INTCON || now.getValue() == TokenType.PLUS || now.getValue() == TokenType.MINU || now.getValue() == TokenType.NOT) {
            // 属于第二种且有exp，并且exp的最左终结符不是ident
            exp = parseExp();
            if (now.getValue() != TokenType.SEMICN) {
                // 有错
                addError(new Error(ErrorType.i, getLastToken().getLine()));
            } else {
                now = getNextToken();
            }
            return new ExpStmt(exp);
        } else if (now.getValue() == TokenType.SEMICN) {
            // 属于第二种且无Exp且有分号结尾
            now = getNextToken();
            return new ExpStmt(exp);
        } else if (now.getValue() != TokenType.IDENFR) {
            // 应该不会出现这种情况
            // 如果不是ident，说明是第二种且引号缺失
            addError(new Error(ErrorType.i, getLastToken().getLine()));
            return new ExpStmt(exp);
        } else {
            // 只剩ident开头的可能了，现在now指向ident
            // exp → 可能是LVal，即Ident ['[' Exp ']']    也有可能是Ident '(' [FuncRParams] ')'
            // 唯一判据是第一种情况解析完LVal后有等号，而第二种没有
            Token preReadToken = getPreReadToken();
            if (preReadToken.getValue() == TokenType.LPARENT) {
                // 说明是Ident '(' [FuncRParams] ')'， 因此也就确定了此处是第二种而非第一种
                exp = parseExp();
                if (now.getValue() != TokenType.SEMICN) {
                    // 有错
                    addError(new Error(ErrorType.i, getLastToken().getLine()));
                } else {
                    now = getNextToken();
                }
                return new ExpStmt(exp);
            } else {
                // 不论是第一种还是第二种，都是以Ident ['[' Exp ']']开头，即LVal
                // 因此可以直接解析LVal，看解析后的now是不是'='
                // // System.out.println(1111111);
                int p = tokens.getIndex() - 1;  // 当前now的索引
                check = false;
                lVal = parseLVal();     // 试探性的解析一边
                check = true;
                if (now.getValue() == TokenType.ASSIGN) {
                    // 说明是第一种
                    now = tokens.setAndGetToken(p);
                    lVal = parseLVal(); // 重新解析一遍
                    now = getNextToken();
                    exp = parseExp();
                    if (now.getValue() != TokenType.SEMICN) {
                        addError(new Error(ErrorType.i, getLastToken().getLine()));
                    } else {
                        now = getNextToken();
                    }
                    return new AssignmentStmt(lVal, exp);
                } else {
                    // 是第二种
                    now = tokens.setAndGetToken(p);
                    exp = parseExp();
                    if (now.getValue() != TokenType.SEMICN) {
                        // 有错
                        addError(new Error(ErrorType.i, getLastToken().getLine()));
                    } else {
                        now = getNextToken();
                    }
                    return new ExpStmt(exp);
                }
            }
        }
    }

    public Token getNextToken() {
        return tokens.getNextToken();
    }

    public Token getPreReadToken() {
        return tokens.getPreReadToken();
    }

    public Token getPrePreReadToken() {
        return tokens.getPrePreReadToken();
    }

    public Token getLastToken() {
        return tokens.getLastToken();
    }

    public void addError(Error error) {
        if (check) this.errors.add(error);
    }

    public void outputInFile() {
        if (errors.isEmpty()) {
            try {
                // 输出到主输出路径
                PrintWriter writer = new PrintWriter(outputPath);
                writer.println(compUnit.toString());
                writer.close();

                // 如果指定了额外输出路径，也输出到该路径
                if (additionalOutputPath != null) {
                    PrintWriter additionalWriter = new PrintWriter(additionalOutputPath);
                    additionalWriter.println(compUnit.toString());
                    additionalWriter.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
