package symbols;


public class ProcSymbol extends CommonSymbol {
    private final SymbolTable procSymbolTable;
    private final int paramsSize;

    public ProcSymbol(String identifier, int position, int offset, SymbolTable table, int paramsSize) {
        super(identifier, "proc", position, offset);
        this.procSymbolTable = table;
        this.paramsSize = paramsSize;
    }

    public SymbolTable getProcSymbolTable() {
        return procSymbolTable;
    }

    public int getParamsSize() {
        return paramsSize;
    }
}
