package com.axelor.apps.account.service.fixedasset;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FixedAssetLineServiceImpl implements FixedAssetLineService {

  protected FixedAssetRepository fixedAssetRepository;

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject
  public FixedAssetLineServiceImpl(FixedAssetRepository fixedAssetRepository) {
    this.fixedAssetRepository = fixedAssetRepository;
  }
  /**
   * {@inheritDoc}
   *
   * @throws NullPointerException if moveLine is null
   */
  @Override
  public FixedAsset generateFixedAsset(MoveLine moveLine) {
    log.debug("Starting generation of fixed asset for move line :" + moveLine);
    Objects.requireNonNull(moveLine);

    FixedAsset fixedAsset = new FixedAsset();
    fixedAsset.setStatusSelect(FixedAssetRepository.STATUS_DRAFT);
    fixedAsset.setName(moveLine.getDescription());
    if (moveLine.getMove() != null) {
      fixedAsset.setCompany(moveLine.getMove().getCompany());
      fixedAsset.setJournal(moveLine.getMove().getJournal());
    }
    fixedAsset.setFixedAssetCategory(moveLine.getFixedAssetCategory());
    fixedAsset.setPartner(moveLine.getPartner());
    fixedAsset.setPurchaseAccount(moveLine.getAccount());
    if (moveLine.getFixedAssetCategory() != null)
      fixedAsset.setAnalyticDistributionTemplate(
          moveLine.getFixedAssetCategory().getAnalyticDistributionTemplate());
    LocalDate acquisitionDate =
        moveLine.getOriginDate() != null ? moveLine.getOriginDate() : moveLine.getDate();
    fixedAsset.setAcquisitionDate(acquisitionDate);
    fixedAsset.setFirstServiceDate(acquisitionDate);
    fixedAsset.setFirstDepreciationDate(acquisitionDate);
    log.debug("Generated fixed asset : " + fixedAsset);
    return fixedAsset;
  }

  @Transactional
  @Override
  public FixedAsset generateAndSaveFixedAsset(MoveLine moveLine) {

    return fixedAssetRepository.save(generateFixedAsset(moveLine));
  }
}
