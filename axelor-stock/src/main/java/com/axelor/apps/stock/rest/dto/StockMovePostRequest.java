package com.axelor.apps.stock.rest.dto;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.PrintingSettings;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestPostStructure;
import java.time.LocalDate;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class StockMovePostRequest extends RequestPostStructure {

  @NotNull private Long fromAddressId;

  @NotNull private Long toAddressId;

  @NotNull private Long companyId;

  @Min(1)
  private int statusSelect;

  private LocalDate realDate;

  private LocalDate estimatedDate;

  @NotNull private Long fromStockLocationId;

  @NotNull private Long toStockLocationId;

  private String note;

  private Long printingSettingsId;

  @NotNull private int typeSelect;

  private boolean isWithBackorder;

  private boolean isWithReturnSurplus;

  public Long getFromAddressId() {
    return fromAddressId;
  }

  public void setFromAddressId(Long fromAddressId) {
    this.fromAddressId = fromAddressId;
  }

  public Long getToAddressId() {
    return toAddressId;
  }

  public void setToAddressId(Long toAddressId) {
    this.toAddressId = toAddressId;
  }

  public Long getCompanyId() {
    return companyId;
  }

  public void setCompanyId(Long companyId) {
    this.companyId = companyId;
  }

  public int getStatusSelect() {
    return statusSelect;
  }

  public void setStatusSelect(int statusSelect) {
    this.statusSelect = statusSelect;
  }

  public LocalDate getRealDate() {
    return realDate;
  }

  public void setRealDate(LocalDate realDate) {
    this.realDate = realDate;
  }

  public LocalDate getEstimatedDate() {
    return estimatedDate;
  }

  public void setEstimatedDate(LocalDate estimatedDate) {
    this.estimatedDate = estimatedDate;
  }

  public Long getFromStockLocationId() {
    return fromStockLocationId;
  }

  public void setFromStockLocationId(Long fromStockLocationId) {
    this.fromStockLocationId = fromStockLocationId;
  }

  public Long getToStockLocationId() {
    return toStockLocationId;
  }

  public void setToStockLocationId(Long toStockLocationId) {
    this.toStockLocationId = toStockLocationId;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }

  public Long getPrintingSettingsId() {
    return printingSettingsId;
  }

  public void setPrintingSettingsId(Long printingSettingsId) {
    this.printingSettingsId = printingSettingsId;
  }

  public int getTypeSelect() {
    return typeSelect;
  }

  public void setTypeSelect(int typeSelect) {
    this.typeSelect = typeSelect;
  }

  public boolean isWithBackorder() {
    return isWithBackorder;
  }

  public void setWithBackorder(boolean isWithBackorder) {
    this.isWithBackorder = isWithBackorder;
  }

  public boolean isWithReturnSurplus() {
    return isWithReturnSurplus;
  }

  public void setWithReturnSurplus(boolean isWithReturnSurplus) {
    this.isWithReturnSurplus = isWithReturnSurplus;
  }

  // Transform id to object
  public Company fetchCompany() {
    return companyId != null
        ? ObjectFinder.find(Company.class, companyId, ObjectFinder.NO_VERSION)
        : null;
  }

  public Address fetchFromAddress() {
    return fromAddressId != null
        ? ObjectFinder.find(Address.class, fromAddressId, ObjectFinder.NO_VERSION)
        : null;
  }

  public Address fetchToAddress() {
    return toAddressId != null
        ? ObjectFinder.find(Address.class, toAddressId, ObjectFinder.NO_VERSION)
        : null;
  }

  public StockLocation fetchFromStockLocation() {
    return fromStockLocationId != null
        ? ObjectFinder.find(StockLocation.class, fromStockLocationId, ObjectFinder.NO_VERSION)
        : null;
  }

  public StockLocation fetchToStockLocation() {
    return toStockLocationId != null
        ? ObjectFinder.find(StockLocation.class, toStockLocationId, ObjectFinder.NO_VERSION)
        : null;
  }

  public PrintingSettings fetchPrintingSettings() {
    return printingSettingsId != null
        ? ObjectFinder.find(PrintingSettings.class, printingSettingsId, ObjectFinder.NO_VERSION)
        : null;
  }
}
