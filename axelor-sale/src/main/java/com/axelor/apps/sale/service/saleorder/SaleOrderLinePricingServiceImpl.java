package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Pricing;
import com.axelor.apps.base.db.repo.PricingRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.pricing.PricingComputer;
import com.axelor.apps.base.service.pricing.PricingObserver;
import com.axelor.apps.base.service.pricing.PricingService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.saleorder.pricing.SaleOrderLinePricingObserver;
import com.axelor.apps.sale.translation.ITranslation;
import com.axelor.db.EntityHelper;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.Optional;

public class SaleOrderLinePricingServiceImpl implements SaleOrderLinePricingService {

  protected AppBaseService appBaseService;
  protected PricingService pricingService;

  @Inject
  public SaleOrderLinePricingServiceImpl(
      AppBaseService appBaseService, PricingService pricingService) {
    this.appBaseService = appBaseService;
    this.pricingService = pricingService;
  }

  @Override
  public void computePricingScale(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    Optional<Pricing> pricing = getRootPricing(saleOrderLine, saleOrder);
    if (pricing.isPresent() && saleOrderLine.getProduct() != null) {
      PricingComputer pricingComputer =
          getPricingComputer(pricing.get(), saleOrderLine)
              .putInContext("saleOrder", EntityHelper.getEntity(saleOrder));
      pricingComputer.subscribe(getSaleOrderLinePricingObserver(saleOrderLine));
      pricingComputer.apply();
    } else {
      saleOrderLine.setPricingScaleLogs(I18n.get(ITranslation.SALE_ORDER_LINE_OBSERVER_NO_PRICING));
    }
  }

  protected PricingObserver getSaleOrderLinePricingObserver(SaleOrderLine saleOrderLine) {
    return new SaleOrderLinePricingObserver(saleOrderLine);
  }

  protected PricingComputer getPricingComputer(Pricing pricing, SaleOrderLine saleOrderLine)
      throws AxelorException {

    return PricingComputer.of(pricing, saleOrderLine);
  }

  protected Optional<Pricing> getRootPricing(SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    // It is supposed that only one pricing match those criteria (because of the configuration)
    // Having more than one pricing matched may result on a unexpected result
    if (appBaseService.getAppBase().getIsPricingComputingOrder()) {
      return pricingService.getRandomPricing(
          saleOrder.getCompany(),
          saleOrderLine,
          null,
          PricingRepository.PRICING_TYPE_SELECT_SALE_PRICING);
    } else {
      return pricingService.getRootPricingForNextPricings(
          saleOrder.getCompany(),
          saleOrderLine,
          PricingRepository.PRICING_TYPE_SELECT_SALE_PRICING);
    }
  }

  @Override
  public boolean hasPricingLine(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {

    Optional<Pricing> pricing = getRootPricing(saleOrderLine, saleOrder);
    if (pricing.isPresent()) {
      return !getPricingComputer(pricing.get(), saleOrderLine)
          .putInContext("saleOrder", EntityHelper.getEntity(saleOrder))
          .getMatchedPricingLines()
          .isEmpty();
    }

    return false;
  }
}
