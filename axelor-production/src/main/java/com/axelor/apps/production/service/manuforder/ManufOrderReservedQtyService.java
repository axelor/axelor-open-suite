package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.production.db.ManufOrder;
import com.axelor.exception.AxelorException;

public interface ManufOrderReservedQtyService {

  /**
   * Try to allocate every line, meaning the allocated quantity of the line will be changed to match
   * the requested quantity.
   *
   * @param manufOrder an planned or ongoing or paused manuf order.
   * @throws AxelorException if the manuf order does not have a stock move.
   */
  void allocateAll(ManufOrder manufOrder) throws AxelorException;

  /**
   * Deallocate every line, meaning the allocated quantity of the line will be changed to 0.
   *
   * @param manufOrder an planned or ongoing or paused manuf order.
   * @throws AxelorException if the manuf order does not have a stock move.
   */
  void deallocateAll(ManufOrder manufOrder) throws AxelorException;

  /**
   * Reserve the quantity for every line, meaning we change both requested and allocated quantity to
   * the quantity of the line.
   *
   * @param manufOrder an planned or ongoing or paused manuf order.
   * @throws AxelorException if the manuf order does not have a stock move.
   */
  void reserveAll(ManufOrder manufOrder) throws AxelorException;

  /**
   * Cancel the reservation for every line, meaning we change both requested and allocated quantity
   * to 0.
   *
   * @param manufOrder an planned or ongoing or paused manuf order.
   * @throws AxelorException if the manuf order does not have a stock move.
   */
  void cancelReservation(ManufOrder manufOrder) throws AxelorException;
}
