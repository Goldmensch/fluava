package cldrgenerator;

import cldrgenerator.ast.Condition;
import com.palantir.javapoet.*;

import javax.lang.model.element.Modifier;
import java.util.*;
import java.util.function.Predicate;

public class Generator {
    public static final String CLDR_PACKAGE = "dev.goldmensch.cldrplurals";
    public static final String INTERNAL_PACKAGE = CLDR_PACKAGE + ".internal";
    public static final String CLASS_NAME = "GeneratedCldrRules";

    public static String VALUE = "value";

    // Generates an implementation of dev.goldmensch.cldrplurals.RulesProvider
    public JavaFile generate(Map<Locale, Map<String, Condition>> cardinals, Map<Locale, Map<String, Condition>> ordinals) {

        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(CLASS_NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .superclass(ClassName.get(INTERNAL_PACKAGE, "Predefined"))
                .addSuperinterface(ClassName.get(CLDR_PACKAGE, "RulesProvider"));
        addGeneratedConditionMethods("cardinal", cardinals, classBuilder);
        addGeneratedConditionMethods("ordinal", ordinals, classBuilder);
        generateRules(cardinals, ordinals, classBuilder);

        return JavaFile.builder(INTERNAL_PACKAGE, classBuilder.build()).build();
    }

    private void addGeneratedConditionMethods(String type, Map<Locale, Map<String, Condition>> values, TypeSpec.Builder classBuilder) {
        values.forEach((locale, cases) -> cases.forEach((category, condition) -> {
            if (category.equals("OTHER")) return;

            MethodSpec method = generateCondition(locale, type, category, condition);
            classBuilder.addMethod(method);
        }));
    }

    private void generateRules(Map<Locale, Map<String, Condition>> cardinals, Map<Locale, Map<String, Condition>> ordinals, TypeSpec.Builder builder) {
        ClassName pluralRule = ClassName.get(CLDR_PACKAGE, "PluralRule");
        ClassName pluralCategory = ClassName.get(CLDR_PACKAGE, "PluralCategory");
        ParameterizedTypeName predicate = ParameterizedTypeName.get(Predicate.class, String.class);
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("rules")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(ParameterizedTypeName.get(ClassName.get(Collection.class), pluralRule));

        HashSet<Locale> locals = new HashSet<>(cardinals.keySet());
        locals.addAll(ordinals.keySet());

        methodBuilder.addStatement("var list = new $T<$T>()", ArrayList.class, pluralRule);
        methodBuilder.addStatement("var cMap = new $T<$T, $T>($T.class)", EnumMap.class, pluralCategory, predicate, pluralCategory);
        methodBuilder.addStatement("var oMap = new $T<$T, $T>($T.class)", EnumMap.class, pluralCategory, predicate, pluralCategory);

        for (Locale locale : locals) {
            methodBuilder.addStatement("oMap.clear()");
            methodBuilder.addStatement("cMap.clear()");

            // add cardinals
            for (String category : cardinals.getOrDefault(locale, Map.of()).keySet()) {
                if (category.equals("OTHER")) continue;
                methodBuilder.addStatement("cMap.put($T.$L, this::$L)", pluralCategory, category, methodName(locale, "cardinal", category));
            }

            // add ordinals
            for (String category : ordinals.getOrDefault(locale, Map.of()).keySet()) {
                if (category.equals("OTHER")) continue;
                methodBuilder.addStatement("oMap.put($T.$L, this::$L)", pluralCategory, category, methodName(locale, "ordinal", category));
            }

            methodBuilder.addStatement("list.add(new $T($T.of($S), $T.copyOf(cMap), $T.copyOf(oMap)))", pluralRule, Locale.class, locale.getLanguage(), Map.class, Map.class);
        }

        methodBuilder.addStatement("return list");

        builder.addMethod(methodBuilder.build());
    }

    private String methodName(Locale locale, String type, String category) {
        return "%s_%s_%s".formatted(locale.getLanguage().replace('-', '_'), type, category);
    }

    private MethodSpec generateCondition(Locale locale, String type, String category, Condition condition) {
        return MethodSpec.methodBuilder(methodName(locale, type, category))
                .addModifiers(Modifier.PRIVATE)
                .returns(boolean.class)
                .addParameter(String.class, VALUE)
                .addCode("return ")
                .addStatement(condition.get())
                .build();
    }
}
