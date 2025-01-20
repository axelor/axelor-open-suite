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
package com.axelor.apps.base.service.meta;

import com.axelor.apps.base.service.pricing.PricingMetaService;
import com.axelor.apps.base.service.printing.template.PrintingTemplateMetaService;
import com.axelor.meta.schema.views.AbstractView;
import com.axelor.meta.service.ViewProcessor;
import com.google.inject.Inject;

public class BaseViewProcessor implements ViewProcessor {

  protected PricingMetaService pricingMetaService;
  protected PrintingTemplateMetaService printingTemplateMetaService;

  @Inject
  public BaseViewProcessor(
      PricingMetaService pricingMetaService,
      PrintingTemplateMetaService printingTemplateMetaService) {
    this.pricingMetaService = pricingMetaService;
    this.printingTemplateMetaService = printingTemplateMetaService;
  }

  @Override
  public void process(AbstractView view) {
    pricingMetaService.managePricing(view);
    printingTemplateMetaService.addPrintButton(view);
  }
}
