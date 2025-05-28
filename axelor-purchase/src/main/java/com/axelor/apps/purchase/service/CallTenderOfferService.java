package com.axelor.apps.purchase.service;

import com.axelor.apps.purchase.db.CallTenderNeed;
import com.axelor.apps.purchase.db.CallTenderOffer;
import com.axelor.apps.purchase.db.CallTenderSupplier;
import java.util.List;

public interface CallTenderOfferService {

  List<CallTenderOffer> generateCallTenderOfferList(
      CallTenderSupplier supplier, List<CallTenderNeed> needs);

  CallTenderOffer createCallTenderOffer(CallTenderSupplier supplier, CallTenderNeed need);

  boolean alreadyGenerated(CallTenderOffer offer, List<CallTenderOffer> offerList);
}
