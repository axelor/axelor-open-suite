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
package com.axelor.apps.account.service.move.template;

import com.axelor.apps.account.db.MoveTemplate;
import com.axelor.apps.account.db.MoveTemplateLine;
import com.axelor.apps.base.AxelorException;
import java.util.Map;

public interface MoveTemplateLineAnalyticService {

  Map<String, Object> getAnalyticDistributionTemplateOnChangeValuesMap(
      MoveTemplateLine moveTemplateLine, MoveTemplate moveTemplate) throws AxelorException;

  Map<String, Map<String, Object>> getAnalyticDistributionTemplateOnChangeAttrsMap(
      MoveTemplateLine moveTemplateLine, MoveTemplate moveTemplate) throws AxelorException;

  Map<String, Map<String, Object>> getAnalyticDistributionTemplateOnSelectAttrsMap(
      MoveTemplateLine moveTemplateLine, MoveTemplate moveTemplate) throws AxelorException;

  Map<String, Object> getAnalyticAxisOnChangeValuesMap(
      MoveTemplateLine moveTemplateLine, MoveTemplate moveTemplate) throws AxelorException;

  Map<String, Map<String, Object>> getAnalyticAxisOnChangeAttrsMap(
      MoveTemplateLine moveTemplateLine, MoveTemplate moveTemplate) throws AxelorException;

  Map<String, Object> getAnalyticMoveLineOnChangeValuesMap(
      MoveTemplateLine moveTemplateLine, MoveTemplate moveTemplate) throws AxelorException;

  Map<String, Map<String, Object>> getAnalyticMoveLineOnChangeAttrsMap(
      MoveTemplateLine moveTemplateLine, MoveTemplate moveTemplate) throws AxelorException;

  Map<String, Object> getAccountOnChangeValuesMap(
      MoveTemplateLine moveTemplateLine, MoveTemplate moveTemplate) throws AxelorException;

  Map<String, Map<String, Object>> getAccountOnChangeAttrsMap(
      MoveTemplateLine moveTemplateLine, MoveTemplate moveTemplate) throws AxelorException;

  Map<String, Object> getOnLoadAnalyticDistributionValuesMap(
      MoveTemplateLine moveTemplateLine, MoveTemplate moveTemplate) throws AxelorException;

  Map<String, Map<String, Object>> getOnLoadAnalyticDistributionAttrsMap(
      MoveTemplateLine moveTemplateLine, MoveTemplate moveTemplate) throws AxelorException;
}
