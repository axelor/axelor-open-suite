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
package com.axelor.apps.quality.service;

import com.axelor.apps.base.db.Language;
import com.axelor.apps.base.db.repo.LanguageRepository;
import com.axelor.apps.quality.db.ControlPlanFrequency;
import com.axelor.apps.quality.db.repo.ControlPlanFrequencyRepository;
import com.axelor.utils.service.TranslationService;
import com.axelor.utils.service.translation.TranslationBaseService;
import com.google.inject.Inject;
import java.util.List;

public class ControlPlanFrequencyServiceImpl implements ControlPlanFrequencyService {

  protected LanguageRepository languageRepository;
  protected ControlPlanFrequencyRepository controlPlanFrequencyRepository;
  protected TranslationBaseService translationBaseService;
  protected TranslationService translationService;
  protected ControlPlanFrequencyComputeNameService controlPlanFrequencyComputeNameService;

  @Inject
  public ControlPlanFrequencyServiceImpl(
      LanguageRepository languageRepository,
      ControlPlanFrequencyRepository controlPlanFrequencyRepository,
      TranslationBaseService translationBaseService,
      TranslationService translationService,
      ControlPlanFrequencyComputeNameService controlPlanFrequencyComputeNameService) {
    this.languageRepository = languageRepository;
    this.controlPlanFrequencyRepository = controlPlanFrequencyRepository;
    this.translationBaseService = translationBaseService;
    this.translationService = translationService;
    this.controlPlanFrequencyComputeNameService = controlPlanFrequencyComputeNameService;
  }

  @Override
  public void createOrUpdateValueTranslations(ControlPlanFrequency controlPlanFrequency) {
    if (controlPlanFrequency.getSampleQty() == null
        || controlPlanFrequency.getSampleFrequency() == null
        || controlPlanFrequency.getSampleQtyUnit() == null
        || controlPlanFrequency.getSampleFrequencyUnit() == null) {
      return;
    }
    String key = controlPlanFrequencyComputeNameService.computeName(controlPlanFrequency);
    List<Language> languageList = languageRepository.all().fetch();

    for (Language language : languageList) {
      String languageCode = language.getCode();
      String message = getMessage(controlPlanFrequency, languageCode);

      if (controlPlanFrequency.getId() == null) {
        translationBaseService.createValueTranslation(languageCode, key, message);
      } else {
        translationBaseService.updateValueTranslation(
            languageCode,
            controlPlanFrequencyRepository.find(controlPlanFrequency.getId()).getName(),
            key,
            message);
      }
    }
  }

  protected String getMessage(ControlPlanFrequency controlPlanFrequency, String language) {
    return String.format(
        "%s (%s) %s %s (%s)",
        controlPlanFrequency.getSampleQty().stripTrailingZeros().toPlainString(),
        translationService.getValueTranslation(
            controlPlanFrequency.getSampleQtyUnit().getName(), language),
        translationService.getTranslation("each", language),
        controlPlanFrequency.getSampleFrequency().stripTrailingZeros().toPlainString(),
        translationService.getValueTranslation(
            controlPlanFrequency.getSampleFrequencyUnit().getName(), language));
  }
}
