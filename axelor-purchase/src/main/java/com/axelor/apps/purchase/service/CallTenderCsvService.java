package com.axelor.apps.purchase.service;

import com.axelor.apps.purchase.db.CallTenderOffer;
import com.axelor.meta.db.MetaFile;
import java.io.IOException;
import java.util.List;

public interface CallTenderCsvService {

  MetaFile generateCsvFile(List<CallTenderOffer> offerList) throws IOException;
}
