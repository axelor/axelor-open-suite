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
package com.axelor.apps.quality.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.quality.db.ControlType;
import com.axelor.apps.quality.db.ControlTypeField;
import com.axelor.apps.quality.db.repo.ControlTypeFieldLineRepository;
import com.axelor.apps.quality.exception.QualityExceptionMessage;
import com.axelor.i18n.I18n;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.collections.CollectionUtils;

public class ControlTypeFieldServiceImpl implements ControlTypeFieldService {

  protected ControlTypeFieldLineRepository controlTypeFieldLineRepository;

  @Inject
  public ControlTypeFieldServiceImpl(
      ControlTypeFieldLineRepository controlTypeFieldLineRepository) {
    this.controlTypeFieldLineRepository = controlTypeFieldLineRepository;
  }

  @Override
  public void checkControlTypeUsage(ControlTypeField controlTypeField) throws AxelorException {
    List<ControlType> controlTypeList =
        controlTypeFieldLineRepository
            .all()
            .filter("self.controlTypeField = :controlTypeField")
            .bind("controlTypeField", controlTypeField)
            .fetch()
            .stream()
            .flatMap(line -> Stream.of(line.getPlanControlType(), line.getEntryControlType()))
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());

    if (!CollectionUtils.isEmpty(controlTypeList)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(QualityExceptionMessage.CONTROL_TYPE_FIELD_USED_BY_CONTROL_TYPE),
          controlTypeField.getName(),
          controlTypeList.stream()
              .map(ControlType::getName)
              .filter(Objects::nonNull)
              .collect(Collectors.joining(", ")));
    }
  }
}
