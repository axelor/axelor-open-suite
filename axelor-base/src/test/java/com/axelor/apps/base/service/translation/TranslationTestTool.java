package com.axelor.apps.base.service.translation;

import com.axelor.meta.db.MetaTranslation;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Assertions;

public class TranslationTestTool {

  static void createMetaTranslations(String key, List<MetaTranslation> metaTranslations) {
    // FR translations
    metaTranslations.add(createMetaTranslation(key + "fr_1", "fr", "fr_1"));
    metaTranslations.add(createMetaTranslation(key + "fr_2", "fr", "fr_2"));
    metaTranslations.add(createMetaTranslation(key + "fr_CA_1", "fr-CA", "fr_CA_1"));
    metaTranslations.add(createMetaTranslation(key + "fr_CA_2", "fr-CA", "fr_CA_2"));
    metaTranslations.add(createMetaTranslation(key + "fr_BE_1", "fr-BE", "fr_BE_1"));
    metaTranslations.add(createMetaTranslation(key + "fr_BE_2", "fr-BE", "fr_BE_2"));
    // EN translation
    metaTranslations.add(createMetaTranslation(key + "en_1", "en", "en_1"));
    metaTranslations.add(createMetaTranslation(key + "en_2", "en", "en_2"));
    metaTranslations.add(createMetaTranslation(key + "en_US_1", "en-US", "en_US_1"));
    metaTranslations.add(createMetaTranslation(key + "en_US_2", "en-US", "en_US_2"));
    metaTranslations.add(createMetaTranslation(key + "en_GB_1", "en-GB", "en_GB_1"));
    metaTranslations.add(createMetaTranslation(key + "en_GB_2", "en-GB", "en_GB_2"));
  }

  static MetaTranslation createMetaTranslation(String key, String language, String message) {
    MetaTranslation metaTranslation = new MetaTranslation();
    metaTranslation.setKey(key);
    metaTranslation.setLanguage(language);
    metaTranslation.setMessage(message);

    return metaTranslation;
  }

  static void assertTranslationsEquals(
      List<MetaTranslation> expectedList, List<MetaTranslation> actualList) {
    for (MetaTranslation metaTranslation : expectedList) {
      Assertions.assertTrue(
          actualList.stream()
              .anyMatch(
                  mt ->
                      Objects.equals(mt.getKey(), metaTranslation.getKey())
                          && Objects.equals(mt.getMessage(), metaTranslation.getMessage())));
    }
  }
}
