package com.axelor.apps.base.service.translation;

import com.axelor.meta.db.MetaTranslation;

import java.util.ArrayList;
import java.util.List;

public class TranslationTestTool {

    static List<MetaTranslation> generateMockTranslations() {
        List<MetaTranslation> metaTranslations = new ArrayList<>();
        // FR translations
        metaTranslations.add(createMetaTranslation("fr_1", "fr", "fr_1"));
        metaTranslations.add(createMetaTranslation("fr_2", "fr", "fr_2"));
        metaTranslations.add(createMetaTranslation("fr_CA_1", "fr-CA", "fr_CA_1"));
        metaTranslations.add(createMetaTranslation("fr_CA_2", "fr-CA", "fr_CA_2"));
        metaTranslations.add(createMetaTranslation("fr_BE_1", "fr-BE", "fr_BE_1"));
        metaTranslations.add(createMetaTranslation("fr_BE_2", "fr-BE", "fr_BE_2"));
        // EN translation
        metaTranslations.add(createMetaTranslation("en_1", "en", "en_1"));
        metaTranslations.add(createMetaTranslation("en_2", "en", "en_2"));
        metaTranslations.add(createMetaTranslation("en_US_1", "en-US", "en_US_1"));
        metaTranslations.add(createMetaTranslation("en_US_2", "en-US", "en_US_2"));
        metaTranslations.add(createMetaTranslation("en_GB_1", "en-GB", "en_GB_1"));
        metaTranslations.add(createMetaTranslation("en_GB_2", "en-GB", "en_GB_2"));

        return metaTranslations;
    }

    static MetaTranslation createMetaTranslation(String key, String language, String message) {
        MetaTranslation metaTranslation = new MetaTranslation();
        metaTranslation.setKey(key);
        metaTranslation.setLanguage(language);
        metaTranslation.setMessage(message);

        return metaTranslation;
    }
}
