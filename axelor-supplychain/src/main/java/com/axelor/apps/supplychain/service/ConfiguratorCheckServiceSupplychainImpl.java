/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.sale.db.Configurator;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.service.configurator.ConfiguratorCheckServiceImpl;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;

public class ConfiguratorCheckServiceSupplychainImpl extends ConfiguratorCheckServiceImpl {

  protected final SaleOrderLineRepository saleOrderLineRepository;

  @Inject
  public ConfiguratorCheckServiceSupplychainImpl(SaleOrderLineRepository saleOrderLineRepository) {
    super();
    this.saleOrderLineRepository = saleOrderLineRepository;
  }

  @Override
  public void checkLinkedSaleOrderLine(Configurator configurator, Product product)
      throws AxelorException {
    super.checkLinkedSaleOrderLine(configurator, product);

    var terminatedSOL =
        saleOrderLineRepository
            .all()
            .filter(
                "self.product = :product AND self.configurator = :configurator"
                    + " AND (self.deliveryState >= :partiallyDeliveredState OR self.invoicingState >= :partiallyInvoicedState) ")
            .bind("product", product)
            .bind("configurator", configurator)
            .bind(
                "partiallyDeliveredState",
                SaleOrderLineRepository.DELIVERY_STATE_PARTIALLY_DELIVERED)
            .bind(
                "partiallyInvoicedState",
                SaleOrderLineRepository.INVOICING_STATE_PARTIALLY_INVOICED)
            .fetchOne();

    if (terminatedSOL != null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SupplychainExceptionMessage.CONFIGURATOR_CAN_NOT_REGENERATE_PRODUCT));
    }
  }

  @Override
  public void checkLinkedSaleOrderLine(Configurator configurator) throws AxelorException {
    super.checkLinkedSaleOrderLine(configurator);

    var terminatedSOL =
        saleOrderLineRepository
            .all()
            .filter(
                "self.configurator = :configurator"
                    + " AND (self.deliveryState >= :partiallyDeliveredState OR self.invoicingState >= :partiallyInvoicedState) ")
            .bind("configurator", configurator)
            .bind(
                "partiallyDeliveredState",
                SaleOrderLineRepository.DELIVERY_STATE_PARTIALLY_DELIVERED)
            .bind(
                "partiallyInvoicedState",
                SaleOrderLineRepository.INVOICING_STATE_PARTIALLY_INVOICED)
            .fetchOne();

    if (terminatedSOL != null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SupplychainExceptionMessage.CONFIGURATOR_CAN_NOT_REGENERATE_PRODUCT));
    }
  }
}
