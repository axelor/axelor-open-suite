package com.axelor.apps.base.service.administration;

import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.db.SequenceVersion;
import com.axelor.db.Query;
import java.util.Optional;

public class SequenceVersionGeneratorQueryServiceImpl
    implements SequenceVersionGeneratorQueryService {

  @Override
  public Optional<SequenceVersion> lastActiveSequenceVersion(Sequence sequence) {
    return Optional.ofNullable(
        Query.of(SequenceVersion.class)
            .filter("self.sequence = :sequence")
            .bind("sequence", sequence)
            .order("-endDate")
            .fetchOne());
  }
}
