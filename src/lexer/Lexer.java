package lexer;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//词法分析器
public class Lexer {

    private int line = 1;
    private char peek = ' ';
    private final BufferedReader br;

    public List<String> getError() {
        return error;
    }

    public List<Token> getTokens() {
        return tokens;
    }

    private final List<String> error = new ArrayList<>();
    private final List<Token> tokens = new ArrayList<>();

    public Lexer(BufferedReader br) {
        this.br = br;
    }

    private void getCh() throws IOException {
        if (br.ready()) {
            peek = (char) br.read();
            if (peek == '\n')
                line++;
        }
    }

    private boolean getCh(char ch) throws IOException {
        getCh();
        if(peek==ch) {
            getCh();
            return true;
        }
        return false;
    }

    public Token getToken() throws IOException {
        Token ret;
        for (; ; getCh()) {
            if (!br.ready()) {
                if (peek == ' ' || peek == '\t' || peek == '\n' || peek == '\r')
                    return null;
                break;
            }
            if (!(peek == ' ' || peek == '\t' || peek == '\n' || peek == '\r'))
                break;
        }

        //运算符识别自动机
        switch (peek) {
            case '&':
                if (getCh('&')) {
                    ret = new Token("AND", "&&");
                } else {
                    ret = new Token("BA", "&");
                }
                ret.setLine(line);
                return ret;
            case '|':
                if (getCh('|')) {
                    ret = new Token("OR", "||");
                } else {
                    ret = new Token("BO", "|");
                }
                ret.setLine(line);
                return ret;
            case '=':
                if (getCh('=')) {
                    ret = new Token("E", "==");
                } else {
                    ret = new Token("ASS", "=");
                }
                ret.setLine(line);
                return ret;
            case '!':
                if (getCh('=')) {
                    ret = new Token("NE", "!=");
                } else {
                    ret = new Token("EX", "!");
                }
                ret.setLine(line);
                return ret;
            case '<':
                if (getCh('=')) {
                    ret = new Token("LE", "<=");
                } else {
                    ret = new Token("L", "<");
                }
                ret.setLine(line);
                return ret;
            case '>':
                if (getCh('=')) {
                    ret = new Token("GE", ">=");
                } else {
                    ret = new Token("G", ">");
                }
                ret.setLine(line);
                return ret;
            case '+':
                if (getCh('+')) {
                    ret = new Token("INC", "++");
                } else {
                    ret = new Token("PLUS", "+");
                }
                ret.setLine(line);
                return ret;
            case '-':
                if (getCh('-')) {
                    ret = new Token("MO", "--");
                } else {
                    ret = new Token("MINUS", "-");
                }
                ret.setLine(line);
                return ret;
            //注释识别自动机
            case '/':
                getCh();
                if (peek == '/') {
                    do {
                        if (!br.ready())
                            break;
                        getCh();
                    } while (peek != '\n' && peek != '\r');
                    getCh();
                    return null;
                } else if (peek == '*') {
                    char c1, c2;
                    getCh();
                    do {
                        c1 = peek;
                        if (!br.ready())
                            break;
                        getCh();
                        c2 = peek;
                    } while (c1 != '*' && c2 != '/');
                    return null;
                } else {
                    ret = new Token("EXCEPT", "/");
                    ret.setLine(line);
                    return ret;
                }
        }

        //十进制常数识别
        if (Character.isDigit(peek) && peek != '0') {
            int v = 0;
            do {
                v = 10 * v + Character.digit(peek, 10);
                getCh();
            } while (Character.isDigit(peek));
            if (peek == '.') {
                float x = v;
                float d = 10;
                for (; ; ) {
                    getCh();
                    if (!Character.isDigit(peek)) break;
                    x = x + Character.digit(peek, 10) / d;
                    d = d * 10;
                }
                if (peek == 'e') {
                    StringBuilder sb = new StringBuilder();
                    sb.append(x);
                    getCh();
                    if (peek == '+' || peek == '-') {
                        sb.append(peek);
                        getCh();
                    }
                    while (Character.isDigit(peek)) {
                        sb.append(peek);
                        if (!br.ready())
                            break;
                        getCh();
                    }
                    ret = new Token("real", sb.toString(), 0);
                    ret.setLine(line);
                    return ret;
                }
                ret = new Token("real", String.valueOf(v), 0);
                ret.setLine(line);
                return ret;
            }
            //科学计数法
            else if (peek == 'e') {
                StringBuilder sb = new StringBuilder();
                sb.append(v);
                sb.append('e');
                getCh();
                if (peek == '+' || peek == '-') {
                    sb.append(peek);
                    getCh();
                }
                if (!Character.isDigit(peek)) {
                    tokens.add(new Token("num", String.valueOf(v), 0));
                    tokens.add(new Token("id", "e", 0));
                    if (sb.charAt(sb.length() - 1) == '+' || sb.charAt(sb.length() - 1) == '-') {
                        tokens.add(new Token("PLUS", "+"));
                    }
                    ret = new Token("Error", sb.toString(), 0);
                    ret.setLine(line);
                    return ret;
                }
                while (Character.isDigit(peek)) {
                    sb.append(peek);
                    if (!br.ready())
                        break;
                    getCh();
                }
                ret = new Token("real", sb.toString(), 0);
                ret.setLine(line);
                return ret;
            } else {
                ret = new Token("num", String.valueOf(v), 0);
                ret.setLine(line);
                return ret;
            }
        }
        if (peek == '0') {
            getCh();
            //0
            if (!Character.isDigit(peek) && peek != 'x') {
                ret = new Token("num", "0", 0);
                ret.setLine(line);
                return ret;
            }
            //十六进制数识别自动机
            if (peek == 'x') {
                StringBuilder sb = new StringBuilder();
                sb.append("0x");
                getCh();
                while (Character.isDigit(peek) || (peek >= 'a' && peek <= 'f') || (peek >= 'A' && peek <= 'F')) {
                    sb.append(peek);
                    if (!br.ready())
                        break;
                    getCh();
                }
                ret = new Token("digit", sb.toString(), 0);
                ret.setLine(line);
                return ret;
            }
            //八进制数识别
            if (Character.isDigit(peek)) {
                StringBuilder sb = new StringBuilder();
                sb.append("0");
                sb.append(peek);
                getCh();
                while (peek >= '0' && peek <= '7') {
                    sb.append(peek);
                    if (!br.ready())
                        break;
                    getCh();
                }
                ret = new Token("digit", sb.toString(), 0);
                ret.setLine(line);
                return ret;
            }
        }

        //标识符识别
        if (Character.isLetter(peek) || peek == '_') {
            StringBuilder sb = new StringBuilder();
            do {
                sb.append(peek);
                if (!br.ready())
                    break;
                getCh();
            } while (Character.isLetterOrDigit(peek) || peek == '_');

            //一词一码
            if (Words.words.containsKey(sb.toString())) {
                ret = new Token(Words.words.get(sb.toString()));
                ret.setLine(line);
                return ret;
            }
            ret = new Token("id", sb.toString(), 0);
            ret.setLine(line);
            return ret;
        }

        //一词一码
        if (Words.words.containsKey(Character.toString(peek))) {
            char c = peek;
            peek = ' ';
            ret = new Token(Words.words.get(Character.toString(c)));
            ret.setLine(line);
            return ret;
        }

        //字符串常量识别
        if (peek == '"') {
            StringBuilder sb = new StringBuilder();
            do {
                if (peek == '\\') {
                    getCh();
                    if (peek == '"')
                        getCh();
                }
                sb.append(peek);
                if (!br.ready())
                    break;
                getCh();
            } while (peek != '"' && peek != '\n' && peek != '\r');
            if (peek == '\n' || peek == '\r') {
                error.add("Lexical error at Line[" + (line - 1) + "]：" + sb.toString() + "\t非法字符串");
                ret = new Token("ERROR", sb.toString(), 0);
                ret.setLine(line);
                return ret;
            }
            sb.append(peek);
            peek = ' ';
            ret = new Token("CONST", sb.toString(), 0);
            ret.setLine(line);
            return ret;
        }

        char c = peek;
        peek = ' ';
        error.add("Lexical error at Line[" + (line - 1) + "]：" + c + "\t非法字符");
        ret = new Token("ERROR", Character.toString(c), 0);
        ret.setLine(line);
        return ret;
    }

    public void answer() throws IOException {
        Words.Initial();
        Token ans;
        while (true) {
            ans = getToken();
            if (ans != null)
                tokens.add(ans);
            if (!br.ready()) {
                ans = getToken();
                if (ans != null)
                    tokens.add(ans);
                break;
            }
        }
    }

    public static void main(String[] args) {
        String s = "1e-5";
        System.out.println(s.matches("[\\d.]+(?:e-?\\d+)?"));
    }
}
