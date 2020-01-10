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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.SubrogationRelease;
import com.axelor.apps.base.db.Company;
import com.axelor.exception.AxelorException;
import java.io.IOException;
import java.util.List;

public interface SubrogationReleaseService {

  /**
   * Retrieve ventilated invoices from factorized customers.
   *
   * @param company
   * @return
   */
  List<Invoice> retrieveInvoices(Company company);

  /**
   * Transmit a subrogation release (generate a sequence number and change status).
   *
   * @param subrogationRelease
   * @throws AxelorException
   */
  void transmitRelease(SubrogationRelease subrogationRelease) throws AxelorException;

  /**
   * Generate a PDF export.
   *
   * @param subrogationRelease
   * @param name
   * @return
   * @throws AxelorException
   */
  String printToPDF(SubrogationRelease subrogationRelease, String name) throws AxelorException;

  /**
   * Generate a CSV export.
   *
   * @param subrogationRelease
   * @return
   * @throws AxelorException
   * @throws IOException
   */
  String exportToCSV(SubrogationRelease subrogationRelease) throws AxelorException, IOException;

  /**
   * Enter a subrogation release in the accounts (create moves).
   *
   * @param subrogationRelease
   * @throws AxelorException
   */
  void enterReleaseInTheAccounts(SubrogationRelease subrogationRelease) throws AxelorException;

  /**
   * Clear the subrogation release
   *
   * @param subrogationRelease
   */
  void clear(SubrogationRelease subrogationRelease);

  /**
   * Check if the all invoice of the subrogation release are completely cleared
   *
   * @param subrogationRelease
   * @return
   */
  boolean isSubrogationReleaseCompletelyPaid(SubrogationRelease subrogationRelease);
}
