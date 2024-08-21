package com.axelor.apps.sale.rest.dto;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestPostStructure;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class SaleOrderPostRequest extends RequestPostStructure {

  @NotNull
  @Min(0)
  private Long clientPartnerId;

  private Long companyId;
  private Long contactId;
  private Long currencyId;

  public Long getCompanyId() {
    return companyId;
  }

  public Long getContactId() {
    return contactId;
  }

  public Long getCurrencyId() {
    return currencyId;
  }

  private String inAti;

  public Long getClientPartnerId() {
    return clientPartnerId;
  }

  public void setCompanyId(Long companyId) {
    this.companyId = companyId;
  }

  public void setContactId(Long contactId) {
    this.contactId = contactId;
  }

  public void setCurrencyId(Long currencyId) {
    this.currencyId = currencyId;
  }

  public void setClientPartnerId(Long clientPartnerId) {
    this.clientPartnerId = clientPartnerId;
  }

  public String getInAti() {
    return inAti;
  }

  public void setInAti(String inAti) {
    this.inAti = inAti;
  }

  public Partner fetchClientPartner() {
    if (clientPartnerId == null || clientPartnerId == 0L) {
      return null;
    }
    return ObjectFinder.find(Partner.class, clientPartnerId, ObjectFinder.NO_VERSION);
  }

  public Partner fetchContact() {
    if (contactId == null || contactId == 0L) {
      return null;
    }
    return ObjectFinder.find(Partner.class, contactId, ObjectFinder.NO_VERSION);
  }

  public Company fetchCompany() {
    if (companyId == null || companyId == 0L) {
      return null;
    }
    return ObjectFinder.find(Company.class, companyId, ObjectFinder.NO_VERSION);
  }

  public Currency fetchCurrency() {
    if (currencyId == null || currencyId == 0L) {
      return null;
    }
    return ObjectFinder.find(Currency.class, currencyId, ObjectFinder.NO_VERSION);
  }
}
