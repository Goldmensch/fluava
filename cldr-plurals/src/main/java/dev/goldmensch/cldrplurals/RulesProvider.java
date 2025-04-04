package dev.goldmensch.cldrplurals;

import java.util.Collection;

public interface RulesProvider {
    Collection<PluralRule> rules();
}
