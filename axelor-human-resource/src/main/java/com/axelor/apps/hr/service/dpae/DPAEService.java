package com.axelor.apps.hr.service.dpae;

import com.axelor.apps.hr.db.DPAE;
import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaFile;
import java.io.IOException;
import java.util.List;

public interface DPAEService {

  public List<String> checkDPAEValidity(DPAE dpae);

  public void sendSingle(DPAE dpae) throws AxelorException;

  public MetaFile sendMultiple(List<Long> dpaeIdList) throws AxelorException, IOException;
}
