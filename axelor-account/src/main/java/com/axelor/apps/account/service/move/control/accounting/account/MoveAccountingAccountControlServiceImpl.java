package com.axelor.apps.account.service.move.control.accounting.account;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveAccountingAccountControlServiceImpl
    implements MoveAccountingAccountControlService {
  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Override
  public void checkInactiveAccount(Move move) throws AxelorException {

    log.debug("Checking inactive account of move {}", move);

    if (move != null && CollectionUtils.isNotEmpty(move.getMoveLineList())) {
      List<String> inactiveList =
          move.getMoveLineList().stream()
              .map(MoveLine::getAccount)
              .filter(Objects::nonNull)
              .filter(
                  account ->
                      account.getStatusSelect() != null
                          && account.getStatusSelect() != AccountRepository.STATUS_ACTIVE)
              .distinct()
              .map(Account::getCode)
              .collect(Collectors.toList());
      if (inactiveList.size() == 1) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.INACTIVE_ACCOUNT_FOUND),
            inactiveList.get(0));
      } else if (inactiveList.size() > 1) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.INACTIVE_ACCOUNTS_FOUND),
            inactiveList.stream().collect(Collectors.joining(", ")));
      }
    }
  }
}
