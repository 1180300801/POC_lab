package parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Grammar {

    public List<Production_form> grammar = new ArrayList<>(); //文法
    public Set<String> head = new HashSet<>(); // 非终结符
    public Set<String> terminator = new HashSet<>(); //终结符

    /**
     * 从文件读入文法进行构造
     * @throws IOException 文件打开失败时报异常
     */
    public Grammar(String file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String read;
        String[] strings, strings1;
        while((read = br.readLine()) != null){
            if(!read.matches(".+->.+"))
                continue;
            strings = read.split("( )+->( )+");
            head.add(strings[0]);
            strings1 = strings[1].split("( )+丨( )+");
            for (String s : strings1) grammar.add(new Production_form(strings[0], s.split(" "), null));
        }
        terminator.add("$");
        for(Production_form pf:grammar){
            for(String s:pf.getRight()){
                if(!head.contains(s)&&!s.equals("ε")){
                    terminator.add(s);
                }
            }
        }
    }

}
