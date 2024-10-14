package com.axelor.apps.base.listener;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.interfaces.RecursiveModel;
import java.lang.invoke.MethodHandles;
import java.util.Objects;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import com.axelor.i18n.I18n;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecursiveModelListener {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final int MAX_DEPTH = 1000;

  @PrePersist
  @PreUpdate
  public void checkRecursiveLoop(RecursiveModel model) throws AxelorException {
    Objects.requireNonNull(model);
    LOG.debug("Checking recursive loop {}", model);

    checkRecursiveLoop(model.getParent(), model, 0);
  }

  protected void checkRecursiveLoop(RecursiveModel model, RecursiveModel initialModel, int depth)
      throws AxelorException {
    // Model is null, meaning that this is the end and no error occured
    LOG.debug("Current depth {}", depth);
    LOG.debug("Comparing {} <=> {}", model, initialModel);
    if (model == null) {
      return;
    }

    // Maximum depth reached
    if (depth > MAX_DEPTH) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.RECURSIVE_MODEL_MAXIMUM_DEPTH_REACHED),
              initialModel);
    }

    // Model and initial model are the same => Loop
    if (model.equals(initialModel)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.RECURSIVE_MODEL_LOOP_DETECTED),
              initialModel);
    }

    // No loop here, checking next parent
    LOG.debug("Finished depth {}", depth);
    checkRecursiveLoop(model.getParent(), initialModel, depth + 1);
  }
}
