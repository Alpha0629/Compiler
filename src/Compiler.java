import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;

import backend.MipsMaker;
import backend.MipsModule;
import frontend.Lexer.Lexer;
import frontend.Lexer.TokenList;
import frontend.Parser.Node.CompUnit;
import frontend.Parser.Parser;
import frontend.Error.Error;
import frontend.Visitor.Visitor;
import llvm.IrMaker;
import llvm.IrModule;

public class Compiler {
    public static ArrayList<Error> errors = new ArrayList<>();
    public static String filePath = "testfile.txt";
    public static String errorPath = "error.txt";
    public static TokenList tokens;
    public static CompUnit AST;
    public static String content;
    public static Lexer lexer;
    public static Parser parser;
    public static Visitor visitor;
    public static IrMaker irBuilder;
    public static IrModule irModule;
    public static MipsMaker mipsMaker;
    public static MipsModule mipsModule;

    public static void File2String() {
        try {
            content = new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            content = "";
        }
    }

    public static void outputErrorInFile() {
        System.out.println("Contains Errors, Stop code generation");
        try {
            PrintWriter writer = new PrintWriter(errorPath);
            errors.sort(Comparator.comparingInt(Error::getLine));
            for (Error error : errors) {
                writer.println(error.toString());
            }
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void generateLexer() {
        System.out.println("Generating lexer");
        lexer = new Lexer(content, "lexer.txt", errorPath, errors);
        tokens = lexer.parse();
    }

    public static void generateParser() {
        System.out.println("Generating parser");
        parser = new Parser(tokens, "parser.txt", errorPath, errors);
        AST = parser.parse();
        // parser.outputInFile();
    }

    public static void generateVisitor() {
        System.out.println("Generating visitor");
        visitor = new Visitor(AST, "symbol.txt", errorPath, errors);
        visitor.visit();
        // visitor.outputInFile();
    }

    public static void generateIrBuilder() {
        System.out.println("Generating ir builder");
        irModule = new IrModule();
        irBuilder = new IrMaker(irModule, AST, "llvm_ir.txt");
        irBuilder.buildCompUnitIr();
        irBuilder.outputInFile();
        // System.out.println(module.toString());
    }

    public static void generateMips() {
        System.out.println("Generating Mips");
        mipsModule = new MipsModule();
        mipsMaker = new MipsMaker(mipsModule, irModule, "mips.txt");
        mipsMaker.buildMips();
        mipsMaker.outputInFile();
    }

    public static void main(String[] args) {
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
        File2String();
        generateLexer();
        generateParser();
        generateVisitor();
        if (errors.isEmpty()) {
            generateIrBuilder();
            generateMips();
        } else {
            outputErrorInFile();
        }
    }
}