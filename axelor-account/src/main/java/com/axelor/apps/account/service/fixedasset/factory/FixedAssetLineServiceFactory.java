package com.axelor.apps.account.service.fixedasset.factory;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.repo.FixedAssetLineRepository;
import com.axelor.apps.account.service.fixedasset.FixedAssetLineComputationService;
import com.axelor.apps.account.service.fixedasset.FixedAssetLineEconomicComputationServiceImpl;
import com.axelor.apps.account.service.fixedasset.FixedAssetLineEconomicUpdateComputationServiceImpl;
import com.axelor.apps.account.service.fixedasset.FixedAssetLineFiscalComputationServiceImpl;
import com.axelor.apps.account.service.fixedasset.FixedAssetLineIfrsComputationServiceImpl;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.inject.Beans;

public class FixedAssetLineServiceFactory {

  public FixedAssetLineComputationService getFixedAssetComputationService(
      FixedAsset fixedAsset, int typeSelect) throws AxelorException {

    switch (typeSelect) {
      case FixedAssetLineRepository.TYPE_SELECT_ECONOMIC:
        if (fixedAsset.getCorrectedAccountingValue() != null
            && fixedAsset.getCorrectedAccountingValue().signum() > 0) {
          return Beans.get(FixedAssetLineEconomicUpdateComputationServiceImpl.class);
        }
        return Beans.get(FixedAssetLineEconomicComputationServiceImpl.class);
      case FixedAssetLineRepository.TYPE_SELECT_FISCAL:
        return Beans.get(FixedAssetLineFiscalComputationServiceImpl.class);
      case FixedAssetLineRepository.TYPE_SELECT_IFRS:
        return Beans.get(FixedAssetLineIfrsComputationServiceImpl.class);
      default:
        throw new AxelorException(
            TraceBackRepository.CATEGORY_NO_VALUE,
            "There is no implementation for this typeSelect");
    }
  }
}
