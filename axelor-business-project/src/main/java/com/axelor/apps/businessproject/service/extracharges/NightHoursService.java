package com.axelor.apps.businessproject.service.extracharges;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NightHoursService {

  private final TimesheetLineRepository timesheetRepo;

  @Inject
  public NightHoursService(TimesheetLineRepository timesheetRepo) {
    this.timesheetRepo = timesheetRepo;
  }

  public Map<String, NightHoursDetail> getNightHoursBreakdown(List<Long> timesheetLineIds) {
    Map<String, NightHoursDetail> breakdown = new HashMap<>();
    if (timesheetLineIds == null || timesheetLineIds.isEmpty()) return breakdown;

    List<TimesheetLine> lines =
        timesheetRepo.all().filter("self.id IN (:ids)").bind("ids", timesheetLineIds).fetch();

    for (TimesheetLine tsl : lines) {
      if (tsl.getNightHours() == null || tsl.getNightHours().compareTo(BigDecimal.ZERO) <= 0)
        continue;
      Product product = tsl.getProduct();
      if (product == null) continue;

      String productName = product.getName();
      BigDecimal nightHours = tsl.getNightHours();
      BigDecimal rate = product.getSalePrice();
      BigDecimal amount = nightHours.multiply(rate);

      breakdown.merge(
          productName,
          new NightHoursDetail(nightHours, rate, amount),
          (existing, newOne) -> {
            existing.add(newOne);
            return existing;
          });
    }

    return breakdown;
  }

  public static class NightHoursDetail {
    private BigDecimal nightHours;
    private BigDecimal rate;
    private BigDecimal amount;

    public NightHoursDetail(BigDecimal nightHours, BigDecimal rate, BigDecimal amount) {
      this.nightHours = nightHours;
      this.rate = rate;
      this.amount = amount;
    }

    public void add(NightHoursDetail other) {
      this.nightHours = this.nightHours.add(other.nightHours);
      this.amount = this.amount.add(other.amount);
    }

    public BigDecimal getNightHours() {
      return nightHours;
    }

    public BigDecimal getRate() {
      return rate;
    }

    public BigDecimal getAmount() {
      return amount;
    }
  }
}
