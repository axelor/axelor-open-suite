package com.axelor.apps.supplychain.service.saleorder.onchange;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Pricing;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.pricing.PricingComputer;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.event.SaleOrderLineProductOnChange;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineComputeService;
import com.axelor.apps.sale.service.saleorderline.product.SaleOrderLineOnProductChangeServiceImpl;
import com.axelor.apps.supplychain.db.FreightCarrierPricing;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.FreightCarrierPricingService;
import com.axelor.db.EntityHelper;
import com.axelor.event.Event;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SaleOrderLineOnProductChangeSupplyChainServiceImpl
    extends SaleOrderLineOnProductChangeServiceImpl
    implements SaleOrderLineOnProductChangeSupplyChainService {

  protected FreightCarrierPricingService freightCarrierPricingService;
  protected SaleOrderRepository saleOrderRepository;

  @Inject
  public SaleOrderLineOnProductChangeSupplyChainServiceImpl(
      Event<SaleOrderLineProductOnChange> saleOrderLineProductOnChangeEvent,
      SaleOrderLineComputeService saleOrderLineComputeService,
      FreightCarrierPricingService freightCarrierPricingService,
      SaleOrderRepository saleOrderRepository) {
    super(saleOrderLineProductOnChangeEvent, saleOrderLineComputeService);
    this.freightCarrierPricingService = freightCarrierPricingService;
    this.saleOrderRepository = saleOrderRepository;
  }

  @Override
  public Map<String, Object> computeLineFromProduct(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine) throws AxelorException {
    SaleOrderLineProductOnChange saleOrderLineProductOnChange =
        new SaleOrderLineProductOnChange(saleOrderLine, saleOrder);
    saleOrderLineProductOnChangeEvent.fire(saleOrderLineProductOnChange);
    Map<String, Object> saleOrderLineMap =
        new HashMap<>(saleOrderLineProductOnChange.getSaleOrderLineMap());
    this.setShippingCostPriceFromPricing(saleOrderLine, saleOrder);
    saleOrderLineMap.putAll(saleOrderLineComputeService.computeValues(saleOrder, saleOrderLine));
    return saleOrderLineMap;
  }

  protected void setShippingCostPriceFromPricing(
      SaleOrderLine shippingCostLine, SaleOrder saleOrder) {
    if (saleOrder.getFreightCarrierMode() == null) {
      return;
    }

    FreightCarrierPricing freightCarrierPricing =
        freightCarrierPricingService.createFreightCarrierPricing(saleOrder.getFreightCarrierMode());
    this.applyPricing(freightCarrierPricing);

    shippingCostLine.setPrice(freightCarrierPricing.getPricingAmount());
  }

  @Override
  public void applyPricing(Set<FreightCarrierPricing> freightCarrierPricingSet)
      throws AxelorException {
    String errors = "";

    for (FreightCarrierPricing freightCarrierPricing : freightCarrierPricingSet) {
      errors = errors.concat(this.applyPricing(freightCarrierPricing));
    }

    if (errors.length() > 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(SupplychainExceptionMessage.FREIGHT_CARRIER_MODE_PRICING_ERROR),
          errors);
    }
  }

  protected String applyPricing(FreightCarrierPricing freightCarrierPricing) {
    String errors = "";

    if (freightCarrierPricing != null) {
      Pricing pricing = freightCarrierPricing.getPricing();
      if (pricing != null) {
        try {
          PricingComputer pricingComputer =
              PricingComputer.of(pricing, freightCarrierPricing)
                  .putInContext(
                      "priceAmount",
                      EntityHelper.getEntity(freightCarrierPricing.getFreightCarrierMode()));

          pricingComputer.apply();
        } catch (AxelorException e) {
          TraceBackService.trace(e);
          if (errors.length() > 0) {
            errors = errors.concat(", ");
          }
          errors = errors.concat(pricing.getName());
        }
      }
    }

    return errors;
  }
}
