package com.axelor.apps.event.web;

import com.axelor.apps.event.db.Discount;
import com.axelor.apps.event.db.EventRegistrations;
import com.axelor.apps.event.db.Events;
import com.axelor.apps.event.db.repo.EventRegistrationsRepository;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import java.math.BigDecimal;

public class EventRegistrationController {

  @Inject EventRegistrationsRepository eventRegistrationsRepository;

  public void computeRegisterationAmount(ActionRequest request, ActionResponse response) {
    BigDecimal amount = BigDecimal.ZERO;
    EventRegistrations eventRegistrations = request.getContext().asType(EventRegistrations.class);
    System.out.println(eventRegistrations.getEvent().getDiscounts());

    Discount disc = eventRegistrations.getEvent().getDiscounts().get(0); // get first one
    //    Discount amount: Discount percent * Event fees /100.
    amount =
        (disc.getDiscountPercent().multiply(eventRegistrations.getEvent().getEventFees()))
            .divide(new BigDecimal(100));

    System.out.println(amount);
    response.setValue("amount", eventRegistrations.getEvent().getEventFees().subtract(amount));
  }

  public void totalEntry(ActionRequest request, ActionResponse response) {
    Events events = request.getContext().asType(Events.class);
    BigDecimal amountCollected = BigDecimal.ZERO;
    BigDecimal totalDiscount = BigDecimal.ZERO;
    int totalEntry =
        (int)
            eventRegistrationsRepository
                .all()
                .filter("self.event= :event")
                .bind("event", events)
                .count();

    for (EventRegistrations er : events.getEventRegistrations()) {
      amountCollected = amountCollected.add(er.getAmount());
      totalDiscount = totalDiscount.add(events.getEventFees().subtract(er.getAmount()));
      System.out.println();
    }
    response.setValue("totalEntry", totalEntry);
    response.setValue("amountCollected", amountCollected);
    response.setValue("totalDiscount", totalDiscount);
  }

  public String checkId(String id) {
    return id;
  }
}
