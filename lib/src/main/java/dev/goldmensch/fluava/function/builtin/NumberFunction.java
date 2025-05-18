package dev.goldmensch.fluava.function.builtin;

import dev.goldmensch.fluava.function.Context;
import dev.goldmensch.fluava.function.Function;
import dev.goldmensch.fluava.function.Value;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.*;

public class NumberFunction implements Function.Implicit<Value.Number, Double> {

    @Override
    public Value.Number apply(Context context, Double value, Map<String, Object> named) {
        Locale locale = context.locale();
        NumberFormat format = switch ((String) named.getOrDefault("style", "decimal")) {
            case "decimal" -> NumberFormat.getNumberInstance(locale);
            case "currency" -> {
                NumberFormat currencyInstance = DecimalFormat.getCurrencyInstance(locale);
                Currency currency = Currency.getInstance((String) named.get("currency"));
                currencyInstance.setCurrency(currency);

                DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(locale);
                String currencyDisplay = switch ((String) named.getOrDefault("currencyDisplay", "symbol")) {
                    case "code" -> currency.getCurrencyCode();
                    case "symbol" -> currency.getSymbol();
                    case "name" -> currency.getDisplayName();
                    default -> throw new IllegalArgumentException();
                };
                decimalFormatSymbols.setCurrencySymbol(currencyDisplay);

                ((DecimalFormat) currencyInstance).setDecimalFormatSymbols(decimalFormatSymbols);
                yield currencyInstance;
            }
            case "percent" -> NumberFormat.getPercentInstance(locale);
            case "unit" -> throw new UnsupportedOperationException();
            default -> throw new IllegalArgumentException();
        };

        if (named.containsKey("useGrouping")) format.setGroupingUsed((boolean) named.get("useGrouping"));
        if (named.containsKey("minimumIntegerDigits"))
            format.setMinimumFractionDigits((int) named.get("minimumIntegerDigits"));

        if (named.containsKey("minimumFractionDigits"))
            format.setMinimumFractionDigits((int) named.get("minimumFractionDigits"));
        if (named.containsKey("maximumFractionDigits"))
            format.setMaximumFractionDigits((int) named.get("maximumFractionDigits"));

        return new Value.Number(format.format(value), value);
    }

    @Override
    public Collection<Class<? extends Double>> acceptableTypes() {
        return List.of(Double.class);
    }
}
