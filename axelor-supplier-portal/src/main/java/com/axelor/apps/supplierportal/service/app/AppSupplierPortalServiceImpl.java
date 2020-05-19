package com.axelor.apps.supplierportal.service.app;

import com.axelor.apps.base.db.AppSupplierPortal;
import com.axelor.apps.base.db.repo.AppSupplierPortalRepository;
import com.google.inject.Inject;

public class AppSupplierPortalServiceImpl implements AppSupplierPortalService {

  private AppSupplierPortalRepository appSupplierPortalRepo;

  @Inject
  public AppSupplierPortalServiceImpl(AppSupplierPortalRepository appSupplierPortalRepo) {
    this.appSupplierPortalRepo = appSupplierPortalRepo;
  }

  @Override
  public AppSupplierPortal getAppSupplierPortal() {
    return appSupplierPortalRepo.all().fetchOne();
  }
}
