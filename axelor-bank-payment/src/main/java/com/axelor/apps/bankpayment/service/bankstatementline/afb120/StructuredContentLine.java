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
package com.axelor.apps.bankpayment.service.bankstatementline.afb120;

import com.axelor.apps.account.db.InterbankCodeLine;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Currency;
import java.math.BigDecimal;
import java.time.LocalDate;

public class StructuredContentLine {
  private String description;
  private LocalDate operationDate;
  private LocalDate valueDate;
  private int lineType;
  private String additionalInformation;
  private BankDetails bankDetails;
  private Currency currency;
  private InterbankCodeLine operationInterbankCodeLine;
  private InterbankCodeLine rejectInterbankCodeLine;
  private BigDecimal debit;
  private BigDecimal credit;
  private String origin;
  private String reference;
  private String unavailabilityIndexSelect;
  private String commissionExemptionIndexSelect;

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public LocalDate getOperationDate() {
    return operationDate;
  }

  public void setOperationDate(LocalDate operationDate) {
    this.operationDate = operationDate;
  }

  public LocalDate getValueDate() {
    return valueDate;
  }

  public void setValueDate(LocalDate valueDate) {
    this.valueDate = valueDate;
  }

  public int getLineType() {
    return lineType;
  }

  public void setLineType(int lineType) {
    this.lineType = lineType;
  }

  public String getAdditionalInformation() {
    return additionalInformation;
  }

  public void setAdditionalInformation(String additionalInformation) {
    this.additionalInformation = additionalInformation;
  }

  public BankDetails getBankDetails() {
    return bankDetails;
  }

  public void setBankDetails(BankDetails bankDetails) {
    this.bankDetails = bankDetails;
  }

  public Currency getCurrency() {
    return currency;
  }

  public void setCurrency(Currency currency) {
    this.currency = currency;
  }

  public InterbankCodeLine getOperationInterbankCodeLine() {
    return operationInterbankCodeLine;
  }

  public void setOperationInterbankCodeLine(InterbankCodeLine operationInterbankCodeLine) {
    this.operationInterbankCodeLine = operationInterbankCodeLine;
  }

  public InterbankCodeLine getRejectInterbankCodeLine() {
    return rejectInterbankCodeLine;
  }

  public void setRejectInterbankCodeLine(InterbankCodeLine rejectInterbankCodeLine) {
    this.rejectInterbankCodeLine = rejectInterbankCodeLine;
  }

  public BigDecimal getDebit() {
    return debit;
  }

  public void setDebit(BigDecimal debit) {
    this.debit = debit;
  }

  public BigDecimal getCredit() {
    return credit;
  }

  public void setCredit(BigDecimal credit) {
    this.credit = credit;
  }

  public String getOrigin() {
    return origin;
  }

  public void setOrigin(String origin) {
    this.origin = origin;
  }

  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

  public String getUnavailabilityIndexSelect() {
    return unavailabilityIndexSelect;
  }

  public void setUnavailabilityIndexSelect(String unavailabilityIndexSelect) {
    this.unavailabilityIndexSelect = unavailabilityIndexSelect;
  }

  public String getCommissionExemptionIndexSelect() {
    return commissionExemptionIndexSelect;
  }

  public void setCommissionExemptionIndexSelect(String commissionExemptionIndexSelect) {
    this.commissionExemptionIndexSelect = commissionExemptionIndexSelect;
  }
}
