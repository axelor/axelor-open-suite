package com.axelor.apps.base.service.pricing;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Pricing;
import com.axelor.common.ObjectUtils;
import com.axelor.db.EntityHelper;
import com.axelor.db.JpaRepository;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import com.axelor.web.ITranslation;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
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
    List<Pricing> pricingList = getPricings(company, model);

    if (ObjectUtils.isEmpty(pricingList)) {
      return;
    }
    List<StringBuilder> logsList = new ArrayList<>();
    for (Pricing pricing : pricingList) {
      PricingComputer pricingComputer = PricingComputer.of(pricing, model);
      pricingComputer.subscribe(pricingObserver);
      pricingComputer.apply();

      logsList.add(pricingObserver.getLogs());
    }

    updatePricingScaleLogs(logsList, model);

    JpaRepository.of(EntityHelper.getEntityClass(model)).save(model);
  }

  @Override
  public List<Pricing> getPricings(Company company, Model model) {
    List<Pricing> pricingList = pricingService.getPricings(company, model, null);
    pricingList =
        pricingList.stream()
            .filter(pricing -> pricing.getLinkedPricing() == null)
            .collect(Collectors.toList());

    return pricingList;
  }

  @Override
  public void computePricingsOnChildren(Company company, Model model) throws AxelorException {
    return;
  }

  @Override
  public String updatePricingScaleLogs(List<StringBuilder> logsList, Model model) {
    String logs = computePricingLogs(logsList);

    return logs;
  }

  protected String computePricingLogs(List<StringBuilder> logsList) {

    List<StringBuilder> pricingScaleLogs = new ArrayList<>();
    for (StringBuilder logs : logsList) {
      if (!I18n.get(ITranslation.PRICING_OBSERVER_NO_PRICING).equals(logs.toString())) {
        pricingScaleLogs.add(logs);
      }
    }

    if (ObjectUtils.isEmpty(pricingScaleLogs)) {
      return I18n.get(ITranslation.PRICING_OBSERVER_NO_PRICING);
    }

    return pricingScaleLogs.stream().collect(Collectors.joining("\n"));
  }
}
