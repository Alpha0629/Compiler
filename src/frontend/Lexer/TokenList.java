package frontend.Lexer;
import java.util.ArrayList;
import java.util.List;

public class TokenList {
    private List<Token> tokens = new ArrayList<>();
    private int index = 0;

    /**
     * 向TokenList中添加一个Token
     * @param token 要添加的Token对象
     */
    public void addToken(Token token) {
        tokens.add(token);
    }

    /**
     * 获取下一个Token并移动索引
     * @return 当前索引位置的Token对象
     */
    public Token getNextToken() {
        Token token = tokens.get(index);
        index++;
        return token;
    }

    /**
     * 获取下一个Token，但不移动索引
     * @return 当前索引位置的Token对象
     */
    public Token getPreReadToken() {
        return tokens.get(index);
    }

    /**
     * 获取下两个Token，但不移动索引
     * @return 下一个索引位置的Token对象
     */
    public Token getPrePreReadToken() {
        return tokens.get(index + 1);
    }

    public Token getLastToken() {
        return tokens.get(index - 2);
    }

    /**
     * 设置当前索引位置
     * @param index 要设置的索引值
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * 获取当前索引位置
     * @return 当前索引值
     */
    public int getIndex() {
        return index;
    }

    public Token setAndGetToken(int index) {
        this.setIndex(index);
        return this.getNextToken();
    }

    /**
     * 获取TokenList中Token的总数量
     * @return Token列表的大小
     */
    public int size() {
        return tokens.size();
    }

    /**
     * 根据索引获取指定位置的Token
     * @param i 要获取的Token的索引位置
     * @return 指定索引位置的Token对象
     */
    public Token get(int i) {
        return tokens.get(i);
    }

    public Token scan(int i) {
        return tokens.get(index + i - 1);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Token token : tokens) {
            sb.append(token.toString());
        }
        return sb.toString();
    }
}