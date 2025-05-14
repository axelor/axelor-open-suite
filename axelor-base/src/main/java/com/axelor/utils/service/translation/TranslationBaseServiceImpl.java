/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.utils.service.translation;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Language;
import com.axelor.apps.base.db.Localization;
import com.axelor.apps.base.db.repo.LanguageRepository;
import com.axelor.apps.base.service.language.LanguageCheckerService;
import com.axelor.apps.base.service.localization.LocaleService;
import com.axelor.apps.base.service.localization.LocalizationService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.axelor.i18n.I18nBundle;
import com.axelor.meta.db.MetaTranslation;
import com.axelor.meta.db.repo.MetaTranslationRepository;
import com.axelor.utils.service.TranslationService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.NotFoundException;

public class TranslationBaseServiceImpl implements TranslationBaseService {

  protected static final String VALUE_KEY_PREFIX = "value:";

  protected UserService userService;
  protected TranslationService translationService;
  protected MetaTranslationRepository metaTranslationRepository;
  protected LocalizationService localizationService;
  protected LanguageCheckerService languageCheckerService;
  protected LanguageRepository languageRepository;

  @Inject
  public TranslationBaseServiceImpl(
      UserService userService,
      TranslationService translationService,
      MetaTranslationRepository metaTranslationRepository,
      LocalizationService localizationService,
      LanguageCheckerService languageCheckerService,
      LanguageRepository languageRepository) {
    this.userService = userService;
    this.translationService = translationService;
    this.metaTranslationRepository = metaTranslationRepository;
    this.localizationService = localizationService;
    this.languageCheckerService = languageCheckerService;
    this.languageRepository = languageRepository;
  }

  @Override
  public String getValueTranslation(String key) {
    String language = LocaleService.getLanguageFromLocaleCode(userService.getLocalizationCode());
    String valueTranslation = translationService.getValueTranslation(key, language);
    return key.equals(valueTranslation) ? I18n.get(key) : valueTranslation;
  }

  protected List<MetaTranslation> getTranslations(String language, String key) {
    Query<MetaTranslation> query =
        metaTranslationRepository
            .all()
            .filter("self.language = :language " + " AND self.key LIKE :key");
    query.bind("language", language);
    query.bind("key", key);

    return query.fetch();
  }

  @Override
  public List<MetaTranslation> getLocalizationTranslations(String requestLanguage, String key)
      throws AxelorException {
    List<MetaTranslation> metaTranslationList = new ArrayList<>();
    List<MetaTranslation> localizationTranslation = new ArrayList<>();
    List<MetaTranslation> countryTranslation = new ArrayList<>();
    Language language = null;

    try {
      requestLanguage = requestLanguage.replace("-", "_");
      Localization localization = localizationService.getLocalization(requestLanguage);
      languageCheckerService.checkLanguage(localization, requestLanguage);
      language = localization.getLanguage();
      localizationTranslation = this.getTranslations(localization.getCode().replace("_", "-"), key);
    } catch (NotFoundException | AxelorException e) {
      Language lang = languageRepository.findByCode(requestLanguage);
      if (lang == null) {
        throw e;
      }
      language = lang;
    } finally {
      if (language != null) {
        countryTranslation = this.getTranslations(language.getCode(), key);
      }
    }

    metaTranslationList.addAll(localizationTranslation);

    for (MetaTranslation metaTranslation : countryTranslation) {
      List<String> keyList =
          metaTranslationList.stream().map(MetaTranslation::getKey).collect(Collectors.toList());
      if (!keyList.contains(metaTranslation.getKey())) {
        metaTranslationList.add(metaTranslation);
      }
    }

    return metaTranslationList;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void createValueTranslation(String language, String key, String message) {
    MetaTranslation metaTranslation = new MetaTranslation();
    metaTranslation.setLanguage(language);
    metaTranslation.setKey(VALUE_KEY_PREFIX + key);
    metaTranslation.setMessage(message);
    metaTranslationRepository.save(metaTranslation);
    I18nBundle.invalidate();
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void updateValueTranslation(
      String language, String oldKey, String newKey, String message) {
    MetaTranslation metaTranslation =
        metaTranslationRepository.findByKey(VALUE_KEY_PREFIX + oldKey, language);
    if (metaTranslation != null) {
      metaTranslation.setKey(VALUE_KEY_PREFIX + newKey);
      metaTranslation.setMessage(message);
      metaTranslationRepository.save(metaTranslation);
      I18nBundle.invalidate();
    } else {
      createValueTranslation(language, newKey, message);
    }
  }
}
