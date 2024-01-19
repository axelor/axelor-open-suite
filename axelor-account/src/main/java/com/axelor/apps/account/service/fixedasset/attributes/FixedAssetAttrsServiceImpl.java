package com.axelor.apps.account.service.fixedasset.attributes;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.account.service.CurrencyScaleServiceAccount;
import com.axelor.apps.base.db.Company;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class FixedAssetAttrsServiceImpl implements FixedAssetAttrsService {

  protected CurrencyScaleServiceAccount currencyScaleServiceAccount;

  @Inject
  public FixedAssetAttrsServiceImpl(CurrencyScaleServiceAccount currencyScaleServiceAccount) {
    this.currencyScaleServiceAccount = currencyScaleServiceAccount;
  }

  protected void addAttr(
      String field, String attr, Object value, Map<String, Map<String, Object>> attrsMap) {
    if (!attrsMap.containsKey(field)) {
      attrsMap.put(field, new HashMap<>());
    }

    attrsMap.get(field).put(attr, value);
  }

  @Override
  public void addDisposalAmountTitle(
      int disposalTypeSelect, Map<String, Map<String, Object>> attrsMap) {
    String title = "";

    if (disposalTypeSelect == FixedAssetRepository.DISPOSABLE_TYPE_SELECT_SCRAPPING) {
      title = I18n.get("Amount excluding taxes");
    } else if (disposalTypeSelect == FixedAssetRepository.DISPOSABLE_TYPE_SELECT_CESSION) {
      title = I18n.get("Sales price excluding taxes");
    }

    this.addAttr("disposalAmount", "title", title, attrsMap);
  }

  @Override
  public void addDisposalAmountReadonly(
      int disposalTypeSelect, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr(
        "disposalAmount",
        "readonly",
        disposalTypeSelect != FixedAssetRepository.DISPOSABLE_TYPE_SELECT_CESSION,
        attrsMap);
  }

  @Override
  public void addDisposalAmountScale(
      FixedAsset fixedAsset, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr(
        "disposalAmount",
        "scale",
        currencyScaleServiceAccount.getCompanyScale(fixedAsset),
        attrsMap);
  }

  @Override
  public void addSplitTypeSelectValue(BigDecimal qty, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr(
        "splitTypeSelect",
        "value",
        qty.compareTo(BigDecimal.ONE) == 0 ? FixedAssetRepository.SPLIT_TYPE_AMOUNT : 0,
        attrsMap);
  }

  @Override
  public void addSplitTypeSelectReadonly(
      BigDecimal qty, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr("splitTypeSelect", "readonly", qty.compareTo(BigDecimal.ONE) == 0, attrsMap);
  }

  @Override
  public void addGrossValueScale(Company company, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr(
        "grossValue", "scale", currencyScaleServiceAccount.getCompanyScale(company), attrsMap);
  }
}
