/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.custom;

/**
 * Holds the transient line/period counters used while computing the values of a single accounting
 * report. One instance is scoped to a single report computation (see {@link
 * AccountingReportValueServiceImpl}) so the counters are never shared between reports, threads or
 * tenants.
 */
public class AccountingReportValueContext {

  protected int lineOffset = 0;
  protected int periodNumber = 0;

  public void incrementLineOffset() {
    lineOffset++;
  }

  public int getLineOffset() {
    return lineOffset;
  }

  public void incrementPeriodNumber() {
    periodNumber++;
  }

  public int getPeriodNumber() {
    return periodNumber;
  }
}
