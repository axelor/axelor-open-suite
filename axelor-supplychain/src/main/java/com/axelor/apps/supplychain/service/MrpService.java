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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.supplychain.db.Mrp;
import java.time.LocalDate;
import java.util.concurrent.Callable;

public interface MrpService extends Callable<Mrp> {

  /**
   * To call before using it as a callable.
   *
   * @param mrp a mrp managed by hibernate
   */
  public void setMrp(Mrp mrp);

  public void runCalculation(Mrp mrp) throws AxelorException;

  public void generateSelectedProposals(Mrp mrp, boolean isProposalsPerSupplier)
      throws AxelorException;

  void generateAllProposals(Mrp mrp, boolean isProposalsPerSupplier) throws AxelorException;

  public void reset(Mrp mrp);

  public void undoManualChanges(Mrp mrp);

  /**
   * Search for the end date of the mrp. If the end date field in mrp is blank, search in the lines
   * the last date.
   *
   * @param mrp
   * @return the mrp end date
   */
  public LocalDate findMrpEndDate(Mrp mrp);

  public Mrp createProjectedStock(
      Mrp mrp, Product product, Company company, StockLocation stockLocation)
      throws AxelorException;

  /**
   * Called when an exception occurred during the mrp computation. Save the exception message.
   *
   * @param mrp a mrp after computation
   * @param e the exception thrown during the computation
   */
  void saveErrorInMrp(Mrp mrp, Exception e);

  void massUpdateProposalToProcess(Mrp mrp, boolean proposalToProcess);

  /**
   * Methods that checks if mrp is currenctly started.
   *
   * @param mrp
   * @return
   * @throws AxelorException
   */
  boolean isOnGoing(Mrp mrp) throws AxelorException;
}
