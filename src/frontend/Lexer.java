package frontend;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import frontend.Error;

public class Lexer {
    private final String content;
    private final String outputPath;
    private final String errorPath;
    private char ch;
    private final TokenList tokens;
    private final List<Error> errors;
    private int line;
    private int index;
    private int length;

    public Lexer(String content, String outputPath, String errorPath) {
        this.outputPath = outputPath;
        this.errorPath = errorPath;
        this.content = content;
        this.tokens = new TokenList();
        this.errors = new ArrayList<>();
        this.line = 1;
        this.index = 0;
        this.length = content.length();
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
        if (ch == (char)-1) return treatEOF();
        if (Character.isDigit(ch)) return treatDigit();
        if (Character.isLetter(ch)) return treatAlpha();
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

        while (true) {
            ch = getNextChar();
            if (Character.isDigit(ch)) {
                tokenBuilder.append(ch);
            } else {
                rollback();
                break;
            }
        }
        return new Token(tokenBuilder.toString(), TokenType.INTCON, line);
    }

    public void parse() {
        while (true) {
            Token token = getToken();
            // 读到空指针说明是注释符，跳过即可
            if (token == null) continue;
            if (token.getValue() == TokenType.EOF) break;
            tokens.addToken(token);
        }
    }

    public void output() throws IOException {
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
    }

    private void overlookSpace() {
        int c;
        do {
            if (index == length) {
                c = -1;
                return; // 直接返回
            }
            c = content.charAt(index++);    // 读取index对应的字符并将指针向后移动
            if (c == '\n') line++;
        } while (Character.isWhitespace(c));
        // 退出循环代表读到了非空白字符，应该退格
        index--;
    }

    private char getNextChar() {
        if (index == length) return (char)-1;
        char c = content.charAt(index++);
        if ((int)c == -1) return (char)-1;
        if (c == '\n') line++;
        return (char)c;
    }

    private void rollback() {
        index--;
        ch = content.charAt(index - 1);
    }

    public TokenList getTokenList() {
        return tokens;
    }
}