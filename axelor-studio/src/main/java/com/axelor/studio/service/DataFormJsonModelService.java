/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.studio.service;

import com.axelor.auth.db.AuditableModel;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.AxelorException;
import com.axelor.studio.db.DataForm;
import com.axelor.studio.db.DataFormLine;
import com.axelor.studio.service.builder.HtmlFormBuilderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;

public interface DataFormJsonModelService {

  void generateHtmlFormForMetaJsonModel(
      HtmlFormBuilderService htmlFormBuilder, List<DataFormLine> dataFormLineList)
      throws ClassNotFoundException, AxelorException, JsonProcessingException;

  void generateFieldsForMetaJsonModel(DataForm dataForm);

  <T extends AuditableModel> void createRecordMetaJsonModel(
      Map<String, List<InputPart>> formDataMap, String jsonModelName, final Mapper mapper, T bean)
      throws ClassNotFoundException, IOException, AxelorException, NumberFormatException,
          ServletException;
}
