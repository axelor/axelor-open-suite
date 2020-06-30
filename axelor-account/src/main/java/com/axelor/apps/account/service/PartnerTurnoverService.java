package com.axelor.apps.account.service;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PartnerTurnover;
import com.axelor.apps.base.db.Year;
import com.axelor.exception.AxelorException;
import java.math.BigDecimal;
import java.util.List;

public interface PartnerTurnoverService {

  public PartnerTurnover getPartnerTurnoverObject(
      List<PartnerTurnover> lstPartnerTurnover,
      Year yearCalendar,
      Partner partner,
      int intTypeOperation);

  public void calculCA(
      Partner partner,
      boolean isSupplier,
      Year yearFiscal,
      Year yearCivil,
      List<Partner> lstPartnerParent)
      throws AxelorException;

  public BigDecimal getCAPartner(
      Partner partner,
      Year yearCalendar,
      boolean isSupplier,
      boolean withSubdiaries,
      List<Partner> lstPartnerParent);

  public BigDecimal getCalculCACurrency(
      Partner partner, List<Partner> lstPartner, Year year, int intTypeSelect);

  public BigDecimal getCalculCACurrency(Partner partner, Year year, int intTypeSelect);

  public List<Partner> getListChild(List<Partner> lstPartner);
}
