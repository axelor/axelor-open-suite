package com.axelor.apps.supplychain.service.packaging;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.stock.db.LogisticalForm;
import com.axelor.apps.supplychain.db.Packaging;
import com.axelor.apps.supplychain.db.repo.PackagingRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class PackagingCreateServiceImpl implements PackagingCreateService {

  protected final PackagingRepository packagingRepository;

  @Inject
  public PackagingCreateServiceImpl(PackagingRepository packagingRepository) {
    this.packagingRepository = packagingRepository;
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public Packaging createPackaging(
      LogisticalForm logisticalForm, Packaging parentPackaging, Product packageUsed)
      throws AxelorException {
    Packaging packaging = new Packaging();
    packaging.setLogisticalForm(logisticalForm);
    packaging.setParentPackaging(parentPackaging);
    updatePackageUsed(packageUsed, packaging);

    return packagingRepository.save(packaging);
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void updatePackageUsed(Product packageUsed, Packaging packaging) throws AxelorException {
    if (packageUsed != null) {
      if (!packageUsed.getIsPackaging()) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY, I18n.get("Product must be a packaging."));
      }
      packaging.setPackageUsed(packageUsed);
      packaging.setPackagingLevelSelect(packageUsed.getPackagingLevelSelect());
    }
    packagingRepository.save(packaging);
  }
}
