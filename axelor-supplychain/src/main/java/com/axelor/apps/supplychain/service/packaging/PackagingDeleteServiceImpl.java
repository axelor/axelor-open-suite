package com.axelor.apps.supplychain.service.packaging;

import com.axelor.apps.supplychain.db.Packaging;
import com.axelor.apps.supplychain.db.PackagingLine;
import com.axelor.apps.supplychain.db.repo.PackagingLineRepository;
import com.axelor.apps.supplychain.db.repo.PackagingRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class PackagingDeleteServiceImpl implements PackagingDeleteService {

  protected final PackagingRepository packagingRepository;
  protected final PackagingLineRepository packagingLineRepository;

  @Inject
  public PackagingDeleteServiceImpl(
      PackagingRepository packagingRepository, PackagingLineRepository packagingLineRepository) {
    this.packagingRepository = packagingRepository;
    this.packagingLineRepository = packagingLineRepository;
  }

  @Transactional
  @Override
  public void deletePackaging(Packaging packaging) {
    List<PackagingLine> packagingLineList = packaging.getPackagingLineList();
    if (CollectionUtils.isNotEmpty(packagingLineList)) {
      for (PackagingLine packagingLine : packagingLineList) {
        packagingLineRepository.remove(packagingLine);
      }
    }
    packagingRepository.remove(packaging);
  }
}
