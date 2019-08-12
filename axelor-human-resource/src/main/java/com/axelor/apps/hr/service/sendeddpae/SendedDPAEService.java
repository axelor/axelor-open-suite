package com.axelor.apps.hr.service.sendeddpae;

import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.message.db.Message;
import com.axelor.meta.db.MetaFile;
import java.util.List;

public interface SendedDPAEService {

  public void createSendedDPAE(Message message, MetaFile metaFile, List<Employee> employeeList);
}
