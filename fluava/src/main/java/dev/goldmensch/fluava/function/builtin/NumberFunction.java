package dev.goldmensch.fluava.function.builtin;

import dev.goldmensch.cldrplurals.Type;
import dev.goldmensch.fluava.Result;
import dev.goldmensch.fluava.function.Context;
import dev.goldmensch.fluava.function.Function;
import dev.goldmensch.fluava.function.Options;
import dev.goldmensch.fluava.function.Value;
import io.github.kaktushose.proteus.Proteus;
import io.github.kaktushose.proteus.ProteusBuilder;
import io.github.kaktushose.proteus.conversion.ConversionResult;
import io.github.kaktushose.proteus.mapping.Mapper;
import io.github.kaktushose.proteus.mapping.MappingResult;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.*;

public class NumberFunction implements Function.Implicit<Value.Number, Double> {

    public NumberFunction() {
        Proteus.global().register(
                io.github.kaktushose.proteus.type.Type.of(String.class),
                io.github.kaktushose.proteus.type.Type.of(Type.class),
                Mapper.uni((label, _) -> MappingResult.lossless(Type.valueOf(label.toUpperCase()))),
                ProteusBuilder.ConflictStrategy.IGNORE
        );
    }

    @Override
    public Result<Value.Number> apply(Context context, Double value, Options options) {
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
        Type type = options.get("type", Type.class, Type.CARDINAL);

        return new Result.Success<>(new Value.Number(format.format(value), value, type));
    }

    @Override
    public Collection<Class<? extends Double>> acceptableTypes() {
        return List.of(Double.class);
    }
}
