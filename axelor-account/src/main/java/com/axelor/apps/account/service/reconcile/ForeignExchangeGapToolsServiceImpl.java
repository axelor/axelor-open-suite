package com.axelor.apps.account.service.reconcile;

import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import java.util.ArrayList;
import java.util.List;

public class ForeignExchangeGapToolsServiceImpl implements ForeignExchangeGapToolsService {

  @Override
  public List<Integer> getForeignExchangeTypes() {
    return new ArrayList<>(
        List.of(
            InvoicePaymentRepository.TYPE_FOREIGN_EXCHANGE_GAIN,
            InvoicePaymentRepository.TYPE_FOREIGN_EXCHANGE_LOSS));
  }
}
