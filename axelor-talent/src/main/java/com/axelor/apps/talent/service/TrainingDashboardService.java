package com.axelor.apps.talent.service;

import com.axelor.apps.base.AxelorException;
import java.util.List;
import java.util.Map;

public interface TrainingDashboardService {

  List<Map<String, Object>> getTrainingData() throws AxelorException;
}
