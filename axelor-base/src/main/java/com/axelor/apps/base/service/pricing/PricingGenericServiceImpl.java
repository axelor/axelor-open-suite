package com.axelor.apps.base.service.pricing;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Pricing;
import com.axelor.apps.base.db.repo.PricingRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.common.ObjectUtils;
import com.axelor.db.EntityHelper;
import com.axelor.db.JPA;
import com.axelor.db.JpaRepository;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import com.axelor.web.ITranslation;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class PricingGenericServiceImpl implements PricingGenericService {

  protected PricingService pricingService;
  protected PricingObserver pricingObserver;
  protected AppBaseService appBaseService;

  @Inject
  public PricingGenericServiceImpl(
      PricingService pricingService,
      PricingObserver pricingObserver,
      AppBaseService appBaseService) {
    this.pricingService = pricingService;
    this.pricingObserver = pricingObserver;
    this.appBaseService = appBaseService;
  }

  @Override
  public <T extends Model> void usePricings(Company company, Class<?> modelClass, Long modelId)
      throws AxelorException {

    Model model = JPA.find((Class<T>) modelClass, modelId);
    usePricings(company, model);
  }

  @Override
  public <T extends Model> void usePricings(
      Company company, Class<?> modelClass, List<Integer> idList) throws AxelorException {
    for (Number id : idList) {
      usePricings(company, modelClass, id.longValue());

      JPA.clear();
    }
  }

  @Override
  public void usePricings(Company company, Model model) throws AxelorException {
    computePricingsOnModel(company, model);

    computePricingsOnChildren(company, model);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void computePricingsOnModel(Company company, Model model) throws AxelorException {
    List<String> unavailableModels = getUnavailableModels();
    if (!ObjectUtils.isEmpty(unavailableModels)
        && unavailableModels.contains(model.getClass().getSimpleName())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          String.format(
              I18n.get(BaseExceptionMessage.PRICING_UNAVAILABLE_FOR_THIS_CLASS),
              I18n.get(model.getClass().getSimpleName())));
    }

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
    List<Pricing> pricingList = new ArrayList<>();
    if (appBaseService.getAppBase().getIsPricingComputingOrder()) {
      pricingList = pricingService.getPricings(company, model, null);
    } else {
      List<Pricing> resultList = pricingService.getAllPricings(company, model);

      Set<Long> pricingsPointedTo =
          resultList.stream()
              .map(Pricing::getLinkedPricing)
              .filter(Objects::nonNull)
              .map(Pricing::getId)
              .collect(Collectors.toSet());

      // find the pricing that doesn't have any pricing pointing to it, that's the root
      for (Pricing pricing : resultList) {
        if (!pricingsPointedTo.contains(pricing.getId())) {
          pricingList.add(pricing);
        }
      }
    }

    return pricingList;
  }

  @Override
  public void computePricingsOnChildren(Company company, Model model) throws AxelorException {
    // overridden in other modules
  }

  @Override
  public String updatePricingScaleLogs(List<StringBuilder> logsList, Model model) {
    // overridden in other modules
    return computePricingLogs(logsList);
  }

  @Override
  public List<String> getUnavailableModels() {
    return Arrays.asList(
        PricingRepository.PRICING_RESTRICT_PRICING,
        PricingRepository.PRICING_RESTRICT_PRICING_LINE,
        PricingRepository.PRICING_RESTRICT_PRICING_RULE,
        PricingRepository.PRICING_RESTRICT_MOVE,
        PricingRepository.PRICING_RESTRICT_MOVE_LINE,
        PricingRepository.PRICING_RESTRICT_INVOICE,
        PricingRepository.PRICING_RESTRICT_INVOICE_LINE);
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
