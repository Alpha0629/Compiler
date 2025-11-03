package frontend;
import java.util.ArrayList;
import java.util.List;

public class TokenList {
    private List<Token> tokens = new ArrayList<>();
    private int index = 0;

    public void addToken(Token token) {
        tokens.add(token);
    }

    public Token getNextToken() {
        Token token = tokens.get(index);
        index++;
        return token;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public int size() {
        return tokens.size();
    }

    public Token get(int i) {
        return tokens.get(i);
    }
}