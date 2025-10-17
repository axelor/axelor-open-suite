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
package com.axelor.apps.base.service.indicator.evaluator;

import com.axelor.apps.base.service.indicator.utils.ResultTransformHelper;
import com.axelor.common.Inflector;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Parameter;
import javax.persistence.Query;

@Singleton
public class JpqlExpressionEvaluator implements ExpressionEvaluator {

  @Override
  public <T extends Model> Optional<BigDecimal> evaluate(
      String expression, T selectedRecord, String targetModelName, Map<String, Object> params) {

    try {
      String recordParamName = Inflector.getInstance().underscore(targetModelName);
      Query query = JPA.em().createQuery(expression);

      bindDeclaredNamedParameters(query, recordParamName, selectedRecord, params);

      Object rawResult = query.getSingleResult();
      return ResultTransformHelper.toBigDecimal(rawResult);
    } catch (NoResultException e) {
      return Optional.empty();
    } catch (NonUniqueResultException e) {
      throw new IllegalStateException("JPQL expression returned multiple results", e);
    }
  }

  protected void bindDeclaredNamedParameters(
      Query query, String recordParamName, Object recordValue, Map<String, Object> params) {

    Set<Parameter<?>> declaredParams = query.getParameters();

    if (recordParamName != null && !recordParamName.isEmpty()) {
      boolean hasRecordParam =
          declaredParams.stream().anyMatch(p -> recordParamName.equals(p.getName()));
      if (hasRecordParam) {
        query.setParameter(recordParamName, recordValue);
      }
    }

    if (params == null || params.isEmpty()) {
      return;
    }

    for (Map.Entry<String, Object> e : params.entrySet()) {
      String key = e.getKey();
      if (key == null) {
        continue;
      }
      boolean hasParam = declaredParams.stream().anyMatch(p -> key.equals(p.getName()));
      if (hasParam) {
        query.setParameter(key, e.getValue());
      }
    }
  }
}
