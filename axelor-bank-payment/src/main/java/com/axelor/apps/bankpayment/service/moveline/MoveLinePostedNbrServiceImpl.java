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
package com.axelor.apps.bankpayment.service.moveline;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.common.StringUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MoveLinePostedNbrServiceImpl implements MoveLinePostedNbrService {

  @Override
  public MoveLine setMoveLinePostedNbr(MoveLine moveLine, String postedNbr) {
    String posted = moveLine.getPostedNbr();
    if (StringUtils.notEmpty(posted)) {
      List<String> postedNbrs = new ArrayList<String>(Arrays.asList(posted.split(",")));
      postedNbrs.add(postedNbr);
      posted = String.join(",", postedNbrs);
    } else posted = postedNbr;
    moveLine.setPostedNbr(posted);
    return moveLine;
  }

  @Override
  public MoveLine removePostedNbr(MoveLine moveLine, String postedNbr) {
    String posted = moveLine.getPostedNbr();
    List<String> postedNbrs = new ArrayList<String>(Arrays.asList(posted.split(",")));
    postedNbrs.remove(postedNbr);
    posted = String.join(",", postedNbrs);
    moveLine.setPostedNbr(posted);
    return moveLine;
  }
}
