package parser;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Analysis_table {

    public Grammar gra; //对应分析表的文法
    public List<Set<Production_form>> states = new ArrayList<>(); //状态集
    private final Map<String,Set<String>> firsts = new HashMap<>(); //各个非终结符的first集
    public List<Map<String,String>> analysis_table = new ArrayList<>(); //语法分析表

    /**
     * 构造语法分析表
     * @throws IOException 文件打开失败报异常
     */
    public Analysis_table() throws IOException {
        gra = new Grammar("src//Grammar.txt");
        if(!gra.grammar.isEmpty()) {
            //初始化状态0
            Set<Production_form> state = new HashSet<>();
            Set<String> set = new HashSet<>();
            set.add("$");
            for(Production_form production_form:gra.grammar)
                if(production_form.getLeft().equals(gra.grammar.get(0).getLeft()))
                    state.add(new Production_form(gra.grammar.get(0).getLeft(),production_form.getRight(),set));
            Closure(state);
            states.add(state);
            analysis_table.add(new HashMap<>());

            int cur = 0, pre = 0, top;
            Stack<Integer> stack = new Stack<>();
            stack.push(cur);
            while(!stack.empty()){
                top = stack.pop();
                Set<Production_form> cur_state = states.get(top);
                for(Production_form pf:cur_state){
                    if(pf.getIndex() < pf.getRight().length&&!pf.getRight()[0].equals("ε"))
                        Goto(top, cur_state, pf.getRight()[pf.getIndex()]);
                    else {
                        for(String string:pf.getNext())
                            Action(top,cur_state,string);
                    }
                }
                int size = states.size();
                for(;cur<size;cur++){
                    if(cur>pre) {
                        stack.push(cur);
                    }
                }
                pre = cur - 1;
            }
            writeFile(); //将分析表写入文件
        }
    }

    /**
     * 构造状态集的闭包
     * @param state 待构造闭包的状态集
     */
    private void Closure(Set<Production_form> state){
        Stack<Production_form> stack = new Stack<>();
        for(Production_form pf:state){
            stack.push(pf);
        }
        Production_form pf;
        Set<String> next;
        while(!stack.empty()){
            pf = stack.pop();
            if(pf.getIndex()<pf.getRight().length) {
                if(gra.head.contains(pf.getRight()[pf.getIndex()])) {
                    //点号后面还有字符
                    if(pf.getIndex()<pf.getRight().length-1){
                        String[] strings = new String[pf.getRight().length-1-pf.getIndex()];
                        for(int i = pf.getIndex()+1;i<pf.getRight().length;i++){
                            strings[i-pf.getIndex()-1] = pf.getRight()[i];
                        }
                        next = First(strings, pf.getNext());
                    }
                    //点号后面没有字符
                    else
                        next = pf.getNext();
                    //扩展闭包
                    Production_form pro_f;
                    for(Production_form production_form:gra.grammar){
                        if(production_form.getLeft().equals(pf.getRight()[pf.getIndex()])){
                            pro_f = new Production_form(production_form.getLeft(),production_form.getRight(),next);
                            if(!state.contains(pro_f))
                                stack.push(pro_f);
                            state.add(pro_f);
                        }
                    }
                }
            }
        }
    }

    /**
     * 在当前状态读入下一个语法单元后进入何状态或采取何种动作
     * @param i 状态标号
     * @param state 当前所处状态
     * @param cur 遇到的语法单元
     */
    private void Goto(int i,Set<Production_form> state, String cur){
        //存储当前状态集中在等待cur的产生式在接收到cur后的形式
        Set<Production_form> state1 = new HashSet<>();
        for(Production_form pf:state){
            if(pf.getIndex()<pf.getRight().length&&pf.getRight()[pf.getIndex()].equals(cur))
                state1.add(new Production_form(pf.getLeft(),pf.getRight(),pf.getIndex()+1,pf.getNext()));
        }
        if(state1.isEmpty())
            return;
        Closure(state1);
        //遍历所有状态集，看是否有可转移的状态
        if(states.contains(state1)){
            int j = states.indexOf(state1);
            if (gra.head.contains(cur))
                analysis_table.get(i).put(cur, j + "");
            else
                analysis_table.get(i).put(cur, "s" + j);
        }
        //不存在可转移的状态闭包则创建
        else {
            states.add(state1);
            analysis_table.add(new HashMap<>());
            if (gra.head.contains(cur))
                analysis_table.get(i).put(cur, states.size()-1 + "");
            else
                analysis_table.get(i).put(cur, "s" + (states.size()-1));
        }
    }

    /**
     * 当前状态遇到某一个语法单元时采取何种动作
     * @param i 状态标号
     * @param state 当前状态
     * @param cur 当前遇到的语法单元
     */
    private void Action(int i,Set<Production_form> state, String cur){
        for(Production_form pf:state){
            if(pf.getIndex()<pf.getRight().length&&!pf.getRight()[0].equals("ε"))
                Goto(i,state,cur);
            else{
                if(pf.getNext().contains(cur)) {
                    int j = gra.grammar.indexOf(new Production_form(pf.getLeft(), pf.getRight(), null));
                    if (pf.getLeft().equals(gra.grammar.get(0).getLeft()))
                        analysis_table.get(i).put(cur, "r" + j + "_acc");
                    else
                        analysis_table.get(i).put(cur, "r" + j);
                }
            }
        }
    }

    /**
     * 计算展望符
     * @param aft β
     * @param end a
     * @return 展望符
     */
    private Set<String> First(String[] aft, Set<String> end){
        Set<String> set = new HashSet<>();
        if(aft!=null) {
            for (String s : aft) {
                set.addAll(First(s));
                if (!(First(s)).contains("ε")) {
                    set.remove("ε");
                    return set;
                }
            }
        }
        set.remove("ε");
        set.addAll(end);
        return set;
    }

    /**
     * 求first集
     * @param head 产生式头部
     * @return first集
     */
    private Set<String> First(String head){
        Set<String> ret = new HashSet<>();
        Set<String> temp;

        //终结符或空串
        if(!gra.head.contains(head)) {
            ret.add(head);
            return ret;
        }

        //如果之前求过一次，直接取出，避免重复求解
        if(firsts.containsKey(head))
            return firsts.get(head);

        //找到所有左部为该非终结符的产生式，并求其First集
        for(Production_form pf:gra.grammar){
            if(pf.getLeft().equals(head)){
                //对每个产生式求First集
                for(String s:pf.getRight()){
                    if(!s.equals(head)){
                        temp = First(s);
                        ret.addAll(temp);
                        if(!temp.contains("ε"))
                            break;
                    }
                }
            }
        }
        firsts.put(head,ret);
        return ret;
    }

    /**
     * 将语法分析表写入文件
     * @throws IOException 文件打开失败时抛出异常
     */
    private void writeFile() throws IOException {
        PrintWriter printWriter = new PrintWriter("src//analysis_table.txt");
        printWriter.printf("%16s","");
        for(String s: gra.terminator)
            printWriter.printf("%16s",s);
        for (String s: gra.head)
            printWriter.printf("%16s",s);
        printWriter.print("\n");
        int i = 0;
        for(Map<String,String> map:analysis_table){
            printWriter.printf("%16s",i);
            for(String s: gra.terminator)
                printWriter.printf("%16s", map.getOrDefault(s, "--"));
            for (String s: gra.head)
                printWriter.printf("%16s", map.getOrDefault(s, "--"));
            printWriter.print("\n");
            i++;
        }
        printWriter.close();
    }
}