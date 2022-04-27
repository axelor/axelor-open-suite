package com.axelor.apps.stock.rest.dto;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.UnitRepository;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.stock.db.repo.TrackingNumberRepository;
import com.axelor.apps.tool.api.RequestStructure;
import com.axelor.inject.Beans;
import java.math.BigDecimal;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class StockMoveCreateRequest implements RequestStructure {

  @NotNull
  @Min(0)
  private Long productId;

  @NotNull
  @Min(0)
  private Long originStockLocationId;

  @NotNull
  @Min(0)
  private Long destStockLocationId;

  @NotNull
  @Min(0)
  private Long companyId;

  @Min(0)
  private Long trackingNumberId;

  @NotNull
  @Min(0)
  private Long unitId;

  @NotNull
  @Min(0)
  private BigDecimal movedQty;

  public StockMoveCreateRequest() {}

  public Long getProductId() {
    return productId;
  }

  public void setProductId(Long productId) {
    this.productId = productId;
  }

  public Long getOriginStockLocationId() {
    return originStockLocationId;
  }

  public void setOriginStockLocationId(Long originStockLocationId) {
    this.originStockLocationId = originStockLocationId;
  }

  public Long getDestStockLocationId() {
    return destStockLocationId;
  }

  public void setDestStockLocationId(Long destStockLocationId) {
    this.destStockLocationId = destStockLocationId;
  }

  public Long getCompanyId() {
    return companyId;
  }

  public void setCompanyId(Long companyId) {
    this.companyId = companyId;
  }

  public Long getTrackingNumberId() {
    return trackingNumberId;
  }

  public void setTrackingNumberId(Long trackingNumberId) {
    this.trackingNumberId = trackingNumberId;
  }

  public Long getUnitId() {
    return unitId;
  }

  public void setUnitId(Long unitId) {
    this.unitId = unitId;
  }

  public BigDecimal getMovedQty() {
    return movedQty;
  }

  public void setMovedQty(BigDecimal movedQty) {
    this.movedQty = movedQty;
  }

  // Transform id to object
  public Product getProduct() {
    return Beans.get(ProductRepository.class).find(this.productId);
  }

  public StockLocation getOriginStockLocation() {
    return Beans.get(StockLocationRepository.class).find(this.originStockLocationId);
  }

  public StockLocation getDestStockLocation() {
    return Beans.get(StockLocationRepository.class).find(this.destStockLocationId);
  }

  public Company getCompany() {
    return Beans.get(CompanyRepository.class).find(this.companyId);
  }

  public Unit getUnit() {
    return Beans.get(UnitRepository.class).find(this.unitId);
  }

  public TrackingNumber getTrackingNumber() {
    if (this.trackingNumberId != null) {
      return Beans.get(TrackingNumberRepository.class).find(this.trackingNumberId);
    } else {
      return null;
    }
  }
}
