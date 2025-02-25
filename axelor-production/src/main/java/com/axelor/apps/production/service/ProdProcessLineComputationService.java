package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.ProdProcessLine;
import java.math.BigDecimal;

public interface ProdProcessLineComputationService {

  /**
   * Compute and return number of cycle for a given qty
   *
   * @param prodProcessLine
   * @param qty
   * @return Number of cycle.
   */
  BigDecimal getNbCycle(ProdProcessLine prodProcessLine, BigDecimal qty);

  /**
   * Compute and return the installing duration which is: <br>
   * setup duration * (nbCycle - 1) + starting duration + ending duration
   *
   * @param prodProcessLine
   * @return installing duration
   */
  BigDecimal getMachineInstallingDuration(ProdProcessLine prodProcessLine, BigDecimal nbCycles)
      throws AxelorException;

  /**
   * Compute the total machine duration for a given nbCycles
   *
   * @param prodProcessLine
   * @param nbCycles
   * @return totalMachineDuration
   */
  BigDecimal getMachineDuration(ProdProcessLine prodProcessLine, BigDecimal nbCycles)
      throws AxelorException;

  /**
   * Compute the total human duration for a given nbCycles
   *
   * @param prodProcessLine
   * @param nbCycles
   * @return totalMachineDuration
   */
  BigDecimal getHumanDuration(ProdProcessLine prodProcessLine, BigDecimal nbCycles);

  /**
   * Compute the total duration for a given nbCycles
   *
   * @param prodProcessLine
   * @param nbCycles
   * @return total duration
   */
  BigDecimal getTotalDuration(ProdProcessLine prodProcessLine, BigDecimal nbCycles)
      throws AxelorException;

  /**
   * Compute the entire cycle duration of the prod process line with qty given.
   *
   * @param operationOrder
   * @param prodProcessLine
   * @param qty
   * @throws AxelorException
   */
  long computeEntireCycleDuration(
      OperationOrder operationOrder, ProdProcessLine prodProcessLine, BigDecimal qty)
      throws AxelorException;
}
