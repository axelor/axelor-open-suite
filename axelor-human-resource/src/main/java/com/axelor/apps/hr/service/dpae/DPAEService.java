package com.axelor.apps.hr.service.dpae;

import com.axelor.apps.hr.db.DPAE;
import java.util.List;

public interface DPAEService {
  
  public List<String> checkDPAEValidity(DPAE dpae);

  public void exportSingle(DPAE dpae);

  public void exportMultiple(List<Long> dpaeIdList);
}
