package com.axelor.apps.crm.service;

import com.axelor.apps.crm.db.CallForTenders;
import com.axelor.apps.crm.db.repo.CallForTendersRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class CallForTendersServiceImpl implements CallForTendersService {

  protected CallForTendersRepository callForTendersRepo;

  @Inject
  public CallForTendersServiceImpl(CallForTendersRepository callForTendersRepository) {
    this.callForTendersRepo = callForTendersRepository;
  }

  @Transactional
  public void win(CallForTenders call) {
    call.setStatusSelect(CallForTendersRepository.STATUS_WON);
    callForTendersRepo.save(call);
  }

  @Transactional
  public void loose(CallForTenders call) {
    call.setStatusSelect(CallForTendersRepository.STATUS_LOST);
    callForTendersRepo.save(call);
  }

  @Transactional
  public void setBackInProgress(CallForTenders call) {
    call.setStatusSelect(CallForTendersRepository.STATUS_IN_PROGRESS);
    callForTendersRepo.save(call);
  }
}
