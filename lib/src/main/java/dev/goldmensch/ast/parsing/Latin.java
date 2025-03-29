package dev.goldmensch.ast.parsing;

class Latin {
    static boolean upperAlpha(char c) {
        return range(c, 0x41, 0x5A);
    }

    static boolean lowerAlpha(char c) {
        return range(c, 0x61, 0x7A);
    }

    static boolean number(char ch) {
        return range(ch, 0x30, 0x39);
    }

    static boolean hexChar(char ch) {
        return number(ch)
                || range(ch, 'a', 'f')
                || range(ch, 'A', 'F');
    }

    private static boolean range(char ch, int from, int to) {
        return from <= ch && ch <= to;
    }
}
