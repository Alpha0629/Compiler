import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import frontend.Lexer;

public class Compiler {
    public static void main(String[] args) {
        String filePath = "testfile.txt";
        String outputPath = "lexer.txt";
        String errorPath = "error.txt";
        try {
            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            // System.err.println(content);
            Lexer lexer = new Lexer(content, outputPath, errorPath);
            lexer.parse();
            lexer.output();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}