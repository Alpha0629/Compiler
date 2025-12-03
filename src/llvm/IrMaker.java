package llvm;

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
import frontend.Parser.Node.Number;
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
import frontend.Parser.Node.UnaryOp;
import frontend.Parser.Node.VarDecl;
import frontend.Parser.Node.VarDef;
import frontend.Visitor.Visitor;
import llvm.types.ArrayType;
import llvm.types.FuncType;
import llvm.types.IntType;
import llvm.types.PointerType;
import llvm.types.ValueType;
import llvm.types.VoidType;
import llvm.values.BasicBlock;
import llvm.values.Function;
import llvm.values.GlobalString;
import llvm.values.GlobalVar;
import llvm.values.Value;
import llvm.values.constants.ConstArray;
import llvm.values.constants.ConstInt;
import llvm.values.constants.Constant;
import llvm.values.instructions.Alloca;
import llvm.values.instructions.Gep;
import llvm.values.instructions.Icmp;
import llvm.values.instructions.Load;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

public class IrMaker {
    private final Module module;
    private final IrUtils irUtils;
    private final CompUnit AST; // 语法分析得到的抽象语法树
    private final IrSymbolTableStack irSymbolTableStack;  // 符号表栈，每一个符号表代表一个作用区域
    private final String outputPath;

    public static IntType I32 = new IntType(32);
    public static IntType I8 = new IntType(8);
    public static IntType I1 = new IntType(1);

    public static VoidType VoidType = new VoidType();

    public static ConstInt C0 = new ConstInt(I32, 0);

    public static BasicBlock currentBlock;  // 当前的块
    public static Function currentFunction; // 当前的函数体
    public static Constant comprehensiveConst; // 综合属性(能求到常值的向上传递)
    public static ArrayList<Value> comprehensiveValues = new ArrayList<>(); // 综合属性(用于数组中, 向上传递)
    public static Value comprehensiveValue;

    public static BasicBlock trueBlock;
    public static BasicBlock falseBlock;

    public static BasicBlock garbageBlock = new BasicBlock(null, null, null);

    public static String inheritedName; // 继承属性(变量的名字向下传递) (用于向Symbol中添加名字)
    public static String comprehensiveVarName; // 综合属性, 由ident传给上层, 传递的内容是变量的名字
    public static BType inheritedBType; // 继承属性(用于确定当前变量的类型)
    public static Boolean isStatic = Boolean.FALSE; // 继承属性(告诉下面的当前变量定义是不是静态的)
    public static Boolean compileTimeConstRead = Boolean.FALSE;
    public static Boolean isBuildingPointerRParams = Boolean.FALSE;
    public static int inheritedInt = 0;
    public static int blockDeepth = 0;

    public static Stack<AbstractMap.SimpleEntry<BasicBlock, BasicBlock>> stackOfCycle = new Stack<>();

    public IrMaker(Module module, CompUnit AST, String outputPath) {
        this.module = module;
        this.irSymbolTableStack = new IrSymbolTableStack();
        this.irUtils = new IrUtils(this.module, this.irSymbolTableStack);
        this.AST = AST;
        this.outputPath = outputPath;
    }

    public void buildCompUnitIr() {
        initDelcaration();
        ArrayList<Decl> decls = AST.getDecls();
        for (Decl decl : decls) {
            buildDeclIr(decl);
        }
        ArrayList<FuncDef> funcDefs = AST.getFuncDefs();
        for (FuncDef funcDef : funcDefs) {
            buildFuncDefIr(funcDef);
        }
        buildMainFuncDefIr(AST.getMainFuncDef());
    }

    public void buildDeclIr(Decl decl) {
        // 声明 Decl → ConstDecl | VarDecl
        ConstDecl constDecl = decl.getConstDecl();
        VarDecl varDecl = decl.getVarDecl();
        if (constDecl != null) {
            buildConstDeclIr(constDecl);
        } else {
            buildVarDeclIr(varDecl);
        }
    }

    public void buildConstDeclIr(ConstDecl constDecl) {
        // 常量声明 ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
        BType bType = constDecl.getBType();
        for (ConstDef constDef : constDecl.getConstDefs()) {
            // 变量类型要传给下一层，即 buildConstDefIr
            IrMaker.inheritedBType = bType;
            buildConstDefIr(constDef);
        }
    }

    public void buildConstDefIr(ConstDef constDef) {
        // 常量定义 ConstDef → Ident [ '[' ConstExp ']' ] '=' ConstInitVal
        ConstExp constExp = constDef.getConstExp();
        ConstInitVal constInitVal = constDef.getConstInitVal();
        IrMaker.comprehensiveVarName = constDef.getIdent().getKey();   // 从ident传上来的变量名
        // 情况一: Ident '=' ConstInitVal
        // const int a = 3;
        if (constExp == null) {
            // 直接添加到符号表当中，不创建指令
            IrMaker.compileTimeConstRead = Boolean.TRUE;
            buildConstInitValIr(constInitVal); // 传上来 IrMaker.comprehensiveConst
            IrMaker.compileTimeConstRead = Boolean.FALSE;
            assert (IrMaker.comprehensiveConst instanceof ConstInt);
            irSymbolTableStack.putSymbolToCurScope(IrMaker.comprehensiveVarName, IrMaker.comprehensiveConst);
        } else {
            // 情况二: 数组常量
            IrMaker.compileTimeConstRead = Boolean.TRUE;
            buildConstExpIr(constExp);  // 传上来数组的长度len, 放在了 IrMaker.comprehensiveConst 里面
            IrMaker.compileTimeConstRead = Boolean.FALSE;
            assert (IrMaker.comprehensiveConst instanceof ConstInt);
            int len = ((ConstInt) IrMaker.comprehensiveConst).getVal();
            IrMaker.inheritedInt = len; // 把数组长度传给 buildConstInitValIr

            IrMaker.compileTimeConstRead = Boolean.TRUE;
            buildConstInitValIr(constInitVal); // 传上来数组的初值, 放在了 IrMaker.comprehensiveConst 里面
            IrMaker.compileTimeConstRead = Boolean.FALSE;
            assert (IrMaker.comprehensiveConst instanceof ConstArray);

            if (irSymbolTableStack.inGlobalScope()) {
                // 如果是全局变量, 不需要进行Alloca, 直接makeGlobalVar
                GlobalVar globalVar = irUtils.makeGlobalVar(IrMaker.comprehensiveVarName, IrMaker.comprehensiveConst, true, false);
                irSymbolTableStack.putSymbolToGlobal(IrMaker.comprehensiveVarName, globalVar);
            } else {
                // 函数内部的const int array[len] = {constInitVal};
                // 先用Alloca分配内存, 再用getelementptr获取每个元素的地址, 再用store逐个为其赋初始值
                // %v1 = alloca [3 x i32]
                // %v2 = getelementptr inbounds [3 x i32], [3 x i32]* %v1, i32 0, i32 0
                // store i32 0, i32* %v2
                // %v3 = getelementptr inbounds i32, i32* %v2, i32 1
                // store i32 3, i32* %v3
                // %v4 = getelementptr inbounds i32, i32* %v2, i32 2
                // store i32 4, i32* %v4
                ArrayType arrayType = (ArrayType) (IrMaker.comprehensiveConst).getValueType();
                Alloca alloca = irUtils.makeAlloca(arrayType, (ConstArray) IrMaker.comprehensiveConst);
                irSymbolTableStack.putSymbolToCurScope(IrMaker.comprehensiveVarName, alloca); // 把这个数组的名称, 空间, 添加到当前符号表
                // for循环进行gep和store
                ArrayList<Constant> constants = ((ConstArray) IrMaker.comprehensiveConst).getConstants();
                // 第一种gep指令, left和right都必须是i32 0, i32 0
                // 第一个参数用的是之前alloca的%v1
                Gep baseGep = irUtils.makeGep(alloca, C0, C0);
                for (int i = 0; i < len; i++) {
                    Constant curStoredValue = constants.get(i);
                    if (i == 0) {
                        // 把分配到的地方%v2赋上初始值
                        irUtils.makeStore(curStoredValue, baseGep);
                    } else {
                        // 后续都是第二种gep指令
                        Gep gep = irUtils.makeGep(baseGep, new ConstInt(I32, i));   // 按照i进行偏移
                        irUtils.makeStore(curStoredValue, gep);
                    }
                }
            }
        }
    }

    public void buildConstInitValIr(ConstInitVal constInitVal) {
        ConstExp constExp = constInitVal.getConstExp();
        if (constExp != null) {
            // 情况一: 对应的是const int类型的初始化
            IrMaker.compileTimeConstRead = Boolean.TRUE;
            buildConstExpIr(constExp);
            IrMaker.compileTimeConstRead = Boolean.FALSE;
            // 已经得到了初始值, 放入了 IrMaker.comprehensiveConst (向上传递给ConstDef)
        } else {
            // 情况二: 对应的是const数组的初始化
            // 数组长度继承下来, 存放于 IrMaker.inheritedInt
            ArrayList<ConstExp> constExps = constInitVal.getConstExps();
            ArrayList<Constant> constants = new ArrayList<>(); // {1, 2, 9, 8}
            // 前i个有初始值的部分
            for (int i = 0; i < constExps.size(); i++) {
                ConstExp constExpr = constExps.get(i);
                IrMaker.compileTimeConstRead = Boolean.TRUE;
                buildConstExpIr(constExpr);
                IrMaker.compileTimeConstRead = Boolean.FALSE;
                // 已经得到了初始值, 放入了 IrMaker.comprehensiveConst
                assert (IrMaker.comprehensiveConst instanceof ConstInt);
                constants.add(IrMaker.comprehensiveConst);
            }
            // 后len - i个没有初始值的部分，手动设置为0
            for (int i = 0; i < IrMaker.inheritedInt - constExps.size(); i++) {
                constants.add(C0);
            }
            // 所有初始值都存于 ArrayList<Constant> constants
            // 现在需要构造 ConstArray 类型的综合属性
            IrMaker.comprehensiveConst = new ConstArray(new ArrayType(I32, constants.size()), constants);
        }
    }

    public void buildVarDeclIr(VarDecl varDecl) {
        // 变量声明 VarDecl → [ 'static' ] BType VarDef { ',' VarDef } ';'
        IrMaker.isStatic = varDecl.isStatic();
        IrMaker.inheritedBType = varDecl.getBType();
        for (VarDef varDef : varDecl.getVarDefs()) {
            buildVarDefIr(varDef);
        }
    }

    public void buildVarDefIr(VarDef varDef) {
        // 变量定义 VarDef → Ident [ '[' ConstExp ']' ] | Ident [ '[' ConstExp ']' ] '=' InitVal
        ConstExp constExp = varDef.getConstExp();
        InitVal initVal = varDef.getInitVal();
        IrMaker.comprehensiveVarName = varDef.getIdent().getKey();   // 从ident传上来的变量名

        if (constExp == null) {
            // 情况一: just Ident or Ident '=' ConstInitVal
            if (irSymbolTableStack.inGlobalScope()) {
                // 情况1.1: 全局int型变量, 例如int a = 10 or int a;
                if (initVal == null) {
                    // 情况1.1.1 仅有 int a;
                    // 全局变量不可能存在static的情况, 故不需要考虑
                    // 没有明确初始化的int型全局变量, 默认为C0
                    GlobalVar globalVar = irUtils.makeGlobalVar(IrMaker.comprehensiveVarName, C0, false, false);
                    irSymbolTableStack.putSymbolToGlobal(IrMaker.comprehensiveVarName, globalVar);
                } else {
                    // 情况1.1.1 有初始值 int a = 10;
                    // 全局变量一定可以求到具体的值
                    IrMaker.compileTimeConstRead = Boolean.TRUE;
                    buildInitValIr(initVal);    // 返回了ConstInt于 IrMaker.comprehensiveConst
                    IrMaker.compileTimeConstRead = Boolean.FALSE;
                    assert (IrMaker.comprehensiveConst instanceof ConstInt);
                    GlobalVar globalVar = irUtils.makeGlobalVar(IrMaker.comprehensiveVarName, IrMaker.comprehensiveConst, false, false);
                    irSymbolTableStack.putSymbolToGlobal(IrMaker.comprehensiveVarName, globalVar);
                }
            } else {
                // 情况1.2 局部变量
                if (initVal == null) {
                    // 情况1.2.1 局部变量 仅有 int a; 无初始化
                    if (IrMaker.isStatic) {
                        // 情况1.2.1.1 局部变量 + 无初始化 + 静态变量
                        // 默认初始化为C0
                        GlobalVar globalVar = irUtils.makeGlobalVar(IrMaker.comprehensiveVarName, C0, false, true);
                        // 添加到当前作用域的符号表内
                        irSymbolTableStack.putSymbolToCurScope(IrMaker.comprehensiveVarName, globalVar);
                    } else {
                        // 情况1.2.1.2 局部变量 + 无初始化 + 非静态变量
                        // 需要使用Alloca分配内存, 并存储到当前作用域的符号表内
                        // %v1 = alloca i32
                        // System.out.println(11111);
                        Alloca alloca = irUtils.makeAlloca(I32);
                        irSymbolTableStack.putSymbolToCurScope(IrMaker.comprehensiveVarName, alloca);
                    }
                } else {
                    // 情况1.2.2 局部变量 + 有初始化
                    if (IrMaker.isStatic) {
                        // 情况1.2.2.1 局部变量 + 有初始化 + 静态变量
                        // 这种情况下的初始值一定是可以求得的
                        IrMaker.compileTimeConstRead = Boolean.TRUE;
                        buildInitValIr(initVal);    // 返回了ConstInt于 IrMaker.comprehensiveConst
                        IrMaker.compileTimeConstRead = Boolean.FALSE;
                        assert (IrMaker.comprehensiveConst instanceof ConstInt);
                        GlobalVar globalVar = irUtils.makeGlobalVar(IrMaker.comprehensiveVarName, IrMaker.comprehensiveConst, false, true);
                        // 添加到当前作用域的符号表内
                        irSymbolTableStack.putSymbolToCurScope(IrMaker.comprehensiveVarName, globalVar);
                    } else {
                        // 情况1.2.2.2 局部变量 + 有初始化 + 非静态变量
                        // int main() {
                        //     int a = 10;
                        //     int j = a;
                        //     return 0;
                        // }
                        // %v2 = alloca i32     3. 给j分配内存, 但需要放在最前面
                        // %v1 = alloca i32     1. 给a分配内存
                        // store i32 10, i32* %v1   2. 把初始值放在a所在的内存
                        // %v3 = load i32, i32* %v1 4. 把a的值转移到临时变量中
                        // store i32 %v3, i32* %v2  5. 把临时变量的值存储给j所在的内存
                        Alloca alloca = irUtils.makeAlloca(I32);        // 先给j分配内存, 放在最前面
                        IrMaker.compileTimeConstRead = Boolean.FALSE;   // 确保不需要求值
                        buildInitValIr(initVal);
                        // 现在 IrMaker.comprehensiveValue 是%v3
                        // 可以是Instruction, 也可以是Constant
                        irUtils.makeStore(IrMaker.comprehensiveValue, alloca);
                        irSymbolTableStack.putSymbolToCurScope(IrMaker.comprehensiveVarName, alloca);
                    }
                }
            }
        } else {
            // 情况二: 数组形式
            IrMaker.compileTimeConstRead = Boolean.TRUE;
            buildConstExpIr(constExp);  // 传上来数组的长度len, 放在了 IrMaker.comprehensiveConst 里面
            IrMaker.compileTimeConstRead = Boolean.FALSE;
            assert (IrMaker.comprehensiveConst instanceof ConstInt);
            int len = ((ConstInt) IrMaker.comprehensiveConst).getVal();
            IrMaker.inheritedInt = len; // 把数组长度传给 buildInitValIr

            ArrayType zeroArrayType = new ArrayType(I32, len);
            ArrayList<Constant> zeroConstants = new ArrayList<>();
            for (int i = 0; i < len; i++) {
                zeroConstants.add(C0);
            }
            ConstArray zeroInitializer = new ConstArray(zeroArrayType, zeroConstants);
            // 先构造一个zeroInitializerd的constArray

            if (irSymbolTableStack.inGlobalScope()) {
                // 情况2.1: 全局int数组型变量
                if (initVal == null) {
                    // 情况2.1.1: 全局数组变量 + 无初始化
                    // 默认设为全0
                    GlobalVar globalVar = irUtils.makeGlobalVar(IrMaker.comprehensiveVarName, zeroInitializer, false, false);
                    irSymbolTableStack.putSymbolToGlobal(IrMaker.comprehensiveVarName, globalVar);
                } else {
                    // 情况2.1.2: 全局数组变量 + 有初始化
                    IrMaker.compileTimeConstRead = Boolean.TRUE;
                    buildInitValIr(initVal);    // 传入了数组长度
                    IrMaker.compileTimeConstRead = Boolean.FALSE;
                    assert (IrMaker.comprehensiveConst instanceof ConstArray);
                    GlobalVar globalVar = irUtils.makeGlobalVar(IrMaker.comprehensiveVarName, IrMaker.comprehensiveConst, false, false);
                    irSymbolTableStack.putSymbolToGlobal(IrMaker.comprehensiveVarName, globalVar);
                }
            } else {
                // 情况2.2: 局部int数组型变量
                if (initVal == null) {
                    // 情况2.2.1: 局部数组变量 + 无初始化
                    if (IrMaker.isStatic) {
                        // 情况2.2.1.1: 局部数组变量 + 无初始化 + 静态
                        // 默认设为全0
                        GlobalVar globalVar = irUtils.makeGlobalVar(IrMaker.comprehensiveVarName, zeroInitializer, false, true);
                        irSymbolTableStack.putSymbolToCurScope(IrMaker.comprehensiveVarName, globalVar);
                    } else {
                        // 情况2.2.1.2: 局部数组变量 + 无初始化 + 非静态
                        // int main() {
                        //     int array[5];
                        //     return 0;
                        // }
                        // %v1 = alloca [5 x i32] 一步即可
                        ArrayType arrayType = new ArrayType(I32, len);
                        Alloca alloca = irUtils.makeAlloca(arrayType);
                        irSymbolTableStack.putSymbolToCurScope(IrMaker.comprehensiveVarName, alloca);
                    }
                } else {
                    // 情况2.2.2: 局部数组变量 + 有初始化
                    if (IrMaker.isStatic) {
                        // 情况2.2.2.1: 局部数组变量 + 有初始化 + 静态
                        // 有初始化的部分一定能求到具体的值, 没有初始化的部分要设为0
                        IrMaker.compileTimeConstRead = Boolean.TRUE;
                        buildInitValIr(initVal);
                        IrMaker.compileTimeConstRead = Boolean.FALSE;
                        assert (IrMaker.comprehensiveConst instanceof ConstArray);
                        // 现在的 IrMaker.comprehensiveConst 是 ConstArray 类型, 并且长度即为len
                        GlobalVar globalVar = irUtils.makeGlobalVar(IrMaker.comprehensiveVarName, IrMaker.comprehensiveConst, false, true);
                        irSymbolTableStack.putSymbolToCurScope(IrMaker.comprehensiveVarName, globalVar);
                    } else {
                        // 情况2.2.2.2: 局部数组变量 + 有初始化 + 非静态
                        // %v2 = alloca [5 x i32]
                        // %v1 = alloca i32
                        // store i32 1, i32* %v1
                        // %v3 = load i32, i32* %v1
                        // %v4 = getelementptr inbounds [5 x i32], [5 x i32]* %v2, i32 0, i32 0
                        // store i32 %v3, i32* %v4
                        // %v5 = getelementptr inbounds i32, i32* %v4, i32 1
                        // store i32 4, i32* %v5
                        // %v6 = getelementptr inbounds i32, i32* %v4, i32 2
                        // store i32 9, i32* %v6
                        // int main() {
                        //     int x = 1;
                        //     int array[5] = {x, 4, 9};
                        // return 0;
                        // }
                        ArrayType arrayType = new ArrayType(I32, len);
                        Alloca alloca = irUtils.makeAlloca(arrayType);
                        irSymbolTableStack.putSymbolToCurScope(IrMaker.comprehensiveVarName, alloca);
                        IrMaker.compileTimeConstRead = Boolean.FALSE;
                        buildInitValIr(initVal);    // 传入了数组长度
                        // buildInitValIr(initVal); 的结果存在了 IrMaker.comprehensiveValues
                        Gep baseGep = irUtils.makeGep(alloca, C0, C0);
                        for (int i = 0; i < IrMaker.comprehensiveValues.size(); i++) {
                            Value curStoredValue = IrMaker.comprehensiveValues.get(i);
                            if (i == 0) {
                                irUtils.makeStore(curStoredValue, baseGep);
                            } else {
                                Gep gep = irUtils.makeGep(baseGep, new ConstInt(I32, i));   // 按照i进行偏移
                                irUtils.makeStore(curStoredValue, gep);
                            }
                        }
                    }
                }
            }
        }
    }

    public void buildInitValIr(InitVal initVal) {
        Exp exp = initVal.getExp();
        if (exp != null) {
            // 情况一: int型变量的初始化
            // System.out.println(exp);
            buildExpIr(exp);
            // 如果需要求值, 则回传ConstInt类型给 IrMaker.comprehensiveConst
            // 如果不需要求值, 则回传Value类型给 IrMaker.comprehensiveValue
        } else {
            // 情况二: 数组型变量的初始化
            // 现在数组维度已知, 初始化的部分的长度也可知
            // 此时要分为是全局数组 or 静态数组的变量初始化, 还是局部变量的初始化
            ArrayList<Exp> exps = initVal.getExps();
            ArrayList<Constant> constants = new ArrayList<>();
            if (irSymbolTableStack.inGlobalScope() || IrMaker.isStatic) {
                // 有初始化的部分
                for (int i = 0; i < exps.size(); i++) {
                    Exp expr = exps.get(i);
                    buildExpIr(expr);   // 一定能求出常数值
                    assert (IrMaker.comprehensiveConst instanceof ConstInt);
                    constants.add(IrMaker.comprehensiveConst);
                }
                // 在没有初始化的部分, 使用0
                for (int i = 0; i < IrMaker.inheritedInt - exps.size(); i++) {
                    constants.add(C0);
                }
                IrMaker.comprehensiveConst = new ConstArray(new ArrayType(I32, constants.size()), constants);
            } else {
                IrMaker.comprehensiveValues.clear();    // 先清空
                for (int i = 0; i < exps.size(); i++) {
                    Exp expr = exps.get(i);
                    buildExpIr(expr);   // 得不到常数值, 但放在了 IrMaker.comprehensiveValue 当中
                    IrMaker.comprehensiveValues.add(IrMaker.comprehensiveValue);
                }
                return;
            }
        }
    }

    public void buildConstExpIr(ConstExp constExp) {
        IrMaker.compileTimeConstRead = Boolean.TRUE;
        buildAddExpIr(constExp.getAddExp());
        IrMaker.compileTimeConstRead = Boolean.FALSE;
        // 经过这步可以得到向上传递的 comprehensiveConst 记录了这个ConstExp的值(Int类型)
    }

    public void buildExpIr(Exp exp) {
        buildAddExpIr(exp.getAddExp());
    }

    public void buildAddExpIr(AddExp addExp) {
        ArrayList<MulExp> mulExps = addExp.getMulExps();
        ArrayList<Token> signs = addExp.getSigns();
        if (IrMaker.compileTimeConstRead == Boolean.TRUE) {
            // 可以求到具体的值
            int value = 0;
            for (int i = 0; i < mulExps.size(); i++) {
                MulExp mulExp = mulExps.get(i);
                buildMulExpIr(mulExp);
                // System.out.println(IrMaker.comprehensiveConst);
                assert (IrMaker.comprehensiveConst instanceof ConstInt);// 求到了常数, 放在 comprehensiveConst
                if (i == 0) {
                    // 求到了常数, 放在 comprehensiveConst
                    value = ((ConstInt) IrMaker.comprehensiveConst).getVal();
                } else {
                    if (signs.get(i - 1).getValue() == TokenType.PLUS) {
                        // 加法
                        value += ((ConstInt) IrMaker.comprehensiveConst).getVal();
                    } else {
                        // 减法
                        value -= ((ConstInt) IrMaker.comprehensiveConst).getVal();
                    }
                }
            }
            IrMaker.comprehensiveConst = new ConstInt(I32, value);
        } else {
            // 无法求出具体的值
            Value memory = null;
            for (int i = 0; i < mulExps.size(); i++) {
                MulExp mulExp = mulExps.get(i);
                buildMulExpIr(mulExp);
                if (i == 0) {
                    memory = IrMaker.comprehensiveValue;
                } else {
                    if (signs.get(i - 1).getValue() == TokenType.PLUS) {
                        // 加法
                        memory = irUtils.makeAdd(memory, IrMaker.comprehensiveValue);
                    } else {
                        // 减法
                        memory = irUtils.makeSub(memory, IrMaker.comprehensiveValue);
                    }
                }
            }
            IrMaker.comprehensiveValue = memory;
        }
    }

    public void buildMulExpIr(MulExp mulExp) {
        ArrayList<UnaryExp> unaryExps = mulExp.getUnaryExps();
        ArrayList<Token> signs = mulExp.getSigns();
        if (IrMaker.compileTimeConstRead == Boolean.TRUE) {
            int value = 0;
            for (int i = 0; i < unaryExps.size(); i++) {
                UnaryExp unaryExp = unaryExps.get(i);
                buildUnaryExpIr(unaryExp);
                if (i == 0) {
                    // 求到了常数, 放在 comprehensiveConst
                    value = ((ConstInt) IrMaker.comprehensiveConst).getVal();
                } else {
                    if (signs.get(i - 1).getValue() == TokenType.MULT) {
                        // 乘法
                        value *= ((ConstInt) IrMaker.comprehensiveConst).getVal();
                    } else if (signs.get(i - 1).getValue() == TokenType.DIV) {
                        // 除法
                        value /= ((ConstInt) IrMaker.comprehensiveConst).getVal();
                    } else {
                        // 取模
                        value %= ((ConstInt) IrMaker.comprehensiveConst).getVal();
                    }
                }
            }
            IrMaker.comprehensiveConst = new ConstInt(I32, value);
        } else {
            Value memory = null;
            for (int i = 0; i < unaryExps.size(); i++) {
                UnaryExp unaryExp = unaryExps.get(i);
                buildUnaryExpIr(unaryExp);
                if (i == 0) {
                    memory = IrMaker.comprehensiveValue;
                } else {
                    if (signs.get(i - 1).getValue() == TokenType.MULT) {
                        // 乘法
                        memory = irUtils.makeMul(memory, IrMaker.comprehensiveValue);
                    } else if (signs.get(i - 1).getValue() == TokenType.DIV) {
                        // 除法
                        memory = irUtils.makeSdiv(memory, IrMaker.comprehensiveValue);
                    } else {
                        // 取模
                        memory = irUtils.makeSrem(memory, IrMaker.comprehensiveValue);
                    }
                }
            }
            IrMaker.comprehensiveValue = memory;
        }
    }

    public void buildUnaryExpIr(UnaryExp unaryExp) {
        PrimaryExp primaryExp = unaryExp.getPrimaryExp();
        UnaryExp unaryExpr = unaryExp.getUnaryExp();
        UnaryOp unaryOp = unaryExp.getUnaryOp();
        FuncRParams funcRParams = unaryExp.getFuncRParams();
        Token funcName = unaryExp.getIdent();
        if (IrMaker.compileTimeConstRead == Boolean.TRUE) {
            // 必须能够读到常量
            assert (funcRParams == null);
            assert (funcName == null);
            if (primaryExp != null) {
                buildPrimaryExpIr(primaryExp);
            } else {
                buildUnaryExpIr(unaryExpr);
                // 得到了常数值
                int value = ((ConstInt) IrMaker.comprehensiveConst).getVal();
                if (unaryOp.getUnaryOp().getValue() == TokenType.PLUS) {
                    // 保持不变
                    IrMaker.comprehensiveConst = new ConstInt(I32, +value);
                } else if (unaryOp.getUnaryOp().getValue() == TokenType.MINU) {
                    // 取反
                    IrMaker.comprehensiveConst = new ConstInt(I32, -value);
                } else {
                    // '!'仅出现在条件表达式中
                    // 这种情况应该不会发生
                    int bool = value >= 0 ? 0 : 1;
                    IrMaker.comprehensiveConst = new ConstInt(I1, bool);
                }
            }
        } else {
            if (primaryExp != null) {
                buildPrimaryExpIr(primaryExp);
            } else if (unaryExpr != null) {
                buildUnaryExpIr(unaryExpr);
                if (unaryOp.getUnaryOp().getValue() == TokenType.PLUS) {
                    // 保持不变
                } else if (unaryOp.getUnaryOp().getValue() == TokenType.MINU) {
                    // 取反
                    // int x = 1;
                    // int y = -x;
                    // %v2 = alloca i32
                    // %v1 = alloca i32
                    // store i32 1, i32* %v1
                    // %v3 = load i32, i32* %v1
                    // %v4 = sub i32 0, %v3
                    // store i32 %v4, i32* %v2
                    // 现在%v3在 IrMaker.comprehensiveValue, 目标是得到%v4即可
                    IrMaker.comprehensiveValue = irUtils.makeSub(C0, IrMaker.comprehensiveValue);
                } else {
                    // '!'仅出现在条件表达式中
                    if (comprehensiveValue.getValueType().getBits() != I32.getBits()) {
                        comprehensiveValue = irUtils.makeZext(comprehensiveValue, I32);
                    }
                    // 要和0进行比较, 期望等于0
                    IrMaker.comprehensiveValue = irUtils.makeIcmp(TokenType.EQL, comprehensiveValue, C0);
                    if (comprehensiveValue.getValueType().getBits() != I32.getBits()) {
                        comprehensiveValue = irUtils.makeZext(comprehensiveValue, I32);
                    }
                }
            } else {
                assert (funcName != null);  // 只能保证一定是函数, 故存在函数名, 但不能保证有参数funcRParams(可能为NULL)
                Function function = (Function) irSymbolTableStack.findSymbol(funcName.getKey());;
                ArrayList<Exp> exps = funcRParams != null ? funcRParams.getExps() : new ArrayList<>();
                ArrayList<Value> fParams = function.getArgs();
                ArrayList<Value> rParams = new ArrayList<>();

                for (int i = 0; i < fParams.size(); i++) {
                    ValueType fParamType = fParams.get(i).getValueType();

                    if (fParamType instanceof PointerType) isBuildingPointerRParams = Boolean.TRUE;
                    else isBuildingPointerRParams = Boolean.FALSE;

                    buildExpIr(exps.get(i));    // 得到的Value存储在 comprehensiveValue
                    rParams.add(comprehensiveValue);
                    isBuildingPointerRParams = Boolean.FALSE;   // 保险起见
                }
                // 最后
                IrMaker.comprehensiveValue = irUtils.makeCall(function, rParams);
            }
        }
    }

    public void buildPrimaryExpIr(PrimaryExp primaryExp) {
        Exp exp = primaryExp.getExp();
        LVal lVal = primaryExp.getLVal();
        Number number = primaryExp.getNumber();
        if (IrMaker.compileTimeConstRead == Boolean.TRUE) {
            if (exp != null) {
                buildExpIr(exp);
                // 常数存在了 IrMaker.comprehensiveConst
            } else if (lVal != null) {
                buildLValIr(lVal);
                // 常数存在了 IrMaker.comprehensiveConst
            } else {
                buildNumber(number);
                // 常数存在了 IrMaker.comprehensiveConst
            }
        } else {
            if (exp != null) {
                buildExpIr(exp);  // 照旧
            } else if (lVal != null) {
                if (isBuildingPointerRParams) {
                    // 实参是指针类型, 直接调用即可
                    isBuildingPointerRParams = Boolean.FALSE;
                    buildLValIr(lVal);
                } else {
                    // func(x, arr);  // x传值，arr传地址
                    // func(global_var, arr);  // 全局常值global_var传值，arr传地址
                    buildLValIr(lVal);  // IrMaker.comprehensiveValue 是Alloca指令 or ConstInt
                    if (IrMaker.comprehensiveValue.getValueType() instanceof PointerType) {
                        IrMaker.comprehensiveValue = irUtils.makeLoad(IrMaker.comprehensiveValue);
                    }
                }
            } else {
                buildNumber(number);
                // 即便是常数, 也要存在 IrMaker.comprehensiveValue
            }
        }
    }

    public void buildLValIr(LVal lVal) {
        Token ident = lVal.getIdent();
        Exp exp = lVal.getExp();
        Value curLValValue = irSymbolTableStack.findSymbol(ident.getKey()); // 在当前作用域的符号表或者全局符号表找到左值
        // 首先以是否能读出常量为界限进行划分
        if (IrMaker.compileTimeConstRead == Boolean.TRUE) {
            // 说明这个变量是
            // 1. int类型变量: 全局变量 or 全局常量 or 当前作用域静态变量 or 当前作用域常量
            // 2. 数组类型变量: 全局变量数组 or 全局常量数组 or 当前作用域静态数组 or 当前作用域常量数组
            if (curLValValue.getValueType() instanceof IntType) {
                // 如果当前不是指针而是常量, 说明是左值是 const int
                IrMaker.comprehensiveConst = (ConstInt) curLValValue;
            } else {
                // 剩下就是指针的情况
                assert (curLValValue.getValueType() instanceof PointerType);
                // 指针指向的内容
                // 对于可以计算结果的情况, 指针指向的内容只能是Int型或者数组
                ValueType pointerType = ((PointerType) curLValValue.getValueType()).getPointedType();
                if (pointerType instanceof IntType) {
                    // i32* --> i32
                    // int a = 77;
                    // int b = a;
                    // @g_a = dso_local global i32 77
                    // @g_b = dso_local global i32 77
                    // 要么是全局变量要么是当前作用域静态变量
                    // 获取GlobalVar的初始常数值
                    IrMaker.comprehensiveConst = (ConstInt) (((GlobalVar) curLValValue).getConstInit());
                } else if (pointerType instanceof ArrayType) {
                    // [3 x i32]* --> [3 * i32]
                    // int a[3] = {1, 2, 3};
                    // int b = a[2];
                    // @g_a = dso_local global [3 x i32] [i32 1, i32 2, i32 3]
                    // @g_b = dso_local global i32
                    assert (exp != null);   // 必然成立
                    IrMaker.compileTimeConstRead = Boolean.TRUE;
                    buildExpIr(exp);
                    IrMaker.compileTimeConstRead = Boolean.FALSE;
                    // 现在获取到了数组的索引, 例如a[2], 这个2就存储在 IrMaker.comprehensiveConst
                    // 现在要通过索引来获取具体的值
                    if (curLValValue instanceof GlobalVar) {
                        // 如果是全局变量, 先获取数组的一系列常量, 然后再根据索引取对应的值放到 IrMaker.comprehensiveConst
                        ArrayList<Constant> constants = ((ConstArray) ((GlobalVar) curLValValue).getConstInit()).getConstants();
                        IrMaker.comprehensiveConst = constants.get(((ConstInt) comprehensiveConst).getVal());
                    } else {
                        // 局部的常量数组, 是用Alloca进行内存分配并store的
                        assert (curLValValue instanceof Alloca);
                        ArrayList<Constant> constants = ((Alloca) curLValValue).getInitArray().getConstants();
                        IrMaker.comprehensiveConst = constants.get(((ConstInt) comprehensiveConst).getVal());
                    }
                } else {
                    // 应该不存在这种情况
                    assert false;
                }
            }
        } else {
            // 即便不必求常量值, 但也可能用到const int, 例如给局部变量赋以const int var
            // System.out.println(curLValValue.getValueType());
            if (curLValValue.getValueType() instanceof IntType) {
                IrMaker.comprehensiveValue = curLValValue;
                return;
            }
            assert (curLValValue.getValueType() instanceof PointerType);
            ValueType pointerType = ((PointerType) curLValValue.getValueType()).getPointedType();

            if (pointerType instanceof IntType) {
                // 指向int类型的变量, 直接返回结果即可
                IrMaker.comprehensiveValue = curLValValue;
            } else if (pointerType instanceof ArrayType) {
                // 如果exp非空, 那么是一个数组的某个index
                if (exp != null) {
                    buildExpIr(exp);    // 把index存在了comprehensiveValue当中
                    IrMaker.comprehensiveValue = irUtils.makeGep(curLValValue, C0, comprehensiveValue);
                } else {
                    // exp为空但仍然是数组形式, 说明是函数实参传递
                    // int arr[3] = {1, 2, 3};
                    // test(arr);
                    // 直接获取头指针, 即a[0]
                    IrMaker.comprehensiveValue = irUtils.makeGep(curLValValue, C0, C0);
                }
            } else if (pointerType instanceof PointerType) {
                // void test (int array[]) {
                //     array[3] = 100;
                // }
                // define dso_local void @test(i32* %a0) {
                //     b0:
                //     %v1 = alloca i32*
                //     store i32* %a0, i32** %v1
                //     %v2 = load i32*, i32** %v1
                //     %v3 = getelementptr inbounds i32, i32* %v2, i32 3
                //     store i32 100, i32* %v3
                //     ret void
                // }
                // 这里得到的curLValValue是%v1, Load指令得到的para是%v2
                Load para = irUtils.makeLoad(curLValValue);
                if (exp != null) {
                    buildExpIr(exp);    // 把index存在了comprehensiveValue当中
                    IrMaker.comprehensiveValue = irUtils.makeGep(para, IrMaker.comprehensiveValue);
                } else {
                    IrMaker.comprehensiveValue = para;
                }
            } else {
                // 不存在这种情况
                assert false;
            }
        }
    }

    public void buildCondIr(Cond cond) {
        // trueBlock 和 falseBlock 已经在上层(Stmt)被设定好了
        LOrExp lorExp = cond.getLOrExp();
        buildLOrExpIr(lorExp);
    }

    public void buildLOrExpIr(LOrExp lorExp) {
        // 消除左递归 LOrExp → LAndExp {'||' LAndExp}
        ArrayList<LAndExp> lAndExps = lorExp.getLAndExps();
        ArrayList<Token> signs = lorExp.getSigns();

        if (signs.isEmpty()) {
            // 仅有一个Cond, 直接求
            // trueBlock 和 falseBlock 已经在上层(Stmt)被设定好了
            buildLAndExpIr(lAndExps.get(0));
        } else {
            // 多个或并列, 需要实现短路求值
            BasicBlock memory = IrMaker.falseBlock;
            for (int i = 0; i < lAndExps.size() - 1; i++) {
                // 遍历除了最后一个的所有Cond
                IrMaker.falseBlock = irUtils.makeBasicBlock(false);  // 创建一个新的block, 暂不更新currentBlock
                buildLAndExpIr(lAndExps.get(i));
                IrMaker.currentBlock = IrMaker.falseBlock;
            }
            // 单独处理最后一个Cond
            IrMaker.falseBlock = memory;    // 恢复falseBlock
            buildLAndExpIr(lAndExps.get(lAndExps.size() - 1));
        }
    }

    public void buildLAndExpIr(LAndExp landExp) {
        // 消除左递归 LAndExp → EqExp {'&&' EqExp}
        ArrayList<EqExp> eqExps = landExp.getEqExps();
        ArrayList<Token> signs = landExp.getSigns();

        if (signs.isEmpty()) {
            buildEqExpIr(eqExps.get(0));    // 保证返回I1
            irUtils.makeBranch(comprehensiveValue, trueBlock, falseBlock);
        } else {
            // BasicBlock memory = IrMaker.trueBlock;
            for (int i = 0; i < eqExps.size() - 1; i++) {
                buildEqExpIr(eqExps.get(i));
                assert (IrMaker.comprehensiveValue instanceof Icmp);    // 保证返回I1
                BasicBlock nextBlock = irUtils.makeBasicBlock(false);
                irUtils.makeBranch(comprehensiveValue, nextBlock, falseBlock);
                IrMaker.currentBlock = nextBlock;
            }
            // IrMaker.trueBlock = memory;
            buildEqExpIr(eqExps.get(eqExps.size() - 1));
            irUtils.makeBranch(comprehensiveValue, trueBlock, falseBlock);
        }
    }

    public void buildEqExpIr(EqExp eqExp) {
        ArrayList<RelExp> relExps = eqExp.getRelExps();
        ArrayList<Token> signs = eqExp.getSigns();

        if (signs.isEmpty()) {
            RelExp relExp = relExps.get(0);
            // 证明relExp不等于0, relExp返回的是 I1 or I32
            buildRelExpIr(relExp);
            // 结果在 IrMaker.comprehensiveValue
            // 如果是I32, 则直接进行Icmp, 判断不等于0
            // 如果是I1, 则直接向上传递
            assert (comprehensiveValue.getValueType() instanceof IntType);
            if (comprehensiveValue.getValueType().getBits() == I32.getBits()) {
                comprehensiveValue = irUtils.makeIcmp(TokenType.NEQ, comprehensiveValue, C0);
            } else {
                // 直接向上传递
            }
        } else {
            Value memory = null;
            // 至少有两个relExp, 即一定存在等号
            for (int i = 0; i < relExps.size(); i++) {
                // 先拿第一个和第二个进行比较, 第一个和第二个都是I1类型, 都需要转换成I32进行Icmp
                // 得到的Icmp是I1类型, 再转换成I32, 然后和第三个I1 to I32的结果进行比较, Icmp得到I1
                buildRelExpIr(relExps.get(i));
                assert (comprehensiveValue.getValueType() instanceof IntType);
                if (comprehensiveValue.getValueType().getBits() != I32.getBits()) {
                    comprehensiveValue = irUtils.makeZext(comprehensiveValue, I32);
                }
                if (i == 0) {
                    memory = comprehensiveValue;
                } else if (i < relExps.size() - 1) {
                    memory = irUtils.makeIcmp(signs.get(i - 1).getValue(), memory, comprehensiveValue);
                    // 因为还需要和后面的exp进行比较, 因此要扩充成I32
                    memory = irUtils.makeZext(memory, I32);
                } else {
                    comprehensiveValue = irUtils.makeIcmp(signs.get(i - 1).getValue(), memory, comprehensiveValue);
                }
            }
        }
    }

    public void buildRelExpIr(RelExp relExp) {
        ArrayList<AddExp> addExps = relExp.getAddExps();
        ArrayList<Token> signs = relExp.getSigns();

        if (signs.isEmpty()) {
            AddExp addExp = addExps.get(0);
            buildAddExpIr(addExp);
            // 结果在 IrMaker.comprehensiveValue, 一定是I32
            // 直接返回
        } else {
            Value memory = null;
            for (int i = 0; i < addExps.size(); i++) {
                buildAddExpIr(addExps.get(i));
                // I32
                if (i == 0) {
                    memory = comprehensiveValue;
                } else if (i < addExps.size() - 1) {
                    memory = irUtils.makeIcmp(signs.get(i - 1).getValue(), memory, comprehensiveValue);
                    memory = irUtils.makeZext(memory, I32); // 扩充成32
                } else {
                    comprehensiveValue = irUtils.makeIcmp(signs.get(i - 1).getValue(), memory, comprehensiveValue);
                }
            }
            // 一定是I1
        }
    }

    public void buildNumber(Number number) {
        int val = Integer.parseInt(number.getIntConst().getKey());
        if (IrMaker.compileTimeConstRead) {
            IrMaker.comprehensiveConst = new ConstInt(I32, val);
        } else {
            IrMaker.comprehensiveValue = new ConstInt(I32, val);
        }
    }

    public void buildFuncDefIr(FuncDef funcDef) {
        ValueType returnValueType = funcDef.getFuncType().isVoid() ? VoidType : I32;
        Block block = funcDef.getBlock();
        IrMaker.inheritedName = funcDef.getIdent().getKey();    // 函数名

        FuncFParams funcFParams = funcDef.getFuncFParams();

        ArrayList<ValueType> parameterTypes = new ArrayList<>();
        if (funcFParams != null) {
            ArrayList<FuncFParam> funcFParamsList = funcFParams.getFuncFParams();
            for (FuncFParam funcFParam : funcFParamsList) {
                ValueType paramValueType = funcFParam.getBType().getToken().getValue() == TokenType.INTTK ? I32 : VoidType;
                if (funcFParam.isArray()) {
                    // 参数是数组类型, 最终要转换成指针类型
                    // int func(int c, int array[])
                    // define dso_local i32 @func(i32 %a0, i32* %a1)
                    parameterTypes.add(new PointerType(paramValueType));
                } else {
                    parameterTypes.add(paramValueType);
                }
            }
        }

        FuncType funcType = new FuncType(returnValueType, parameterTypes);
        irUtils.makeFunction(IrMaker.inheritedName, funcType, false);
        irSymbolTableStack.putSymbolToGlobal(IrMaker.inheritedName, IrMaker.currentFunction);

        irSymbolTableStack.push(new IrSymbolTable());
        blockDeepth++;

        irUtils.makeBasicBlock(true);
        if (funcFParams != null) {
            buildFuncFParamsIr(funcFParams);
        }

        buildBlockIr(block);

        // 如果是一个void函数, 并且最后不是以return结尾的
        if (funcDef.getFuncType().isVoid() && !Visitor.hasReturnAtEnd(funcDef)) {
            irUtils.makeRet();
        }
        blockDeepth--;
        irSymbolTableStack.pop();
    }

    public void buildFuncFParamsIr(FuncFParams funcFParams) {
        for (int i = 0; i < funcFParams.getFuncFParams().size(); i++) {
            // buildFuncFParamIr(funcFParam);
            // 不必在 buildFuncFParamIr(funcFParam); 逐个构建了, 那样会缺失当前索引, 直接在这个里面构建吧
            // void func(int a, int b) {
            //
            // }
            // define dso_local void @func(i32 %a0, i32 %a1) {
            // b0:
            //     %v2 = alloca i32
            //     %v1 = alloca i32
            //     store i32 %a0, i32* %v1
            //     store i32 %a1, i32* %v2
            //     ret void
            // }
            FuncFParam funcFParam = funcFParams.getFuncFParams().get(i);
            IrMaker.comprehensiveVarName = funcFParam.getIdent().getKey();
            ValueType paramValueType = funcFParam.getBType().getToken().getValue() == TokenType.INTTK ? I32 : VoidType;
            if (funcFParam.isArray()) {
                paramValueType = new PointerType(paramValueType);
            }
            Alloca alloca = irUtils.makeAlloca(paramValueType);
            irSymbolTableStack.putSymbolToCurScope(IrMaker.comprehensiveVarName, alloca);

            // 把形参存储到alloca分配的空间当中
            Value curArg = IrMaker.currentFunction.getArgs().get(i);
            irUtils.makeStore(curArg, alloca);
        }
    }

    public void buildFuncFParamIr(FuncFParam funcFParam) {
        // 暂时不用
    }

    public void buildBlockIr(Block block) {
        ArrayList<BlockItem> blockItems = block.getBlockItems();
        for (BlockItem blockItem : blockItems) {
            buildBlockItemIr(blockItem);
        }
    }

    public void buildBlockItemIr(BlockItem blockItem) {
        Decl decl = blockItem.getDecl();
        Stmt stmt = blockItem.getStmt();
        if (decl != null) {
            buildDeclIr(decl);
        } else {
            buildStmtIr(stmt);
        }
    }

    public void buildStmtIr(Stmt stmt) {
        if (stmt instanceof AssignmentStmt assignmentStmt) {
            // LVal '=' Exp
            LVal lVal = assignmentStmt.getLVal();
            buildLValIr(lVal);
            Value valueOfLVal = IrMaker.comprehensiveValue;
            Exp exp = assignmentStmt.getExp();
            buildExpIr(exp);
            // System.out.println(comprehensiveValue);
            Value valueOfExp = IrMaker.comprehensiveValue;
            irUtils.makeStore(valueOfExp, valueOfLVal);
        } else if (stmt instanceof ExpStmt expStmt) {
            // [Exp]
            Exp exp = expStmt.getExp();
            if (exp != null) buildExpIr(exp);
        } else if (stmt instanceof BlockStmt blockStmt) {
            // Block
            Block block = blockStmt.getBlock();
            irSymbolTableStack.push(new IrSymbolTable());
            blockDeepth++;
            buildBlockIr(block);
            blockDeepth--;
            irSymbolTableStack.pop();
        } else if (stmt instanceof IfStmt ifStmt) {
            // 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
            Cond cond = ifStmt.getCond();
            IrMaker.trueBlock = irUtils.makeBasicBlock(false);
            if (ifStmt.hasElse()) {
                IrMaker.falseBlock = irUtils.makeBasicBlock(false);
                BasicBlock trueBlockMemory = IrMaker.trueBlock;
                BasicBlock falseBlockMemory = IrMaker.falseBlock;
                BasicBlock destBlock = irUtils.makeBasicBlock(false);
                buildCondIr(cond);

                IrMaker.currentBlock = trueBlock;
                buildStmtIr(ifStmt.getHeadStmt());  // 这里可能会修改trueblock和falseblock, 造成错误
                // 复原
                IrMaker.trueBlock = trueBlockMemory;
                IrMaker.falseBlock = falseBlockMemory;
                irUtils.makeBranch(destBlock);

                IrMaker.currentBlock = falseBlock;
                buildStmtIr(ifStmt.getRearStmt());
                // 复原
                IrMaker.trueBlock = trueBlockMemory;
                IrMaker.falseBlock = falseBlockMemory;
                irUtils.makeBranch(destBlock);

                IrMaker.currentBlock = destBlock;
            } else {
                BasicBlock destBlock = irUtils.makeBasicBlock(false);
                IrMaker.falseBlock = destBlock;
                buildCondIr(cond);

                IrMaker.currentBlock = trueBlock;
                buildStmtIr(ifStmt.getHeadStmt());
                irUtils.makeBranch(destBlock);

                IrMaker.currentBlock = destBlock;
            }
        } else if (stmt instanceof ForLoopStmt forLoopStmt) {
            // 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
            // for (i = 0; i < 10; i = i + 1) {
            //     printf("Hello");
            // }
            // 在处理第一个ForStmt的时候, 仍然在for循环之前的block里面, 然后跳转到Cond所在的block
            // 成立则跳转到Stmt所在block, 该block的尾部要跳转到第二个ForStmt
            // 第二个ForStmt处理完后, 无条件跳转到Cond所在的block里面
            // 失败则跳转到Stmt之外
            ForStmt headForStmt = forLoopStmt.getHeadForStmt();
            ForStmt rearForStmt = forLoopStmt.getRearForStmt();
            Cond cond = forLoopStmt.getCond();
            Stmt s = forLoopStmt.getStmt();

            // 如果第一个ForStmt不存在, 无任何影响
            if (headForStmt != null) {
                buildForStmtIr(headForStmt);
            }
            BasicBlock condBlock = irUtils.makeBasicBlock(false);   // 构建但不更新currentBlock
            irUtils.makeBranch(condBlock);

            // 现在才进入
            IrMaker.currentBlock = condBlock;

            BasicBlock incrementBlock = irUtils.makeBasicBlock(false);
            BasicBlock stmtBlock = irUtils.makeBasicBlock(false);
            BasicBlock dest = irUtils.makeBasicBlock(false);

            if (cond != null) {
                IrMaker.trueBlock = stmtBlock;
                IrMaker.falseBlock = dest;
                buildCondIr(cond);
            } else {
                irUtils.makeBranch(stmtBlock);
            }

            IrMaker.currentBlock = incrementBlock;
            if (rearForStmt != null) {
                buildForStmtIr(rearForStmt);
            }
            irUtils.makeBranch(condBlock);

            IrMaker.currentBlock = stmtBlock;
            stackOfCycle.push(new SimpleEntry<>(incrementBlock, dest)); // 记录了自增表达式和for循环的结尾
            // 前者为continue定位, 后者为break定位
            buildStmtIr(s);
            irUtils.makeBranch(incrementBlock);
            stackOfCycle.pop();
            IrMaker.currentBlock = dest;

        } else if (stmt instanceof BreakStmt breakStmt) {
            // 'break' ';'
            irUtils.makeBranch(stackOfCycle.peek().getValue()); // 无条件跳转
            // break后的指令需要丢弃
            IrMaker.currentBlock = garbageBlock;
        } else if (stmt instanceof ContinueStmt continueStmt) {
            // 'continue' ';'
            irUtils.makeBranch(stackOfCycle.peek().getKey()); // 无条件跳转
            // continue后的指令要丢弃
            IrMaker.currentBlock = garbageBlock;
        } else if (stmt instanceof ReturnStmt returnStmt) {
            // 'return' [Exp] ';'
            // 思路: 需要知道return对应的函数的返回值类型, 如果是int才处理, 否则(void类型)放到buildFuncDefIr来处理
            TokenType returnType = returnStmt.getFuncReturnType();
            Exp returnExp = returnStmt.getExp();
            // 只有int类型的函数才允许在这里处理return, void一律在buildFuncDefIr来处理
            if (returnType == TokenType.INTTK) {
                if (returnExp == null) {
                    irUtils.makeRet();  // 直接退出当前函数
                } else {
                    // System.out.println(compileTimeConstRead);
                    buildExpIr(returnExp);  // 对应的值存在 comprehensiveValue
                    irUtils.makeRet(comprehensiveValue);
                }
            } else if (returnType == TokenType.VOIDTK) {
                irUtils.makeRet();
            }
            // return后的指令要丢弃
            IrMaker.currentBlock = garbageBlock;
            // IrMaker.currentFunction = null;
        } else {
            // 'printf''('StringConst {','Exp}')'';'
            PrintfStmt printfStmt = (PrintfStmt) stmt;
            ArrayList<Exp> exps = printfStmt.getExps();
            Queue<Value> args = new LinkedList<>();
            // ArrayList<Value> args = new ArrayList<>(exps.size());
            for (Exp exp : exps) {
                buildExpIr(exp);
                args.add(IrMaker.comprehensiveValue);
                // args.add(IrMaker.comprehensiveValue);
            }

            String string = printfStmt.getStringConst().getKey();   // origin: 前后双引号, 换行符, %d
            ArrayList<String> strings = irUtils.splitConstString(string);
            // %d的数量一定和args的数量匹配
            for (String s : strings) {
                Value value;
                if (s.equals("%d")) {
                    value = args.remove();
                    irUtils.makeCall(Function.putint, new ArrayList<>(Collections.singletonList(value)));
                } else {
                    // "Hello"
                    // 全局: @str.0 = constant [6 x i8] c"Hello\00"
                    // 局部:
                    // %v1 = getelementptr inbounds [6 x i8], [6 x i8]* @str.0, i32 0, i32 0
                    // call void @putstr(i8*  %v1)
                    GlobalString globalString = irUtils.makeGlobalString(s);    // 实现 @str.0 = constant [6 x i8] c"Hello\00"
                    // System.out.println(globalString.getValueType());
                    value = irUtils.makeGep(globalString, C0, C0); // 实现 %v1 = getelementptr inbounds [6 x i8], [6 x i8]* @str.0, i32 0, i32 0
                    irUtils.makeCall(Function.putstr, new ArrayList<>(Collections.singletonList(value)));
                }
            }
        }
    }

    public void buildForStmtIr(ForStmt forStmt) {
        // 语句 ForStmt → LVal '=' Exp { ',' LVal '=' Exp }
        // 与assignStmt的区别在于可以对多个左值进行赋值
        ArrayList<LVal> lVals = forStmt.getLVals();
        ArrayList<Exp> exps = forStmt.getExps();
        for (int i = 0; i < lVals.size(); i++) {
            LVal lVal = lVals.get(i);
            buildLValIr(lVal);
            Value valueOfLVal = IrMaker.comprehensiveValue;
            Exp exp = exps.get(i);
            buildExpIr(exp);
            Value valueOfExp = IrMaker.comprehensiveValue;
            irUtils.makeStore(valueOfExp, valueOfLVal);
        }
    }

    public void buildMainFuncDefIr(MainFuncDef mainFuncDef) {
        ValueType returnValueType = I32;
        Block block = mainFuncDef.getBlock();
        IrMaker.inheritedName = "main";   // 函数名

        ArrayList<ValueType> parameterTypes = new ArrayList<>();    // 空参数列表

        FuncType funcType = new FuncType(returnValueType, parameterTypes);
        irUtils.makeFunction(IrMaker.inheritedName, funcType, false);

        irSymbolTableStack.putSymbolToGlobal("main", currentFunction);
        irSymbolTableStack.push(new IrSymbolTable());

        blockDeepth++;

        irUtils.makeBasicBlock(true);

        buildBlockIr(block);

        blockDeepth--;
        irSymbolTableStack.pop();
    }

    public void initDelcaration() {
        Function.getint = irUtils.makeFunction("getint", new FuncType(I32, new ArrayList<>()), true);
        irSymbolTableStack.putSymbolToGlobal("getint", Function.getint);

        Function.putint = irUtils.makeFunction("putint", new FuncType(VoidType, new ArrayList<>(Collections.singleton(I32))), true);
        irSymbolTableStack.putSymbolToGlobal("putint", Function.putint);

        Function.putch = irUtils.makeFunction("putch", new FuncType(VoidType, new ArrayList<>(Collections.singleton(I32))), true);
        irSymbolTableStack.putSymbolToGlobal("putch", Function.putch);

        Function.putstr = irUtils.makeFunction("putstr", new FuncType(VoidType, new ArrayList<>(Collections.singleton(new PointerType(I8)))), true);
        irSymbolTableStack.putSymbolToGlobal("putstr", Function.putstr);

        IrMaker.currentFunction = null;
    }

    public void outputInFile() {
        try {
            // 输出到主输出路径
            PrintWriter writer = new PrintWriter(outputPath);
            writer.println(module.toString());
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}