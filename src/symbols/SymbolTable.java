package symbols;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private final SymbolTable prev;
    private final Map<String, CommonSymbol> symbolTable;

    public SymbolTable(SymbolTable prev) {
        this.prev = prev;
        symbolTable = new HashMap<>();
    }

    public Map<String, CommonSymbol> getSymbolTable() {
        return symbolTable;
    }

    public void putSymbolItem(String id, CommonSymbol item) {
        symbolTable.put(id, item);
    }

    public CommonSymbol getSymbolItem(String id) {
        for (SymbolTable symbolBoard = this; symbolBoard != null; symbolBoard = symbolBoard.prev) {
            if (symbolBoard.symbolTable.containsKey(id))
                return symbolTable.get(id);
        }
        return null;
    }

    public SymbolTable getPrev() {
        return prev;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (CommonSymbol item : symbolTable.values()) {
            stringBuilder.append(item).append("\n");
            if (item instanceof ProcSymbol)
                stringBuilder.append(item.getIdentifier()).append(" Table:\n").append("{\n").append(((ProcSymbol) item).getProcSymbolTable()).append("}\n");
        }
        return stringBuilder.toString();
    }
}
