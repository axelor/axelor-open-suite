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
package com.axelor.apps.base.service.wordreport.config;

import com.axelor.db.mapper.Mapper;
import com.axelor.exception.AxelorException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.script.ScriptException;
import org.docx4j.wml.Text;

public interface ReportValueService {

  public void setTextValue(
      Mapper mapper,
      Text text,
      Object object,
      ResourceBundle resourceBundle,
      Map<String, List<Object>> reportQueryBuilderResultMap)
      throws AxelorException, ClassNotFoundException, IOException, ScriptException;
}
