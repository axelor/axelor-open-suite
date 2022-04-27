package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Move;
import java.util.List;

public interface MoveRemoveService {

  void archiveDaybookMove(Move move) throws Exception;

  Move archiveMove(Move move);

  int deleteMultiple(List<? extends Move> moveList);

  void deleteMove(Move move) throws Exception;
}
