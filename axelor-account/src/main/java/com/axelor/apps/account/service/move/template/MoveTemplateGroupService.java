package com.axelor.apps.account.service.move.template;

import com.axelor.apps.base.AxelorException;
import java.util.Map;

public interface MoveTemplateGroupService {
  Map<String, Object> getOnNewValuesMap(
      Long companyId, Long moveTemplateTypeId, Long moveTemplateOriginId) throws AxelorException;
}
