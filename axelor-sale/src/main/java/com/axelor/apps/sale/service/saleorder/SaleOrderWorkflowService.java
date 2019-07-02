/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.db.CancelReason;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;

public interface SaleOrderWorkflowService {

  @Transactional
  public Partner validateCustomer(SaleOrder saleOrder);

  public String getSequence(Company company) throws AxelorException;

  public void cancelSaleOrder(
      SaleOrder saleOrder, CancelReason cancelReason, String cancelReasonStr);

  public void finalizeQuotation(SaleOrder saleOrder) throws AxelorException;

  public void confirmSaleOrder(SaleOrder saleOrder) throws AxelorException;

  public void saveSaleOrderPDFAsAttachment(SaleOrder saleOrder) throws AxelorException;

  public String getFileName(SaleOrder saleOrder);
}
