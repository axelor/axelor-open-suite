package com.axelor.apps.supplierportal.service;

import com.axelor.auth.db.User;
import java.util.Map;

public interface SupplierViewService {
  public User getSupplierUser();

  public Map<String, Object> updateSupplierViewIndicators();

  public String getPurchaseOrdersOfSupplier(User user);

  public String getPurchaseQuotationsInProgressOfSupplier(User user);

  public String getLastPurchaseOrderOfSupplier(User user);

  public String getLastDeliveryOfSupplier(User user);

  public String getNextDeliveryOfSupplier(User user);

  public String getDeliveriesToPrepareOfSupplier(User user);

  public String getAwaitingInvoicesOfSupplier(User user);

  public String getTotalRemainingOfSupplier(User user);
}
