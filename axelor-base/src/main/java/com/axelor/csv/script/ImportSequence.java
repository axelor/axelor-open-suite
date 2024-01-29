/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.csv.script;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.db.SequenceVersion;
import com.axelor.apps.base.db.repo.SequenceBaseRepository;
import com.axelor.apps.base.db.repo.SequenceVersionRepository;
import com.axelor.apps.base.service.administration.SequenceService;
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
    sequence.addSequenceVersionListItem(seqVersion);
    sequenceRepository.save(sequence);
    return sequence;
  }
}
