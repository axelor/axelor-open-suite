package com.axelor.apps.base.service.pricing;

import com.axelor.apps.base.db.Pricing;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.translation.ITranslation;
import com.axelor.common.StringUtils;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.views.Button;
import com.axelor.meta.schema.views.FormView;
import com.axelor.rpc.Response;
import com.google.inject.Inject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PricingMetaServiceImpl implements PricingMetaService {

  private static final Logger LOG = LoggerFactory.getLogger(PricingMetaServiceImpl.class);

  @Inject
  public PricingMetaServiceImpl() {}

  @Override
  public Response managePricing(Response response, String model) {
    if (noPricingConfigured(model)) {
      return response;
    }
    if (response.getData() instanceof FormView) {
      addPricingButton((FormView) response.getData(), model);
    }

    return response;
  }

  protected boolean noPricingConfigured(String model) {
    return JPA.all(Pricing.class)
            .filter("self.concernedModel.fullName = :modelFullName")
            .bind("modelFullName", model)
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
    try {
      Button pricingButton = new Button();
      pricingButton.setTitle(I18n.get(ITranslation.PRICING_BTN));
      pricingButton.setName("pricingBtn");
      pricingButton.setOnClick("action-group-use-pricings");

      String condition = setButtonCondition(model);
      if (StringUtils.notEmpty(condition)) {
        pricingButton.setConditionToCheck(condition);
      }

      setButtonIcon(pricingButton);
      toolbar.add(0, pricingButton);
    } catch (Exception e) {
      LOG.error(
          String.format(
              I18n.get(BaseExceptionMessage.PRICING_BUTTON_ERROR), e.getLocalizedMessage()));
    }
    return toolbar;
  }

  @SuppressWarnings("java:S3011")
  protected void setButtonIcon(Button pricingButton)
      throws NoSuchFieldException, IllegalAccessException {
    Field iconField = Button.class.getDeclaredField("icon");
    iconField.setAccessible(true);
    iconField.set(pricingButton, "calculator");
    iconField.setAccessible(false);
  }

  @Override
  public String setButtonCondition(String model) {
    return "__config__.app.getApp('base')?.getEnablePricingScale()";
  }
}
