package com.axelor.apps.sale.rest.dto;

import com.axelor.apps.base.db.Partner;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestPostStructure;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class SaleOrderPostRequest extends RequestPostStructure {

  @NotNull
  @Min(0)
  private Long clientPartnerId;

  public Long getClientPartnerId() {
    return clientPartnerId;
  }

  public void setClientPartner(Long clientPartnerId) {
    this.clientPartnerId = clientPartnerId;
  }

  public Partner fetchClientPartner() {
    if (clientPartnerId == null || clientPartnerId == 0L) {
      return null;
    }
    return ObjectFinder.find(Partner.class, clientPartnerId, ObjectFinder.NO_VERSION);
  }
}
