package lexer;

//token系列
public class Token {
    private final String tag;
    private final String value;
    private int line = -1;
    private int flag = 1; //为1表示一词一码，为0表示多词一码

    public Token(String tag, String value) {
        this.tag = tag;
        this.value = value;
    }

    public Token(String tag, String value, int flag) {
        this.tag = tag;
        this.value = value;
        this.flag = flag;
    }

    public Token(Token token) {
        this.tag = token.getTag();
        this.value = token.getValue();
    }

    public String getTag() {
        return tag;
    }

    public String getValue() {
        return value;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getFlag() {
        return flag;
    }

    @Override
    public String toString() {
        if(flag==1)
            return "< "+tag+" , - >";
        else
            return "< "+tag+" , "+ value + " >";
    }
}
