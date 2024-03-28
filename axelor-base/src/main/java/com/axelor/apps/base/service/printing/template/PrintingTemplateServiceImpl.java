/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.printing.template;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.PrintingTemplate;
import com.axelor.apps.base.db.repo.PrintingTemplateRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class PrintingTemplateServiceImpl implements PrintingTemplateService {

  protected PrintingTemplateRepository printingTemplateRepository;

  @Inject
  public PrintingTemplateServiceImpl(PrintingTemplateRepository printingTemplateRepository) {
    this.printingTemplateRepository = printingTemplateRepository;
  }

  @Override
  public List<PrintingTemplate> getActivePrintingTemplates(String modelName)
      throws AxelorException {

    List<PrintingTemplate> templates =
        getPrintTemplates(
            modelName, List.of(PrintingTemplateRepository.PRINTING_TEMPLATE_STATUS_SELECT_ACTIVE));

    if (CollectionUtils.isEmpty(templates)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.TEMPLATE_CONFIG_NOT_FOUND));
    }

    return templates;
  }

  @Override
  public boolean hasActivePrintingTemplates(String modelName) {
    List<PrintingTemplate> templates =
        getPrintTemplates(
            modelName, List.of(PrintingTemplateRepository.PRINTING_TEMPLATE_STATUS_SELECT_ACTIVE));
    return ObjectUtils.notEmpty(templates);
  }

  protected List<PrintingTemplate> getPrintTemplates(
      String modelName, List<Integer> statusSelects) {
    return printingTemplateRepository
        .all()
        .filter(
            "self.metaModel.fullName = :modelName AND (:statusSelects IS NULL OR self.statusSelect IN :statusSelects)")
        .bind("modelName", modelName)
        .bind("statusSelects", statusSelects)
        .cacheable()
        .fetch();
  }
}
