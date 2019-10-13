/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service;

import com.axelor.apps.base.db.ABCAnalysis;
import com.axelor.apps.base.db.ABCAnalysisClass;
import com.axelor.exception.AxelorException;
import java.util.List;

public interface ABCAnalysisService {
  void reset(ABCAnalysis abcAnalysis);

  void runAnalysis(ABCAnalysis abcAnalysis) throws AxelorException;

  List<ABCAnalysisClass> initABCClasses();

  void setSequence(ABCAnalysis abcAnalysis);

  String printReport(ABCAnalysis abcAnalysis, String reportType) throws AxelorException;

  void checkClasses(ABCAnalysis abcAnalysis) throws AxelorException;
}
