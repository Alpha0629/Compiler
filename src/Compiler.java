import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import frontend.Lexer.Lexer;
import frontend.Lexer.TokenList;
import frontend.Parser.Parser;
import frontend.Error.Error;
import frontend.Visitor.Visitor;

public class Compiler {
    public static void main(String[] args) {
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);

        String filePath = "testfile.txt";
        String outputPath = "symbol.txt";
        String errorPath = "error.txt";
        String content = new String();
        try {
            content = new String(Files.readAllBytes(Paths.get(filePath)));
            // System.err.println(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ArrayList<Error> errors = new ArrayList<>();
        Lexer lexer = new Lexer(content, outputPath, errorPath, errors);
        TokenList tokens = lexer.parse();
        // lexer.outputInFile();
        Parser parser = new Parser(tokens, outputPath, errorPath, errors);
        // parser.parse();
        Visitor visitor = new Visitor(parser.parse(), outputPath, errorPath, errors);
        visitor.visit();
        // System.out.println(parser.parse());
    }
}