package com.axelor.apps.businessproject.service.projectgenerator;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ProjectGenerationServiceImpl implements ProjectGenerationService {

  protected final SaleOrderRepository saleOrderRepository;

  @Inject
  ProjectGenerationServiceImpl(SaleOrderRepository saleOrderRepository) {
    this.saleOrderRepository = saleOrderRepository;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void setIsFilledProject(SaleOrder saleOrder) {
    saleOrder.setIsFilledProject(true);
    saleOrderRepository.save(saleOrder);
  }
}
