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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.AxelorException;
import com.axelor.auth.db.User;

public interface InvoiceVisibilityService {
  boolean isPfpButtonVisible(Invoice invoice, User user, boolean litigation) throws AxelorException;

  boolean isPaymentButtonVisible(Invoice invoice) throws AxelorException;

  boolean isValidatorUserVisible(Invoice invoice) throws AxelorException;

  boolean isDecisionPfpVisible(Invoice invoice) throws AxelorException;

  boolean isSendNotifyVisible(Invoice invoice) throws AxelorException;

  boolean getManagePfpCondition(Invoice invoice) throws AxelorException;

  boolean getOperationTypePurchaseCondition(Invoice invoice) throws AxelorException;

  boolean getPaymentVouchersStatus(Invoice invoice) throws AxelorException;

  boolean getPfpCondition(Invoice invoice) throws AxelorException;
}
