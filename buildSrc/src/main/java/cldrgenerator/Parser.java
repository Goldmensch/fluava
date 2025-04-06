package cldrgenerator;

import cldrgenerator.ast.*;
import io.github.parseworks.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static io.github.parseworks.Combinators.*;

public class Parser {
    private static final io.github.parseworks.Parser<Character, Value> value = chr(c -> 48 <= c && c <= 57) // 0-9
            .many()
            .map(characters -> Long.parseLong(Utils.listToString(characters)))
            .map(Value::new);

    private static final io.github.parseworks.Parser<Character, Operand> operand = oneOf(List.of(
            chr('n').as(Operand.ABSOLUTE_VALUE),
            chr('i').as(Operand.INTEGER_DIGITS),
            chr('v').as(Operand.NUM_FRACTIONS),
            chr('w').as(Operand.NUM_FRACTIONS_WOZ),
            chr('f').as(Operand.NUM_FRACTIONS_AS_INT),
            chr('t').as(Operand.NUM_FRACTIONS_WOZ_AS_INT),
            chr('c').or(chr('e')).as(Operand.COMPACT_DEC_EXPO)
    ));

    private static final io.github.parseworks.Parser<Character, Expr> expr = operand
            .then(string("mod").or(string("%")).skipThen(value).optional())
            .map(op -> mod -> new Expr(op, mod.orElse(null)));

    private static final io.github.parseworks.Parser<Character, Optional<String>> opt_not = string("not").optional();

    private static final io.github.parseworks.Parser<Character, Range> range = value
            .thenSkip(string(".."))
            .then(value)
            .map(from -> to -> new Range(from, to));

    private static final Ref<Character, RangeList> range_list = io.github.parseworks.Parser.ref();

    private static final io.github.parseworks.Parser<Character, Relation> is_relation = expr
            .thenSkip(string("is"))
            .then(string("not").optional())
            .then(value)
            .map(expr -> not -> value -> {
                var relation = new Relation.In(expr, new RangeList(FList.of(value)));
                return not.<Relation>map(_ -> new Relation.Not(relation)).orElse(relation);
            });

    private static final io.github.parseworks.Parser<Character, Relation> in_relation = expr.then(
            oneOf(
                    opt_not.thenSkip(string("in")).map(s -> s.map(_ -> "!=").orElse("=")),
                    string("="),
                    string("!=")
            ))
            .then(range_list)
            .map(expr -> cmd -> range_list -> {
                Relation.In in = new Relation.In(expr, range_list);
                return cmd.equals("!=")
                        ? new Relation.Not(in)
                        : in;
            });

    private static final io.github.parseworks.Parser<Character, Relation> within_relation = expr
            .then(opt_not)
            .thenSkip(string("within"))
            .then(range_list)
            .map(expr -> not -> range_list -> {
                Relation.Within within = new Relation.Within(expr, range_list);
                return not.<Relation>map(_ -> new Relation.Not(within)).orElse(within);
            });


    private static final io.github.parseworks.Parser<Character, Relation> relation = oneOf(
            is_relation,
            in_relation,
            within_relation
    );

    private static final io.github.parseworks.Parser<Character, AndCondition> and_condition = relation
            .then(string("and").skipThen(relation).zeroOrMany())
            .map(first -> others -> {
                others.addFirst(first);
                return new AndCondition(others);
            });

    private static final Ref<Character, Condition> condition = io.github.parseworks.Parser.ref();

    public static final io.github.parseworks.Parser<Character, Condition> rule = condition.optional()
            .thenSkip(oneOf(string("@integer"), string("@decimal")).thenSkip(any(Character.class)).optional())
            .map(cond -> cond.orElse(new Condition(new FList<>())));

    static {
        range_list.set(oneOf(
                range.map(RangeList.Element.class::cast),
                value.map(RangeList.Element.class::cast))
                .then(chr(',').skipThen(range_list).zeroOrMany().map(rangeLists -> rangeLists.stream()
                        .map(RangeList::elements)
                        .flatMap(Collection::stream)
                        .toList()))
                .map(element -> range -> {
                    ArrayList<RangeList.Element> elements = new ArrayList<>(range);
                    elements.addFirst(element);
                    return new RangeList(elements);
                })
        );

        condition.set(and_condition
                .then(string("or").skipThen(and_condition).zeroOrMany())
                .map(first -> others -> {
                    others.addFirst(first);
                    return new Condition(others);
                })
        );
    }

}
