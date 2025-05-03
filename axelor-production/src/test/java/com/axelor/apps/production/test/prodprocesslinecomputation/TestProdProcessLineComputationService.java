package com.axelor.apps.production.test.prodprocesslinecomputation;

import com.axelor.apps.production.db.Machine;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.repo.WorkCenterRepository;
import com.axelor.apps.production.service.ProdProcessLineComputationService;
import com.axelor.apps.production.service.ProdProcessLineComputationServiceImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

public class TestProdProcessLineComputationService {

  protected ProdProcessLineComputationService prodProcessLineComputationService;

  protected static WorkCenter humanWorkCenter;
  protected static WorkCenter machineWorkCenter;
  protected static WorkCenter bothWorkCenter;

  @BeforeAll
  static void prepareWorkCenter() {
    humanWorkCenter = new WorkCenter();
    humanWorkCenter.setWorkCenterTypeSelect(WorkCenterRepository.WORK_CENTER_TYPE_HUMAN);
    machineWorkCenter = new WorkCenter();
    Machine machine = new Machine();
    machineWorkCenter.setMachine(machine);
    machineWorkCenter.setWorkCenterTypeSelect(WorkCenterRepository.WORK_CENTER_TYPE_MACHINE);
    bothWorkCenter = new WorkCenter();
    bothWorkCenter.setWorkCenterTypeSelect(WorkCenterRepository.WORK_CENTER_TYPE_BOTH);
    bothWorkCenter.setMachine(machine);
  }

  @BeforeEach
  void prepare() {
    prodProcessLineComputationService = new ProdProcessLineComputationServiceImpl();
  }
}
