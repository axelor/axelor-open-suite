package com.axelor.csv.script;

import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.inject.Beans;

import java.util.Map;

public class SequenceScript {
    public Object computeFullname(Object bean, Map<String, Object> values) {
        assert bean instanceof Sequence;
        Sequence sequence = ((Sequence) bean);

        sequence.setFullName(Beans.get(SequenceService.class).computeFullName(sequence));

        return sequence;
    }
}
