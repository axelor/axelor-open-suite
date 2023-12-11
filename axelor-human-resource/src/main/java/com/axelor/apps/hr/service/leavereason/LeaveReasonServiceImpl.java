package com.axelor.apps.hr.service.leavereason;

import com.axelor.apps.hr.db.LeaveReason;
import com.axelor.apps.hr.db.repo.LeaveReasonRepository;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaSelectItem;
import com.axelor.meta.db.repo.MetaSelectItemRepository;
import com.google.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

public class LeaveReasonServiceImpl implements LeaveReasonService {

  protected MetaSelectItemRepository metaSelectItemRepository;

  @Inject
  public LeaveReasonServiceImpl(MetaSelectItemRepository metaSelectItemRepository) {
    this.metaSelectItemRepository = metaSelectItemRepository;
  }

  @Override
  public boolean isExceptionalDaysReason(LeaveReason leaveReason) {
    return leaveReason.getLeaveReasonTypeSelect()
        == LeaveReasonRepository.TYPE_SELECT_EXCEPTIONAL_DAYS;
  }

  @Override
  public List<Integer> getIncrementLeaveReasonTypeSelects() {
    return Beans.get(MetaSelectItemRepository.class).all()
        .filter("self.select.name = :selectName AND self.value != :exceptionalSelect")
        .bind("selectName", "hr.leave.reason.type.select")
        .bind("exceptionalSelect", LeaveReasonRepository.TYPE_SELECT_EXCEPTIONAL_DAYS).fetch()
        .stream()
        .map(MetaSelectItem::getValue)
        .collect(Collectors.toList())
        .stream()
        .map(Integer::valueOf)
        .collect(Collectors.toList());
  }
}
