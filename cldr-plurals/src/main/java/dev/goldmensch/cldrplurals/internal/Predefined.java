package dev.goldmensch.cldrplurals.internal;

@SuppressWarnings("unused")
public class Predefined {

    // treats 1000.0/1000.0000/23.0000 etc. as 1000 or 23
    private String[] parts(String value) {
        String normValue = norm(value);
        var parts = normValue.split("[.]");
        if (parts.length == 1 || parts[1].equals("0".repeat(parts[1].length()))) return new String[] { parts[0], "" };
        return parts;
    }

    private String norm(String value) {
        String[] parts = value.split("c");
        if (parts.length == 1) return value;
        int exponent = Integer.parseInt(parts[1]);

        if (exponent < 0) {
            throw new RuntimeException("Negative exponent is unsupported, value: %s".formatted(value));
        }


        StringBuilder builder = new StringBuilder(parts[0]);
        int dotIndex = builder.indexOf(".");
        if (dotIndex != -1) {
            builder.deleteCharAt(dotIndex);
        }

        int newDotIndex = dotIndex + exponent;

        if (dotIndex == -1) {
            builder.append("0".repeat(exponent));
        } else if (newDotIndex < builder.length()) {
            builder.insert(newDotIndex, '.');
        } else {
            builder.append("0".repeat(newDotIndex - builder.length()));
        }
        return builder.toString();
    }

    protected boolean isInt(Object value) {
        return parts(value.toString())[1].isEmpty();
    }

    //  	the absolute value of N.*
    protected double n(String value) {
        return Double.parseDouble(norm(value));
    }

    //  	the integer digits of N.*
    protected int i(String value) {
        return Integer.parseInt(parts(value)[0]);
    }

    //  	the number of visible fraction digits in N, with trailing zeros.*
    protected int v(String value) {
        return parts(value)[1].length();
    }

    //  	the number of visible fraction digits in N, without trailing zeros.*
    protected int w(String value) {

        String[] parts = parts(value);

        // count trailing zeros (0)
        int c = 0;
        for (int i = parts[1].length() - 1; i >= 0; i--) {
            if (parts[1].charAt(i) != '0') break;
            c++;
        }

        return v(value) - c;
    }

    // the visible fraction digits in N, with trailing zeros, expressed as an integer.*
    protected int f(String value) {
        String[] parts = parts(value);
        if (parts[1].isEmpty()) return 0;
        return Integer.parseInt(parts[1]);
    }

    // the visible fraction digits in N, without trailing zeros, expressed as an integer.*
    protected int t(String value) {
        String[] parts = parts(value);
        if (parts[1].isEmpty()) return 0;
        String newFraction = parts[1].substring(0, w(value));
        return f(parts[0] + "." + newFraction);
    }

    protected int c(String value) {
        String[] parts = value.split("c");
        if (parts.length == 1) return 0;
        return Integer.parseInt(parts[1]);
    }

    protected int e(String value) {
        return c(value);
    }



}
