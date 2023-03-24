package com.axelor.apps.base.service.research;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.ResearchRequest;
import com.axelor.apps.base.db.ResearchResultLine;
import java.util.List;
import java.util.Map;

public interface ResearchRequestService {

  public List<ResearchResultLine> searchObject(
      Map<String, Object> searchParams, ResearchRequest researchRequest) throws AxelorException;
}
