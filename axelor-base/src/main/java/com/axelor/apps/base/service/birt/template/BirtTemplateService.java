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
package com.axelor.apps.base.service.birt.template;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BirtTemplate;
import com.axelor.db.Model;
import java.io.File;
import java.util.Map;

public interface BirtTemplateService {

  String generateBirtTemplateLink(BirtTemplate template, Model model, String outputName)
      throws AxelorException;

  String generateBirtTemplateLink(
      BirtTemplate template, Model model, String outputName, Boolean toAttach, String format)
      throws AxelorException;

  File generateBirtTemplateFile(BirtTemplate template, Model model, String outputName)
      throws AxelorException;

  File generateBirtTemplateFile(
      BirtTemplate template, Model model, String outputName, Boolean toAttach, String format)
      throws AxelorException;

  File generateBirtTemplateFile(
      BirtTemplate template,
      Map<String, Object> context,
      String outputName,
      Boolean toAttach,
      String format)
      throws AxelorException;
}
