package com.axelor.csv.script;

import com.axelor.apps.account.db.MoveTemplate;

import java.io.IOException;
import java.util.Map;

public class ImportMoveTemplate {
    public Object setCompany(Object bean, Map<String, Object> values) throws IOException {
        assert bean instanceof MoveTemplate;
        MoveTemplate moveTemplate = (MoveTemplate) bean;

        try {
            if (moveTemplate.getJournal() != null) {
                moveTemplate.setCompany(moveTemplate.getJournal().getCompany());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return moveTemplate;
    }
}
