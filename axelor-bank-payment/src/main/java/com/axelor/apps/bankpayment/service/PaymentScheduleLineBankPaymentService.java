/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.service;

import com.axelor.apps.account.db.InterbankCodeLine;
import com.axelor.apps.account.db.PaymentScheduleLine;
import com.axelor.apps.account.service.PaymentScheduleLineService;
import com.axelor.apps.base.AxelorException;
import com.axelor.meta.CallMethod;
import java.util.Map;

public interface PaymentScheduleLineBankPaymentService extends PaymentScheduleLineService {
  /**
   * Reject a payment schedule line.
   *
   * @param paymentScheduleLine
   * @param rejectionReason
   * @param represent
   * @throws AxelorException
   */
  void reject(
      PaymentScheduleLine paymentScheduleLine, InterbankCodeLine rejectionReason, boolean represent)
      throws AxelorException;

  /**
   * Reject a payment schedule line.
   *
   * @param name
   * @param rejectionReason
   * @param represent
   * @throws AxelorException
   */
  void reject(String name, InterbankCodeLine rejectionReason, boolean represent)
      throws AxelorException;

  /**
   * Reject payment schedule line from ID.
   *
   * @param id
   * @param rejectionReason
   * @param represent
   * @throws AxelorException
   */
  void reject(long id, InterbankCodeLine rejectionReason, boolean represent) throws AxelorException;

  /**
   * Reject payment schedule line from a map of IDs.
   *
   * @param idMap
   * @param represent
   * @return
   */
  int rejectFromIdMap(Map<Long, InterbankCodeLine> idMap, boolean represent);

  /**
   * Reject payment schedule line from a map of names.
   *
   * @param nameMap
   * @param represent
   * @return
   */
  int rejectFromNameMap(Map<String, InterbankCodeLine> nameMap, boolean represent);

  /**
   * Get default rejection reason.
   *
   * @return
   */
  @CallMethod
  InterbankCodeLine getDefaultRejectionReason();
}
