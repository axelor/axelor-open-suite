package com.axelor.csv.script;

import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.db.SequenceVersion;
import com.axelor.apps.base.db.repo.SequenceBaseRepository;
import com.axelor.apps.base.db.repo.SequenceVersionRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;
import java.util.Map;

public class ImportSequence {

  private SequenceBaseRepository sequenceRepository;
  private SequenceVersionRepository sequenceVersionRepository;
  private SequenceService sequenceService;

  @Inject
  public ImportSequence(
      SequenceBaseRepository sequenceBaseRepository,
      SequenceService sequenceService,
      SequenceVersionRepository sequenceVersionRepository) {
    this.sequenceRepository = sequenceBaseRepository;
    this.sequenceService = sequenceService;
    this.sequenceVersionRepository = sequenceVersionRepository;
  }

  @Transactional(rollbackOn = Exception.class)
  public Object importSequence(Object bean, Map<String, Object> values) throws AxelorException {
    assert bean instanceof Sequence;
    Sequence sequence = (Sequence) bean;
    sequence.setFullName(sequenceService.computeFullName(sequence));
    SequenceVersion seqVersion = sequenceService.getVersion(sequence, LocalDate.now());
    sequenceVersionRepository.save(seqVersion);
    sequence.setVersion(seqVersion.getId().intValue());
    sequenceRepository.save(sequence);
    return sequence;
  }
}
