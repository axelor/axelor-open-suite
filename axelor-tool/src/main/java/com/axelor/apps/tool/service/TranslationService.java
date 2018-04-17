package com.axelor.apps.tool.service;

public interface TranslationService {
    /**
     * Create formated value translations
     * 
     * @param format
     * @param args
     */
    void createFormatedValueTranslations(String format, Object... args);

    /**
     * Remove value translations.
     * 
     * @param key
     */
    void removeValueTranslations(String key);
}
