package com.axelor.apps.stock.rest.dto;

import com.axelor.apps.stock.db.MassMove;
import com.axelor.apps.stock.db.MassMoveNeeds;
import com.axelor.apps.stock.db.PickedProducts;
import com.axelor.apps.stock.db.StoredProducts;
import com.axelor.utils.api.ResponseStructure;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MassMoveResponse extends ResponseStructure {

  private final long id;
  private final String sequence;
  private final Integer statusSelect;
  private final Long companyId;
  private final Long cartStockLocationId;
  private final Long commonFromStockLocationId;
  private final Long commonToStockLocationId;
  private final List<Long> pickedProductList;
  private final List<Long> productToMoveList;
  private final List<Long> storedProductList;

  public MassMoveResponse(MassMove massMove) {
    super(massMove.getVersion());
    this.id = massMove.getId();
    this.sequence = massMove.getSequence();
    this.statusSelect = massMove.getStatusSelect();
    this.companyId = massMove.getCompany().getId();
    this.cartStockLocationId = massMove.getCartStockLocation().getId();
    this.commonFromStockLocationId = massMove.getCommonFromStockLocation().getId();
    this.commonToStockLocationId = massMove.getCommonFromStockLocation().getId();
    this.pickedProductList =
        massMove.getPickedProductsList() != null
            ? massMove.getPickedProductsList().stream()
                .map(PickedProducts::getId)
                .collect(Collectors.toList())
            : new ArrayList<>();
    this.productToMoveList =
        massMove.getProductsToMoveList() != null
            ? massMove.getProductsToMoveList().stream()
                .map(MassMoveNeeds::getId)
                .collect(Collectors.toList())
            : new ArrayList<>();
    this.storedProductList =
        massMove.getStoredProductsList() != null
            ? massMove.getStoredProductsList().stream()
                .map(StoredProducts::getId)
                .collect(Collectors.toList())
            : new ArrayList<>();
  }

  public List<Long> getPickedProductList() {
    return pickedProductList;
  }

  public List<Long> getProductToMoveList() {
    return productToMoveList;
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
