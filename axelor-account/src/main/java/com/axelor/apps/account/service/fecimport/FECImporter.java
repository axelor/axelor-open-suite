package com.axelor.apps.account.service.fecimport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.axelor.apps.account.db.FECImport;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.FECImportRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.base.db.ImportHistory;
import com.axelor.apps.base.service.imports.importer.Importer;
import com.axelor.apps.base.service.imports.listener.ImporterListener;
import com.axelor.data.csv.CSVImporter;
import com.axelor.db.Model;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class FECImporter extends Importer {

	  protected MoveValidateService moveValidateService;
	  protected AppAccountService appAccountService;
	  protected MoveRepository moveRepository;
	  protected FECImportRepository fecImportRepository;
  private final List<Move> moveList = new ArrayList<>();
private FECImport fecImport;

  
  @Inject
 public FECImporter(MoveValidateService moveValidateService,
		 AppAccountService appAccountService,
		 MoveRepository moveRepository,
		 FECImportRepository fecImportRepository) {
	this.moveValidateService = moveValidateService;
	this.appAccountService = appAccountService;
	this.moveRepository = moveRepository;
	this.fecImportRepository = fecImportRepository;
}
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
        	  try {
				completeAndvalidateMoves(fecImport, moveList);
			} catch (Exception e) {
				this.handle(null, e);
			}
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
  
  public FECImporter addFecImport(FECImport fecImport) {
	  this.fecImport = fecImport;
	  return this;
  }

  @Override
  protected ImportHistory process(String bind, String data) throws IOException {
    return process(bind, data, null);
  }

  public List<Move> getMoves() {
    return this.moveList;
  }
  
  @Transactional
  protected void completeAndvalidateMoves(FECImport fecImport, List<Move> moveList) throws Exception {
    if (fecImport != null) {
      fecImport = fecImportRepository.find(fecImport.getId());
      for (Move move : moveList) {
        try {
          move = moveRepository.find(move.getId());
          if (move != null) {
            move.setDescription(fecImport.getMoveDescription());
            if (move.getValidationDate() != null) {
              move.setReference(
                  String.format(
                      "%s%s%s",
                      fecImport.getId().toString(),
                      move.getReference(),
                      appAccountService.getTodayDate(move.getCompany()).toString()));
            }
            if (fecImport.getCompany() == null) {
              fecImport.setCompany(move.getCompany());
            }
            move.setFecImport(fecImport);
            if (fecImport.getValidGeneratedMove()) {
              moveValidateService.validate(move);
            } else {
              moveRepository.save(move);
            }
          }

        } catch (Exception e) {
          move.setStatusSelect(MoveRepository.STATUS_NEW);
          throw e;
        }
      }
    }
  }
}
