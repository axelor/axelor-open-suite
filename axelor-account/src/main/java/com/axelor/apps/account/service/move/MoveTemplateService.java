/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.MoveTemplateLineRepository;
import com.axelor.apps.account.db.repo.MoveTemplateRepository;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MoveTemplateService {

  protected MoveService moveService;
  protected MoveRepository moveRepo;
  protected MoveLineService moveLineService;
  protected PartnerRepository partnerRepo;

  @Inject protected MoveTemplateRepository moveTemplateRepo;

  @Inject
  public MoveTemplateService(
      MoveService moveService,
      MoveRepository moveRepo,
      MoveLineService moveLineService,
      PartnerRepository partnerRepo) {
    this.moveService = moveService;
    this.moveRepo = moveRepo;
    this.moveLineService = moveLineService;
    this.partnerRepo = partnerRepo;
  }

  @Transactional
  public void validateMoveTemplateLine(MoveTemplate moveTemplate) {
    moveTemplate.setIsValid(true);

    for (MoveTemplateLine line : moveTemplate.getMoveTemplateLineList()) {
      line.setIsValid(true);
    }

    moveTemplateRepo.save(moveTemplate);
  }

  @SuppressWarnings("unchecked")
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public List<Long> generateMove(MoveTemplate moveTemplate, List<HashMap<String, Object>> dataList)
      throws AxelorException {
    List<Long> moveList = new ArrayList<Long>();
    BigDecimal hundred = new BigDecimal(100);
    for (HashMap<String, Object> data : dataList) {
      LocalDate moveDate = LocalDate.parse(data.get("date").toString(), DateTimeFormatter.ISO_DATE);
      boolean isDebit = false;
      Partner debitPartner = null;
      Partner creditPartner = null;
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
      Move move =
          moveService
              .getMoveCreateService()
              .createMove(
                  moveTemplate.getJournal(),
                  moveTemplate.getJournal().getCompany(),
                  null,
                  partner,
                  moveDate,
                  null,
                  MoveRepository.TECHNICAL_ORIGIN_TEMPLATE);
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

        MoveLine moveLine =
            moveLineService.createMoveLine(
                move,
                partner,
                moveTemplateLine.getAccount(),
                moveBalance
                    .multiply(moveTemplateLine.getPercentage())
                    .divide(hundred, RoundingMode.HALF_EVEN),
                isDebit,
                moveDate,
                moveDate,
                counter,
                moveTemplate.getFullName(),
                moveTemplateLine.getName());
        move.getMoveLineList().add(moveLine);

        counter++;
      }
      moveRepo.save(move);
      moveList.add(move.getId());
    }
    return moveList;
  }
}
