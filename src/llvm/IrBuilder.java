package llvm;

import frontend.Parser.Node.CompUnit;
import frontend.Parser.Node.ConstDecl;
import frontend.Parser.Node.Decl;
import frontend.Parser.Node.FuncDef;
import frontend.Parser.Node.MainFuncDef;
import frontend.Parser.Node.VarDecl;

import java.util.ArrayList;

/**
 * {@code @Description} Ir构建器
 */
public class IrBuilder {
    private final CompUnit AST; // 语法分析得到的抽象语法树
    private final IrSymbolTableStack symbolTableStack;  // 符号表栈，每一个符号表代表一个作用区域

    public IrBuilder(CompUnit AST) {
        this.AST = AST;
        this.symbolTableStack = new IrSymbolTableStack();
    }

    public void buildCompUnitIr() {
        ArrayList<Decl> decls = AST.getDecls();
        for (Decl decl : decls) {
            buildDeclIr(decl);
        }
        ArrayList<FuncDef> funcDefs = AST.getFuncDefs();
        for (FuncDef funcDef : funcDefs) {
            buildFuncDef(funcDef);
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

    }

    public void buildVarDeclIr(VarDecl varDecl) {

    }

    public void buildFuncDef(FuncDef funcDef) {

    }

    public void buildMainFuncDefIr(MainFuncDef mainFuncDef) {

    }
}
