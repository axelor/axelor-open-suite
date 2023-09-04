package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.sale.db.SaleOrder;
import java.time.LocalDateTime;

public interface SaleOrderVersionService {
  public void createNewVersion(SaleOrder saleOrder);

  public LocalDateTime getVersionDateTime(SaleOrder saleOrder, Integer versionNumber);

  public Integer getCorrectedVersionNumber(Integer versionNumber, Integer previousVersionNumber);

  public boolean recoverVersion(
      SaleOrder saleOrder, Integer versionNumber, boolean saveActualVersion);
}
