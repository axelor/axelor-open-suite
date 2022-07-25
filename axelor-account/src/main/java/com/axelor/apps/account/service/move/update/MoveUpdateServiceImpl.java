package com.axelor.apps.account.service.move.update;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.move.MoveCompletionService;
import com.axelor.apps.account.service.move.MoveCustAccountService;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveUpdateServiceImpl implements MoveUpdateService {
  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  protected AccountRepository accountRepository;
  protected PartnerRepository partnerRepository;
  protected MoveCustAccountService moveCustAccountService;
  protected MoveCompletionService moveCompletionService;

  @Inject
  public MoveUpdateServiceImpl(
      AccountRepository accountRepository,
      PartnerRepository partnerRepository,
      MoveCustAccountService moveCustAccountService,
      MoveCompletionService moveCompletionService) {
    this.accountRepository = accountRepository;
    this.partnerRepository = partnerRepository;
    this.moveCustAccountService = moveCustAccountService;
    this.moveCompletionService = moveCompletionService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateInDayBookMode(Move move) throws AxelorException {

    if (move.getStatusSelect() == MoveRepository.STATUS_ACCOUNTED
        || move.getStatusSelect() == MoveRepository.STATUS_SIMULATED) {

      log.debug("Updating customer account of move {}", move);
      Set<Partner> partnerSet = new HashSet<>();

      partnerSet.addAll(this.getPartnerOfMoveBeforeUpdate(move));
      partnerSet.addAll(moveCustAccountService.getPartnerOfMove(move));

      List<Partner> partnerList = new ArrayList<>();
      partnerList.addAll(partnerSet);

      moveCustAccountService.updateCustomerAccount(partnerList, move.getCompany());
    }
  }

  /**
   * Get the distinct partners of an account move that impact the partner balances
   *
   * @param move
   * @return A list of partner
   */
  public List<Partner> getPartnerOfMoveBeforeUpdate(Move move) {
    List<Partner> partnerList = new ArrayList<Partner>();
    for (MoveLine moveLine : move.getMoveLineList()) {
      if (moveLine.getAccountId() != null) {
        Account account = accountRepository.find(moveLine.getAccountId());
        if (account != null
            && account.getUseForPartnerBalance()
            && moveLine.getPartnerId() != null) {
          Partner partner = partnerRepository.find(moveLine.getPartnerId());
          if (partner != null && !partnerList.contains(partner)) {
            partnerList.add(partner);
          }
        }
      }
    }
    return partnerList;
  }
}
