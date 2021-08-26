package com.axelor.apps.account.service.fixedasset.factory;

import com.axelor.apps.account.db.repo.FixedAssetLineRepository;
import com.axelor.apps.account.service.fixedasset.FixedAssetLineComputationService;
import com.axelor.apps.account.service.fixedasset.FixedAssetLineEconomicComputationServiceImpl;
import com.axelor.apps.account.service.fixedasset.FixedAssetLineFiscalComputationServiceImpl;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.inject.Beans;

public class FixedAssetLineServiceFactory {

  public FixedAssetLineComputationService getFixedAssetComputationService(int typeSelect)
      throws AxelorException {

    switch (typeSelect) {
      case FixedAssetLineRepository.TYPE_SELECT_ECONOMIC:
        return Beans.get(FixedAssetLineEconomicComputationServiceImpl.class);
      case FixedAssetLineRepository.TYPE_SELECT_FISCAL:
        return Beans.get(FixedAssetLineFiscalComputationServiceImpl.class);
      default:
        throw new AxelorException(
            TraceBackRepository.CATEGORY_NO_VALUE,
            "There is no implementation for this typeSelect");
    }
  }
}
