/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.service.config;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.PrintingTemplate;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.quality.db.QualityConfig;
import com.axelor.apps.quality.db.repo.QualityConfigRepository;
import com.axelor.apps.quality.service.config.QualityConfigService;
import com.axelor.i18n.I18n;
import jakarta.inject.Inject;

public class QualityConfigProductionService {

  protected final QualityConfigService qualityConfigService;

  @Inject
  public QualityConfigProductionService(QualityConfigService qualityConfigService) {
    this.qualityConfigService = qualityConfigService;
  }

  public int getManufOrderFinalControlCheckSelect(Company company) {
    QualityConfig qualityConfig = company == null ? null : company.getQualityConfig();
    if (qualityConfig == null
        || qualityConfig.getManufOrderFinalControlCheckSelect()
            < QualityConfigRepository.MANUF_ORDER_FINAL_CONTROL_CHECK_NONE) {
      return QualityConfigRepository.MANUF_ORDER_FINAL_CONTROL_CHECK_NONE;
    }
    return qualityConfig.getManufOrderFinalControlCheckSelect();
  }

  public PrintingTemplate getManufOrderConformityDeclarationPrintTemplate(Company company)
      throws AxelorException {
    PrintingTemplate printTemplate =
        qualityConfigService
            .getQualityConfig(company)
            .getManufOrderConformityDeclarationPrintTemplate();

    if (printTemplate == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.TEMPLATE_CONFIG_NOT_FOUND));
    }
    return printTemplate;
  }
}
