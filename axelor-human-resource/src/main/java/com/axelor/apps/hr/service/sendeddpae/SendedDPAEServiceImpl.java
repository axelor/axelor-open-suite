package com.axelor.apps.hr.service.sendeddpae;

import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.SendedDPAE;
import com.axelor.apps.hr.db.repo.SendedDPAERepository;
import com.axelor.apps.message.db.Message;
import com.axelor.meta.db.MetaFile;
import com.google.inject.persist.Transactional;
import java.util.HashSet;
import java.util.List;
import javax.inject.Inject;

public class SendedDPAEServiceImpl implements SendedDPAEService {

  @Inject protected SendedDPAERepository sendedDPAERepo;

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Transactional
  @Override
  public void createSendedDPAE(Message message, MetaFile metaFile, List<Employee> employeeList) {
    SendedDPAE sendedDPAE = new SendedDPAE();
    sendedDPAE.setMessage(message);
    sendedDPAE.setMetaFile(metaFile);
    sendedDPAE.setEmployeeList(new HashSet(employeeList));
    sendedDPAERepo.save(sendedDPAE);
  }
}
