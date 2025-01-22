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
package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Language;
import com.axelor.apps.base.db.Localization;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.utils.service.TranslationService;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InternationalServiceImpl implements InternationalService {

  protected TranslationService translationService;

  @Inject
  public InternationalServiceImpl(TranslationService translationService) {
    this.translationService = translationService;
  }

  @Override
  public String translate(String source, String sourceLanguage, String targetLanguage) {
    String translationKey = translationService.getTranslationKey(source, sourceLanguage);
    return translationService.getValueTranslation(translationKey, targetLanguage);
  }

  @Override
  public Map<String, String> getProductDescriptionAndNameTranslation(
      Product product, Partner partner) {
    Map<String, String> translation = new HashMap<>();
    String userLanguage =
        Optional.of(AuthUtils.getUser())
            .map(User::getLocalization)
            .map(Localization::getLanguage)
            .map(Language::getCode)
            .orElse(null);
    String partnerLanguage =
        Optional.ofNullable(partner)
            .map(Partner::getLocalization)
            .map(Localization::getLanguage)
            .map(Language::getCode)
            .orElse(null);

    if (userLanguage == null || partnerLanguage == null) {
      return translation;
    }
    translation.put(
        "description", translate(product.getDescription(), userLanguage, partnerLanguage));
    translation.put("productName", translate(product.getName(), userLanguage, partnerLanguage));

    return translation;
  }
}
