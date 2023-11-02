package com.axelor.apps.base.service.tax;

import com.axelor.apps.base.interfaces.OrderLineTax;
import com.axelor.apps.base.interfaces.PricedOrder;
import com.axelor.apps.base.interfaces.PricedOrderLine;
import java.util.Set;

public interface OrderLineTaxService {

  boolean isCustomerSpecificNote(PricedOrder pricedOrder);

  void addTaxEquivSpecificNote(
      PricedOrderLine pricedOrderLine, boolean customerSpecificNote, Set<String> specificNotes);

  void computeTax(OrderLineTax orderLineTax);

  void setSpecificNotes(
      boolean customerSpecificNote,
      PricedOrder pricedOrder,
      Set<String> specificNotes,
      String partnerNote);
}
