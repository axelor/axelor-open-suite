package com.axelor.apps.businessproject.service;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.exception.AxelorException;
import java.time.LocalDateTime;

public interface SaleOrderBusinessProjectService {

  Project generateProject(SaleOrder saleOrder) throws AxelorException;

  LocalDateTime getElementStartDate(SaleOrder saleOrder);
}
