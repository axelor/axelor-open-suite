package com.axelor.apps.account.service.debtrecovery;

import com.axelor.exception.AxelorException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public interface DebtRecoveryHistoryService {

  String printDebtRecoveryHistory(List<Integer> ids) throws IOException, AxelorException;

  Optional<Path> zipDebtRecoveryHistoryAttachments(List<Integer> ids);
}
