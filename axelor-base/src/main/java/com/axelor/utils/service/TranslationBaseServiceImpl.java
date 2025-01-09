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
package com.axelor.utils.service;

import com.axelor.apps.base.service.LocaleService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;

public class TranslationBaseServiceImpl implements TranslationBaseService {

  protected UserService userService;
  protected TranslationService translationService;

  @Inject
  public TranslationBaseServiceImpl(
      UserService userService, TranslationService translationService) {
    this.userService = userService;
    this.translationService = translationService;
  }

  @Override
  public String getValueTranslation(String key) {
    String language = LocaleService.getLanguageFromLocaleCode(userService.getLocalizationCode());
    String valueTranslation = translationService.getValueTranslation(key, language);
    return key.equals(valueTranslation) ? I18n.get(key) : valueTranslation;
  }
}
