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
package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.TrackingNumberConfigurationProfile;
import com.axelor.apps.stock.db.TrackingNumberConfigurationProfileFieldFormula;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.db.EntityHelper;
import com.axelor.db.mapper.Mapper;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.rpc.Context;
import com.axelor.script.GroovyScriptHelper;
import com.axelor.script.ScriptHelper;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrackingNumberConfigurationProfileServiceImpl
    implements TrackingNumberConfigurationProfileService {

  protected final MetaFieldRepository metaFieldRepository;
  protected final MetaModelRepository metaModelRepository;
  protected final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject
  public TrackingNumberConfigurationProfileServiceImpl(
      MetaFieldRepository metaFieldRepository, MetaModelRepository metaModelRepository) {
    this.metaFieldRepository = metaFieldRepository;
    this.metaModelRepository = metaModelRepository;
  }

  @Override
  public void calculateDimension(
      TrackingNumber trackingNumber,
      TrackingNumberConfigurationProfile trackingNumberConfigurationProfile)
      throws AxelorException {

    Objects.requireNonNull(trackingNumber);
    Objects.requireNonNull(trackingNumberConfigurationProfile);

    boolean atLeastOneFieldComputed = false;
    log.debug("Calculating dimension of {}", trackingNumberConfigurationProfile);
    var profileFieldFormulaSet = trackingNumberConfigurationProfile.getProfileFieldFormulaSet();
    if (profileFieldFormulaSet != null) {
      // Will try to compute everything multiple times (max size of the set)
      log.debug("{} loops planned to compute", profileFieldFormulaSet.size());
      for (int i = 0; i < profileFieldFormulaSet.size(); i++) {

        for (TrackingNumberConfigurationProfileFieldFormula fieldFormula :
            profileFieldFormulaSet.stream()
                .sorted(
                    Comparator.comparingInt(
                        TrackingNumberConfigurationProfileFieldFormula::getComputationPriority))
                .collect(Collectors.toList())) {
          log.debug("Loop {}, computing field formula {}", i, fieldFormula);
          var optValueFieldFormula = calculateValue(trackingNumber, fieldFormula);

          Mapper trackingNumberMapper = Mapper.of(EntityHelper.getEntityClass(trackingNumber));
          if (optValueFieldFormula.isPresent()) {
            trackingNumberMapper.set(
                trackingNumber, fieldFormula.getMetaField().getName(), optValueFieldFormula.get());
            atLeastOneFieldComputed = true;
          }
        }
      }
    }

    if (!atLeastOneFieldComputed) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(StockExceptionMessage.STOCK_MOVE_TRACKING_NUMBER_DIMENSION_NOT_COMPUTED));
    }
  }

  protected Optional<BigDecimal> calculateValue(
      TrackingNumber trackingNumber, TrackingNumberConfigurationProfileFieldFormula fieldFormula)
      throws AxelorException {
    String formula = fieldFormula.getFormula();
    Context scriptContext = new Context(Mapper.toMap(trackingNumber), TrackingNumber.class);
    ScriptHelper scriptHelper = new GroovyScriptHelper(scriptContext);

    Object result;

    result = scriptHelper.eval(formula);

    if (result == null) {
      return Optional.empty();
    }

    if (!(result instanceof BigDecimal)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(
              StockExceptionMessage.STOCK_MOVE_TRACKING_NUMBER_DIMENSION_EXPECTED_DECIMAL_RESULT),
          fieldFormula.getMetaField().getName());
    }

    return Optional.of((BigDecimal) result);
  }
}
