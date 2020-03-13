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
package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.exception.AxelorException;
import java.math.BigDecimal;
import java.util.List;

public interface SaleOrderComputeService {

  public SaleOrder _computeSaleOrderLineList(SaleOrder saleOrder) throws AxelorException;

  public SaleOrder computeSaleOrder(SaleOrder saleOrder) throws AxelorException;

  /**
   * Peupler un devis.
   *
   * <p>Cette fonction permet de déterminer les tva d'un devis.
   *
   * @param saleOrder
   * @throws AxelorException
   */
  public void _populateSaleOrder(SaleOrder saleOrder) throws AxelorException;

  /**
   * Calculer le montant d'une facture.
   *
   * <p>Le calcul est basé sur les lignes de TVA préalablement créées.
   *
   * @param invoice
   * @param vatLines
   * @throws AxelorException
   */
  public void _computeSaleOrder(SaleOrder saleOrder) throws AxelorException;

  /**
   * Permet de réinitialiser la liste des lignes de TVA
   *
   * @param saleOrder Un devis
   */
  public void initSaleOrderLineTaxList(SaleOrder saleOrder);

  /**
   * Return the total price, computed from the lines. This price is usually equals to {@link
   * SaleOrder#exTaxTotal} but not in all cases.
   *
   * @param saleOrder
   * @return total price from the sale order lines
   */
  public BigDecimal getTotalSaleOrderPrice(SaleOrder saleOrder);

  public List<SaleOrderLine> removeSubLines(List<SaleOrderLine> lines);
}
