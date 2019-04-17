/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.debtrecovery;

import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.DebtRecovery;
import com.axelor.apps.account.db.DebtRecoveryConfigLine;
import com.axelor.apps.account.db.DebtRecoveryMethod;
import com.axelor.apps.account.db.DebtRecoveryMethodLine;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.PaymentScheduleLine;
import com.axelor.apps.account.db.repo.DebtRecoveryRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
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

    AccountingSituation accountingSituation = debtRecovery.getAccountingSituation();
    Company company = accountingSituation.getCompany();
    Partner partner = accountingSituation.getPartner();
    List<DebtRecoveryConfigLine> debtRecoveryConfigLines =
        company.getAccountConfig().getDebtRecoveryConfigLineList();

    for (DebtRecoveryConfigLine debtRecoveryConfigLine : debtRecoveryConfigLines) {
      if (debtRecoveryConfigLine.getPartnerCategory().equals(partner.getPartnerCategory())) {

        log.debug("méthode de relance determinée ");
        return debtRecoveryConfigLine.getDebtRecoveryMethod();
      }
    }

    log.debug("méthode de relance non determinée ");

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

    int debtRecoveryLevel = 0;
    if (debtRecovery.getDebtRecoveryMethodLine() != null
        && debtRecovery.getDebtRecoveryMethodLine().getDebtRecoveryLevel().getName() != null) {
      debtRecoveryLevel = debtRecovery.getDebtRecoveryMethodLine().getDebtRecoveryLevel().getName();
    }

    int theoricalDebtRecoveryLevel;

    int levelMax = this.getMaxLevel(debtRecovery);

    // Test inutile... à verifier
    if ((appAccountService.getTodayDate().isAfter(referenceDate)
            || appAccountService.getTodayDate().isEqual(referenceDate))
        && balanceDueDebtRecovery.compareTo(BigDecimal.ZERO) > 0) {
      log.debug(
          "Si la date actuelle est égale ou ultérieur à la date de référence et le solde due relançable positif");
      // Pour les client à haut risque vital, on passe directement du niveau de relance 2 au niveau
      // de relance 4
      if (debtRecoveryLevel < levelMax) {
        log.debug("Sinon ce n'est pas un client à haut risque vital");
        theoricalDebtRecoveryLevel = debtRecoveryLevel + 1;
      } else {
        log.debug("Sinon c'est un client à un haut risque vital");
        theoricalDebtRecoveryLevel = levelMax;
      }

      DebtRecoveryMethodLine debtRecoveryMethodLine =
          this.getDebtRecoveryMethodLine(debtRecovery, theoricalDebtRecoveryLevel);

      if ((!(referenceDate.plusDays(debtRecoveryMethodLine.getStandardDeadline()))
              .isAfter(appAccountService.getTodayDate()))
          && balanceDueDebtRecovery.compareTo(debtRecoveryMethodLine.getMinThreshold()) > 0) {
        log.debug("Si le seuil du solde exigible relançable est respecté et le délai est respecté");

        if (!debtRecoveryMethodLine.getManualValidationOk()) {
          log.debug("Si le niveau ne necessite pas de validation manuelle");
          debtRecovery.setDebtRecoveryMethodLine(
              debtRecoveryMethodLine); // Afin d'afficher la ligne de niveau sur le tiers
          debtRecovery.setWaitDebtRecoveryMethodLine(null);
          // et lancer les autres actions du niveau
        } else {
          log.debug("Si le niveau necessite une validation manuelle");
          debtRecovery.setWaitDebtRecoveryMethodLine(
              debtRecoveryMethodLine); // Si le passage est manuel
        }
      }

    } else {
      log.debug("Sinon on lance une réinitialisation");
      this.debtRecoveryInitialization(debtRecovery);
    }
    log.debug("End debtRecoverySession service");
    return debtRecovery;
  }

  public int getMaxLevel(DebtRecovery debtRecovery) {

    DebtRecoveryMethod debtRecoveryMethod = debtRecovery.getDebtRecoveryMethod();

    int levelMax = 0;

    if (debtRecoveryMethod != null && debtRecoveryMethod.getDebtRecoveryMethodLineList() != null) {
      for (DebtRecoveryMethodLine debtRecoveryMethodLine :
          debtRecoveryMethod.getDebtRecoveryMethodLineList()) {
        int currentLevel = debtRecoveryMethodLine.getDebtRecoveryLevel().getName();
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
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void debtRecoveryInitialization(DebtRecovery debtRecovery) throws AxelorException {

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
              + I18n.get(IExceptionMessage.DEBT_RECOVERY_SESSION_1),
          I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.EXCEPTION),
          debtRecovery.getAccountingSituation().getPartner().getName());
    }
    for (DebtRecoveryMethodLine debtRecoveryMethodLine :
        debtRecovery.getDebtRecoveryMethod().getDebtRecoveryMethodLineList()) {
      if (debtRecoveryMethodLine.getDebtRecoveryLevel().getName().equals(debtRecoveryLevel)) {
        return debtRecoveryMethodLine;
      }
    }

    throw new AxelorException(
        debtRecovery,
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        I18n.get(
            com.axelor.apps.account.exception.IExceptionMessage
                .DEBT_RECOVERY_DEBT_RECOVERY_LEVEL_NOT_FOUND));
  }
}
