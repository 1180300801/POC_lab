package parser;

import lexer.Lexer;
import lexer.Token;
import symbols.ProcSymbol;
import symbols.SymbolTable;
import symbols.CommonSymbol;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class parser {

    private final List<Token> In_list; //输入程序经过词法分析后得到的token序列
    private Node root; //语法分析树的根节点
    private final Stack<Integer> state_stack = new Stack<>(); //状态栈
    private final Stack<Node> in_stack = new Stack<>(); //符号栈
    private final Stack<Symbol> symbol_stack = new Stack<>();
    private final Analysis_table table = new Analysis_table(); // 语法分析表
    private final StringBuilder out = new StringBuilder(); //存放输出的语法树
    private final StringBuilder errors = new StringBuilder(); //存放检测到的错误
    private final List<InterCode> interCodeList = new ArrayList<>(); // 中间代码
    private final List<SymbolTable> symbol_tables = new ArrayList<>(); //符号表

    public List<SymbolTable> getSymbol_tables() {
        return symbol_tables;
    }

    public StringBuilder getErrors() {
        return errors;
    }

    public List<InterCode> getInterCodeList() {
        return interCodeList;
    }

    public parser(String path) throws IOException {
        //词语法分析器使用的词法分析器
        Lexer lexer = new Lexer(new BufferedReader(new FileReader(path)));
        lexer.answer();
        this.In_list = lexer.getTokens();
    }


    private String lookUpSymbolTableForUse(String lexeme, SymbolTable table) {
        for (SymbolTable symbolBoard = table; symbolBoard != null; symbolBoard = symbolBoard.getPrev()) {
            if (symbolBoard.getSymbolItem(lexeme) != null)
                return lexeme;  // TODO 仅仅返回了该id对应的名字 而未真正返回地址
        }
        return null;
    }

    private String lookUpSymbolTableForCheck(String lexeme, SymbolTable table) {
        // TODO 仅仅返回了该id对应的名字 而未真正返回地址
        return table.getSymbolItem(lexeme) != null ? lexeme : null;
    }

    private CommonSymbol lookUpSymbolTable(String lexeme, SymbolTable table) {
        CommonSymbol ans;
        for (; table != null; table = table.getPrev()) {
            ans = table.getSymbolItem(lexeme);
            if (ans != null)
                return ans;
        }
        return null;
    }

    private String getArrayElemType(String type) {
        int left = type.lastIndexOf("[");
        int right = type.lastIndexOf("]");
        if (left < right && left > 0) {
            int ll = type.indexOf("[");
            if (ll == left)
                return type.substring(0, ll);
            else
                return type.substring(0, ll) + type.substring(left, right + 1);
//            return type.substring(0, type.indexOf("["));
        }
        return type;
    }

    private int getArrayTypeWidth(String type) {
        int left = type.indexOf("[");
        int right = type.indexOf("]");
        String arrayBaseType = type;
        int base = 0;
        int len = 1;
        if (left > 0 && right > left) {
            arrayBaseType = type.substring(0, left);
        }
        if (arrayBaseType.equals("int")) {
            base = 4;
        } else if (arrayBaseType.equals("float")) {
            base = 8;
        }
        while (left > 0 && right > left) {
            len *= Integer.parseInt(type.substring(left + 1, right));
            left = type.indexOf("[", left + 1);
            right = type.indexOf("]", right + 1);
        }
        return base * len;
    }

    /**
     * 语法及语义分析
     */
    public void solution(){
        int offset = 0;
        int nextInstr = 0;
        SymbolTable symbol_table = new SymbolTable(null);
        Stack<SymbolTable> tableStack = new Stack<>();
        tableStack.push(symbol_table);
        symbol_tables.add(symbol_table);
        // 用于语义动作
        String declarations_t = "";
        String declarations_w = "";
        Stack<Integer> offsetStack = new Stack<>();
        StringBuilder errorMessage = new StringBuilder();
        // 用于语义分析的临时变量
        String temp = "t";
        int countTemp = 1;

        
        if(In_list.size()==0){
            System.out.println("文件为空");
            return;
        }
        int ii = 0;
        state_stack.push(0);
        Token cur_token = In_list.get(ii);
        String cur = cur_token.getValue();
        String cur_type = cur_token.getTag();
        String action;
        while(!state_stack.empty()){
            boolean semanticErrorOccurred = false;

            // 用于语义动作
            if (!symbol_stack.empty()&&symbol_stack.peek().getName().equals("X")) {
                declarations_t = symbol_stack.peek().getAttribute("type");
                declarations_w = symbol_stack.peek().getAttribute("width");
            }
            if(table.analysis_table.get(state_stack.peek()).containsKey(cur)
                    ||table.analysis_table.get(state_stack.peek()).containsKey(cur_type)){
                action = table.analysis_table.get(state_stack.peek()).containsKey(cur)?
                        table.analysis_table.get(state_stack.peek()).get(cur):
                        table.analysis_table.get(state_stack.peek()).get(cur_type);
                // System.out.println(action);
                //移入
                if(action.charAt(0)=='s'){
                    in_stack.push(new Node(cur));
                    in_stack.peek().tok = cur_token;
                    in_stack.peek().line = cur_token.getLine();
                    state_stack.push(Integer.parseInt(action.substring(1)));

                    Symbol symbol = new Symbol(cur_token.getValue());
                    switch (cur_token.getTag()) {
                        case "num":
                        case "real":
                            symbol.addAttribute("value", cur_token.getValue());
                            break;
                        case "id":
                            symbol.addAttribute("lexeme", cur_token.getValue());
                            symbol.addAttribute("line", String.valueOf(cur_token.getLine()));
                            break;
                    }
                    if (!symbol_stack.empty()&&symbol_stack.peek().getName().equals("S")) {
                        Symbol S = symbol_stack.peek();
                        if (S.getAttribute("type") != null) {
                            if (S.getAttribute("type").equals("if") || S.getAttribute("type").equals("if-else") || S.getAttribute("type").equals("while")) {
                                if (S.getNextList().size() != 0) {
                                    for (int i : S.getNextList())
                                        interCodeList.get(i).backPatch(nextInstr + "");
                                }
                            }
                        }
                    }
                    symbol_stack.push(symbol);

                    ii++;
                    if(ii<In_list.size()) {
                        cur_token = In_list.get(ii);
                        cur = cur_token.getValue();
                        cur_type = cur_token.getTag();
                    }
                    else
                        cur = "$";
                }
                //规约
                else if(action.charAt(0)=='r'){
                    int num;
                    boolean flag = false;
                    //终止状态
                    if(action.charAt(action.length()-1)=='c') {
                        num = Integer.parseInt(action.split("_")[0].substring(1));
                        flag = true;
                    }
                    else
                        num = Integer.parseInt(action.substring(1));
                    Production_form pf = table.gra.grammar.get(num);
                    // System.out.println(pf.toString());
                    // 声明语句
                    // D -> T id A ;
                    if (pf.getLeft().equals("D") && pf.getRight().length == 4) {
                        symbol_stack.pop(); // pop ";"
                        symbol_stack.pop();
                        Symbol id = symbol_stack.pop();
                        Symbol T = symbol_stack.pop();
                        
                        String p = lookUpSymbolTableForCheck(id.getAttribute("lexeme"), tableStack.peek());
                        if (p == null) {
                            tableStack.peek().putSymbolItem(id.getAttribute("lexeme"), new CommonSymbol(id.getAttribute("lexeme"), T.getAttribute("type"), Integer.parseInt(id.getAttribute("line")), offset));
                            offset += Integer.parseInt(T.getAttribute("width"));
                        }else {
                            semanticErrorOccurred = true;
                            errorMessage.append("Error at line[").append(id.getAttribute("line")).append("], ").append(id.getAttribute("lexeme")).append(" is defined early.");
                        }
                        Symbol symbol = new Symbol(pf.getLeft());
                        symbol_stack.push(symbol);
                    }
                    // T -> X C
                    else if (pf.getLeft().equals("T")) {
                        Symbol C = symbol_stack.pop();
                        symbol_stack.pop();
                        Symbol T = new Symbol(pf.getLeft());
                        T.addAttribute("type", C.getAttribute("type"));
                        T.addAttribute("width", C.getAttribute("width"));
                        symbol_stack.push(T);
                    }
                    // X -> int
                    else if (pf.getLeft().equals("X") && pf.getRight().length == 1 && pf.getRight()[0].equals("int")) {
                        Symbol X = new Symbol("X");
                        symbol_stack.pop();
                        X.addAttribute("type", "int");
                        X.addAttribute("width", "4");
                        symbol_stack.push(X);
                    }
                    // X -> float
                    else if (pf.getLeft().equals("X") && pf.getRight().length == 1 && pf.getRight()[0].equals("float")) {
                        Symbol X = new Symbol("X");
                        symbol_stack.pop();
                        X.addAttribute("type", "float");
                        X.addAttribute("width", "8");
                        symbol_stack.push(X);
                    }
                    // C -> ε
                    else if (pf.getLeft().equals("C") && pf.getRight().length == 1 && pf.getRight()[0].equals("ε")) {
                        Symbol C = new Symbol("C");
                        C.addAttribute("type", declarations_t);
                        C.addAttribute("width", declarations_w);
                        symbol_stack.push(C);
                    }
                    // C -> [ num ] C
                    else if (pf.getLeft().equals("C") && pf.getRight().length == 4) {
                        Symbol C1 = symbol_stack.pop();
                        symbol_stack.pop(); // "]"
                        Symbol num1 = symbol_stack.pop();
                        symbol_stack.pop();  // "["
                        Symbol C = new Symbol("C");
                        String c1_type = C1.getAttribute("type");
                        int last = c1_type.indexOf('[');
                        if (last > 0)
                            C.addAttribute("type", c1_type.substring(0, last) + "[" + num1.getAttribute("value") + "]" + c1_type.substring(last));
                        else
                            C.addAttribute("type", c1_type + "[" + num1.getAttribute("value") + "]");
                        C.addAttribute("width", String.valueOf(Integer.parseInt(num1.getAttribute("value")) * Integer.parseInt(C1.getAttribute("width"))));
                        symbol_stack.push(C);
                    }
                    // 嵌套声明
                    // D -> proc X id DM ( M ) { P }
                    else if (pf.getLeft().equals("D") && pf.getRight().length == 10 && pf.getRight()[0].equals("proc")) {
                        int paramSize = 0;
                        for (int i = 0; i < pf.getRight().length; i++) {
                            if (i == 7) {
                                SymbolTable tempTable = tableStack.pop();
                                offset = offsetStack.pop();
                                Symbol id = symbol_stack.pop();
                                CommonSymbol item = null;
                                for (SymbolTable now = tableStack.peek(); now != null; now = now.getPrev()) {
                                    item = now.getSymbolItem(id.getAttribute("lexeme"));
                                    if (item != null)
                                        break;
                                }
                                if (item != null) {
                                    semanticErrorOccurred = true;
                                    errorMessage.append("Error at line[").append(id.getAttribute("line")).append("], proc defined again");
                                    // 替换重复定义
                                }
                                CommonSymbol symbolItem = new ProcSymbol(id.getAttribute("lexeme"), Integer.parseInt(id.getAttribute("line")), offset, tempTable, paramSize);
                                tableStack.peek().putSymbolItem(id.getAttribute("lexeme"), symbolItem);
                                
                            } else if (i == 4) {
                                Symbol M = symbol_stack.pop();
                                paramSize = Integer.parseInt(M.getAttribute("size"));
                            } else {
                                symbol_stack.pop();
                            }
                        }
                        symbol_stack.push(new Symbol("B"));
                    }
                    // M1 -> epsilon
                    else if (pf.getLeft().equals("M1") && pf.getRight().length == 1) {
                        SymbolTable newEnvTable = new SymbolTable(tableStack.peek());
                        tableStack.push(newEnvTable);
                        symbol_tables.add(newEnvTable);
                        offsetStack.push(offset);
                        offset = 0;
                        symbol_stack.push(new Symbol("M1"));
                    }
                    // PM -> PM , X id
                    else if (pf.getLeft().equals("PM") && pf.getRight().length > 2) {
                        Symbol id = symbol_stack.pop();
                        Symbol X = symbol_stack.pop();
                        symbol_stack.pop();  // ","
                        Symbol M1 = symbol_stack.pop();  // "M"

                        Symbol PM = new Symbol("PM");
                        PM.addAttribute("size", (Integer.parseInt(M1.getAttribute("size")) + 1) + "");
                        CommonSymbol item = new CommonSymbol(id.getAttribute("lexeme"), X.getAttribute("type"), Integer.parseInt(id.getAttribute("line")), offset);
                        offset += Integer.parseInt(X.getAttribute("width"));
                        tableStack.peek().putSymbolItem(id.getAttribute("lexeme"), item);
                        symbol_stack.push(PM);
                    }
                    // M -> X id
                    else if (pf.getLeft().equals("PM") && pf.getRight().length == 2) {
                        Symbol id = symbol_stack.pop();
                        Symbol X = symbol_stack.pop();
                        // 可以与上一个判断合并

                        Symbol M = new Symbol("PM");
                        M.addAttribute("size", "1");
                        CommonSymbol item = new CommonSymbol(id.getAttribute("lexeme"), X.getAttribute("type"), Integer.parseInt(id.getAttribute("line")), offset);
                        offset += Integer.parseInt(X.getAttribute("width"));
                        tableStack.peek().putSymbolItem(id.getAttribute("lexeme"), item);
                        symbol_stack.push(M);
                    }
                    // 赋值语句
                    // S -> id = E ;
                    else if (pf.getLeft().equals("S") && pf.getRight().length == 4 && pf.getRight()[0].equals("id")) {
                        symbol_stack.pop(); // ";"
                        Symbol E = symbol_stack.pop();
                        symbol_stack.pop();  // "="
                        Symbol id = symbol_stack.pop();

                        String p = lookUpSymbolTableForUse(id.getAttribute("lexeme"), tableStack.peek());
                        if (p == null) {
                            semanticErrorOccurred = true;
                            errorMessage.append("Error at line[").append(id.getAttribute("line")).append("], ").append(id.getAttribute("lexeme")).append(" not defined");
                        } else {
                            InterCode interCode = new InterCode(new String[]{p, "=", E.getAttribute("addr")});
                            interCodeList.add(interCode);
                            nextInstr++;
                        }
                        symbol_stack.push(new Symbol("S"));
                    }
                    // E -> E + G
                    else if (pf.getLeft().equals("E") && pf.getRight().length == 3 && pf.getRight()[2].equals("G")) {
                        Symbol G = symbol_stack.pop();
                        symbol_stack.pop();  // "+"
                        Symbol E1 = symbol_stack.pop();

                        Symbol E = new Symbol("E");
                        E.addAttribute("addr", temp + countTemp);
                        countTemp++;
                        InterCode interCode = new InterCode(new String[]{E.getAttribute("addr"), "=", E1.getAttribute("addr"), "+", G.getAttribute("addr")});
                        interCodeList.add(interCode);
                        nextInstr++;
                        symbol_stack.push(E);
                    }
                    // E -> G
                    else if (pf.getLeft().equals("E") && pf.getRight().length == 1 && pf.getRight()[0].equals("G")) {
                        Symbol G = symbol_stack.pop();
                        Symbol E = new Symbol("E");
                        E.addAttribute("addr", G.getAttribute("addr"));
                        symbol_stack.push(E);
                    }
                    // G -> G * F
                    else if (pf.getLeft().equals("G") && pf.getRight().length == 3) {
                        Symbol F = symbol_stack.pop();
                        symbol_stack.pop();  // "*"
                        Symbol G1 = symbol_stack.pop();

                        Symbol G = new Symbol("G");

                        CommonSymbol item1 = lookUpSymbolTable(F.getAttribute("addr"), tableStack.peek());
                        CommonSymbol item2 = lookUpSymbolTable(G1.getAttribute("addr"), tableStack.peek());
                        if (item1 == null || item2 == null || (item1.getType().equals(item2.getIdentifier()))) {
                            G.addAttribute("addr", temp + countTemp);
                            countTemp++;
                            InterCode interCode;
                            if (G1.getAttribute("addr").equals("2")) {
                                interCode = new InterCode(new String[]{G.getAttribute("addr"), "=", F.getAttribute("addr"), "+", F.getAttribute("addr")});
                                interCodeList.add(interCode);
                            } else if (F.getAttribute("addr").equals("2")) {
                                interCode = new InterCode(new String[]{G.getAttribute("addr"), "=", G1.getAttribute("addr"), "+", G1.getAttribute("addr")});
                                interCodeList.add(interCode);
                            } else {
                                semanticErrorOccurred = true;
                                errorMessage.append("Error at line[").append(nextInstr).append("]").append("Inconsistent operation components");
                                //interCode = new InterCode(new String[]{G.getAttribute("addr"), "=", G1.getAttribute("addr"), "*", F.getAttribute("addr")});
                            }
                            nextInstr++;
                        } else {
                            if (item1.getType().equals("int") && item2.getType().equals("float")) {
                                G.addAttribute("addr", temp + countTemp);
                                countTemp++;
                                InterCode interCode = new InterCode(new String[]{G.getAttribute("addr"), "=", G1.getAttribute("addr"), "*", "(float)", F.getAttribute("addr")});
                                interCodeList.add(interCode);
                                nextInstr++;
                            } else if (item1.getType().equals("float") && item2.getType().equals("int")) {
                                G.addAttribute("addr", temp + countTemp);
                                countTemp++;
                                InterCode interCode = new InterCode(new String[]{G.getAttribute("addr"), "=", "(float)", G1.getAttribute("addr"), "*", F.getAttribute("addr")});
                                interCodeList.add(interCode);
                                nextInstr++;
                            } else {
                                semanticErrorOccurred = true;
                                errorMessage.append("Error at line[").append(nextInstr).append("]").append(", type is not matched");
                                // TODO
                            }
                        }
                        symbol_stack.push(G);
                    }
                    // G -> F
                    else if (pf.getLeft().equals("G") && pf.getRight().length == 1) {
                        Symbol F = symbol_stack.pop();
                        Symbol G = new Symbol("G");
                        G.addAttribute("addr", F.getAttribute("addr"));
                        symbol_stack.push(G);
                    }
                    // F -> ( E )
                    else if (pf.getLeft().equals("F") && pf.getRight().length == 3) {
                        symbol_stack.pop(); // "("
                        Symbol E = symbol_stack.pop();
                        symbol_stack.pop(); // ")"
                        Symbol F = new Symbol("F");
                        F.addAttribute("addr", E.getAttribute("addr"));
                        symbol_stack.push(F);
                    }
                    // F -> id
                    else if (pf.getLeft().equals("F") && pf.getRight().length == 1 && pf.getRight()[0].equals("id")) {
                        Symbol id = symbol_stack.pop();
                        String p = lookUpSymbolTableForUse(id.getAttribute("lexeme"), tableStack.peek());
                        Symbol F = new Symbol("F");
                        F.addAttribute("addr", p);
                        if (p == null) {
                            semanticErrorOccurred = true;
                            errorMessage.append("Error at line[").append(id.getAttribute("line")).append("], ").append(id.getAttribute("lexeme")).append(" not defined");
                        }
                        symbol_stack.push(F);
                    }
                    // F -> num
                    else if (pf.getLeft().equals("F") && pf.getRight().length == 1 && (pf.getRight()[0].equals("num") || pf.getRight()[0].equals("real"))) {
                        Symbol number = symbol_stack.pop();
                        Symbol F = new Symbol("F");
                        F.addAttribute("addr", number.getAttribute("value"));
                        symbol_stack.push(F);
                    }
                    // S -> L = E ;
                    else if (pf.getLeft().equals("S") && pf.getRight().length == 4 && pf.getRight()[0].equals("L")) {
                        symbol_stack.pop();  // ";"
                        Symbol E = symbol_stack.pop();
                        symbol_stack.pop();  //"="
                        Symbol L = symbol_stack.pop();
                        if (L.getAttribute("array") != null) {
                            // TODO L.array即标识符名字 如 list, addr为不带方括号位置 如t1
                            InterCode interCode = new InterCode(new String[]{L.getAttribute("array"), "[", L.getAttribute("addr"), "]", "=", E.getAttribute("addr")});
                            interCodeList.add(interCode);
                            nextInstr++;
                        }
                        symbol_stack.push(new Symbol("S"));
                    }
                    // F -> L
                    else if (pf.getLeft().equals("F") && pf.getRight().length == 1 && pf.getRight()[0].equals("L")) {
                        Symbol L = symbol_stack.pop();
                        Symbol F = new Symbol("F");
                        F.addAttribute("addr", L.getAttribute("array") + "[" + L.getAttribute("addr") + "]");
                        symbol_stack.push(F);
                    }
                    // L -> L [ E ]
                    else if (pf.getLeft().equals("L") && pf.getRight().length == 4 && pf.getRight()[0].equals("L")) {
                        symbol_stack.pop();  // "]"
                        Symbol E = symbol_stack.pop();
                        symbol_stack.pop();  // "["
                        Symbol L1 = symbol_stack.pop();
                        

                        Symbol L = new Symbol("L");
                        if (L1.getAttribute("type") != null && L1.getAttribute("type").contains("[")) {
                            L.addAttribute("array", L1.getAttribute("array"));
                            L.addAttribute("type", getArrayElemType(L1.getAttribute("type")));
                            L.addAttribute("width", getArrayTypeWidth(L.getAttribute("type")) + "");
                            String t = temp + countTemp;
                            countTemp++;
                            L.addAttribute("addr", temp + countTemp);
                            countTemp++;
                            InterCode interCode1 = new InterCode(new String[]{t, "=", E.getAttribute("addr"), "*", L.getAttribute("width")});
                            InterCode interCode2 = new InterCode(new String[]{L.getAttribute("addr"), "=", L1.getAttribute("addr"), "+", t});
                            interCodeList.add(interCode1);
                            interCodeList.add(interCode2);
                            nextInstr = nextInstr + 2;
                        }
                        symbol_stack.push(L);
                    }
                    // L -> id [ E ]
                    else if (pf.getLeft().equals("L") && pf.getRight().length == 4 && pf.getRight()[0].equals("id")) {
                        symbol_stack.pop();  // "]"
                        Symbol E = symbol_stack.pop();
                        symbol_stack.pop();  // "["
                        Symbol id = symbol_stack.pop();
                        

                        Symbol L = new Symbol("L");
                        CommonSymbol lItem = null;
                        for (SymbolTable now = tableStack.peek(); now != null; now = now.getPrev()) {
                            lItem = now.getSymbolItem(id.getAttribute("lexeme"));
                            if (lItem != null)
                                break;
                        }
                        if (lItem == null) {
                            semanticErrorOccurred = true;
                            errorMessage.append("Error at line[").append(id.getAttribute("line")).append("]").append(", ").append(id.getAttribute("lexeme")).append(" not defined");
                        } else if (!lItem.getType().contains("[")) {
                            semanticErrorOccurred = true;
                            errorMessage.append("Error at line[").append(id.getAttribute("line")).append("]").append(", ").append(id.getAttribute("lexeme")).append(" type error");
                        } else {
                            L.addAttribute("array", lItem.getIdentifier());
                            L.addAttribute("type", getArrayElemType(lItem.getType()));
                            L.addAttribute("addr", temp + countTemp);
                            countTemp++;
                            InterCode interCode = new InterCode(new String[]{L.getAttribute("addr"), "=", E.getAttribute("addr"), "*", getArrayTypeWidth(L.getAttribute("type")) + ""});
                            interCodeList.add(interCode);
                            nextInstr++;
                        }
                        symbol_stack.push(L);
                    }
                    // 流程控制
                    // I -> false
                    else if (pf.getLeft().equals("I") && pf.getRight().length == 1 && pf.getRight()[0].equals("false")) {
                        symbol_stack.pop();  // pop false
                        Symbol I = new Symbol("I");
//                    I.addAttribute("falseList", nextInstr + "");
                        I.makeList(nextInstr, 0);
                        InterCode interCode = new InterCode(new String[]{"goto"});
                        interCodeList.add(interCode);
                        nextInstr++;
                        symbol_stack.push(I);
                    }
                    // I -> true
                    else if (pf.getLeft().equals("I") && pf.getRight().length == 1 && pf.getRight()[0].equals("true")) {
                        symbol_stack.pop(); // pop true
                        Symbol I = new Symbol("I");
//                    I.addAttribute("trueList", nextInstr + "");
                        I.makeList(nextInstr, 1);
                        InterCode interCode = new InterCode(new String[]{"goto"});
                        interCodeList.add(interCode);
                        nextInstr++;
                        symbol_stack.push(I);
                    }
                    // I -> E Relop E
                    else if (pf.getLeft().equals("I") && pf.getRight().length == 3 && pf.getRight()[0].equals("E")) {
                        Symbol E1 = symbol_stack.pop();
                        Symbol Relop = symbol_stack.pop();
                        Symbol E2 = symbol_stack.pop();

                        Symbol I = new Symbol("I");
                        I.makeList(nextInstr, 1);
                        I.makeList(nextInstr + 1, 0);
                        InterCode interCode1 = new InterCode(new String[]{"if", E2.getAttribute("addr"), Relop.getAttribute("op"), E1.getAttribute("addr"), "goto"});
                        InterCode interCode2 = new InterCode(new String[]{"goto"});
                        interCodeList.add(interCode1);
                        interCodeList.add(interCode2);
                        nextInstr = nextInstr + 2;
                        symbol_stack.push(I);
                    }
                    // I -> ( B )
                    else if (pf.getLeft().equals("I") && pf.getRight().length == 3 && pf.getRight()[0].equals("(")) {
                        symbol_stack.pop();  // "("
                        Symbol B = symbol_stack.pop();
                        symbol_stack.pop();  // ")"
                        

                        Symbol I = new Symbol("I");
                        I.addList(B.getTrueList(), 1);
                        I.addList(B.getFalseList(), 0);
                        symbol_stack.push(I);
                    }
                    // I -> ! I
                    else if (pf.getLeft().equals("I") && pf.getRight().length == 2) {
                        Symbol I1 = symbol_stack.pop();
                        symbol_stack.pop();  // "!"

                        Symbol I = new Symbol("I");
                        I.addList(I1.getFalseList(), 1);
                        I.addList(I1.getTrueList(), 0);
                        symbol_stack.push(I);
                    }
                    // Relop -> < | <= | > | >= | == | !=
                    else if (pf.getLeft().equals("Relop") && pf.getRight().length == 1) {
                        Symbol op = symbol_stack.pop();

                        Symbol Relop = new Symbol("Relop");
                        Relop.addAttribute("op", op.getName());
                        symbol_stack.push(Relop);
                    }
                    // M2 -> epsilon
                    else if (pf.getLeft().equals("M2")) {
                        Symbol M = new Symbol("M2");
                        M.addAttribute("instr", nextInstr + "");
                        symbol_stack.push(M);
                    }
                    // H -> H && BM I
                    else if (pf.getLeft().equals("H") && pf.getRight().length > 1) {
                        Symbol I = symbol_stack.pop();
                        Symbol BM = symbol_stack.pop();
                        symbol_stack.pop(); // &&
                        Symbol H1 = symbol_stack.pop();

                        // backpatch
                        for (int i : H1.getTrueList())
                            interCodeList.get(i).backPatch(BM.getAttribute("instr"));

                        Symbol H = new Symbol("H");
                        H.addList(I.getTrueList(), 1);
                        H.merge(H1.getFalseList(), I.getFalseList(), 0);
                        symbol_stack.push(H);
                    }
                    // H -> I
                    else if (pf.getLeft().equals("H") && pf.getRight().length == 1) {
                        Symbol I = symbol_stack.pop();
                        Symbol H = new Symbol("H");
                        H.addList(I.getTrueList(), 1);
                        H.addList(I.getFalseList(), 0);
                        symbol_stack.push(H);
                    }
                    // B -> B || BM H
                    else if (pf.getLeft().equals("B") && pf.getRight().length > 1) {
                        Symbol H = symbol_stack.pop();
                        Symbol BM = symbol_stack.pop();
                        symbol_stack.pop();  // pop "||"
                        Symbol B1 = symbol_stack.pop();
                        Symbol B = new Symbol("B");
                        for (int i : B1.getFalseList())
                            interCodeList.get(i).backPatch(BM.getAttribute("instr"));
                        B.merge(B1.getTrueList(), H.getTrueList(), 1);
                        B.addList(H.getFalseList(), 0);
                        symbol_stack.push(B);
                    }
                    // B -> H
                    else if (pf.getLeft().equals("B") && pf.getRight().length == 1) {
                        Symbol H = symbol_stack.pop();
                        Symbol B = new Symbol("B");
                        B.addList(H.getFalseList(), 0);
                        B.addList(H.getTrueList(), 1);
                        symbol_stack.push(B);
                    }
                    // S -> if ( B ) BM S N else BM S
                    else if (pf.getLeft().equals("S") && pf.getRight().length == 10) {
                        Symbol S2 = symbol_stack.pop();
                        Symbol BM2 = symbol_stack.pop();
                        symbol_stack.pop(); // pop "else"
                        Symbol N = symbol_stack.pop();
                        Symbol S1 = symbol_stack.pop();
                        Symbol BM1 = symbol_stack.pop();
                        symbol_stack.pop();  // pop ")"
                        Symbol B = symbol_stack.pop();
                        symbol_stack.pop();  // pop "("
                        symbol_stack.pop();  // pop "if"

                        Symbol S = new Symbol("S");
                        for (int i : B.getTrueList())
                            interCodeList.get(i).backPatch(BM1.getAttribute("instr"));
                        for (int i : B.getFalseList())
                            interCodeList.get(i).backPatch(BM2.getAttribute("instr"));
                        List<Integer> tempIntegerList = new ArrayList<>();
                        tempIntegerList.addAll(new HashSet<>(S1.getNextList()));
                        tempIntegerList.addAll(new HashSet<>(N.getNextList()));
                        S.merge(tempIntegerList, S2.getNextList(), -1);
                        S.addAttribute("type", "if-else");
                        symbol_stack.push(S);
                    }
                    // S -> if ( B ) BM S
                    else if (pf.getLeft().equals("S") && pf.getRight().length == 6 && pf.getRight()[0].equals("if")) {
                        Symbol S1 = symbol_stack.pop();
                        Symbol BM = symbol_stack.pop();
                        symbol_stack.pop();  // ")"
                        Symbol B = symbol_stack.pop();
                        symbol_stack.pop();  // "("
                        symbol_stack.pop();  // "if"

                        for (int i : B.getTrueList())
                            interCodeList.get(i).backPatch(BM.getAttribute("instr"));
                        Symbol S = new Symbol("S");
                        S.merge(B.getFalseList(), S1.getNextList(), -1);
                        S.addAttribute("type", "if");
                        symbol_stack.push(S);
                    }
                    // S -> while BM ( B ) BM S
                    else if (pf.getLeft().equals("S") && pf.getRight().length == 7 && pf.getRight()[0].equals("while")) {
                        Symbol S1 = symbol_stack.pop();
                        Symbol BM2 = symbol_stack.pop();
                        symbol_stack.pop(); // pop ")"
                        Symbol B = symbol_stack.pop();
                        symbol_stack.pop();  // pop "("
                        Symbol BM1 = symbol_stack.pop();
                        symbol_stack.pop();  // pop "while"

                        Symbol S = new Symbol("S");
                        for (int i : S1.getNextList())
                            interCodeList.get(i).backPatch(BM1.getAttribute("instr"));
                        for (int i : B.getTrueList())
                            interCodeList.get(i).backPatch(BM2.getAttribute("instr"));
                        S.addList(B.getFalseList(), -1);
                        InterCode interCode = new InterCode(new String[]{"goto", BM1.getAttribute("instr")});
                        interCodeList.add(interCode);
                        nextInstr++;
                        S.addAttribute("type", "while");
                        symbol_stack.push(S);
                    }
                    // N -> epsilon
                    else if (pf.getLeft().equals("N") && pf.getRight().length == 1) {
                        Symbol N = new Symbol("N");
                        N.makeList(nextInstr, -1);
                        InterCode interCode = new InterCode(new String[]{"goto"});
                        interCodeList.add(interCode);
                        nextInstr++;
                        symbol_stack.push(N);
                    }
                    // Elist -> Elist , E
                    else if (pf.getLeft().equals("Elist") && pf.getRight().length == 3) {
                        symbol_stack.pop();  //  "E"
                        symbol_stack.pop();  //  ","
                        Symbol Elist1 = symbol_stack.pop();

                        Symbol Elist = new Symbol("Elist");
                        Elist.addAttribute("size", (Integer.parseInt(Elist1.getAttribute("size") + 1) + ""));
                        symbol_stack.push(Elist);
                    }
                    // Elist -> E
                    else if (pf.getLeft().equals("Elist") && pf.getRight().length == 1) {
                        symbol_stack.pop();

                        Symbol Elist = new Symbol("Elist");
                        Elist.addAttribute("size", "1");
                        symbol_stack.push(Elist);
                    }
                    // S -> call id ( Elist ) ;
                    else if (pf.getLeft().equals("S") && pf.getRight()[0].equals("call")){
                        symbol_stack.pop();  // ';'
                        symbol_stack.pop();  // ')'
                        Symbol Elist = symbol_stack.pop();
                        symbol_stack.pop();  // "("
                        Symbol id = symbol_stack.pop();
                        symbol_stack.pop();  // "call"

                        ProcSymbol item = null;
                        for(SymbolTable now = tableStack.peek(); now != null; now = now.getPrev()){
                            if(now.getSymbolItem(id.getAttribute("lexeme")) instanceof ProcSymbol) {
                                item = (ProcSymbol) now.getSymbolItem(id.getAttribute("lexeme"));
                                break;
                            }
                        }
                        if(item != null){
                            if(item.getParamsSize()!= Integer.parseInt(Elist.getAttribute("size"))){
                                semanticErrorOccurred = true;
                                errorMessage.append("Error at line[").append(id.getAttribute("line")).append("]").append(", param size is different");
                            } else{
                            InterCode interCode = new InterCode(new String[]{"call", id.getAttribute("lexeme")});
                            interCodeList.add(interCode);
                            }
                        }
                        else {
                            semanticErrorOccurred = true;
                            errorMessage.append("Error at line[").append(id.getAttribute("line")).append("]").append(", ").append(id.getAttribute("lexeme")).append("func not defined");
                        }
                        symbol_stack.push(new Symbol("S"));
                    }
                    // without semantic action
                    else {
                        int rightSize = pf.getRight().length == 1 && pf.getRight()[0].equals("ε") ? 0 : pf.getRight().length;
                        for (int j = 0; j < rightSize; j++) {
                            symbol_stack.pop();
                        }
                        Symbol symbol = new Symbol(pf.getLeft());
                        symbol_stack.push(symbol);
                    }

                    int right_size = pf.getRight().length;
                    Node parent = new Node(pf.getLeft());
                    if(!pf.getRight()[0].equals("ε")) {
                        parent.line = in_stack.get(in_stack.size()-right_size).line;
                        for (int j = 0; j < pf.getRight().length; j++) {
                            state_stack.pop();
                            parent.children.add(in_stack.get(in_stack.size()-right_size));
                            right_size--;
                        }
                        for (int j = 0; j < pf.getRight().length; j++)
                            in_stack.pop();
                    }
                    else {
                        parent.line = in_stack.peek().line;
                        parent.children.add(new Node("ε", in_stack.peek().line));
                    }
                    if(flag){
                        root = parent;
                        return;
                    }
                    in_stack.push(parent);
                    state_stack.push(Integer.parseInt(table.analysis_table.get(state_stack.peek()).get(pf.getLeft())));
                    //System.out.println(state_stack.peek());
                    if (semanticErrorOccurred) {
                        System.err.println(errorMessage.toString());
                        errors.append(errorMessage.toString()).append("\n");
                        errorMessage = new StringBuilder();
                    }
                }
            }
            // 恐慌模式处理错误输入
            else {
                errors.append("Syntax error at Line[").append(in_stack.peek().line).append("]:[没有遇到期望出现的符号]\n");
                if(cur.equals("$")){
                    return;
                }
                int num = 0;
                // 从栈顶开始查找状态，若当前状态GOTO表非空，则选择一个使其GOTO的输入压入in_stack，然后继续执行程序，
                // 否则将当前状态出栈，且in_stack也要弹出栈顶的一个节点
                here:
                while (true){
                    for (String s : table.analysis_table.get(state_stack.peek()).keySet()) {
                        if (table.gra.head.contains(s)) {
                            Node parent = new Node(s);
                            int old_num = num;
                            if(num != 0)
                                parent.line = in_stack.get(in_stack.size()-num).line;
                            else if(!in_stack.empty())
                                parent.line = in_stack.peek().line;
                            for (int j = 0; j < old_num; j++) {
                                parent.children.add(in_stack.get(in_stack.size()-num));
                                num--;
                            }
                            for (int j = 0; j < old_num; j++) {
                                in_stack.pop();
                                symbol_stack.pop();
                            }
                            in_stack.push(parent);
                            symbol_stack.push(new Symbol(s));
                            state_stack.push(Integer.parseInt(table.analysis_table.get(state_stack.peek()).get(s)));
                            while (!table.analysis_table.get(state_stack.peek()).containsKey(cur)
                                    &&!table.analysis_table.get(state_stack.peek()).containsKey(cur_type)) {
                                ii++;
                                if (ii < In_list.size()) {
                                    cur_token = In_list.get(ii);
                                    cur = cur_token.getValue();
                                    cur_type = cur_token.getTag();
                                } else
                                    cur = "$";
                            }
                            break here;
                        }
                    }
                    state_stack.pop();
                    num++;
                }
            }
        }
    }

    public StringBuilder getOut() {
        return out;
    }

    private void output(Node root, String space){
        if(root==null)
            return;
        out.append(space).append(root.value).append(" (").append(root.line).append(")\n");
        //System.out.println(space+root.value+"("+root.line+")");
        for(Node node:root.children){
            output(node,"  "+space);
        }
    }

    public void output(){
        output(root,"  ");
    }

    public static void main(String[] args) throws IOException {
        parser p = new parser("D:\\ctf\\IdeaProjects\\POC_lab\\src\\test.c");
        p.solution();
        p.output();
        List<InterCode> interCodeList = p.getInterCodeList();
        for (int i = 0; i < interCodeList.size(); i++) {
            System.out.println(i + " : " + interCodeList.get(i).anotherToString() + "\t" + interCodeList.get(i));
        }
    }

}

/**
 * 语法树的节点
 */
class Node{
    String value;
    int line = -1;
    List<Node> children = new ArrayList<>();
    Token tok;

    Node(String value){
        this.value = value;
    }

    public Node(String value, int line) {
        this.value = value;
        this.line = line;
    }
}