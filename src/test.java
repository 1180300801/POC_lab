import parser.Analysis_table;
import parser.Grammar;
import parser.Production_form;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class test {
    public static void main(String[] args) throws IOException {
        Grammar gra = new Grammar("D:\\ctf\\IdeaProjects\\POC_lab\\src\\parser\\Grammar.txt");
        for(int i = 0;i<gra.grammar.size();i++){
            System.out.println(gra.grammar.get(i));
        }
        //Grammar gra = new Grammar("D:\\ctf\\IdeaProjects\\POC_lab\\src\\parser\\Grammar.txt");
        System.out.println(gra.head);
        Analysis_table analysis_table = new Analysis_table();
        for(int i = 0;i<analysis_table.states.size();i++){
            System.out.println(analysis_table.states.get(i));
        }
        int i = -1;
        for(Map<String, String> map:analysis_table.analysis_table){
            i++;
            for(Map.Entry<String, String> entry:map.entrySet()){
                System.out.println(i +"\t"+entry.getKey()+"\t"+entry.getValue());
            }
        }
//        String[] s = {"E","+","T"};
//        String[] s1 = {"E","+","T"};
//        Set<String> set = new HashSet<>();
//        set.add("$");
//        set.add(")");
//        set.add("*");
//        set.add("+");
//        Set<String> set2 = new HashSet<>();
//        set2.add("$");
//        set2.add(")");
//        set2.add("*");
//        set2.add("+");
//        Production_form pf1 = new Production_form("E",s,set);
//        Production_form pf2 = new Production_form("E",s1,set2);
//        Set<Production_form> set1 = new HashSet<>();
//        set1.add(pf1);
//        set1.add(pf2);
//        System.out.println(set1.toString());
    }
}
