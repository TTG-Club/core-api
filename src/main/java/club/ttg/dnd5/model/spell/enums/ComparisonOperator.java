package club.ttg.dnd5.model.spell.enums;

public enum ComparisonOperator {
    LESS("<"),
    GREATER(">"),
    EQUAL("=");

    private final String symbol;

    ComparisonOperator(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public static ComparisonOperator fromSymbol(String symbol) {
        for (ComparisonOperator op : values()) {
            if (op.symbol.equals(symbol)) {
                return op;
            }
        }
        throw new IllegalArgumentException("Invalid comparison operator: " + symbol);
    }
}

