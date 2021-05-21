package lexer;

import java.util.Hashtable;

public class Words {
    public static Hashtable<String,Token> words = new Hashtable<>();

    private static void add(Token tok){
        words.put(tok.getValue(),tok);
    }

    public static void Initial(){
        add(new Token("IF","if"));
        add(new Token("ELSE","else"));
        add(new Token("WHERE","where"));
        add(new Token("DO","do"));
        add(new Token("WHILE","while"));
        add(new Token("BREAK","break"));
        add(new Token("SWITCH","switch"));
        add(new Token("CASE","case"));

        add(new Token("SLP","("));
        add(new Token("SRP",")"));
        add(new Token("MLP","["));
        add(new Token("MRP","]"));
        add(new Token("LP","{"));
        add(new Token("RP","}"));
        add(new Token("COM",","));
        add(new Token("SEMI",";"));
        add(new Token("COLON",":"));
        add(new Token("PO","."));

        add(new Token("CHAR","char"));
        add(new Token("SHORT","short"));
        add(new Token("INT","int"));
        add(new Token("LONG","long"));
        add(new Token("FLOAT","float"));
        add(new Token("DOUBLE","double"));
        add(new Token("STR","struct"));
        add(new Token("VOID","void"));

        add(new Token("RET","return"));

        add(new Token("EXCEPT","/"));
        add(new Token("MUL","*"));
        add(new Token("MOD","%"));
    }

}
