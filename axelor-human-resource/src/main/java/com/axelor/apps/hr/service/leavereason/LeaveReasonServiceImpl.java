/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
