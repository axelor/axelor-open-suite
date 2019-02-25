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
package com.axelor.apps.purchase.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;
import java.util.List;

public interface PurchaseOrderService {

  PurchaseOrder _computePurchaseOrderLines(PurchaseOrder purchaseOrder) throws AxelorException;

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
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

  PurchaseOrder createPurchaseOrder(
      User buyerUser,
      Company company,
      Partner contactPartner,
      Currency currency,
      LocalDate deliveryDate,
      String internalReference,
      String externalReference,
      LocalDate orderDate,
      PriceList priceList,
      Partner supplierPartner,
      TradingName tradingName)
      throws AxelorException;

  String getSequence(Company company) throws AxelorException;

  public void setDraftSequence(PurchaseOrder purchaseOrder) throws AxelorException;

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public Partner validateSupplier(PurchaseOrder purchaseOrder);

  public void savePurchaseOrderPDFAsAttachment(PurchaseOrder purchaseOrder) throws AxelorException;

  public void requestPurchaseOrder(PurchaseOrder purchaseOrder) throws AxelorException;

  public PurchaseOrder mergePurchaseOrders(
      List<PurchaseOrder> purchaseOrderList,
      Currency currency,
      Partner supplierPartner,
      Company company,
      Partner contactPartner,
      PriceList priceList,
      TradingName tradingName)
      throws AxelorException;

  public void updateCostPrice(PurchaseOrder purchaseOrder) throws AxelorException;

  public void draftPurchaseOrder(PurchaseOrder purchaseOrder);

  public void validatePurchaseOrder(PurchaseOrder purchaseOrder) throws AxelorException;

  public void finishPurchaseOrder(PurchaseOrder purchaseOrder);

  public void cancelPurchaseOrder(PurchaseOrder purchaseOrder);
}
