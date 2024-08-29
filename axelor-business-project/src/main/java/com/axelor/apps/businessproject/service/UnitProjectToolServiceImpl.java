package com.axelor.apps.businessproject.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.businessproject.exception.BusinessProjectExceptionMessage;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.project.db.Project;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

public class UnitProjectToolServiceImpl implements UnitProjectToolService {

  protected AppBusinessProjectService appBusinessProjectService;

  public static final int BIG_DECIMAL_SCALE = 2;

  @Inject
  public UnitProjectToolServiceImpl(AppBusinessProjectService appBusinessProjectService) {
    this.appBusinessProjectService = appBusinessProjectService;
  }

  @Override
  public BigDecimal getConvertedTime(
      BigDecimal duration, Unit fromUnit, Unit toUnit, BigDecimal numberHoursADay)
      throws AxelorException {
    if (appBusinessProjectService.getDaysUnit().equals(fromUnit)
        && appBusinessProjectService.getHoursUnit().equals(toUnit)) {
      return duration.multiply(numberHoursADay);
    } else if (appBusinessProjectService.getHoursUnit().equals(fromUnit)
        && appBusinessProjectService.getDaysUnit().equals(toUnit)) {
      return duration.divide(numberHoursADay, BIG_DECIMAL_SCALE, RoundingMode.HALF_UP);
    } else {
      return duration;
    }
  }

  @Override
  public BigDecimal getNumberHoursADay(Project project) throws AxelorException {
    BigDecimal numberHoursADay =
        Optional.ofNullable(project).map(Project::getNumberHoursADay).orElse(BigDecimal.ZERO);

    if (numberHoursADay.signum() <= 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BusinessProjectExceptionMessage.PROJECT_CONFIG_DEFAULT_HOURS_PER_DAY_MISSING));
    }

    return numberHoursADay;
  }
}
