package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.AxelorException;
import java.util.List;

public interface ManufOrderMergingService {

  /**
   * Merge different manufacturing orders into a single one.
   *
   * @param ids List of ids of manufacturing orders to merge
   * @throws AxelorException
   */
  public void merge(List<Long> ids) throws AxelorException;

  /**
   * Check if the manufacturing orders can be merged.
   *
   * @param ids List of ids of manufacturing orders to merge
   */
  public boolean canMerge(List<Long> ids);
}
