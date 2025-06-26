/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.service.fixedasset;

import com.axelor.apps.account.db.AssetDisposalReason;
import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetDerogatoryLine;
import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.FixedAssetLineRepository;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.fixedasset.factory.FixedAssetLineServiceFactory;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.apache.commons.collections.CollectionUtils;

public class FixedAssetDisposalServiceImpl implements FixedAssetDisposalService {

  protected FixedAssetRepository fixedAssetRepo;
  protected FixedAssetLineMoveService fixedAssetLineMoveService;
  protected FixedAssetService fixedAssetService;
  protected FixedAssetLineServiceFactory fixedAssetLineServiceFactory;
  protected FixedAssetLineGenerationService fixedAssetLineGenerationService;
  protected FixedAssetDerogatoryLineService fixedAssetDerogatoryLineService;
  protected FixedAssetLineService fixedAssetLineService;
  protected FixedAssetRecordService fixedAssetRecordService;

  @Inject
  public FixedAssetDisposalServiceImpl(
      FixedAssetRepository fixedAssetRepo,
      FixedAssetLineMoveService fixedAssetLineMoveService,
      FixedAssetService fixedAssetService,
      FixedAssetLineServiceFactory fixedAssetLineServiceFactory,
      FixedAssetLineGenerationService fixedAssetLineGenerationService,
      FixedAssetDerogatoryLineService fixedAssetDerogatoryLineService,
      FixedAssetLineService fixedAssetLineService,
      FixedAssetRecordService fixedAssetRecordService) {
    this.fixedAssetRepo = fixedAssetRepo;
    this.fixedAssetLineMoveService = fixedAssetLineMoveService;
    this.fixedAssetService = fixedAssetService;
    this.fixedAssetLineServiceFactory = fixedAssetLineServiceFactory;
    this.fixedAssetLineGenerationService = fixedAssetLineGenerationService;
    this.fixedAssetDerogatoryLineService = fixedAssetDerogatoryLineService;
    this.fixedAssetLineService = fixedAssetLineService;
    this.fixedAssetRecordService = fixedAssetRecordService;
  }

  @Override
  public List<FixedAsset> processDisposal(
      FixedAsset fixedAsset,
      List<FixedAsset> fixedAssetList,
      LocalDate disposalDate,
      int disposalQtySelect,
      BigDecimal disposalQty,
      Boolean generateSaleMove,
      Set<TaxLine> saleTaxLineSet,
      Integer disposalTypeSelect,
      BigDecimal disposalAmount,
      AssetDisposalReason assetDisposalReason,
      String comments)
      throws AxelorException {
    List<FixedAsset> createdFixedAssetList = new ArrayList<>();
    if (disposalTypeSelect == null || disposalDate == null) {
      return createdFixedAssetList;
    }

    if (Objects.equals(
        FixedAssetRepository.DISPOSABLE_TYPE_SELECT_ONGOING_CESSION, disposalTypeSelect)) {
      generateSaleMove = false;
    }

    if (fixedAsset != null) {
      FixedAsset createdFixedAsset =
          fullDisposal(
              fixedAsset,
              disposalDate,
              disposalQtySelect,
              disposalQty,
              generateSaleMove,
              saleTaxLineSet,
              disposalTypeSelect,
              disposalAmount,
              assetDisposalReason,
              comments);
      if (createdFixedAsset != null) {
        createdFixedAssetList.add(createdFixedAsset);
      }

      return createdFixedAssetList;
    }

    for (FixedAsset fixedAssetItem : fixedAssetList) {
      FixedAsset createdFixedAsset =
          fullDisposal(
              fixedAssetItem,
              disposalDate,
              FixedAssetRepository.DISPOSABLE_QTY_SELECT_TOTAL,
              fixedAssetItem.getQty(),
              generateSaleMove,
              saleTaxLineSet,
              disposalTypeSelect,
              fixedAssetRecordService.setDisposalAmount(fixedAssetItem, disposalTypeSelect),
              assetDisposalReason,
              comments);

      if (createdFixedAsset != null) {
        createdFixedAssetList.add(createdFixedAsset);
      }
    }

    return createdFixedAssetList;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public FixedAsset fullDisposal(
      FixedAsset fixedAsset,
      LocalDate disposalDate,
      int disposalQtySelect,
      BigDecimal disposalQty,
      Boolean generateSaleMove,
      Set<TaxLine> saleTaxLineSet,
      Integer disposalTypeSelect,
      BigDecimal disposalAmount,
      AssetDisposalReason assetDisposalReason,
      String comments)
      throws AxelorException {

    this.checkFixedAssetBeforeDisposal(
        fixedAsset, disposalDate, disposalQtySelect, disposalQty, generateSaleMove, saleTaxLineSet);

    int transferredReason =
        this.computeTransferredReason(
            disposalTypeSelect, disposalQtySelect, disposalQty, fixedAsset);

    FixedAsset createdFixedAsset =
        this.computeDisposal(
            fixedAsset,
            disposalDate,
            disposalQty,
            disposalAmount,
            transferredReason,
            assetDisposalReason,
            comments);
    if (generateSaleMove
        && CollectionUtils.isNotEmpty(saleTaxLineSet)
        && fixedAsset.getGrossValue().signum() >= 0) {
      if (createdFixedAsset != null) {
        fixedAssetLineMoveService.generateSaleMove(
            createdFixedAsset, saleTaxLineSet, disposalAmount, disposalDate);
      } else {
        fixedAssetLineMoveService.generateSaleMove(
            fixedAsset, saleTaxLineSet, disposalAmount, disposalDate);
      }
    }
    return createdFixedAsset;
  }

  protected void checkFixedAssetBeforeDisposal(
      FixedAsset fixedAsset,
      LocalDate disposalDate,
      int disposalQtySelect,
      BigDecimal disposalQty,
      Boolean generateSaleMove,
      Set<TaxLine> saleTaxLineSet)
      throws AxelorException {
    if (disposalDate != null
            && Stream.of(
                    fixedAsset.getFixedAssetLineList(),
                    fixedAsset.getFiscalFixedAssetLineList(),
                    fixedAsset.getIfrsFixedAssetLineList())
                .flatMap(Collection::stream)
                .anyMatch(
                    fixedAssetLine ->
                        fixedAssetLine.getStatusSelect() != FixedAssetLineRepository.STATUS_REALIZED
                            && fixedAssetLine.getDepreciationDate() != null
                            && fixedAssetLine.getDepreciationDate().isBefore(disposalDate))
        || fixedAsset.getFixedAssetDerogatoryLineList().stream()
            .anyMatch(
                fixedAssetDerogatoryLine ->
                    fixedAssetDerogatoryLine.getStatusSelect()
                            != FixedAssetLineRepository.STATUS_REALIZED
                        && fixedAssetDerogatoryLine.getDepreciationDate().isBefore(disposalDate))) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(
              AccountExceptionMessage
                  .IMMO_FIXED_ASSET_DEPRECIATIONS_NOT_ACCOUNTED_BEFORE_DISPOSAL_DATE),
          fixedAsset.getQty().toString());
    }

    if (disposalQtySelect == FixedAssetRepository.DISPOSABLE_QTY_SELECT_PARTIAL
        && disposalQty.compareTo(fixedAsset.getQty()) > 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.IMMO_FIXED_ASSET_DISPOSAL_QTY_GREATER_ORIGINAL),
          fixedAsset.getQty().toString());
    }
    if (generateSaleMove
        && fixedAsset.getGrossValue().signum() >= 0
        && CollectionUtils.isNotEmpty(saleTaxLineSet)
        && fixedAsset.getCompany().getAccountConfig().getCustomerSalesJournal() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(
              AccountExceptionMessage
                  .IMMO_FIXED_ASSET_DISPOSAL_COMPANY_ACCOUNT_CONFIG_CUSTOMER_SALES_JOURNAL_EMPTY));
    }
  }

  protected int computeTransferredReason(
      Integer disposalTypeSelect,
      Integer disposalQtySelect,
      BigDecimal disposalQty,
      FixedAsset fixedAsset) {
    boolean partialCession =
        disposalTypeSelect == FixedAssetRepository.DISPOSABLE_TYPE_SELECT_CESSION
            && disposalQtySelect == FixedAssetRepository.DISPOSABLE_QTY_SELECT_PARTIAL;
    if (partialCession && disposalQty.compareTo(fixedAsset.getQty()) < 0) {
      return FixedAssetRepository.TRANSFERED_REASON_PARTIAL_CESSION;
    } else if (disposalTypeSelect == FixedAssetRepository.DISPOSABLE_TYPE_SELECT_CESSION
        || (partialCession && disposalQty.compareTo(fixedAsset.getQty()) == 0)) {
      return FixedAssetRepository.TRANSFERED_REASON_CESSION;
    } else if (disposalTypeSelect == FixedAssetRepository.DISPOSABLE_TYPE_SELECT_ONGOING_CESSION) {
      return FixedAssetRepository.TRANSFERED_REASON_ONGOING_CESSION;
    }
    return FixedAssetRepository.TRANSFERED_REASON_SCRAPPING;
  }

  @Transactional(rollbackOn = {Exception.class})
  protected FixedAsset computeDisposal(
      FixedAsset fixedAsset,
      LocalDate disposalDate,
      BigDecimal disposalQty,
      BigDecimal disposalAmount,
      int transferredReason,
      AssetDisposalReason assetDisposalReason,
      String comments)
      throws AxelorException {
    FixedAsset createdFixedAsset = null;
    FixedAssetLine depreciationFixedAssetLine = null;
    if (transferredReason == FixedAssetRepository.TRANSFERED_REASON_PARTIAL_CESSION) {
      List<FixedAsset> createdFixedAssetList =
          fixedAssetService.splitFixedAsset(
              fixedAsset,
              FixedAssetRepository.SPLIT_TYPE_QUANTITY,
              disposalQty,
              disposalDate,
              comments);
      if (!ObjectUtils.isEmpty(createdFixedAssetList) && createdFixedAssetList.size() == 1) {
        createdFixedAsset = createdFixedAssetList.get(0);
        depreciationFixedAssetLine =
            computeCession(
                createdFixedAsset,
                disposalDate,
                disposalAmount,
                transferredReason,
                createdFixedAsset.getComments());
        filterListsByDates(createdFixedAsset, disposalDate);
      }
    } else if (transferredReason == FixedAssetRepository.TRANSFERED_REASON_CESSION
        || transferredReason == FixedAssetRepository.TRANSFERED_REASON_SCRAPPING) {
      depreciationFixedAssetLine =
          computeCession(fixedAsset, disposalDate, disposalAmount, transferredReason, comments);
      filterListsByDates(fixedAsset, disposalDate);
    } else {
      List<Integer> typeSelectList = fixedAssetLineServiceFactory.getTypeSelectList(fixedAsset);
      for (Integer typeSelect : typeSelectList) {
        depreciationFixedAssetLine =
            disposal(disposalDate, disposalAmount, fixedAsset, transferredReason, typeSelect);
      }
      if (disposalAmount.compareTo(BigDecimal.ZERO) != 0) {
        fixedAssetLineMoveService.generateDisposalMove(
            fixedAsset,
            depreciationFixedAssetLine,
            transferredReason,
            Optional.ofNullable(depreciationFixedAssetLine)
                .map(FixedAssetLine::getDepreciationDate)
                .orElse(null));
      } else if (disposalAmount.compareTo(fixedAsset.getResidualValue()) == 0) {
        fixedAssetLineMoveService.generateDisposalMove(
            fixedAsset, null, transferredReason, disposalDate);
      }
      filterListsByDates(fixedAsset, disposalDate);
    }
    fixedAssetLineGenerationService.generateAndComputeFixedAssetDerogatoryLines(fixedAsset);
    fixedAssetLineMoveService.realize(depreciationFixedAssetLine, false, true, true);
    fixedAsset.setAssetDisposalReason(assetDisposalReason);
    fixedAssetRepo.save(fixedAsset);
    if (createdFixedAsset != null) {
      createdFixedAsset.setAssetDisposalReason(assetDisposalReason);
      return fixedAssetRepo.save(createdFixedAsset);
    }
    return null;
  }

  protected FixedAssetLine computeCession(
      FixedAsset fixedAsset,
      LocalDate disposalDate,
      BigDecimal disposalAmount,
      int transferredReason,
      String comments)
      throws AxelorException {

    List<Integer> typeSelectList = fixedAssetLineServiceFactory.getTypeSelectList(fixedAsset);
    FixedAssetLine correspondingFixedAssetLine = null;
    for (Integer typeSelect : typeSelectList) {
      correspondingFixedAssetLine =
          cession(
              fixedAsset, disposalDate, disposalAmount, transferredReason, comments, typeSelect);
    }

    String depreciationPlanSelect = fixedAsset.getDepreciationPlanSelect();
    if (correspondingFixedAssetLine != null
        && StringUtils.notEmpty(depreciationPlanSelect)
        && depreciationPlanSelect.contains(FixedAssetRepository.DEPRECIATION_PLAN_DEROGATION)) {
      generateDerogatoryCessionMove(fixedAsset, disposalDate);
    }
    fixedAssetLineMoveService.generateDisposalMove(
        fixedAsset, correspondingFixedAssetLine, transferredReason, disposalDate);
    return correspondingFixedAssetLine;
  }

  protected FixedAssetLine cession(
      FixedAsset fixedAsset,
      LocalDate disposalDate,
      BigDecimal disposalAmount,
      int transferredReason,
      String comments,
      int fixedAssetLineTypeSelect)
      throws AxelorException {

    FixedAssetLineService fixedAssetLineService =
        fixedAssetLineServiceFactory.getFixedAssetService(fixedAssetLineTypeSelect);

    checkCessionDates(fixedAsset, disposalDate, fixedAssetLineService);

    FixedAssetLine correspondingFixedAssetLine =
        fixedAssetLineService.generateProrataDepreciationLine(fixedAsset, disposalDate);

    setDisposalFields(fixedAsset, disposalDate, disposalAmount, transferredReason);
    fixedAsset.setComments(comments);
    return correspondingFixedAssetLine;
  }

  protected void checkCessionDates(
      FixedAsset fixedAsset, LocalDate disposalDate, FixedAssetLineService fixedAssetLineService)
      throws AxelorException {
    LocalDate firstServiceDate =
        fixedAsset.getFirstServiceDate() == null
            ? fixedAsset.getAcquisitionDate()
            : fixedAsset.getFirstServiceDate();
    if (disposalDate == null || firstServiceDate == null) {
      return;
    }
    if (disposalDate.isBefore(firstServiceDate)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.IMMO_FIXED_ASSET_CESSION_BEFORE_FIRST_SERVICE_DATE));
    }
    Optional<FixedAssetLine> fixedAssetLine =
        fixedAssetLineService.findOldestFixedAssetLine(
            fixedAsset, FixedAssetLineRepository.STATUS_REALIZED, 0);
    if (fixedAssetLine.isPresent()
        && fixedAssetLine.get().getDepreciationDate() != null
        && !disposalDate.isAfter(fixedAssetLine.get().getDepreciationDate())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.FIXED_ASSET_DISPOSAL_DATE_YEAR_ALREADY_ACCOUNTED));
    }
  }

  protected void generateDerogatoryCessionMove(FixedAsset fixedAsset, LocalDate disposalDate)
      throws AxelorException {

    List<FixedAssetDerogatoryLine> fixedAssetDerogatoryLineList =
        fixedAsset.getFixedAssetDerogatoryLineList();
    fixedAssetDerogatoryLineList.sort(
        (line1, line2) -> line2.getDepreciationDate().compareTo(line1.getDepreciationDate()));
    FixedAssetDerogatoryLine lastRealizedDerogatoryLine =
        fixedAssetDerogatoryLineList.stream()
            .filter(line -> line.getStatusSelect() == FixedAssetLineRepository.STATUS_REALIZED)
            .sorted(
                Comparator.comparing(
                    FixedAssetDerogatoryLine::getDepreciationDate,
                    Comparator.nullsFirst(Comparator.reverseOrder())))
            .findFirst()
            .orElse(null);
    fixedAssetDerogatoryLineList.sort(
        (line1, line2) -> line1.getDepreciationDate().compareTo(line2.getDepreciationDate()));
    FixedAssetDerogatoryLine firstPlannedDerogatoryLine =
        fixedAssetDerogatoryLineList.stream()
            .filter(line -> line.getStatusSelect() == FixedAssetLineRepository.STATUS_PLANNED)
            .sorted(
                Comparator.comparing(
                    FixedAssetDerogatoryLine::getDepreciationDate,
                    Comparator.nullsFirst(Comparator.naturalOrder())))
            .findFirst()
            .orElse(null);
    if (firstPlannedDerogatoryLine == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(AccountExceptionMessage.IMMO_FIXED_ASSET_MISSING_DEROGATORY_LINE));
    }
    fixedAssetDerogatoryLineService.generateDerogatoryCessionMove(
        firstPlannedDerogatoryLine, lastRealizedDerogatoryLine, disposalDate);
  }

  protected FixedAsset filterListsByDates(FixedAsset fixedAsset, LocalDate date) {
    Objects.requireNonNull(fixedAsset);
    fixedAssetLineService.filterListByDate(fixedAsset.getFixedAssetLineList(), date);
    fixedAssetLineService.filterListByDate(fixedAsset.getFiscalFixedAssetLineList(), date);
    fixedAssetLineService.filterListByDate(fixedAsset.getIfrsFixedAssetLineList(), date);
    fixedAssetDerogatoryLineService.filterListByDate(
        fixedAsset.getFixedAssetDerogatoryLineList(), date);
    return fixedAsset;
  }

  @Transactional(rollbackOn = {Exception.class})
  protected FixedAssetLine disposal(
      LocalDate disposalDate,
      BigDecimal disposalAmount,
      FixedAsset fixedAsset,
      int transferredReason,
      int fixedAssetLineTypeSelect)
      throws AxelorException {
    FixedAssetLineService fixedAssetLineService =
        fixedAssetLineServiceFactory.getFixedAssetService(fixedAssetLineTypeSelect);

    FixedAssetLine depreciationFixedAssetLine = null;
    if (disposalAmount.compareTo(BigDecimal.ZERO) != 0) {

      depreciationFixedAssetLine =
          fixedAssetLineService.generateProrataDepreciationLine(fixedAsset, disposalDate);

      if (depreciationFixedAssetLine != null) {
        fixedAssetLineMoveService.realize(depreciationFixedAssetLine, false, true, true);
        fixedAssetLineMoveService.generateDisposalMove(
            fixedAsset,
            depreciationFixedAssetLine,
            transferredReason,
            depreciationFixedAssetLine.getDepreciationDate());
      }
    }

    setDisposalFields(fixedAsset, disposalDate, disposalAmount, transferredReason);
    fixedAssetRepo.save(fixedAsset);
    return depreciationFixedAssetLine;
  }

  protected void setDisposalFields(
      FixedAsset fixedAsset,
      LocalDate disposalDate,
      BigDecimal disposalAmount,
      int transferredReason) {
    fixedAsset.setDisposalDate(disposalDate);
    fixedAsset.setDisposalValue(disposalAmount);
    fixedAsset.setTransferredReasonSelect(transferredReason);
    fixedAsset.setStatusSelect(FixedAssetRepository.STATUS_TRANSFERRED);
  }
}
