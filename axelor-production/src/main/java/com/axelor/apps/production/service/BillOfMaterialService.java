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
package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.TempBomTree;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.meta.CallMethod;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public interface BillOfMaterialService {

  static final String UNIT_MIN_CODE = "MIN";

  static final String UNIT_DAY_CODE = "JR";

  public List<BillOfMaterial> getBillOfMaterialSet(Product product);

  public void updateProductCostPrice(BillOfMaterial billOfMaterial) throws AxelorException;

  public BillOfMaterial customizeBillOfMaterial(SaleOrderLine saleOrderLine) throws AxelorException;

  public BillOfMaterial generateNewVersion(BillOfMaterial billOfMaterial);

  public String getFileName(BillOfMaterial billOfMaterial);

  public String getReportLink(
      BillOfMaterial billOfMaterial, String name, String language, String format)
      throws AxelorException;

  public TempBomTree generateTree(BillOfMaterial billOfMaterial, boolean useProductDefaultBom);

  public void setBillOfMaterialAsDefault(BillOfMaterial billOfMaterial) throws AxelorException;

  BillOfMaterial customizeBillOfMaterial(BillOfMaterial billOfMaterial) throws AxelorException;

  BillOfMaterial customizeBillOfMaterial(BillOfMaterial billOfMaterial, int depth)
      throws AxelorException;

  String computeName(BillOfMaterial bom);

  void addRawMaterials(long billOfMaterialId, ArrayList<LinkedHashMap<String, Object>> rawMaterials)
      throws AxelorException;

  @CallMethod
  public BillOfMaterial getDefaultBOM(Product originalProduct, Company company)
      throws AxelorException;

  List<BillOfMaterial> getAlternativesBOM(Product originalProduct, Company company)
      throws AxelorException;

  /**
   * This method will return a BOM fetched by priority that goes like this 1) search for company
   * specific default BOM in the the original product 2) Any BOM with original product and company.
   * 3) Default bom of the original product regardless of the company
   *
   * @param originalProduct
   * @param company
   * @return Bom found
   * @throws AxelorException
   */
  public BillOfMaterial getBOM(Product originalProduct, Company company) throws AxelorException;

  /**
   * Returns all the products from boms
   *
   * @param companySet
   * @return
   * @throws AxelorException
   */
  List<Long> getBillOfMaterialProductsId(Set<Company> companySet) throws AxelorException;
}
