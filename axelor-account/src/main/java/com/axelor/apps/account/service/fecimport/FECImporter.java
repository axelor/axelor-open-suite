package com.axelor.apps.account.service.fecimport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.base.db.ImportHistory;
import com.axelor.apps.base.service.imports.importer.Importer;
import com.axelor.apps.base.service.imports.listener.ImporterListener;
import com.axelor.data.csv.CSVImporter;
import com.axelor.db.Model;

public class FECImporter extends Importer {


  private final List<Move> moveList = new ArrayList<>();

  @Override
  protected ImportHistory process(String bind, String data, Map<String, Object> importContext)
      throws IOException {

    CSVImporter importer = new CSVImporter(bind, data);

    ImporterListener listener =
        new ImporterListener(getConfiguration().getName()) {
          @Override
          public void handle(Model bean, Exception e) {
            super.handle(bean, e);
          }

          @Override
          public void imported(Integer total, Integer success) {
            super.imported(total, success);
          }

          @Override
          public void imported(Model bean) {
            addMoveFromMoveLine(bean);
            super.imported(bean);
          }
        };

    importer.addListener(listener);
    importer.setContext(importContext);
    importer.run();
    return addHistory(listener);
  }



  protected void addMoveFromMoveLine(Model bean) {
    if (bean.getClass().equals(MoveLine.class)) {
      MoveLine moveLine = (MoveLine) bean;
      if (moveLine.getMove() != null) {
        Move move = moveLine.getMove();
        if (!moveList.contains(move)) {
          moveList.add(move);
        }
      }
    }
  }

  @Override
  protected ImportHistory process(String bind, String data) throws IOException {
    return process(bind, data, null);
  }
  
  public List<Move> getMoves() {
	  return this.moveList;
  }
}
