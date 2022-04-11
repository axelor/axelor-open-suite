package com.axelor.apps.production.service;

import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.WorkCenterGroup;
import com.axelor.exception.AxelorException;
import java.math.BigDecimal;

public interface WorkCenterService {

  public Long getDurationFromWorkCenter(WorkCenter workCenter);

  public BigDecimal getMinCapacityPerCycleFromWorkCenter(WorkCenter workCenter);

  public BigDecimal getMaxCapacityPerCycleFromWorkCenter(WorkCenter workCenter);

  /**
   * Returns work center with min sequence in {@link workCenterGroup}. Can return null if the work
   * center group is null, else if the work center group has no work centers, throws an exception.
   */
  public WorkCenter getMainWorkCenterFromGroup(WorkCenterGroup workCenterGroup)
      throws AxelorException;
}
