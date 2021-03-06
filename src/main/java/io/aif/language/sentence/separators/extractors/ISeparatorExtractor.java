package io.aif.language.sentence.separators.extractors;

import java.util.List;

import io.aif.language.common.IExtractor;
import io.aif.language.common.settings.ISettings;

public interface ISeparatorExtractor extends IExtractor<List<String>, List<Character>> {

  public static enum Type {
    PREDEFINED(new PredefinedSeparatorExtractor()),
    PROBABILITY(new StatSeparatorExtractor()),
    NON_ALPHABETIC_CHARACTERS_EXTRACTOR(new NonAlphabeticCharactersExtractor());

    private final ISeparatorExtractor instance;

    private Type(final ISeparatorExtractor instance) {
      this.instance = instance;
    }

    public static ISeparatorExtractor getDefault() {
      return ISettings.SETTINGS.useIsAlphabeticMethod() ?
          Type.NON_ALPHABETIC_CHARACTERS_EXTRACTOR.getInstance() :
          Type.PROBABILITY.getInstance();
    }

    public ISeparatorExtractor getInstance() {
      return instance;
    }

  }


}
