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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.TradingName;
import java.math.BigDecimal;
import java.util.List;

public interface AccountCustomerService {

  public AccountingSituationService getAccountingSituationService();

  /**
   * Fonction permettant de calculer le solde total d'un tiers
   *
   * @param partner Un tiers
   * @param company Une société
   * @return Le solde total
   */
  public BigDecimal getBalance(Partner partner, Company company);

  /**
   * Compute the balance due for a specific (company, trading name) combination.
   *
   * <p>Computation of the balance due of a partner : Total amount of the invoices and expired
   * deadlines (date of the day >= date of the deadline)
   *
   * @param partner A Partner
   * @param company A Company
   * @param tradingName (Optional) A trading name of the company
   * @return The balance due of this trading name, in the scope of the activities of this company
   */
  public BigDecimal getBalanceDue(Partner partner, Company company, TradingName tradingName);

  /**
   * **************************************** 2. Calcul du solde exigible (relançable) du tiers
   * *****************************************
   */
  /**
   * solde des factures exigibles non bloquées en relance et dont « la date de facture » + « délai
   * d’acheminement(X) » <« date du jour » si la date de facture = date d'échéance de facture, sinon
   * pas de prise en compte du délai d'acheminement **
   */
  /**
   * solde des échéances rejetées qui ne sont pas bloqués
   * *****************************************************
   */
  public BigDecimal getBalanceDueDebtRecovery(
      Partner partner, Company company, TradingName tradingName);

  /**
   * Méthode permettant de récupérer l'ensemble des lignes d'écriture pour une société et un tiers
   *
   * @param partner Un tiers
   * @param company Une société
   * @return
   */
  public List<? extends MoveLine> getMoveLine(Partner partner, Company company);

  /**
   * Procédure mettant à jour les soldes du compte client des tiers pour une société
   *
   * @param partnerList Une liste de tiers à mettre à jour
   * @param company Une société
   */
  public void updatePartnerAccountingSituation(
      List<Partner> partnerList,
      Company company,
      boolean updateCustAccount,
      boolean updateDueCustAccount,
      boolean updateDueDebtRecoveryCustAccount)
      throws AxelorException;

  public void flagPartners(List<Partner> partnerList, Company company) throws AxelorException;

  public AccountingSituation updateAccountingSituationCustomerAccount(
      AccountingSituation accountingSituation,
      boolean updateCustAccount,
      boolean updateDueCustAccount,
      boolean updateDueDebtRecoveryCustAccount)
      throws AxelorException;

  public Account getPartnerAccount(Partner partner, Company company, boolean isSupplierInvoice)
      throws AxelorException;
}
