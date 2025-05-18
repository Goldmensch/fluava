package dev.goldmensch.fluava.function.builtin;

import dev.goldmensch.fluava.function.Context;
import dev.goldmensch.fluava.function.Function;
import dev.goldmensch.fluava.function.Options;
import dev.goldmensch.fluava.function.Value;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.*;

public class NumberFunction implements Function.Implicit<Value.Number, Double> {

    @Override
    public Value.Number apply(Context context, Double value, Options options) {
        Locale locale = context.locale();
        NumberFormat format = switch (options.get("style", String.class, "decimal")) {
            case "decimal" -> NumberFormat.getNumberInstance(locale);
            case "currency" -> {
                NumberFormat currencyInstance = DecimalFormat.getCurrencyInstance(locale);
                Currency currency = Currency.getInstance(options.get("currency", String.class));
                currencyInstance.setCurrency(currency);

                DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(locale);
                String currencyDisplay = switch (options.get("currencyDisplay", String.class, "symbol")) {
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

        options.ifHasDo("useGrouping", boolean.class, format::setGroupingUsed);
        options.ifHasDo("minimumIntegerDigits", int.class, format::setMinimumIntegerDigits);
        options.ifHasDo("minimumFractionDigits", int.class, format::setMinimumFractionDigits);
        options.ifHasDo("maximumFractionDigits", int.class, format::setMaximumFractionDigits);

        return new Value.Number(format.format(value), value);
    }

    @Override
    public Collection<Class<? extends Double>> acceptableTypes() {
        return List.of(Double.class);
    }
}
