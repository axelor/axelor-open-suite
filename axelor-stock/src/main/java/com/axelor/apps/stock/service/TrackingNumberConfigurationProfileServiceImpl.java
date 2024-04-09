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
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;

public class TrackingNumberConfigurationProfileServiceImpl
    implements TrackingNumberConfigurationProfileService {

  protected final MetaFieldRepository metaFieldRepository;
  protected final MetaModelRepository metaModelRepository;

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

    for (TrackingNumberConfigurationProfileFieldFormula fieldFormula :
        trackingNumberConfigurationProfile.getProfileFieldFormulaSet().stream()
            .sorted(
                Comparator.comparingInt(
                    TrackingNumberConfigurationProfileFieldFormula::getComputationPriority))
            .collect(Collectors.toList())) {
      Objects.requireNonNull(trackingNumber);

      var optValueFieldFormula = calculateValue(trackingNumber, fieldFormula);

      Mapper trackingNumberMapper = Mapper.of(EntityHelper.getEntityClass(trackingNumber));
      if (optValueFieldFormula.isPresent()) {
        trackingNumberMapper.set(
            trackingNumber, fieldFormula.getMetaField().getName(), optValueFieldFormula.get());
        atLeastOneFieldComputed = true;
      }
    }

    if (!atLeastOneFieldComputed) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(StockExceptionMessage.STOCK_MOVE_TRACKING_NUMBER_DIMENSION_NOT_COMPUTED));
    }
  }

  @Override
  public void setDefaultsFieldFormula(TrackingNumberConfigurationProfile profile) {

    var volumeFormula =
        "if (unitMass && product.productDensity) {\n"
            + "return (unitMass / product.productDensity) * 1000000;\n"
            + "} else {\n"
            + "return null;}";
    addDefaultField(profile, "volume", volumeFormula, 1);
    var unitMassFormula =
        "if (volume && product.productDensity) {\n"
            + "return (volume * product.productDensity) / 1000000;\n"
            + "} else {\n"
            + "return null;}";

    addDefaultField(profile, "unitMass", unitMassFormula, 2);

    var massFormula =
        "if (volume && metricMass) {\n"
            + "return volume * metricMass;\n"
            + "} else {\n"
            + "return null;}";

    addDefaultField(profile, "mass", massFormula, 3);

    var metricMassFormula =
        "if (volume && mass) {\n" + "return mass / volume;\n" + "} else {\n" + "return null;}";

    addDefaultField(profile, "metricMass", metricMassFormula, 4);
  }

  protected void addDefaultField(
      TrackingNumberConfigurationProfile profile, String fieldName, String formula, int priority) {
    var metaModel = metaModelRepository.findByName("TrackingNumber");
    var metricMassField = metaFieldRepository.findByModel(fieldName, metaModel);
    var metricMassFieldFormula =
        new TrackingNumberConfigurationProfileFieldFormula(metricMassField, formula, priority);
    profile.addProfileFieldFormulaSetItem(metricMassFieldFormula);
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
