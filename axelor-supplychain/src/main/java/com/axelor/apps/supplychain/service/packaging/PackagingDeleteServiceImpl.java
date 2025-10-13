package com.axelor.apps.supplychain.service.packaging;

import com.axelor.apps.supplychain.db.Packaging;
import com.axelor.apps.supplychain.db.repo.PackagingRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class PackagingDeleteServiceImpl implements PackagingDeleteService {

  protected final PackagingRepository packagingRepository;

  @Inject
  public PackagingDeleteServiceImpl(PackagingRepository packagingRepository) {
    this.packagingRepository = packagingRepository;
  }

  @Transactional
  @Override
  public void deletePackaging(Packaging packaging) {
    packagingRepository.remove(packaging);
  }
}
