package com.axelor.apps.base.service.administration;

import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.db.SequenceVersion;
import java.time.LocalDate;

public interface SequenceVersionGeneratorService {
  SequenceVersion createNewSequenceVersion(Sequence sequence, LocalDate refDate);
}
