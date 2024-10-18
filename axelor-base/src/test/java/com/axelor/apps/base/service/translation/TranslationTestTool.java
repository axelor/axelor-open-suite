package com.axelor.apps.base.service.translation;

import com.axelor.meta.db.MetaTranslation;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Assertions;

public class TranslationTestTool {

  static void createMetaTranslations(String key, List<MetaTranslation> metaTranslations) {
    // FR translations
    metaTranslations.add(createMetaTranslation(key + "fr_1", "fr", key + "fr_1"));
    metaTranslations.add(createMetaTranslation(key + "fr_2", "fr", key + "fr_2"));
    metaTranslations.add(createMetaTranslation(key + "fr_3", "fr", key + "fr_3"));
    metaTranslations.add(createMetaTranslation(key + "fr_2", "fr-CA", key + "fr_CA_2"));
    metaTranslations.add(createMetaTranslation(key + "fr_3", "fr-BE", key + "fr_BE_3"));
    // EN translation
    metaTranslations.add(createMetaTranslation(key + "en_1", "en", key + "en_1"));
    metaTranslations.add(createMetaTranslation(key + "en_2", "en", key + "en_2"));
    metaTranslations.add(createMetaTranslation(key + "en_3", "en", key + "en_3"));
    metaTranslations.add(createMetaTranslation(key + "en_2", "en-US", key + "en_US_2"));
    metaTranslations.add(createMetaTranslation(key + "en_3", "en-GB", key + "en_GB_3"));
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
    Assertions.assertEquals(expectedList.size(), actualList.size());
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
