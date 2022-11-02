package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.ChequeRejection;
import java.math.BigDecimal;

public class ChequeRejectionManagementRepository extends ChequeRejectionRepository {

  @Override
  public ChequeRejection copy(ChequeRejection entity, boolean deep) {
    entity.setName(null);
    entity.setStatusSelect(ChequeRejectionRepository.STATUS_DRAFT);
    entity.setMove(null);
    entity.setAmountRejected(BigDecimal.ZERO);
    entity.setPaymentVoucher(null);
    entity.setPartner(null);
    return super.copy(entity, deep);
  }
}
