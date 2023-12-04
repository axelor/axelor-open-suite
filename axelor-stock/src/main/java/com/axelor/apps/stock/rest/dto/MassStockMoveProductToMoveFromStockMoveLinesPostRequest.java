package com.axelor.apps.stock.rest.dto;

import com.axelor.apps.stock.db.MassStockMove;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestPostStructure;
import java.util.List;
import javax.validation.constraints.NotNull;

public class MassStockMoveProductToMoveFromStockMoveLinesPostRequest extends RequestPostStructure {

  @NotNull private Long massStockMoveId;

  @NotNull List<Long> stockMoveLinesIds;

  public Long getMassMoveId() {
    return massStockMoveId;
  }

  public void setMassMoveId(Long massStockMoveId) {
    this.massStockMoveId = massStockMoveId;
  }

  public List<Long> getStockMoveLinesIds() {
    return stockMoveLinesIds;
  }

  public void setStockMoveLinesIds(List<Long> stockMoveLinesIds) {
    this.stockMoveLinesIds = stockMoveLinesIds;
  }

  // Transform id to object
  public MassStockMove fetchMassStockMove() {
    return ObjectFinder.find(MassStockMove.class, massStockMoveId, ObjectFinder.NO_VERSION);
  }
}
