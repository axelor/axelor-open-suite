package com.axelor.apps.businessproject.service.taskreport;

import com.axelor.apps.businessproject.db.ExtraExpenseLine;
import com.axelor.apps.businessproject.db.TaskReport;
import java.util.List;

public interface TaskReportExpenseService {

  /**
   * Create or update extra expense lines from task report based on travel expenses, tools usage,
   * and dirt allowance checkboxes
   *
   * @param taskReport
   * @return List of ExtraExpenseLine created or updated
   * @throws Exception
   */
  List<ExtraExpenseLine> createOrUpdateExtraExpenseLinesFromTaskReport(TaskReport taskReport)
      throws Exception;
}
