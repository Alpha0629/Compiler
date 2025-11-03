package frontend;
public class Error {
    private ErrorType error;
    private int line;

    public Error(ErrorType error, int line) {
        this.error = error;
        this.line = line;
    }

    public ErrorType getErrorType() {
        return error;
    }

    public int getLine() {
        return line;
    }

    @Override
    public String toString() {
        return line + " " + error;
    }
}