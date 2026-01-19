package dev.goldmensch.fluava.ast;

class Latin {
    static boolean upperAlpha(char c) {
        return range(c, 'A', 'Z');
    }

    static boolean lowerAlpha(char c) {
        return range(c, 'a', 'z');
    }

    static boolean number(char ch) {
        return range(ch, '0', '9');
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
