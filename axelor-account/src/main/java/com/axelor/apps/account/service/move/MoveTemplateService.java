/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.MoveTemplate;
import com.axelor.apps.account.db.MoveTemplateLine;
import com.axelor.apps.account.db.MoveTemplateType;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.MoveTemplateLineRepository;
import com.axelor.apps.account.db.repo.MoveTemplateRepository;
import com.axelor.apps.account.db.repo.MoveTemplateTypeRepository;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineService;
import com.axelor.apps.account.service.moveline.MoveLineComputeAnalyticService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveTemplateService {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected MoveCreateService moveCreateService;
  protected MoveValidateService moveValidateService;
  protected MoveRepository moveRepo;
  protected MoveLineCreateService moveLineCreateService;
  protected MoveLineComputeAnalyticService moveLineComputeAnalyticService;
  protected PartnerRepository partnerRepo;
  protected AnalyticMoveLineService analyticMoveLineService;
  protected TaxService taxService;

  @Inject protected MoveTemplateRepository moveTemplateRepo;

  @Inject
  public MoveTemplateService(
      MoveCreateService moveCreateService,
      MoveValidateService moveValidateService,
      MoveRepository moveRepo,
      MoveLineCreateService moveLineCreateService,
      PartnerRepository partnerRepo,
      AnalyticMoveLineService analyticMoveLineService,
      TaxService taxService,
      MoveLineComputeAnalyticService moveLineComputeAnalyticService) {
    this.moveCreateService = moveCreateService;
    this.moveValidateService = moveValidateService;
    this.moveRepo = moveRepo;
    this.moveLineCreateService = moveLineCreateService;
    this.partnerRepo = partnerRepo;
    this.analyticMoveLineService = analyticMoveLineService;
    this.taxService = taxService;
    this.moveLineComputeAnalyticService = moveLineComputeAnalyticService;
  }

  @Transactional
  public void validateMoveTemplateLine(MoveTemplate moveTemplate) {
    moveTemplate.setIsValid(true);

    for (MoveTemplateLine line : moveTemplate.getMoveTemplateLineList()) {
      line.setIsValid(true);
    }

    moveTemplateRepo.save(moveTemplate);
  }

  @Transactional(rollbackOn = {Exception.class})
  public List<Long> generateMove(
      MoveTemplateType moveTemplateType,
      MoveTemplate moveTemplate,
      List<HashMap<String, Object>> dataList,
      LocalDate date,
      List<HashMap<String, Object>> moveTemplateList)
      throws AxelorException {

    if (moveTemplateType.getTypeSelect() == MoveTemplateTypeRepository.TYPE_PERCENTAGE) {
      return this.generateMove(moveTemplate, dataList);
    } else if (moveTemplateType.getTypeSelect() == MoveTemplateTypeRepository.TYPE_AMOUNT) {
      return this.generateMove(date, moveTemplateList);
    }
    return new ArrayList<>();
  }

  @Transactional(rollbackOn = {Exception.class})
  public List<Long> generateMove(MoveTemplate moveTemplate, List<HashMap<String, Object>> dataList)
      throws AxelorException {
    List<Long> moveList = new ArrayList<>();
    BigDecimal hundred = new BigDecimal(100);
    for (HashMap<String, Object> data : dataList) {
      LocalDate moveDate = LocalDate.parse(data.get("date").toString(), DateTimeFormatter.ISO_DATE);
      boolean isDebit = false;
      Partner debitPartner = null;
      Partner creditPartner = null;
      String origin = moveTemplate.getCode();
      BigDecimal moveBalance = new BigDecimal(data.get("moveBalance").toString());
      Partner partner = null;
      if (data.get("debitPartner") != null) {
        debitPartner =
            partnerRepo.find(
                Long.parseLong(
                    ((HashMap<String, Object>) data.get("debitPartner")).get("id").toString()));
        partner = debitPartner;
      }
      if (data.get("creditPartner") != null) {
        creditPartner =
            partnerRepo.find(
                Long.parseLong(
                    ((HashMap<String, Object>) data.get("creditPartner")).get("id").toString()));
        partner = creditPartner;
      }
      if (moveTemplate.getJournal().getCompany() != null) {
        Move move =
            moveCreateService.createMove(
                moveTemplate.getJournal(),
                moveTemplate.getJournal().getCompany(),
                null,
                partner,
                moveDate,
                moveDate,
                null,
                partner != null ? partner.getFiscalPosition() : null,
                MoveRepository.TECHNICAL_ORIGIN_TEMPLATE,
                0,
                origin,
                null);

        int counter = 1;

        for (MoveTemplateLine moveTemplateLine : moveTemplate.getMoveTemplateLineList()) {
          partner = null;
          if (moveTemplateLine.getDebitCreditSelect().equals(MoveTemplateLineRepository.DEBIT)) {
            isDebit = true;
            if (moveTemplateLine.getHasPartnerToDebit()) {
              partner = debitPartner;
            }
          } else if (moveTemplateLine
              .getDebitCreditSelect()
              .equals(MoveTemplateLineRepository.CREDIT)) {
            isDebit = false;
            if (moveTemplateLine.getHasPartnerToCredit()) {
              partner = creditPartner;
            }
          }

          BigDecimal amount =
              moveBalance
                  .multiply(moveTemplateLine.getPercentage())
                  .divide(hundred, RoundingMode.HALF_UP);

          MoveLine moveLine =
              moveLineCreateService.createMoveLine(
                  move,
                  partner,
                  moveTemplateLine.getAccount(),
                  amount,
                  isDebit,
                  moveDate,
                  moveDate,
                  counter,
                  origin,
                  moveTemplateLine.getName());
          move.getMoveLineList().add(moveLine);

          Tax tax = moveTemplateLine.getTax();

          if (tax != null) {
            TaxLine taxLine = taxService.getTaxLine(tax, moveDate);
            if (taxLine != null) {
              moveLine.setTaxLine(taxLine);
              moveLine.setTaxRate(taxLine.getValue());
              moveLine.setTaxCode(tax.getCode());
            }
          }

          moveLine.setAnalyticDistributionTemplate(
              moveTemplateLine.getAnalyticDistributionTemplate());
          moveLineComputeAnalyticService.generateAnalyticMoveLines(moveLine);

          counter++;
        }

        if (moveTemplate.getAutomaticallyValidate()) {
          moveValidateService.validate(move);
        }

        moveRepo.save(move);
        moveList.add(move.getId());
      }
    }
    return moveList;
  }

  @Transactional(rollbackOn = {Exception.class})
  public List<Long> generateMove(LocalDate moveDate, List<HashMap<String, Object>> moveTemplateList)
      throws AxelorException {
    List<Long> moveList = new ArrayList<>();

    for (HashMap<String, Object> moveTemplateMap : moveTemplateList) {

      MoveTemplate moveTemplate =
          moveTemplateRepo.find(Long.valueOf((Integer) moveTemplateMap.get("id")));

      if (moveTemplate.getJournal().getCompany() != null) {
        Move move =
            moveCreateService.createMove(
                moveTemplate.getJournal(),
                moveTemplate.getJournal().getCompany(),
                null,
                null,
                moveDate,
                moveDate,
                null,
                null,
                MoveRepository.TECHNICAL_ORIGIN_TEMPLATE,
                0,
                moveTemplate.getFullName(),
                null);
        int counter = 1;

        for (MoveTemplateLine moveTemplateLine : moveTemplate.getMoveTemplateLineList()) {

          BigDecimal amount = moveTemplateLine.getDebit().add(moveTemplateLine.getCredit());

          MoveLine moveLine =
              moveLineCreateService.createMoveLine(
                  move,
                  moveTemplateLine.getPartner(),
                  moveTemplateLine.getAccount(),
                  amount,
                  moveTemplateLine.getDebit().compareTo(BigDecimal.ZERO) > 0,
                  moveDate,
                  moveDate,
                  counter,
                  moveTemplate.getFullName(),
                  moveTemplateLine.getName());
          move.getMoveLineList().add(moveLine);

          Tax tax = moveTemplateLine.getTax();

          if (tax != null) {
            TaxLine taxLine = taxService.getTaxLine(tax, moveDate);
            if (taxLine != null) {
              moveLine.setTaxLine(taxLine);
              moveLine.setTaxRate(taxLine.getValue());
              moveLine.setTaxCode(tax.getCode());
            }
          }

          moveLine.setAnalyticDistributionTemplate(
              moveTemplateLine.getAnalyticDistributionTemplate());
          moveLineComputeAnalyticService.generateAnalyticMoveLines(moveLine);

          counter++;
        }

        if (moveTemplate.getAutomaticallyValidate()) {
          moveValidateService.validate(move);
        }

        moveRepo.save(move);
        moveList.add(move.getId());
      }
    }
    return moveList;
  }

  public boolean checkValidity(MoveTemplate moveTemplate) {

    MoveTemplateType moveTemplateType = moveTemplate.getMoveTemplateType();

    if (moveTemplateType == null) {
      return false;
    }

    if (moveTemplateType.getTypeSelect() == MoveTemplateTypeRepository.TYPE_PERCENTAGE) {
      return this.checkValidityInPercentage(moveTemplate);
    } else if (moveTemplateType.getTypeSelect() == MoveTemplateTypeRepository.TYPE_AMOUNT) {
      return this.checkValidityInAmount(moveTemplate);
    } else {
      return false;
    }
  }

  protected boolean checkValidityInPercentage(MoveTemplate moveTemplate) {
    BigDecimal debitPercent = BigDecimal.ZERO;
    BigDecimal creditPercent = BigDecimal.ZERO;
    for (MoveTemplateLine line : moveTemplate.getMoveTemplateLineList()) {
      LOG.debug("Adding percent: {}", line.getPercentage());
      if (MoveTemplateLineRepository.DEBIT.equals(line.getDebitCreditSelect())) {
        debitPercent = debitPercent.add(line.getPercentage());
      } else {
        creditPercent = creditPercent.add(line.getPercentage());
      }
    }

    LOG.debug("Debit percent: {}, Credit percent: {}", debitPercent, creditPercent);
    if (debitPercent.compareTo(BigDecimal.ZERO) != 0
        && creditPercent.compareTo(BigDecimal.ZERO) != 0
        && debitPercent.compareTo(creditPercent) == 0) {
      this.validateMoveTemplateLine(moveTemplate);
      return true;
    } else {
      return false;
    }
  }

  protected boolean checkValidityInAmount(MoveTemplate moveTemplate) {
    BigDecimal debit = BigDecimal.ZERO;
    BigDecimal credit = BigDecimal.ZERO;
    for (MoveTemplateLine line : moveTemplate.getMoveTemplateLineList()) {
      debit = debit.add(line.getDebit());
      credit = credit.add(line.getCredit());
    }

    LOG.debug("Debit : {}, Credit : {}", debit, credit);
    if (debit.compareTo(BigDecimal.ZERO) != 0
        && credit.compareTo(BigDecimal.ZERO) != 0
        && debit.compareTo(credit) == 0) {
      this.validateMoveTemplateLine(moveTemplate);
      return true;
    } else {
      return false;
    }
  }

  public Map<String, Object> computeTotals(MoveTemplate moveTemplate) {
    Map<String, Object> values = new HashMap<>();

    if (moveTemplate.getMoveTemplateLineList() == null
        || moveTemplate.getMoveTemplateLineList().isEmpty()) {
      return values;
    }
    values.put("$totalLines", moveTemplate.getMoveTemplateLineList().size());

    BigDecimal totalDebit =
        moveTemplate.getMoveTemplateLineList().stream()
            .map(MoveTemplateLine::getDebit)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    values.put("$totalDebit", totalDebit);

    BigDecimal totalCredit =
        moveTemplate.getMoveTemplateLineList().stream()
            .map(MoveTemplateLine::getCredit)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    values.put("$totalCredit", totalCredit);

    BigDecimal difference = totalDebit.subtract(totalCredit);
    values.put("$difference", difference);

    return values;
  }
}
