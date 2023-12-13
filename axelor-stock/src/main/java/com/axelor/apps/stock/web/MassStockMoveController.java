package com.axelor.apps.stock.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.MassStockMove;
import com.axelor.apps.stock.db.repo.MassStockMoveRepository;
import com.axelor.apps.stock.service.MassStockMoveService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class MassStockMoveController {

  public void getSequence(ActionRequest request, ActionResponse response) throws AxelorException {
    MassStockMove massStockMove = request.getContext().asType(MassStockMove.class);
    massStockMove = Beans.get(MassStockMoveRepository.class).find(massStockMove.getId());
    String sequence =
        Beans.get(MassStockMoveService.class)
            .getAndSetSequence(massStockMove.getCompany(), massStockMove);
    response.setValue("sequence", sequence);
  }
}
