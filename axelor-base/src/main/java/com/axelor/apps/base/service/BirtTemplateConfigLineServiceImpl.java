/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BirtTemplate;
import com.axelor.apps.base.db.BirtTemplateConfigLine;
import com.axelor.apps.base.db.repo.BirtTemplateConfigLineRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class BirtTemplateConfigLineServiceImpl implements BirtTemplateConfigLineService {

  protected BirtTemplateConfigLineRepository configLineRepo;

  @Inject
  public BirtTemplateConfigLineServiceImpl(BirtTemplateConfigLineRepository configLineRepo) {
    this.configLineRepo = configLineRepo;
  }

  @Override
  public Set<BirtTemplate> getBirtTemplates(String modelName) throws AxelorException {
    Set<BirtTemplate> birtTemplatSet =
        configLineRepo.all().filter("self.metaModel.fullName = :metaModel")
            .bind("metaModel", modelName).fetch().stream()
            .map(BirtTemplateConfigLine::getBirtTemplate)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

    if (CollectionUtils.isEmpty(birtTemplatSet)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.BIRT_TEMPLATE_CONFIG_NOT_FOUND));
    }

    return birtTemplatSet;
  }
}
