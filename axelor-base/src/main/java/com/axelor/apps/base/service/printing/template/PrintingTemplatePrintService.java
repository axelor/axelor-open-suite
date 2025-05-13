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
package com.axelor.apps.base.service.printing.template;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.PrintingTemplate;
import com.axelor.apps.base.service.printing.template.model.PrintingGenFactoryContext;
import com.axelor.db.Model;
import java.io.File;
import java.io.IOException;
import java.util.List;

public interface PrintingTemplatePrintService {

  String getPrintLink(PrintingTemplate template, PrintingGenFactoryContext context)
      throws AxelorException;

  String getPrintLink(
      PrintingTemplate template, PrintingGenFactoryContext context, String outputFileName)
      throws AxelorException;

  String getPrintLink(
      PrintingTemplate template, PrintingGenFactoryContext context, boolean toAttach)
      throws AxelorException;

  String getPrintLink(
      PrintingTemplate template,
      PrintingGenFactoryContext context,
      String outputFileName,
      boolean toAttach)
      throws AxelorException;

  File getPrintFile(PrintingTemplate template, PrintingGenFactoryContext context)
      throws AxelorException;

  File getPrintFile(
      PrintingTemplate template, PrintingGenFactoryContext context, String outputFileName)
      throws AxelorException;

  File getPrintFile(PrintingTemplate template, PrintingGenFactoryContext context, Boolean toAttach)
      throws AxelorException;

  File getPrintFile(
      PrintingTemplate template,
      PrintingGenFactoryContext context,
      String outputFileName,
      Boolean toAttach)
      throws AxelorException;

  <T extends Model> String getPrintLinkForList(
      List<Integer> idList, Class<T> contextClass, PrintingTemplate template)
      throws IOException, AxelorException;

  String getPrintFileName(PrintingTemplate template, PrintingGenFactoryContext context)
      throws AxelorException;
}
