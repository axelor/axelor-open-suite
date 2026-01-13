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
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.pricing.PricingComputer;
import com.axelor.apps.supplychain.db.FreightCarrierPricing;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
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
    Pricing pricing =
        Optional.ofNullable(freightCarrierPricing)
            .map(FreightCarrierPricing::getPricing)
            .orElse(null);
    Pricing delay =
        Optional.ofNullable(freightCarrierPricing)
            .map(FreightCarrierPricing::getDelayPricing)
            .orElse(null);

    String errors = this.computeFreightCarrierPricing(pricing, freightCarrierPricing);
    errors = errors.concat(this.computeFreightCarrierPricing(delay, freightCarrierPricing));

    return errors;
  }

  public String computeFreightCarrierPricing(
      Pricing pricing, FreightCarrierPricing freightCarrierPricing) {
    String errors = "";

    if (pricing != null) {
      try {
        PricingComputer.of(pricing, freightCarrierPricing).apply();
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
