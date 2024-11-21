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
package com.axelor.apps.base.service.pricing;

import com.axelor.apps.base.db.Pricing;
import com.axelor.apps.base.db.repo.PricingRepository;
import com.axelor.apps.base.translation.ITranslation;
import com.axelor.common.StringUtils;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.views.AbstractView;
import com.axelor.meta.schema.views.Button;
import com.axelor.meta.schema.views.FormView;
import java.util.ArrayList;
import java.util.List;

public class PricingMetaServiceImpl implements PricingMetaService {

  @Override
  public void managePricing(AbstractView view) {
    String model = view.getModel();
    if (!(view instanceof FormView) || noPricingConfigured(model)) {
      return;
    }
    addPricingButton((FormView) view, model);
  }

  protected boolean noPricingConfigured(String model) {
    return JPA.all(Pricing.class)
            .filter(
                "self.concernedModel.fullName = :modelFullName AND (self.typeSelect IS NULL OR self.typeSelect = :typeSelectDefault)")
            .bind("modelFullName", model)
            .bind("typeSelectDefault", PricingRepository.PRICING_TYPE_SELECT_DEFAULT)
            .fetchOne()
        == null;
  }

  protected void addPricingButton(FormView formView, String model) {
    if (formView.getToolbar() == null) {
      formView.setToolbar(new ArrayList<>());
    }
    formView.setToolbar(addPricingButton(formView.getToolbar(), model));
  }

  protected List<Button> addPricingButton(List<Button> toolbar, String model) {
    Button pricingButton = new Button();
    pricingButton.setTitle(I18n.get(ITranslation.PRICING_BTN));
    pricingButton.setName("pricingBtn");
    pricingButton.setOnClick("action-group-use-pricings");

    String condition = setButtonCondition(model);
    if (StringUtils.notEmpty(condition)) {
      pricingButton.setConditionToCheck(condition);
    }
    pricingButton.setIcon("calculator");
    toolbar.add(0, pricingButton);
    return toolbar;
  }

  @Override
  public String setButtonCondition(String model) {
    return "__config__.app.getApp('base')?.getEnablePricingScale()";
  }
}
