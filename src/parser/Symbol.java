package parser;

import java.util.*;

public class Symbol {
    private final String name;
    private final Map<String, String> attribute = new HashMap<>();
    private final List<Integer> falseList = new ArrayList<>();
    private final List<Integer> trueList = new ArrayList<>();
    private final List<Integer> nextList = new ArrayList<>();

    public Symbol(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addAttribute(String key, String value) {
        attribute.put(key, value);
    }

    public String getAttribute(String key) {
        return attribute.get(key);
    }

    public void makeList(int nextInstr, int i) {
        if (i == 1)   // true List
        {
            trueList.add(nextInstr);
        } else if (i == 0) {
            // false list
            falseList.add(nextInstr);
        } else {
            // nextList
            nextList.add(nextInstr);
        }
    }

    public List<Integer> getFalseList() {
        return new ArrayList<>(falseList);
    }

    public List<Integer> getTrueList() {
        return new ArrayList<>(trueList);
    }

    public List<Integer> getNextList() {
        return new ArrayList<>(nextList);
    }

    public void merge(List<Integer> list1, List<Integer> list2, int i) {
        if (i == 1) {
            // true list
            trueList.addAll(new HashSet<>(list1));
            trueList.addAll(new HashSet<>(list2));
        } else if (i == 0) {
            falseList.addAll(new HashSet<>(list1));
            falseList.addAll(new HashSet<>(list2));
        } else {
            nextList.addAll(new HashSet<>(list1));
            nextList.addAll(new HashSet<>(list2));
        }
    }

    public void addList(List<Integer> list, int i) {
        if (i == 1) {
            // true List
            trueList.addAll(new HashSet<>(list));
        } else if (i == 0) {
            // false list
            falseList.addAll(new HashSet<>(list));
        } else {
            nextList.addAll(new HashSet<>(list));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Symbol that = (Symbol) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
