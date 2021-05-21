package parser;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

public class Production_form{
    private final String left; //产生式左部
    private final String[] right; //产生式右部
    private int index = 0; //处于等待产生式右部第index个单元的状态，初始为0
    private final Set<String> next; //展望符

    public String getLeft() {
        return left;
    }

    public String[] getRight() {
        return right;
    }

    public int getIndex() {
        return index;
    }

    public Set<String> getNext() {
        return next;
    }

    @Override
    public String toString() {
        return "Production_form{" +
                "left='" + left + '\'' +
                ", right=" + Arrays.toString(right) +
                ", index=" + index +
                ", next=" + next +
                '}';
    }

    public Production_form(String left, String[] right, Set<String> next) {
        this.left = left;
        this.right = right;
        this.next = next;
    }

    public Production_form(String left, String[] right, int index, Set<String> next) {
        this.left = left;
        this.right = right;
        this.index = index;
        this.next = next;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Production_form)) return false;
        Production_form that = (Production_form) o;
        return index == that.index && Objects.equals(left, that.left) && Arrays.equals(right, that.right) && Objects.equals(next, that.next);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(left, index, next);
        result = 31 * result + Arrays.hashCode(right);
        return result;
    }
}