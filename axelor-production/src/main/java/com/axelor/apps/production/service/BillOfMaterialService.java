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
package com.axelor.apps.production.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.TempBomTree;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;
import java.util.List;

public interface BillOfMaterialService {

  static final String UNIT_MIN_CODE = "MIN";

  static final String UNIT_DAY_CODE = "JR";

  public List<BillOfMaterial> getBillOfMaterialSet(Product product);

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void updateProductCostPrice(BillOfMaterial billOfMaterial) throws AxelorException;

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public BillOfMaterial customizeBillOfMaterial(SaleOrderLine saleOrderLine) throws AxelorException;

  public BillOfMaterial generateNewVersion(BillOfMaterial billOfMaterial);

  public String getFileName(BillOfMaterial billOfMaterial);

  public String getReportLink(
      BillOfMaterial billOfMaterial, String name, String language, String format)
      throws AxelorException;

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public TempBomTree generateTree(BillOfMaterial billOfMaterial);

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  BillOfMaterial customizeBillOfMaterial(BillOfMaterial billOfMaterial) throws AxelorException;

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  BillOfMaterial customizeBillOfMaterial(BillOfMaterial billOfMaterial, int depth)
      throws AxelorException;
}
