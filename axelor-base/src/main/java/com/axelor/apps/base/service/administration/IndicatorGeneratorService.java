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
package com.axelor.apps.base.service.administration;

import com.axelor.apps.base.db.IndicatorGenerator;
import com.axelor.apps.base.db.repo.IndicatorGeneratorRepository;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigInteger;
import javax.persistence.Query;

public class IndicatorGeneratorService {

  @Inject private IndicatorGeneratorRepository indicatorGeneratorRepo;

  @Transactional(rollbackOn = {Exception.class})
  public String run(IndicatorGenerator indicatorGenerator) throws AxelorException {

    String log = "";

    int requestType = indicatorGenerator.getRequestLanguage();

    String request = indicatorGenerator.getRequest();

    if (request == null || request.isEmpty()) {
      log =
          String.format(
              I18n.get(IExceptionMessage.INDICATOR_GENERATOR_1), indicatorGenerator.getCode());
    }

    String result = "";

    try {
      if (request != null && !request.isEmpty()) {
        if (requestType == 0) {

          result = this.runSqlRequest(request);

        } else if (requestType == 1) {

          result = this.runJpqlRequest(request);
        }
      }
    } catch (Exception e) {

      log +=
          String.format(
              I18n.get(IExceptionMessage.INDICATOR_GENERATOR_2), indicatorGenerator.getCode());
    }

    indicatorGenerator.setLog(log);

    indicatorGenerator.setResult(result);

    indicatorGeneratorRepo.save(indicatorGenerator);

    return result;
  }

  public String runSqlRequest(String request) {
    String result = "";

    Query query = JPA.em().createNativeQuery(request);

    BigInteger requestResult = (BigInteger) query.getSingleResult();

    result = String.format("%s", requestResult);

    return result;
  }

  public String runJpqlRequest(String request) {
    String result = "";

    Query query = JPA.em().createQuery(request);

    Long requestResult = (Long) query.getSingleResult();

    result = String.format("%s", requestResult);

    return result;
  }
}
