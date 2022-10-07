package com.axelor.apps.base.service.administration;

import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.db.SequenceVersion;
import java.util.Optional;

public interface SequenceVersionGeneratorQueryService {

  /**
   * Fetch last active sequence version.
   *
   * @param sequence the parent sequence
   * @return an optional containing the found version an empty optional if there is no version,
   */
  Optional<SequenceVersion> lastActiveSequenceVersion(Sequence sequence);
}
