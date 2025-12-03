package frontend.Visitor;

import frontend.Error.ErrorType;
import frontend.Lexer.Token;
import frontend.Lexer.TokenType;
import frontend.Parser.Node.BType;
import frontend.Parser.Node.Block;
import frontend.Parser.Node.BlockItem;
import frontend.Parser.Node.CompUnit;
import frontend.Parser.Node.ConstDecl;
import frontend.Parser.Node.ConstDef;
import frontend.Parser.Node.ConstInitVal;
import frontend.Parser.Node.Decl;
import frontend.Parser.Node.Exp.AddExp;
import frontend.Parser.Node.Exp.Cond;
import frontend.Parser.Node.Exp.ConstExp;
import frontend.Parser.Node.Exp.EqExp;
import frontend.Parser.Node.Exp.Exp;
import frontend.Parser.Node.Exp.LAndExp;
import frontend.Parser.Node.Exp.LOrExp;
import frontend.Parser.Node.Exp.MulExp;
import frontend.Parser.Node.Exp.PrimaryExp;
import frontend.Parser.Node.Exp.RelExp;
import frontend.Parser.Node.Exp.UnaryExp;
import frontend.Parser.Node.ForStmt;
import frontend.Parser.Node.FuncDef;
import frontend.Parser.Node.FuncFParam;
import frontend.Parser.Node.FuncFParams;
import frontend.Parser.Node.FuncRParams;
import frontend.Parser.Node.InitVal;
import frontend.Parser.Node.LVal;
import frontend.Parser.Node.MainFuncDef;
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
import frontend.Parser.Node.VarDecl;
import frontend.Error.Error;
import frontend.Parser.Node.VarDef;
import frontend.Visitor.Symbol.FuncSymbol;
import frontend.Visitor.Symbol.IntSymbol;
import frontend.Visitor.Symbol.ReturnType;
import frontend.Visitor.Symbol.Symbol;
import frontend.Visitor.Symbol.SymbolType;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Stack;

public class Visitor {
    private final CompUnit compUnit;
    private final SymbolTable root;
    private final ArrayList<Error> errors;
    private final String outputPath;
    private final String errorPath;
    private final String additionalOutputPath;
    private int scopeId;
    private static final SymbolTableStack symbolTableStack = new SymbolTableStack();
    private final Stack<Integer> loopStack;
    private boolean enterVoidFunc;

    public Visitor(CompUnit compUnit, String outputPath, String errorPath, ArrayList<Error> errors) {
        this.compUnit = compUnit;
        this.root = null;
        this.outputPath = outputPath;
        this.errorPath = errorPath;
        this.additionalOutputPath = "C:\\Users\\Alpha\\IdeaProjects\\Compiler\\myOutput.txt";
        this.errors = errors;
        this.scopeId = 1;
        // this.symbolTableStack = new SymbolTableStack();
        this.loopStack = new Stack<>();
        this.enterVoidFunc = false;
    }

    public static SymbolTableStack getSymbolTableStack() {
        return symbolTableStack;
    }

    public void visit() {
        SymbolTable root = visitCompUnit(compUnit);
    }

    //  CompUnit → {Decl} {FuncDef} MainFuncDef
    public SymbolTable visitCompUnit(CompUnit compUnit) {
        SymbolTable rootSymbolTable = new SymbolTable(this.scopeId);    // 1, 并且是根节点，父节点为null
        symbolTableStack.push(rootSymbolTable);
        FuncSymbol funcSymbol = generateParamsSymbol("getint", ReturnType.Int, null);
        addNewSymbol2StackTop(funcSymbol);
        // 遍历所有声明
        for (Decl decl : compUnit.getDecls()) {
            visitDecl(decl);
        }
        // 遍历所有函数定义
        for (FuncDef funcDef : compUnit.getFuncDefs()) {
            visitFuncDef(funcDef);
        }
        // 处理主函数
        visitMainFuncDef(compUnit.getMainFuncDef());
        return rootSymbolTable;
    }

    // 声明 Decl → ConstDecl | VarDecl
    public void visitDecl(Decl decl) {
        ConstDecl constDecl = decl.getConstDecl();
        VarDecl varDecl = decl.getVarDecl();
        if (constDecl != null) visitConstDecl(constDecl);
        else visitVarDecl(varDecl);
    }

    // 常量声明 ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
    public void visitConstDecl(ConstDecl constDecl) {
        BType bType = constDecl.getBType();
        Token token = bType.getToken();
        for (ConstDef constDef : constDecl.getConstDefs()) {
            visitConstDef(constDef, true);
        }
    }

    // 常量定义 ConstDef → Ident [ '[' ConstExp ']' ] '=' ConstInitVal // b
    public void visitConstDef(ConstDef constDef, boolean isInt) {
        // 首先要判断是否是重复定义，重复定义要在当前SymbolTable里面找
        Token ident = constDef.getIdent();
        String name = ident.getKey();
        judgeRedefinition(ident);
        // make当前定义的常量的Symbol
        ConstExp constExp = constDef.getConstExp();
        boolean isArray = constExp != null;
        SymbolType symbolType = generateSymbolType(false, true, isInt, isArray, false);
        Symbol symbol = new IntSymbol(name, symbolType, scopeId);
        addNewSymbol2StackTop(symbol);  // 把这个symbol加入到当前SymbolTable中
        // 检查ConstExp
        if (constExp != null) visitConstExp(constExp);
        // 检查ConstInitVal
        visitConstInitVal(constDef.getConstInitVal());
    }

    // 变量声明 VarDecl → [ 'static' ] BType VarDef { ',' VarDef } ';'
    public void visitVarDecl(VarDecl varDecl) {
        boolean isStatic = varDecl.isStatic();
        BType bType = varDecl.getBType();
        Token token = bType.getToken();
        for (VarDef varDef : varDecl.getVarDefs()) {
            visitVarDef(varDef, isStatic, true);
        }
    }

    // 变量定义 VarDef → Ident [ '[' ConstExp ']' ] | Ident [ '[' ConstExp ']' ] '=' InitVal // b
    public void visitVarDef(VarDef varDef, boolean isStatic, boolean isInt) {
        // 首先要判断是否是重复定义，重复定义要在当前SymbolTable里面找
        Token ident = varDef.getIdent();
        String name = ident.getKey();
        judgeRedefinition(ident);
        // make当前定义的常量的Symbol
        ConstExp constExp = varDef.getConstExp();
        boolean isArray = constExp != null;
        SymbolType symbolType = generateSymbolType(isStatic, false, isInt, isArray, false);
        Symbol symbol = new IntSymbol(name, symbolType, scopeId);
        // System.out.println(symbol.toString());
        addNewSymbol2StackTop(symbol);  // 把这个symbol加入到当前SymbolTable中
        // 检查ConstExp
        if (constExp != null) visitConstExp(constExp);
        // 检查ConstInitVal
        InitVal initVal = varDef.getInitVal();
        if (initVal != null) visitInitVal(initVal);
    }

    // 常量表达式 ConstExp → AddExp
    public void visitConstExp(ConstExp constExp) {
        visitAddExp(constExp.getAddExp());
    }

    // 常量初值 ConstInitVal → ConstExp | '{' [ ConstExp { ',' ConstExp } ] '}'
    public void visitConstInitVal(ConstInitVal constInitVal) {
        ConstExp constExp = constInitVal.getConstExp();
        ArrayList<ConstExp> constExps = constInitVal.getConstExps();
        if (constExp != null) visitConstExp(constExp);
        else if (constExps != null) {
            for (ConstExp constExp1 : constExps) {
                visitConstExp(constExp1);
            }
        }
        // StringConst 不需要检查
    }

    // 变量初值 InitVal → Exp | '{' [ Exp { ',' Exp } ] '}'
    public void visitInitVal(InitVal initVal) {
        Exp exp = initVal.getExp();
        ArrayList<Exp> exps = initVal.getExps();
        if (exp != null) visitExp(exp);
        else if (exps != null) {
            for (Exp exp1 : exps) {
                visitExp(exp1);
            }
        }
    }

    // 函数定义 FuncDef → FuncType Ident '(' [FuncFParams] ')' Block // b g
    public void visitFuncDef(FuncDef funcDef) {
        Token funcIdent = funcDef.getIdent();
        String funcName = funcIdent.getKey();
        Block block = funcDef.getBlock();
        ReturnType returnType = ReturnType.Void;    // default
        // 判断错误b：重复定义函数
        judgeRedefinition(funcIdent);
        // 判断错误g：返回值缺失(前提是非void类型函数)
        if (!funcDef.isVoid()) {
            judgeMissReturnValue(block);
            returnType = ReturnType.Int;    // 修改为Int
        } else {
            this.enterVoidFunc = true;
        }
        // 要进入函数体，先要把当前函数生成Symbol;
        FuncFParams funcFParams = funcDef.getFuncFParams();
        // 生成函数Symbol的方法
        FuncSymbol funcSymbol = generateParamsSymbol(funcName, returnType, funcFParams);
        addNewSymbol2StackTop(funcSymbol);
        // 函数转换Symbol问题处理完毕
        // 下面要处理大括号，push方法自带scopeId++
        pushNewSymbolTable();
        // 检查
        if (funcFParams != null) visitFuncFParams(funcFParams);
        // 出栈
        visitBlock(block);
        popLastSymbolTable();
        this.enterVoidFunc = false;
    }

    // 函数形参表 FuncFParams → FuncFParam { ',' FuncFParam }
    public void visitFuncFParams(FuncFParams funcFParams) {
        for (FuncFParam funcFParam : funcFParams.getFuncFParams()) {
            visitFuncFParam(funcFParam);
        }
    }

    // 函数形参 FuncFParam → BType Ident ['[' ']'] // b
    public void visitFuncFParam(FuncFParam funcFParam) {
        // 检查是否有b错误：变量名重定义
        BType bType = funcFParam.getBType();
        Token token = bType.getToken();     // 函数参数变量的类型
        Token ident = funcFParam.getIdent();
        String name = ident.getKey();   // 函数参数变量的名字
        judgeRedefinition(ident);
        boolean isArray = funcFParam.isArray();
        SymbolType symbolType = generateSymbolType(false, false, true, isArray, false);
        // 创建新的Symbol
        Symbol symbol = new IntSymbol(name, symbolType, scopeId);
        // System.out.println("新创建的函数参数变量为：" + symbol.getName());
        addNewSymbol2StackTop(symbol);
    }

    // 语句块 Block → '{' { BlockItem } '}'
    public void visitBlock(Block block) {
        for (BlockItem blockItem : block.getBlockItems()) {
            visitBlockItem(blockItem);
        }
    }

    // 语句块项 BlockItem → Decl | Stmt
    public void visitBlockItem(BlockItem blockItem) {
        Decl decl = blockItem.getDecl();
        Stmt stmt = blockItem.getStmt();
        if (decl != null) visitDecl(decl);
        else if (stmt != null) visitStmt(stmt);
    }

    public void visitStmt(Stmt stmt) {
        if (stmt instanceof AssignmentStmt) {
            // Stmt → LVal '=' Exp ';' // h
            LVal lVal = ((AssignmentStmt) stmt).getLVal();
            Exp exp = ((AssignmentStmt) stmt).getExp();
            judgeAssignToConst(lVal);
            visitLVal(lVal);
            visitExp(exp);
        } else if (stmt instanceof ExpStmt) {
            // | [Exp] ';'
            Exp exp = ((ExpStmt) stmt).getExp();
            if (exp != null) visitExp(exp);
        } else if (stmt instanceof BlockStmt) {
            // | Block
            Block block = ((BlockStmt) stmt).getBlock();
            // 进入下一作用域
            pushNewSymbolTable();
            visitBlock(block);
            popLastSymbolTable();
        } else if (stmt instanceof IfStmt) {
            // | 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
            Cond cond = ((IfStmt) stmt).getCond();
            ArrayList<Stmt> stmts = ((IfStmt) stmt).getStmts();
            assert cond != null;
            visitCond(cond);
            for (Stmt s : stmts) {
                visitStmt(s);   // 其实最多就两个
            }
        } else if (stmt instanceof ForLoopStmt) {
            // | 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt // h
            ForStmt headForStmt = ((ForLoopStmt) stmt).getHeadForStmt();
            Cond cond = ((ForLoopStmt) stmt).getCond();
            ForStmt rearForStmt = ((ForLoopStmt) stmt).getRearForStmt();
            Stmt subStmt = ((ForLoopStmt) stmt).getStmt();
            if (headForStmt != null) visitForStmt(headForStmt);
            if (cond != null) visitCond(cond);
            if (rearForStmt != null) visitForStmt(rearForStmt);
            enterLoop();
            visitStmt(subStmt);
            exitLoop();
        } else if (stmt instanceof BreakStmt) {
            // | 'break' ';' // m
            Token token = ((BreakStmt) stmt).getToken();
            judgeLoop(token);
        } else if (stmt instanceof ContinueStmt) {
            // | 'continue' ';' // m
            Token token = ((ContinueStmt) stmt).getToken();
            judgeLoop(token);
        } else if (stmt instanceof ReturnStmt) {
            // | 'return' [Exp] ';' // f
            ((ReturnStmt) stmt).setFuncReturnType(findFuncTokenType()); // 直接在栈上找这个return语句对应的函数的返回值, 为代码生成做准备
            Token Return = ((ReturnStmt) stmt).getToken();
            Exp exp = ((ReturnStmt) stmt).getExp();
            judgeMismatchedReturnValue(Return, exp);
            if (exp != null) visitExp(exp);
        } else {
            // | 'printf''('StringConst {','Exp}')'';' // l
            assert stmt instanceof PrintfStmt;
            Token Printf = ((PrintfStmt) stmt).getToken();
            Token stringConst = ((PrintfStmt) stmt).getStringConst();
            ArrayList<Exp> exps = ((PrintfStmt) stmt).getExps();
            judgeMismatchedFormat(Printf, stringConst, exps);
            for (Exp exp : exps) {
                visitExp(exp);
            }
        }
    }

    // 主函数定义 MainFuncDef → 'int' 'main' '(' ')' Block // g
    public void visitMainFuncDef(MainFuncDef mainFuncDef) {
        Block block = mainFuncDef.getBlock();
        judgeMissReturnValue(block);
        // 错误处理完成
        // 下面要处理大括号
        pushNewSymbolTable();
        // 进入子作用域
        visitBlock(block);
        // 出子作用域
        popLastSymbolTable(); // 该子作用域的生命周期彻底结束了，再也不会被访问
    }

    // 语句 ForStmt → LVal '=' Exp { ',' LVal '=' Exp } // h
    public void visitForStmt(ForStmt forStmt) {
        ArrayList<LVal> lVals = forStmt.getLVals();
        ArrayList<Exp> exps = forStmt.getExps();
        judgeAssignToConst(lVals, exps);
        int size = lVals.size();
        for (int i = 0; i < size; i++) {
            visitLVal(lVals.get(i));
            visitExp(exps.get(i));
        }
    }

    // 表达式 Exp → AddExp
    public void visitExp(Exp exp) {
        visitAddExp(exp.getAddExp());
    }

    // 条件表达式 Cond → LOrExp
    public void visitCond(Cond cond) {
        visitLOrExp(cond.getLOrExp());
    }

    // 左值表达式 LVal → Ident ['[' Exp ']'] // c
    public void visitLVal(LVal lVal) {
        Token ident = lVal.getIdent();
        Exp exp = lVal.getExp();
        // 检查ident是不是未定义，未定义则报错
        judgeUndefined(ident);
        if (exp != null) visitExp(exp);
    }

    // 基本表达式 PrimaryExp → '(' Exp ')' | LVal | Number
    public void visitPrimaryExp(PrimaryExp primaryExp) {
        Exp exp = primaryExp.getExp();
        LVal lVal = primaryExp.getLVal();
        if (exp != null) visitExp(exp);
        else if (lVal != null) visitLVal(lVal);
        // Number不需要检查
    }

    // 函数实参表 FuncRParams → Exp { ',' Exp }
    public void visitFuncRParams(FuncRParams funcRParams) {
        for (Exp exp : funcRParams.getExps()) {
            visitExp(exp);
        }
    }

    // 逻辑或表达式 LOrExp → LAndExp | LOrExp '||' LAndExp
    public void visitLOrExp(LOrExp lOrExp) {
        for (LAndExp lAndExp : lOrExp.getLAndExps()) {
            visitLAndExp(lAndExp);
        }
    }

    // 逻辑与表达式 LAndExp → EqExp | LAndExp '&&' EqExp
    public void visitLAndExp(LAndExp lAndExp) {
        for (EqExp eqExp : lAndExp.getEqExps()) {
            visitEqExp(eqExp);
        }
    }

    // 相等性表达式 EqExp → RelExp | EqExp ('==' | '!=') RelExp
    public void visitEqExp(EqExp eqExp) {
        for (RelExp relExp : eqExp.getRelExps()) {
            visitRelExp(relExp);
        }
    }

    // 关系表达式 RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
    public void visitRelExp(RelExp relExp) {
        for (AddExp addExp : relExp.getAddExps()) {
            visitAddExp(addExp);
        }
    }

    // 加减表达式 AddExp → MulExp | AddExp ('+' | '−') MulExp
    public void visitAddExp(AddExp addExp) {
        for (MulExp mulExp : addExp.getMulExps()) {
            visitMulExp(mulExp);
        }
    }

    // 乘除模表达式 MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
    public void visitMulExp(MulExp mulExp) {
        for (UnaryExp unaryExp : mulExp.getUnaryExps()) {
            visitUnaryExp(unaryExp);
        }
    }

    // 一元表达式 UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp // c d e
    public void visitUnaryExp(UnaryExp unaryExp) {
        PrimaryExp primaryExp = unaryExp.getPrimaryExp();
        if (primaryExp != null) {
            visitPrimaryExp(primaryExp);
            return;
        }
        UnaryExp unaryExp2 = unaryExp.getUnaryExp();
        if (unaryExp2 != null) {
            visitUnaryExp(unaryExp2);
            return;
        }
        Token funcIdent = unaryExp.getIdent();
        if (funcIdent != null) {    // 是中间情况
            // 检查是否c：name未定义
            if (!judgeUndefined(funcIdent)) {
                // 如果定义了name，才考虑检查d，e错误
                FuncRParams funcRParams = unaryExp.getFuncRParams();
                // 检查是否d：参数个数不匹配
                boolean faultD = judgeParametersCount(funcIdent, funcRParams);
                if (!faultD) judgeParametersType(funcIdent, funcRParams);   // 一行不会有两个错误，只有不存在d错误才考虑e错误
            }
            FuncRParams funcRParams = unaryExp.getFuncRParams();
            if (funcRParams != null) visitFuncRParams(funcRParams);
        }
    }

    public void addError(Error error) {
        this.errors.add(error);
    }

    public void judgeRedefinition(Token ident) {
        SymbolTable curSymbolTable = symbolTableStack.top();
        String name = ident.getKey();
        if (curSymbolTable.hasSymbol(name)) {
            addError(new Error(ErrorType.b, ident.getLine()));    // 报错行号 Ident 所在行数
        }
    }

    public boolean judgeUndefined(Token ident) {
        SymbolTable symbolTable = symbolTableStack.top();
        String name = ident.getKey();
        // 要不断遍历父节点直到根节点
        boolean isUndefined = true;
        while (symbolTable != null) {
            if (symbolTable.hasSymbol(name)) {
                isUndefined = false;
                break;
            } else {
                symbolTable = symbolTable.getFather();
            }
        }
        if (isUndefined) {
            addError(new Error(ErrorType.c, ident.getLine()));
        }
        return isUndefined;
    }

    public void judgeMissReturnValue(Block block) {
        boolean isMissReturnValue = false;
        int line = block.getRBraceLine();
        ArrayList<BlockItem> blockItems = block.getBlockItems();
        if (blockItems.isEmpty()) isMissReturnValue = true;
        else {
            BlockItem blockItem = blockItems.get(blockItems.size() - 1);
            // 拿到最后一个blockItem
            Stmt stmt = blockItem.getStmt();
            if (stmt == null) isMissReturnValue = true;   // 如果最后一个不是可能有返回值的模块，直接return
            else {
                if (!(stmt instanceof ReturnStmt)) isMissReturnValue = true;  // 如果是ReturnStmt则未缺失，返回false
            }
        }
        if (isMissReturnValue) {
            addError(new Error(ErrorType.g, line));
        }
    }

    public void judgeMismatchedReturnValue(Token token, Exp exp) {
        // 无返回值的函数存在不匹配的return语句:f
        if (this.enterVoidFunc && exp != null) {
            addError(new Error(ErrorType.f, token.getLine()));
        }
    }

    public void judgeMismatchedFormat(Token Printf, Token stringConst, ArrayList<Exp> exps) {
        String string = stringConst.getKey();
        int count = 0;
        for (int i = 0; i < string.length() - 1; i++) {
            if (string.charAt(i) == '%' && string.charAt(i + 1) == 'd') {
                count++;
            }
        }
        if (count != exps.size()) {
            addError(new Error(ErrorType.l, Printf.getLine()));
        }
    }

    public void judgeAssignToConst(ArrayList<LVal> lVals, ArrayList<Exp> exps) {
        int size = lVals.size();
        for (int i = 0; i < size; i++) {
            SymbolTable curSymbolTable = symbolTableStack.top();
            LVal lVal = lVals.get(i);
            Token ident = lVal.getIdent();
            while (curSymbolTable != null) {
                if (curSymbolTable.hasSymbol(ident.getKey())) {
                    IntSymbol symbol = (IntSymbol) curSymbolTable.getSymbol(ident.getKey());
                    if (symbol.isConst()) {
                        // System.out.println(symbol.toString());
                        addError(new Error(ErrorType.h, ident.getLine()));
                    }
                    break;
                }
                curSymbolTable = curSymbolTable.getFather();
            }
        }
    }

    public void judgeAssignToConst(LVal lVal) {
        // 要解决的是Stmt → LVal '=' Exp ';' // h
        SymbolTable curSymbolTable = symbolTableStack.top();
        // System.out.println(curSymbolTable.getScopeId());
        Token ident = lVal.getIdent();
        while (curSymbolTable != null) {
            if (curSymbolTable.hasSymbol(ident.getKey())) {
                // System.out.println(curSymbolTable.getScopeId());
                IntSymbol symbol = (IntSymbol) curSymbolTable.getSymbol(ident.getKey());
                if (symbol.isConst()) {
                    addError(new Error(ErrorType.h, ident.getLine()));
                }
                break;
            }
            curSymbolTable = curSymbolTable.getFather();
        }
    }

    public boolean judgeParametersCount(Token funcIdent, FuncRParams funcRParams) {
        // 需要搜索ident
        SymbolTable rootTable = symbolTableStack.getRoot();
        String name = funcIdent.getKey();
        FuncSymbol funcSymbol = (FuncSymbol) rootTable.getSymbol(name);
        assert (funcSymbol != null);
        // 函数调用语句中，参数个数与函数定义中的参数个数不匹配。
        // 报错行号为函数调用语句的函数名所在行数
        int realSize = funcRParams == null ? 0 : funcRParams.getExps().size();
        if (funcSymbol.getCount() != realSize) {
            addError(new Error(ErrorType.d, funcIdent.getLine()));
            return true;
        }
        return false;
    }

    public void judgeParametersType(Token funcIdent, FuncRParams funcRParams) {
        // 这里保证形参和实参的数量是一致的
        //  函数实参表 FuncRParams → Exp { ',' Exp }
        if (funcRParams == null || funcRParams.getExps().isEmpty()) return; // 如果某一方为空，直接返回，不需要判断
        // System.out.println(funcRParams.getExps().size());
        SymbolTable rootTable = symbolTableStack.getRoot();
        String name = funcIdent.getKey();
        FuncSymbol funcSymbol = (FuncSymbol) rootTable.getSymbol(name);
        // System.out.println(funcSymbol.toString());
        // for (Symbol symbol : funcSymbol.getParams()) {
        //     System.out.println(symbol.toString());
        // }
        assert (funcSymbol != null);
        int size = funcRParams.getExps().size();
        assert (size == funcSymbol.getCount());
        ArrayList<Exp> exps = funcRParams.getExps();
        // System.out.println(name);
        for (int i = 0; i < size; i++) {
            IntSymbol curSymbol = (IntSymbol) funcSymbol.getParams().get(i);    // 第i个已经确定的形参
            SymbolType symbolType = curSymbol.getType();    // 必定是INT
            boolean isArray = curSymbol.isArray();  // dim=1表示是数组形式的形参
            // System.out.println(exps.get(i).isArray() + " " + isArray + " " + curSymbol.getName());
            // 现在要找到第i个实参, 也就是第i个
            Exp exp = exps.get(i);
            if (exp.isArray() != isArray) {
                addError(new Error(ErrorType.e, funcIdent.getLine()));
            }
        }
    }

    public void judgeLoop(Token token) {
        if (this.loopStack.isEmpty()) {
            // 代表当前不处于for循环内
            // 报错行号为 ‘break’ 与 ’continue’ 所在行号。
            addError(new Error(ErrorType.m, token.getLine()));
        }
    }

    public FuncSymbol generateParamsSymbol(String funcName, ReturnType returnType, FuncFParams funcFParams) {
        ArrayList<Symbol> params = new ArrayList<>();
        if (funcFParams != null) {
            for (FuncFParam funcFParam : funcFParams.getFuncFParams()) {
                // 每个参数就对应一个IntSymbol
                // 函数形参 FuncFParam → BType Ident ['[' ']'] // k
                // public IntSymbol(String name, SymbolType type, int dim, int scopeId)
                BType bType = funcFParam.getBType();
                Token ident = funcFParam.getIdent();
                String name = ident.getKey();
                boolean isArray = funcFParam.isArray();
                SymbolType symbolType = generateSymbolType(false, false, true, isArray, false);
                params.add(new IntSymbol(name, symbolType, scopeId + 1));  // 参数列表中已经属于下一个域了
            }
        }
        // 函数声明的作用域永远是1
        return new FuncSymbol(funcName, SymbolType.Func, returnType, 1, params);
    }

    public void pushNewSymbolTable() {
        scopeId++;
        SymbolTable father = symbolTableStack.top();
        // 创建的同时就把new的father指针指向father
        // 同时把father的children指针增加一个new
        SymbolTable newSymbolTable = new SymbolTable(scopeId, father);
        symbolTableStack.push(newSymbolTable);
    }

    public void popLastSymbolTable() {
        symbolTableStack.pop(); // 该子作用域的生命周期彻底结束了，再也不会被访问
    }

    public void addNewSymbol2StackTop(Symbol symbol) {
        symbolTableStack.top().putSymbol(symbol);
    }

    public SymbolType generateSymbolType(boolean isStatic, boolean isConst, boolean isInt, boolean isArray, boolean isFunc) {
        if (isFunc) return SymbolType.Func;
        if (isInt) {
            if (isStatic) {
                if (isArray) return SymbolType.StaticIntArray;
                else return SymbolType.StaticInt;
            } else {
                if (isConst) {
                    if (isArray) return SymbolType.ConstIntArray;
                    else return SymbolType.ConstInt;
                } else {
                    if (isArray) return SymbolType.IntArray;
                    else return SymbolType.Int;
                }
            }
        }
        return null;
    }

    public TokenType findFuncTokenType() {
        SymbolTable curSymbolTable = symbolTableStack.top();
        // System.out.println(curSymbolTable.getScopeId());
        while (curSymbolTable != null) {
            LinkedHashMap<String, Symbol> symbols = curSymbolTable.getSymbols();
            for (Symbol symbol : symbols.values()) {
                if (symbol instanceof FuncSymbol funcSymbol) {
                    return funcSymbol.getReturnType() == ReturnType.Void ? TokenType.VOIDTK : TokenType.INTTK;
                }
            }
            curSymbolTable = curSymbolTable.getFather();
        }
        // System.out.println("没有找到return语句所属的函数??");
        return null;
    }

    public static boolean hasReturnAtEnd(FuncDef funcDef) {
        // 如果这个函数的直接大括号内, 缺少return, 则返回false, 如果在函数最开始有return, 也代表true
        Block block = funcDef.getBlock();
        ArrayList<BlockItem> blockItems = block.getBlockItems();
        for (BlockItem blockItem : blockItems) {
            if (blockItem.getStmt() != null && blockItem.getStmt() instanceof ReturnStmt) {
                return true;
            }
        }
        return false;
    }

    public void enterLoop() {
        loopStack.push(1);
    }

    public void exitLoop() {
        loopStack.pop();
    }

    public void outputInFile() {
        if (errors.isEmpty()) {
            try {
                // 输出到主输出路径
                PrintWriter writer = new PrintWriter(outputPath);
                writer.println(root.toString());
                writer.close();

                // 如果指定了额外输出路径，也输出到该路径
                if (additionalOutputPath != null) {
                    PrintWriter additionalWriter = new PrintWriter(additionalOutputPath);
                    additionalWriter.println(root.toString());
                    additionalWriter.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}