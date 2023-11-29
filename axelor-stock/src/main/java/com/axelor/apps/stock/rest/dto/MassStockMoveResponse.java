package com.axelor.apps.stock.rest.dto;

import com.axelor.apps.stock.db.MassStockMove;
import com.axelor.apps.stock.db.PickedProduct;
import com.axelor.apps.stock.db.StoredProduct;
import com.axelor.utils.api.ResponseStructure;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MassStockMoveResponse extends ResponseStructure {

  private final long id;
  private final String sequence;
  private final Integer statusSelect;
  private final Long companyId;
  private final Long cartStockLocationId;
  private final Long commonFromStockLocationId;
  private final Long commonToStockLocationId;
  private final List<Long> pickedProductList;
  private final List<Long> storedProductList;

  public MassStockMoveResponse(MassStockMove massStockMove) {
    super(massStockMove.getVersion());
    this.id = massStockMove.getId();
    this.sequence = massStockMove.getSequence();
    this.statusSelect = massStockMove.getStatusSelect();
    this.companyId = massStockMove.getCompany().getId();
    this.cartStockLocationId = massStockMove.getCartStockLocation().getId();
    this.commonFromStockLocationId = massStockMove.getCommonFromStockLocation().getId();
    this.commonToStockLocationId = massStockMove.getCommonFromStockLocation().getId();
    this.pickedProductList =
        massStockMove.getPickedProductList() != null
            ? massStockMove.getPickedProductList().stream()
                .map(PickedProduct::getId)
                .collect(Collectors.toList())
            : new ArrayList<>();
    this.storedProductList =
        massStockMove.getStoredProductList() != null
            ? massStockMove.getStoredProductList().stream()
                .map(StoredProduct::getId)
                .collect(Collectors.toList())
            : new ArrayList<>();
  }

  public List<Long> getPickedProductList() {
    return pickedProductList;
  }

  public List<Long> getStoredProductList() {
    return storedProductList;
  }

  public long getId() {
    return id;
  }

  public String getSequence() {
    return sequence;
  }

  public Integer getStatusSelect() {
    return statusSelect;
  }

  public Long getCompanyId() {
    return companyId;
  }

  public Long getCartStockLocationId() {
    return cartStockLocationId;
  }

  public Long getCommonFromStockLocationId() {
    return commonFromStockLocationId;
  }

  public Long getCommonToStockLocationId() {
    return commonToStockLocationId;
  }
}
