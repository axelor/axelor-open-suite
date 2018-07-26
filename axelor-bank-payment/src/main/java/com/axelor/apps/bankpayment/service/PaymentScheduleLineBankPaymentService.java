/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.service;

import com.axelor.apps.account.db.PaymentScheduleLine;
import com.axelor.apps.account.service.PaymentScheduleLineService;
import com.axelor.exception.AxelorException;
import java.util.Collection;
import java.util.List;

public interface PaymentScheduleLineBankPaymentService extends PaymentScheduleLineService {
  /**
   * Reject a payment schedule line.
   *
   * @param paymentScheduleLine
   * @param represent
   * @throws AxelorException
   */
  void reject(PaymentScheduleLine paymentScheduleLine, boolean represent) throws AxelorException;

  /**
   * Reject and represent a payment schedule line.
   *
   * @param paymentScheduleLine
   * @throws AxelorException
   */
  void reject(PaymentScheduleLine paymentScheduleLine) throws AxelorException;

  /**
   * Reject payment schedule lines.
   *
   * @param paymentScheduleLines
   * @param represent
   * @return
   */
  Collection<Exception> reject(List<PaymentScheduleLine> paymentScheduleLines, boolean represent);

  /**
   * Reject and represent payment schedule lines.
   *
   * @param paymentScheduleLines
   * @return
   */
  Collection<Exception> reject(List<PaymentScheduleLine> paymentScheduleLines);

  /**
   * Reject payment schedule lines from ID.
   *
   * @param id
   * @param represent
   * @throws AxelorException
   */
  void reject(long id, boolean represent) throws AxelorException;

  /**
   * Reject and represent payment schedule lines from ID.
   *
   * @param id
   * @throws AxelorException
   */
  void reject(long id) throws AxelorException;

  /**
   * Reject payment schedule lines from list of IDs.
   *
   * @param idList
   * @param represent
   * @return
   */
  int rejectFromIdList(List<Long> idList, boolean represent);

  /**
   * Reject and represent payment schedule lines from list of IDs.
   *
   * @param idList
   * @return
   */
  int rejectFromIdList(List<Long> idList);
}
