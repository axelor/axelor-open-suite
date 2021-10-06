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
import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaFile;
import com.axelor.studio.db.DataForm;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.xml.bind.JAXBException;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;

public interface DataFormService {
  String generateHtmlForm(DataForm dataForm)
      throws ClassNotFoundException, AxelorException, JsonProcessingException;

  void generateFields(DataForm dataForm) throws JAXBException, JsonProcessingException;

  <T extends AuditableModel> T createRecord(
      Map<String, List<InputPart>> formDataMap,
      Class<?> klass,
      Boolean custom,
      String jsonModelName)
      throws ClassNotFoundException, IOException, AxelorException, ServletException;

  DataForm getDataForm(Map<String, List<InputPart>> formDataMap)
      throws AxelorException, IOException;

  void checkRecordToCreate(Map<String, List<InputPart>> formDataMap, String dataFormModel)
      throws AxelorException, IOException;

  MetaFile processFileUpload(InputPart part);

  String getFailedFormSubmissionMessage();

  String getSuccessfulFormSubmissionMessage();

  Long getIdFromInputPart(InputPart inputPart);
}
