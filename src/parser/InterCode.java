package parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InterCode {
    private final List<String> interCode = new ArrayList<>();

    public InterCode(String[] interCode) {
        Collections.addAll(this.interCode, interCode);
    }

    public void backPatch(String back) {
        interCode.add(back);
    }

    public List<String> getInterCode() {
        return new ArrayList<>(interCode);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < interCode.size(); i++) {
            if (i != 0)
                sb.append(" ");
            sb.append(interCode.get(i));
        }
        return sb.toString();
    }

    public String anotherToString(){
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        switch (interCode.size()){
            case 2:
                sb.append("j, -, -, ");
                sb.append(interCode.get(1));
                break;
            case 3:
                sb.append("=, ");
                sb.append(interCode.get(0));
                sb.append(", -, ");
                sb.append(interCode.get(2));
                break;
            case 5:
                sb.append(interCode.get(3)).append(", ");
                sb.append(interCode.get(2)).append(", ");
                sb.append(interCode.get(4)).append(", ");
                sb.append(interCode.get(1));
                break;
            case 6:
                sb.append("j").append(interCode.get(2)).append(", ");
                sb.append(interCode.get(1)).append(", ");
                sb.append(interCode.get(3)).append(", ");
                sb.append(interCode.get(5));
                break;
        }
        sb.append(")");
        return sb.toString();
    }
}
