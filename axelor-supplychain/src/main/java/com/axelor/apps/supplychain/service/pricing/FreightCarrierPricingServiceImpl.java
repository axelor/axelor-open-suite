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
package com.axelor.apps.supplychain.service.pricing;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Pricing;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.stock.db.FreightCarrierMode;
import com.axelor.apps.stock.db.repo.FreightCarrierModeRepository;
import com.axelor.apps.supplychain.db.FreightCarrierPricing;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderShipmentService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class FreightCarrierPricingServiceImpl implements FreightCarrierPricingService {

  protected final SaleOrderRepository saleOrderRepository;
  protected final FreightCarrierModeRepository freightCarrierModeRepository;
  protected final PartnerRepository partnerRepository;
  protected final SaleOrderShipmentService saleOrderShipmentService;
  protected final FreightCarrierApplyPricingService freightCarrierApplyPricingService;

  @Inject
  public FreightCarrierPricingServiceImpl(
      SaleOrderRepository saleOrderRepository,
      FreightCarrierModeRepository freightCarrierModeRepository,
      PartnerRepository partnerRepository,
      SaleOrderShipmentService saleOrderShipmentService,
      FreightCarrierApplyPricingService freightCarrierApplyPricingService) {
    this.saleOrderRepository = saleOrderRepository;
    this.freightCarrierModeRepository = freightCarrierModeRepository;
    this.partnerRepository = partnerRepository;
    this.saleOrderShipmentService = saleOrderShipmentService;
    this.freightCarrierApplyPricingService = freightCarrierApplyPricingService;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public String computeFreightCarrierPricing(
      List<FreightCarrierPricing> freightCarrierPricingList, Long saleOrderId)
      throws AxelorException {
    SaleOrder saleOrder = saleOrderRepository.find(saleOrderId);
    if (saleOrder != null) {
      this.checkSelectedFreightPricingMode(freightCarrierPricingList);
      FreightCarrierPricing freightCarrierPricing = freightCarrierPricingList.get(0);
      saleOrder.setFreightCarrierMode(
          freightCarrierModeRepository.find(freightCarrierPricing.getFreightCarrierMode().getId()));
      saleOrder.setCarrierPartner(
          partnerRepository.find(freightCarrierPricing.getCarrierPartner().getId()));

      if (saleOrder.getEstimatedShippingDate() != null) {
        saleOrder.setEstimatedDeliveryDate(
            saleOrder
                .getEstimatedShippingDate()
                .plusDays(freightCarrierPricing.getDelay().longValue()));
      }

      String message =
          saleOrderShipmentService.createShipmentCostLine(saleOrder, saleOrder.getShipmentMode());
      saleOrderRepository.save(saleOrder);
      return message;
    }
    return null;
  }

  public Set<FreightCarrierPricing> getFreightCarrierPricingSet(
      Long shipmentModeId, Long saleOrderId) {
    Set<FreightCarrierPricing> freightCarrierPricings = new HashSet<>();
    SaleOrder saleOrder = saleOrderRepository.find(saleOrderId);

    List<FreightCarrierMode> freightCarrierModeList =
        freightCarrierModeRepository
            .all()
            .filter("self.shipmentMode.id = :id")
            .bind("id", shipmentModeId)
            .fetch();

    if (!freightCarrierModeList.isEmpty() && saleOrder != null) {
      freightCarrierModeList.forEach(
          fc -> {
            freightCarrierPricings.add(this.createFreightCarrierPricing(fc, saleOrder));
          });
    }

    return freightCarrierPricings;
  }

  @Override
  public FreightCarrierPricing createFreightCarrierPricing(
      FreightCarrierMode freightCarrierMode, SaleOrder saleOrder) {

    if (freightCarrierMode == null) {
      return null;
    }

    FreightCarrierPricing freightCarrierPricing = new FreightCarrierPricing();
    freightCarrierPricing.setFreightCarrierMode(freightCarrierMode);
    freightCarrierPricing.setCarrierPartner(freightCarrierMode.getCarrierPartner());
    freightCarrierPricing.setPricing(freightCarrierMode.getFreightCarrierPricing());
    freightCarrierPricing.setDelayPricing(freightCarrierMode.getFreightCarrierDelay());
    freightCarrierPricing.setSaleOrder(saleOrder);
    return freightCarrierPricing;
  }

  protected void checkSelectedFreightPricingMode(
      List<FreightCarrierPricing> freightCarrierPricingList) throws AxelorException {
    if (freightCarrierPricingList.isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          String.format(
              I18n.get(
                  SupplychainExceptionMessage.SALE_ORDER_NO_FREIGHT_CARRIER_PRICING_SELECTED)));
    }

    if (freightCarrierPricingList.size() > 1) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          String.format(
              I18n.get(
                  SupplychainExceptionMessage
                      .SALE_ORDER_MORE_THAN_ONE_FREIGHT_CARRIER_PRICING_SELECTED)));
    }
  }

  @Override
  public void updateEstimatedDeliveryDateWithPricingDelay(SaleOrder saleOrder)
      throws AxelorException {
    FreightCarrierPricing freightCarrierPricing =
        this.createFreightCarrierPricing(saleOrder.getFreightCarrierMode(), saleOrder);

    if (freightCarrierPricing == null) {
      return;
    }

    Pricing delay =
        Optional.of(freightCarrierPricing).map(FreightCarrierPricing::getDelayPricing).orElse(null);

    String errors =
        freightCarrierApplyPricingService.computeFreightCarrierPricing(
            delay, freightCarrierPricing);

    if (errors.length() > 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(SupplychainExceptionMessage.FREIGHT_CARRIER_MODE_PRICING_ERROR),
          errors);
    }

    saleOrder.setEstimatedDeliveryDate(
        saleOrder
            .getEstimatedShippingDate()
            .plusDays(freightCarrierPricing.getDelay().longValue()));
  }

  @Override
  public String notifyEstimatedDeliveryDateUpdate(SaleOrder saleOrder) {
    FreightCarrierMode freightCarrierMode = saleOrder.getFreightCarrierMode();
    if (freightCarrierMode == null) {
      return null;
    }

    Pricing delayPricing = freightCarrierMode.getFreightCarrierDelay();
    if (delayPricing != null) {
      return I18n.get(SupplychainExceptionMessage.SALE_ORDER_ESTIMATED_SHIPPING_DATE_NOT_UPDATED);
    }

    return null;
  }
}
