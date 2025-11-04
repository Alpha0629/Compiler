package frontend.Lexer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import frontend.Error.Error;
import frontend.Error.ErrorType;

public class Lexer {
    private final String content;
    private final String outputPath;
    private final String errorPath;
    private char ch;
    private char nextChar;
    private final TokenList tokens;
    private final ArrayList<Error> errors;
    private int line;
    private int index;  // index永远指向下一个要读的字符!!!
    private int length;

    public Lexer(String content, String outputPath, String errorPath, ArrayList<Error> errors) {
        this.outputPath = outputPath;
        this.errorPath = errorPath;
        this.content = content;
        this.tokens = new TokenList();
        this.line = 1;
        this.index = 0;
        this.length = content.length();
        this.errors = errors;
    }

    public TokenList parse() {
        while (true) {
            Token token = getToken();
            // 读到空指针说明是注释符，跳过即可
            if (token == null) continue;
            if (token.getValue() == TokenType.EOF) {
                tokens.addToken(token);
                break;
            }
            tokens.addToken(token);
        }
        return tokens;
    }

    public Token getToken() {
        overlookSpace();
        ch = getNextChar();
        // System.out.println("ch: " + ch);
        if (ch == (char)-1) return treatEOF();
        if (Character.isDigit(ch)) return treatDigit();
        if (Character.isLetter(ch) || ch == '_') return treatAlpha();
        if (isSign(ch)) return treatSign();
        if (isString(ch)) return treatString();
        return null;
    }

    private boolean isSign(char ch) {
        return ch == '+' || ch == '-' || ch == '*' || ch == '/' || ch == '%' || ch == ';' || ch == ',' ||
                ch == '(' || ch == ')' || ch == '[' || ch == ']' || ch == '{' || ch == '}' || ch == '!' ||
                ch == '<' || ch == '>' || ch == '=' || ch == '&' || ch == '|';
    }

    private boolean isString(char ch) {
        return ch == '\"';
    }

    private Token treatEOF() {
        return new Token("EOF", TokenType.EOF, line);
    }
    
    private Token treatAlpha() {
        int curline = line; // 第一个符号所在行
        StringBuilder tokenBuilder = new StringBuilder();
        tokenBuilder.append(ch);

        while (true) {
            // ch = getNextChar();
            nextChar = peek();
            if (!Character.isLetterOrDigit(nextChar) && nextChar != '_') {
                // rollback();
                break;
            }
            ch = getNextChar();
            tokenBuilder.append(ch);
        }

        String token = tokenBuilder.toString();
        
        // 关键字
        if (token.equals("const")) return new Token("const", TokenType.CONSTTK, curline);
        if (token.equals("int")) return new Token("int", TokenType.INTTK, curline);
        if (token.equals("static")) return new Token("static", TokenType.STATICTK, curline);
        if (token.equals("break")) return new Token("break", TokenType.BREAKTK, curline);
        if (token.equals("continue")) return new Token("continue", TokenType.CONTINUETK, curline);
        if (token.equals("if")) return new Token("if", TokenType.IFTK, curline);
        if (token.equals("main")) return new Token("main", TokenType.MAINTK, curline);
        if (token.equals("else")) return new Token("else", TokenType.ELSETK, curline);
        if (token.equals("for")) return new Token("for", TokenType.FORTK, curline);
        if (token.equals("return")) return new Token("return", TokenType.RETURNTK, curline);
        if (token.equals("void")) return new Token("void", TokenType.VOIDTK, curline);
        if (token.equals("printf")) return new Token("printf", TokenType.PRINTFTK, curline);
        
        // 如果不是关键字，则返回标识符
        return new Token(token, TokenType.IDENFR, curline);
    }

    private Token treatString() {
        // 是字符串，位于printf内部
        int curline = line;
        StringBuilder tokenBuilder = new StringBuilder();
        tokenBuilder.append(ch);

        do {
            ch = getNextChar();
            tokenBuilder.append(ch);
        } while (ch != '\"');
        // 现在ch一定为引号
        return new Token(tokenBuilder.toString(), TokenType.STRCON, curline);
    }

    private Token treatSign() {
        // 符号
        int curline = line;
        switch (ch) {
            case '+': return new Token("+", TokenType.PLUS, curline);
            case '-': return new Token("-", TokenType.MINU, curline);
            case '*': return new Token("*", TokenType.MULT, curline);
            case '%': return new Token("%", TokenType.MOD, curline);
            case ';': return new Token(";", TokenType.SEMICN, curline);
            case ',': return new Token(",", TokenType.COMMA, curline);
            case '(': return new Token("(", TokenType.LPARENT, curline);
            case ')': return new Token(")", TokenType.RPARENT, curline);
            case '[': return new Token("[", TokenType.LBRACK, curline);
            case ']': return new Token("]", TokenType.RBRACK, curline);
            case '{': return new Token("{", TokenType.LBRACE, curline);
            case '}': return new Token("}", TokenType.RBRACE, curline);

            case '/':
                // ch = getNextChar();
                nextChar = peek();
                if (nextChar == '/' || nextChar == '*') {
                    ch = getNextChar(); // 现在ch要么是/ 要么是*
                    if (ch == '/') {
                        // 是单行注释
                        do {
                            ch = getNextChar();
                            if (ch == (char)-1) return new Token("EOF", TokenType.EOF, curline);
                        } while (ch != '\n');
                        // 要么已经读到文件尾退出了，要么读到了换行符，代表单行注释结束
                        return null;
                    } else {
                        // 是多行注释
                        while (true) {
                            ch = getNextChar();
                            if (ch == '*') {
                                // 当读到了一个星号
                                // ch = getNextChar();
                                nextChar = peek();
                                if (nextChar == '/') {
                                    // 多行注释结束了
                                    ch = getNextChar(); // 现在ch是/
                                    return null;
                                } else {
                                    // 仅仅是一个星号，什么都不做
                                }
                            }
                        }
                    }
                } else {
                    return new Token("/", TokenType.DIV, curline);
                }

            case '!':
                // ch = getNextChar();
                nextChar = peek();
                if (nextChar == '=') {
                    ch = getNextChar();
                    return new Token("!=", TokenType.NEQ, curline);
                }
                else {
                    return new Token("!", TokenType.NOT, curline);
                }

            case '<':
                // ch = getNextChar();
                nextChar = peek();
                if (nextChar == '=') {
                    ch = getNextChar();
                    return new Token("<=", TokenType.LEQ, curline);
                }
                else {
                    return new Token("<", TokenType.LSS, curline);
                }

            case '>':
                // ch = getNextChar();
                nextChar = peek();
                if (nextChar == '=') {
                    ch = getNextChar();
                    return new Token(">=", TokenType.GEQ, curline);
                }
                else {
                    return new Token(">", TokenType.GRE, curline);
                }

            case '=':
                // ch = getNextChar();
                nextChar = peek();
                if (nextChar == '=') {
                    ch = getNextChar();
                    return new Token("==", TokenType.EQL, curline);
                }
                else {
                    return new Token("=", TokenType.ASSIGN, curline);
                }

            case '&':
                // ch = getNextChar();
                nextChar = peek();
                if (nextChar == '&') {
                    ch = getNextChar();
                    return new Token("&&", TokenType.AND, curline);
                }
                else {
                    errors.add(new Error(ErrorType.a, curline));
                    return new Token("&&", TokenType.AND, curline);
                }

            case '|':
                // ch = getNextChar();
                nextChar = peek();
                if (nextChar == '|') {
                    ch = getNextChar();
                    return new Token("||", TokenType.OR, curline);
                }
                else {
                    errors.add(new Error(ErrorType.a, curline));
                    return new Token("||", TokenType.OR, curline);
                }

            default: return null;
        }
    }

    private Token treatDigit() {
        // 纯数字
        int curline = line;
        StringBuilder tokenBuilder = new StringBuilder();
        tokenBuilder.append(ch);
        // System.out.println("digit is here: " + ch + " " + line);
        while (true) {
            // ch = getNextChar();
            nextChar = peek();
            // System.out.println(line);
            if (Character.isDigit(nextChar)) {
                ch = getNextChar();
                tokenBuilder.append(ch);
            } else {
                break;
            }
        }
        return new Token(tokenBuilder.toString(), TokenType.INTCON, curline);
    }

    public void output() {
        System.out.println(tokens.toString());
    }

    public void outputInFile() {
        if (errors.isEmpty()) {
            PrintWriter writer = null;
            try {
                writer = new PrintWriter(outputPath);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            for (int i = 0; i < tokens.size(); i++) {
                if (tokens.get(i).getValue() != TokenType.EOF) {
                    writer.println(tokens.get(i).toStringInFIle());
                }
            }
            writer.close();
        }
    }


    private void overlookSpace() {
        int c;
        do {
            if (index == length) {  // 将要读的位置已经到达文件尾部了
                c = -1;
                return; // 直接返回
            }
            c = content.charAt(index++);    // 读取index对应的字符并将指针向后移动
            if (c == '\n') {
                line++;
                // System.out.println("\\n is here: " + c + " " + line);
            }
        } while (Character.isWhitespace(c));
        // 退出循环代表读到了非空白字符，应该退格
        index--;
    }

    private char getNextChar() {
        if (index == length) return (char)-1;
        char c = content.charAt(index++);   // 先获取index对饮的字符，再自增
        // System.out.println("next char: " + (int)c);
        if (c == '\n') {
            line++;
        }
        return c;
    }

    private char peek() {
        if (index == length) return (char)-1;
        return content.charAt(index);
    }

    private void rollback() {
        index--;
        ch = content.charAt(index - 1);
        if (content.charAt(index) == '\n') line--;
    }

    public TokenList getTokenList() {
        return tokens;
    }
}












/*
package frontend.Lexer;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

        import frontend.Error.Error;
import frontend.Error.ErrorType;

public class Lexer {
    private final String content;
    private final String outputPath;
    private final String errorPath;
    private char ch;
    private final TokenList tokens;
    private final ArrayList<Error> errors;
    private int line;
    private int index;
    private int length;

    public Lexer(String content, String outputPath, String errorPath, ArrayList<Error> errors) {
        this.outputPath = outputPath;
        this.errorPath = errorPath;
        this.content = content;
        this.tokens = new TokenList();
        this.line = 1;
        this.index = 0;
        this.length = content.length();
        this.errors = errors;
    }

    private boolean isSign(char ch) {
        return ch == '+' || ch == '-' || ch == '*' || ch == '/' || ch == '%' || ch == ';' || ch == ',' ||
                ch == '(' || ch == ')' || ch == '[' || ch == ']' || ch == '{' || ch == '}' || ch == '!' ||
                ch == '<' || ch == '>' || ch == '=' || ch == '&' || ch == '|';
    }

    private boolean isString(char ch) {
        return ch == '\"';
    }

    public Token getToken() {
        overlookSpace();
        ch = getNextChar();
        // System.out.println("ch: " + ch);
        if (ch == (char)-1) return treatEOF();
        if (Character.isDigit(ch)) return treatDigit();
        if (Character.isLetter(ch) || ch == '_') return treatAlpha();
        if (isSign(ch)) return treatSign();
        if (isString(ch)) return treatString();
        return null;
    }

    private Token treatEOF() {
        return new Token("EOF", TokenType.EOF, line);
    }

    private Token treatAlpha() {
        StringBuilder tokenBuilder = new StringBuilder();
        tokenBuilder.append(ch);

        while (true) {
            ch = getNextChar();
            if (!Character.isLetterOrDigit(ch) && ch != '_') {
                rollback();
                break;
            }
            tokenBuilder.append(ch);
        }

        String token = tokenBuilder.toString();

        // 关键字
        if (token.equals("const")) return new Token("const", TokenType.CONSTTK, line);
        if (token.equals("int")) return new Token("int", TokenType.INTTK, line);
        if (token.equals("static")) return new Token("static", TokenType.STATICTK, line);
        if (token.equals("break")) return new Token("break", TokenType.BREAKTK, line);
        if (token.equals("continue")) return new Token("continue", TokenType.CONTINUETK, line);
        if (token.equals("if")) return new Token("if", TokenType.IFTK, line);
        if (token.equals("main")) return new Token("main", TokenType.MAINTK, line);
        if (token.equals("else")) return new Token("else", TokenType.ELSETK, line);
        if (token.equals("for")) return new Token("for", TokenType.FORTK, line);
        if (token.equals("return")) return new Token("return", TokenType.RETURNTK, line);
        if (token.equals("void")) return new Token("void", TokenType.VOIDTK, line);
        if (token.equals("printf")) return new Token("printf", TokenType.PRINTFTK, line);

        // 如果不是关键字，则返回标识符
        return new Token(token, TokenType.IDENFR, line);
    }

    private Token treatString() {
        StringBuilder tokenBuilder = new StringBuilder();
        tokenBuilder.append(ch);

        do {
            ch = getNextChar();
            tokenBuilder.append(ch);
        } while (ch != '\"');

        return new Token(tokenBuilder.toString(), TokenType.STRCON, line);
    }

    private Token treatSign() {
        switch (ch) {
            case '+': return new Token("+", TokenType.PLUS, line);
            case '-': return new Token("-", TokenType.MINU, line);
            case '*': return new Token("*", TokenType.MULT, line);
            case '%': return new Token("%", TokenType.MOD, line);
            case ';': return new Token(";", TokenType.SEMICN, line);
            case ',': return new Token(",", TokenType.COMMA, line);
            case '(': return new Token("(", TokenType.LPARENT, line);
            case ')': return new Token(")", TokenType.RPARENT, line);
            case '[': return new Token("[", TokenType.LBRACK, line);
            case ']': return new Token("]", TokenType.RBRACK, line);
            case '{': return new Token("{", TokenType.LBRACE, line);
            case '}': return new Token("}", TokenType.RBRACE, line);

            case '/':
                ch = getNextChar();
                if (ch == '/' || ch == '*') {
                    if (ch == '/') {
                        do {
                            ch = getNextChar();
                            if (ch == (char)-1) return new Token("EOF", TokenType.EOF, line);
                        } while (ch != '\n');
                        return null;
                    } else {
                        while (true) {
                            ch = getNextChar();
                            if (ch == '*') {
                                ch = getNextChar();
                                if (ch == '/') {
                                    return null;
                                } else {
                                    rollback();
                                }
                            }
                        }
                    }
                } else {
                    rollback();
                    return new Token("/", TokenType.DIV, line);
                }

            case '!':
                ch = getNextChar();
                if (ch == '=') return new Token("!=", TokenType.NEQ, line);
                else {
                    rollback();
                    return new Token("!", TokenType.NOT, line);
                }

            case '<':
                ch = getNextChar();
                if (ch == '=') return new Token("<=", TokenType.LEQ, line);
                else {
                    rollback();
                    return new Token("<", TokenType.LSS, line);
                }

            case '>':
                ch = getNextChar();
                if (ch == '=') return new Token(">=", TokenType.GEQ, line);
                else {
                    rollback();
                    return new Token(">", TokenType.GRE, line);
                }

            case '=':
                ch = getNextChar();
                if (ch == '=') return new Token("==", TokenType.EQL, line);
                else {
                    rollback();
                    return new Token("=", TokenType.ASSIGN, line);
                }

            case '&':
                ch = getNextChar();
                if (ch == '&') return new Token("&&", TokenType.AND, line);
                else {
                    rollback();
                    errors.add(new Error(ErrorType.a, line));
                    return new Token("&&", TokenType.AND, line);
                }

            case '|':
                ch = getNextChar();
                if (ch == '|') return new Token("||", TokenType.OR, line);
                else {
                    rollback();
                    errors.add(new Error(ErrorType.a, line));
                    return new Token("||", TokenType.OR, line);
                }

            default: return null;
        }
    }

    private Token treatDigit() {
        StringBuilder tokenBuilder = new StringBuilder();
        tokenBuilder.append(ch);
        // System.out.println("digit is here: " + ch + " " + line);
        while (true) {
            ch = getNextChar();
            // System.out.println(line);
            if (Character.isDigit(ch)) {
                tokenBuilder.append(ch);
            } else {
                rollback();
                break;
            }
        }
        return new Token(tokenBuilder.toString(), TokenType.INTCON, line);
    }

    public TokenList parse() {
        while (true) {
            Token token = getToken();
            // 读到空指针说明是注释符，跳过即可
            if (token == null) continue;
            if (token.getValue() == TokenType.EOF) {
                tokens.addToken(token);
                break;
            }
            tokens.addToken(token);
        }
        return tokens;
    }

    public void output() {
        System.out.println(tokens.toString());
    }

    public void outputInFile() {
        try {
            if (errors.isEmpty()) {
                PrintWriter writer = new PrintWriter(outputPath);
                for (int i = 0; i < tokens.size(); i++) {
                    writer.println(tokens.get(i).toString());
                }
                writer.close();
            } else {
                PrintWriter writer = new PrintWriter(errorPath);
                for (int i = 0; i < errors.size(); i++) {
                    writer.println(errors.get(i).toString());
                }
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void overlookSpace() {
        int c;
        do {
            if (index == length) {
                c = -1;
                return; // 直接返回
            }
            c = content.charAt(index++);    // 读取index对应的字符并将指针向后移动
            if (c == '\n') {
                line++;
                // System.out.println("\\n is here: " + c + " " + line);
            }
        } while (Character.isWhitespace(c));
        // 退出循环代表读到了非空白字符，应该退格
        index--;
    }

    private char getNextChar() {
        if (index == length) return (char)-1;
        char c = content.charAt(index++);
        // System.out.println("next char: " + (int)c);
        if (c == '\n') {
            line++;
            // System.out.println("abcd");
        }
        return (char)c;
    }

    private void rollback() {
        index--;
        ch = content.charAt(index - 1);
        if (content.charAt(index) == '\n') line--;
    }

    public TokenList getTokenList() {
        return tokens;
    }
}*/