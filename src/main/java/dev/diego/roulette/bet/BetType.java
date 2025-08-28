package dev.diego.roulette.bet;

public enum BetType {
    STRAIGHT(35),
    SPLIT(17),
    STREET(11),
    CORNER(8),
    LINE(5),
    DOZEN(2),
    COLUMN(2),
    COLOR(1),
    EVEN_ODD(1),
    LOW_HIGH(1);

    private final int multiplier;
    BetType(int m) { this.multiplier = m; }
    public int multiplier() { return multiplier; }
}
