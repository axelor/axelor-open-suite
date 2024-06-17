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
package com.axelor.apps.purchase.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.google.inject.persist.Transactional;

public interface PurchaseOrderService {

  PurchaseOrder _computePurchaseOrderLines(PurchaseOrder purchaseOrder) throws AxelorException;

  @Transactional(rollbackOn = {Exception.class})
  PurchaseOrder computePurchaseOrder(PurchaseOrder purchaseOrder) throws AxelorException;

  /**
   * Peupler une commande.
   *
   * <p>Cette fonction permet de déterminer les tva d'une commande à partir des lignes de factures
   * passées en paramètres.
   *
   * @param purchaseOrder
   * @throws AxelorException
   */
  void _populatePurchaseOrder(PurchaseOrder purchaseOrder) throws AxelorException;

  /**
   * Calculer le montant d'une commande.
   *
   * <p>Le calcul est basé sur les lignes de TVA préalablement créées.
   *
   * @param purchaseOrder
   * @throws AxelorException
   */
  void _computePurchaseOrder(PurchaseOrder purchaseOrder) throws AxelorException;

  /**
   * Permet de réinitialiser la liste des lignes de TVA
   *
   * @param purchaseOrder Une commande.
   */
  void initPurchaseOrderLineTax(PurchaseOrder purchaseOrder);

  @Transactional
  public Partner validateSupplier(PurchaseOrder purchaseOrder);

  public void savePurchaseOrderPDFAsAttachment(PurchaseOrder purchaseOrder) throws AxelorException;

  public void requestPurchaseOrder(PurchaseOrder purchaseOrder) throws AxelorException;

  public void updateCostPrice(PurchaseOrder purchaseOrder) throws AxelorException;

  void checkPrintingSettings(PurchaseOrder purchaseOrder) throws AxelorException;
}
