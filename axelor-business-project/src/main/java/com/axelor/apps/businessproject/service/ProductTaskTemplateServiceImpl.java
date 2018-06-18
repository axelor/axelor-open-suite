package com.axelor.apps.businessproject.service;

import com.axelor.apps.businessproject.db.ProductTaskTemplate;
import com.axelor.apps.businessproject.db.repo.ProductTaskTemplateRepository;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ProductTaskTemplateServiceImpl implements ProductTaskTemplateService {

  protected ProductTaskTemplateRepository repository;

  @Inject
  public ProductTaskTemplateServiceImpl(ProductTaskTemplateRepository repository) {
    this.repository = repository;
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void remove(ProductTaskTemplate productTaskTemplate) {
    productTaskTemplate = repository.find(productTaskTemplate.getId());
    if (productTaskTemplate != null) {
      repository.remove(productTaskTemplate);
    }
  }
}
