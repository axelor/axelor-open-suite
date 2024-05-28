package com.axelor.apps.businessproject.service.projectgenerator;

import com.axelor.apps.sale.db.SaleOrder;

public interface ProjectGenerationService {
  void setIsFilledProject(SaleOrder saleOrder);
}
