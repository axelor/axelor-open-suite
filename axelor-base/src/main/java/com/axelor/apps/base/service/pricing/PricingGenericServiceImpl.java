package com.axelor.apps.base.service.pricing;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Pricing;
import com.axelor.common.ObjectUtils;
import com.axelor.db.EntityHelper;
import com.axelor.db.JpaRepository;
import com.axelor.db.Model;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;
import java.util.stream.Collectors;

public class PricingGenericServiceImpl implements PricingGenericService {

  protected PricingService pricingService;
  protected PricingObserver pricingObserver;

  @Inject
  public PricingGenericServiceImpl(PricingService pricingService, PricingObserver pricingObserver) {
    this.pricingService = pricingService;
    this.pricingObserver = pricingObserver;
  }

  @Override
  public void usePricings(Company company, Model model) throws AxelorException {
    computePricingsOnModel(company, model);

    computePricingsOnChildren(company, model);
  }

  @Override
  @Transactional
  public void computePricingsOnModel(Company company, Model model) throws AxelorException {
    List<Pricing> pricingList = pricingService.getPricings(company, model, null);
    pricingList =
        pricingList.stream()
            .filter(pricing -> pricing.getLinkedPricing() == null)
            .collect(Collectors.toList());

    if (ObjectUtils.isEmpty(pricingList)) {
      return;
    }

    for (Pricing pricing : pricingList) {
      PricingComputer pricingComputer = PricingComputer.of(pricing, model);
      pricingComputer.subscribe(pricingObserver);
      pricingComputer.apply();
    }

    JpaRepository.of(EntityHelper.getEntityClass(model)).save(model);
  }

  @Override
  public void computePricingsOnChildren(Company company, Model model) throws AxelorException {
    return;
  }
}
