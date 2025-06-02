package com.axelor.apps.supplychain.service.pricing;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Pricing;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.pricing.PricingComputer;
import com.axelor.apps.supplychain.db.FreightCarrierPricing;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.db.EntityHelper;
import com.axelor.i18n.I18n;
import java.util.Optional;
import java.util.Set;

public class FreightCarrierApplyPricingServiceImpl implements FreightCarrierApplyPricingService {

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

  @Override
  public String applyPricing(FreightCarrierPricing freightCarrierPricing) {
    String errors = "";
    Pricing pricing =
        Optional.ofNullable(freightCarrierPricing)
            .map(FreightCarrierPricing::getPricing)
            .orElse(null);
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
        if (!errors.isEmpty()) {
          errors = errors.concat(", ");
        }
        errors = errors.concat(pricing.getName());
      }
    }

    return errors;
  }
}
