/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.tool.service;

import java.util.Collection;
import java.util.HashSet;

import com.axelor.common.StringUtils;
import com.axelor.meta.db.MetaTranslation;
import com.axelor.meta.db.repo.MetaTranslationRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class TranslationServiceImpl implements TranslationService {
    protected MetaTranslationRepository metaTranslationRepo;

    protected static final String VALUE_KEY_PREFIX = "value:";

    @Inject
    TranslationServiceImpl(MetaTranslationRepository metaTranslationRepo) {
        this.metaTranslationRepo = metaTranslationRepo;
    }

    @Override
    @Transactional
    public void updateFormatedValueTranslations(String oldKey, String format, Object... args) {
        if (!StringUtils.isBlank(oldKey)) {
            removeValueTranslations(oldKey);
        }

        createFormatedValueTranslations(format, args);
    }

    @Override
    @Transactional
    public void createFormatedValueTranslations(String format, Object... args) {
        String key = VALUE_KEY_PREFIX + String.format(format, args);

        for (String language : getLanguages(args)) {
            MetaTranslation metaTranslation = metaTranslationRepo.findByKey(key, language);

            if (metaTranslation == null) {
                metaTranslation = new MetaTranslation();
                metaTranslation.setKey(key);
                metaTranslation.setLanguage(language);
            }

            String message = String.format(format, getTranslatedArgs(args, language));
            metaTranslation.setMessage(message);

            metaTranslationRepo.save(metaTranslation);
        }
    }

    @Override
    @Transactional
    public void removeValueTranslations(String key) {
        for (MetaTranslation metaTranslation : metaTranslationRepo.all().filter("self.key = :key")
                .bind("key", VALUE_KEY_PREFIX + key).fetch()) {
            metaTranslationRepo.remove(metaTranslation);
        }
    }

    private Object[] getTranslatedArgs(Object[] args, String language) {
        Object[] translatedArgs = new String[args.length];

        for (int i = 0; i < args.length; ++i) {
            String key = String.valueOf(args[i]);
            translatedArgs[i] = getValueMessage(key, language);
        }

        return translatedArgs;
    }

    private String getValueMessage(String key, String language) {
        MetaTranslation metaTranslation = metaTranslationRepo.findByKey(VALUE_KEY_PREFIX + key, language);
        return metaTranslation != null && !StringUtils.isBlank(metaTranslation.getMessage())
                ? metaTranslation.getMessage()
                : key;
    }

    private Collection<String> getLanguages(Object... args) {
        Collection<String> languages = new HashSet<>();

        for (Object arg : args) {
            for (MetaTranslation metaTranslation : metaTranslationRepo.all().filter("self.key = :key")
                    .bind("key", VALUE_KEY_PREFIX + arg).fetch()) {
                languages.add(metaTranslation.getLanguage());
            }
        }

        return languages;
    }

}
