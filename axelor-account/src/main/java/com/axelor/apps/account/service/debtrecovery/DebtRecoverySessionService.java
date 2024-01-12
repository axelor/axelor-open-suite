/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.debtrecovery;

import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.DebtRecovery;
import com.axelor.apps.account.db.DebtRecoveryConfigLine;
import com.axelor.apps.account.db.DebtRecoveryMethod;
import com.axelor.apps.account.db.DebtRecoveryMethodLine;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.PaymentScheduleLine;
import com.axelor.apps.account.db.repo.DebtRecoveryRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DebtRecoverySessionService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected DebtRecoveryRepository debtRecoveryRepo;

  protected AppAccountService appAccountService;

  @Inject
  public DebtRecoverySessionService(
      DebtRecoveryRepository debtRecoveryRepo, AppAccountService appAccountService) {

    this.appAccountService = appAccountService;
    this.debtRecoveryRepo = debtRecoveryRepo;
  }

  /**
   * Fonction permettant de récupérer une méthode de relance en fonction de la categorie du tiers et
   * d'une société
   *
   * @param debtRecovery Une relance
   * @return
   */
  public DebtRecoveryMethod getDebtRecoveryMethod(DebtRecovery debtRecovery) {

    AccountingSituation accountingSituation =
        debtRecovery.getTradingName() == null
            ? debtRecovery.getAccountingSituation()
            : debtRecovery.getTradingNameAccountingSituation();
    Company company = accountingSituation.getCompany();
    Partner partner = accountingSituation.getPartner();
    List<DebtRecoveryConfigLine> debtRecoveryConfigLines =
        company.getAccountConfig().getDebtRecoveryConfigLineList();

    for (DebtRecoveryConfigLine debtRecoveryConfigLine : debtRecoveryConfigLines) {
      if (debtRecoveryConfigLine.getPartnerCategory().equals(partner.getPartnerCategory())) {

        log.debug("reminder method decided ");
        return debtRecoveryConfigLine.getDebtRecoveryMethod();
      }
    }

    log.debug("reminder method not decided ");

    return null;
  }

  /**
   * Session de relance
   *
   * @param debtRecovery Une relance
   * @throws AxelorException
   */
  public DebtRecovery debtRecoverySession(DebtRecovery debtRecovery) throws AxelorException {

    log.debug("Begin debtRecoverySession service...");

    LocalDate referenceDate = debtRecovery.getReferenceDate();
    BigDecimal balanceDueDebtRecovery = debtRecovery.getBalanceDueDebtRecovery();

    int debtRecoveryLevel = -1;
    if (debtRecovery.getDebtRecoveryMethodLine() != null) {
      debtRecoveryLevel = debtRecovery.getDebtRecoveryMethodLine().getSequence();
    }

    int theoricalDebtRecoveryLevel;

    int levelMax = this.getMaxLevel(debtRecovery);

    // Test inutile... à verifier
    if ((appAccountService.getTodayDate(debtRecovery.getCompany()).isAfter(referenceDate)
            || appAccountService.getTodayDate(debtRecovery.getCompany()).isEqual(referenceDate))
        && balanceDueDebtRecovery.compareTo(BigDecimal.ZERO) > 0) {
      log.debug(
          "Current date {} is after reference date {} and balance due debt recovery is positive",
          appAccountService.getTodayDate(debtRecovery.getCompany()),
          referenceDate);
      // Pour les client à haut risque vital, on passe directement du niveau de relance 2 au niveau
      // de relance 4
      if (debtRecoveryLevel < levelMax) {
        log.debug(
            "This is not a high risk vital customer, debt recovery level {} is under the level max {}",
            debtRecoveryLevel,
            levelMax);
        theoricalDebtRecoveryLevel = debtRecoveryLevel + 1;
      } else {
        log.debug(
            "This is a high risk vital customer, debt recovery level {} equal to the level max {}",
            debtRecoveryLevel,
            levelMax);
        theoricalDebtRecoveryLevel = levelMax;
      }

      DebtRecoveryMethodLine debtRecoveryMethodLine =
          this.getDebtRecoveryMethodLine(debtRecovery, theoricalDebtRecoveryLevel);

      setDebtRecoveryMethodLine(debtRecovery, debtRecoveryMethodLine);

    } else {
      log.debug("We reset");
      this.debtRecoveryInitialization(debtRecovery);
    }
    log.debug("End debtRecoverySession service");
    return debtRecovery;
  }

  protected void setDebtRecoveryMethodLine(
      DebtRecovery debtRecovery, DebtRecoveryMethodLine debtRecoveryMethodLine) {

    BigDecimal balanceDueDebtRecovery = debtRecovery.getBalanceDueDebtRecovery();
    LocalDate referenceDate = debtRecovery.getReferenceDate();

    if ((!(referenceDate.plusDays(debtRecoveryMethodLine.getStandardDeadline()))
            .isAfter(appAccountService.getTodayDate(debtRecovery.getCompany())))
        && balanceDueDebtRecovery.compareTo(debtRecoveryMethodLine.getMinThreshold()) > 0) {
      log.debug(
          "The threshold of the balance due debt recovery is respected and the deadline is respected, Threshold : {} < Balance due deb recovery : {}",
          debtRecoveryMethodLine.getMinThreshold(),
          balanceDueDebtRecovery);

      if (!debtRecoveryMethodLine.getManualValidationOk()) {
        log.debug("The debt recovery level doesn't need manual validation");
        debtRecovery.setDebtRecoveryMethodLine(
            debtRecoveryMethodLine); // Afin d'afficher la ligne de niveau sur le tiers
        debtRecovery.setWaitDebtRecoveryMethodLine(null);
        // et lancer les autres actions du niveau
      } else {
        log.debug("The debt recovery level needs manual validation");
        debtRecovery.setWaitDebtRecoveryMethodLine(
            debtRecoveryMethodLine); // Si le passage est manuel
      }
    }
  }

  public int getMaxLevel(DebtRecovery debtRecovery) {

    DebtRecoveryMethod debtRecoveryMethod = debtRecovery.getDebtRecoveryMethod();

    int levelMax = -1;

    if (debtRecoveryMethod != null && debtRecoveryMethod.getDebtRecoveryMethodLineList() != null) {
      for (DebtRecoveryMethodLine debtRecoveryMethodLine :
          debtRecoveryMethod.getDebtRecoveryMethodLineList()) {
        int currentLevel = debtRecoveryMethodLine.getSequence();
        if (currentLevel > levelMax) {
          levelMax = currentLevel;
        }
      }
    }

    return levelMax;
  }

  /**
   * Fonction réinitialisant la relance
   *
   * @throws AxelorException
   * @param relance
   */
  @Transactional
  public void debtRecoveryInitialization(DebtRecovery debtRecovery) {

    if (debtRecovery != null) {
      log.debug("Begin debtRecoveryInitialization service...");

      debtRecovery.setDebtRecoveryMethodLine(null);
      debtRecovery.setWaitDebtRecoveryMethodLine(null);
      debtRecovery.setBalanceDue(BigDecimal.ZERO);
      debtRecovery.setBalanceDueDebtRecovery(BigDecimal.ZERO);
      debtRecovery.setInvoiceDebtRecoverySet(new HashSet<Invoice>());
      debtRecovery.setPaymentScheduleLineDebtRecoverySet(new HashSet<PaymentScheduleLine>());

      log.debug("End debtRecoveryInitialization service");

      debtRecoveryRepo.save(debtRecovery);
    }
  }

  /**
   * Fonction permetant de récupérer l'ensemble des lignes de relance de la matrice de relance pour
   * un tiers
   *
   * @param debtRecovery Une relance
   * @return Une liste de ligne de matrice de relance
   */
  public List<DebtRecoveryMethodLine> getDebtRecoveryMethodLineList(DebtRecovery debtRecovery) {
    return debtRecovery.getDebtRecoveryMethod().getDebtRecoveryMethodLineList();
  }

  /**
   * Fonction permettant de récupérer une ligne de relance de la matrice de relance pour un tiers
   *
   * @param debtRecovery Une relance
   * @param debtRecoveryLevel Un niveau de relance
   * @return Une ligne de matrice de relance
   * @throws AxelorException
   */
  public DebtRecoveryMethodLine getDebtRecoveryMethodLine(
      DebtRecovery debtRecovery, int debtRecoveryLevel) throws AxelorException {
    if (debtRecovery.getDebtRecoveryMethod() == null
        || debtRecovery.getDebtRecoveryMethod().getDebtRecoveryMethodLineList() == null
        || debtRecovery.getDebtRecoveryMethod().getDebtRecoveryMethodLineList().isEmpty()) {
      throw new AxelorException(
          debtRecovery,
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          "%s :\n"
              + I18n.get("Partner")
              + " %s: +"
              + I18n.get(AccountExceptionMessage.DEBT_RECOVERY_SESSION_1),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          (debtRecovery.getTradingName() == null
                  ? debtRecovery.getAccountingSituation()
                  : debtRecovery.getTradingNameAccountingSituation())
              .getPartner()
              .getName());
    }
    for (DebtRecoveryMethodLine debtRecoveryMethodLine :
        debtRecovery.getDebtRecoveryMethod().getDebtRecoveryMethodLineList()) {
      if (debtRecoveryMethodLine.getSequence() == debtRecoveryLevel) {
        return debtRecoveryMethodLine;
      }
    }

    throw new AxelorException(
        debtRecovery,
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        I18n.get(AccountExceptionMessage.DEBT_RECOVERY_DEBT_RECOVERY_LEVEL_NOT_FOUND));
  }

  /**
   * Reset to the min level the debtRecovery.
   *
   * @throws AxelorException
   */
  public void reset(DebtRecovery debtRecovery) throws AxelorException {

    log.debug("Reset of debtRecovery {}", debtRecovery);

    debtRecovery.setDebtRecoveryMethodLine(null);
    debtRecovery.setWaitDebtRecoveryMethodLine(null);
  }
}
