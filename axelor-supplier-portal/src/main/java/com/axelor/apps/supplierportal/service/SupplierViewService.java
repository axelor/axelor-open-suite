/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplierportal.service;

import com.axelor.auth.db.User;
import java.util.Map;

public interface SupplierViewService {
  public User getSupplierUser();

  public Map<String, Object> updateSupplierViewIndicators();

  public String getPurchaseOrdersOfSupplier(User user);

  public String getPurchaseQuotationsInProgressOfSupplier(User user);

  public String getLastPurchaseOrderOfSupplier(User user);

  public String getLastDeliveryOfSupplier(User user);

  public String getNextDeliveryOfSupplier(User user);

  public String getDeliveriesToPrepareOfSupplier(User user);

  public String getAwaitingInvoicesOfSupplier(User user);

  public String getTotalRemainingOfSupplier(User user);
}
