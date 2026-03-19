/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.stock.utils;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.Query;
import com.axelor.inject.Beans;
import com.axelor.utils.ThrowConsumer;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchProcessorHelper {

  private static final Logger logger =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final int batchSize;
  private final boolean flushAfterBatch;
  private final int clearEveryNBatch;
  private final boolean loggingEnabled;

  private BatchProcessorHelper(Builder builder) {
    this.batchSize = builder.batchSize;
    this.flushAfterBatch = builder.flushAfterBatch;
    this.clearEveryNBatch = builder.clearEveryNBatch;
    this.loggingEnabled = builder.loggingEnabled;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static BatchProcessorHelper of() {
    return new Builder().build();
  }

  public static class Builder {
    private int batchSize = resolveDefaultBatchSize();
    private boolean flushAfterBatch = true;
    private int clearEveryNBatch = 1;
    private boolean loggingEnabled = false;

    public Builder batchSize(int batchSize) {
      this.batchSize = batchSize;
      return this;
    }

    public Builder flushAfterBatch(boolean flushAfterBatch) {
      this.flushAfterBatch = flushAfterBatch;
      return this;
    }

    public Builder clearEveryNBatch(int clearEveryNBatch) {
      this.clearEveryNBatch = clearEveryNBatch;
      return this;
    }

    public Builder loggingEnabled(boolean loggingEnabled) {
      this.loggingEnabled = loggingEnabled;
      return this;
    }

    public BatchProcessorHelper build() {
      return new BatchProcessorHelper(this);
    }

    private static int resolveDefaultBatchSize() {
      int defaultBatchLimit = 40;
      try {

        Integer batchLimit =
            Beans.get(AppBaseService.class).getAppBase().getDefaultBatchFetchLimit();
        return batchLimit > 0 ? batchLimit : defaultBatchLimit;
      } catch (Exception e) {
        return defaultBatchLimit;
      }
    }
  }

  public <T extends Model, E extends Exception> void forEachByQuery(
      Query<T> queryBase, ThrowConsumer<T, E> action) throws E {
    forEachByQuery(queryBase, action, null);
  }

  public <T extends Model, E extends Exception> void forEachByQuery(
      Query<T> queryBase, ThrowConsumer<T, E> action, Runnable postBatchAction) throws E {

    logCallerContext();
    long lastSeenId = 0L;
    int batchCount = 0;
    int totalProcessed = 0;
    List<T> entities;
    do {
      entities = queryBase.bind("lastSeenId", lastSeenId).autoFlush(false).fetch(batchSize);

      process(action, entities);

      if (CollectionUtils.isNotEmpty(entities)) {
        lastSeenId = entities.get(entities.size() - 1).getId();
        batchCount++;
        totalProcessed += entities.size();
        afterBatch(postBatchAction, batchCount, totalProcessed);
      }
    } while (!entities.isEmpty());
  }

  public <T extends Model, E extends Exception> void forEachByIds(
      Class<T> entityClass, Set<Long> ids, ThrowConsumer<T, E> action) throws E {
    forEachByIds(entityClass, ids, action, null);
  }

  public <T extends Model, E extends Exception> void forEachByIds(
      Class<T> entityClass, Set<Long> ids, ThrowConsumer<T, E> action, Runnable postBatchAction)
      throws E {
    logCallerContext();
    if (CollectionUtils.isEmpty(ids)) {
      return;
    }
    long lastSeenId = 0L;
    int batchCount = 0;
    int totalProcessed = 0;
    List<T> entities;

    do {
      entities =
          Query.of(entityClass)
              .filter("self.id IN :ids AND self.id > :lastSeenId")
              .bind("ids", ids)
              .bind("lastSeenId", lastSeenId)
              .autoFlush(false)
              .order("id")
              .fetch(batchSize);

      process(action, entities);
      if (CollectionUtils.isNotEmpty(entities)) {
        lastSeenId = entities.get(entities.size() - 1).getId();
        batchCount++;
        totalProcessed += entities.size();
        afterBatch(postBatchAction, batchCount, totalProcessed);
      }
    } while (!entities.isEmpty());
  }

  public <T extends Model, E extends Exception> void forEachByEntities(
      Class<T> entityClass, List<T> entities, ThrowConsumer<T, E> action) throws E {
    forEachByEntities(entityClass, entities, action, null);
  }

  public <T extends Model, E extends Exception> void forEachByEntities(
      Class<T> entityClass, List<T> entities, ThrowConsumer<T, E> action, Runnable postBatchAction)
      throws E {
    if (CollectionUtils.isEmpty(entities)) {
      return;
    }
    Set<Long> ids = entities.stream().map(Model::getId).collect(Collectors.toSet());
    forEachByIds(entityClass, ids, action, postBatchAction);
  }

  public <T extends Model> void forEachByQuery(Query<T> queryBase, Consumer<T> action) {
    forEachByQuery(queryBase, action, null);
  }

  public <T extends Model> void forEachByQuery(
      Query<T> queryBase, Consumer<T> action, Runnable postBatchAction) {
    try {
      forEachByQuery(queryBase, wrap(action), postBatchAction);
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public <T extends Model> void forEachByIds(
      Class<T> entityClass, Set<Long> ids, Consumer<T> action) {
    forEachByIds(entityClass, ids, action, null);
  }

  public <T extends Model> void forEachByIds(
      Class<T> entityClass, Set<Long> ids, Consumer<T> action, Runnable postBatchAction) {
    try {
      forEachByIds(entityClass, ids, wrap(action), postBatchAction);
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public <T extends Model> void forEachByEntities(
      Class<T> entityClass, List<T> entities, Consumer<T> action) {
    forEachByEntities(entityClass, entities, action, null);
  }

  public <T extends Model> void forEachByEntities(
      Class<T> entityClass, List<T> entities, Consumer<T> action, Runnable postBatchAction) {
    try {
      forEachByEntities(entityClass, entities, wrap(action), postBatchAction);
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private <T extends Model, E extends Exception> void process(
      ThrowConsumer<T, E> action, List<T> entities) throws E {
    for (T entity : entities) {
      action.accept(entity);
    }
  }

  private <T> ThrowConsumer<T, Exception> wrap(Consumer<T> consumer) {
    return consumer::accept;
  }

  private void afterBatch(Runnable postBatchAction, int batchCount, int totalProcessed) {

    if (flushAfterBatch) {
      JPA.flush();
    }

    if (clearEveryNBatch > 0 && batchCount % clearEveryNBatch == 0) {
      JPA.clear();
    }

    if (postBatchAction != null) {
      postBatchAction.run();
    }

    if (loggingEnabled) {
      logger.debug("Processed {} records so far [batch={}]", totalProcessed, batchCount);
    }
  }

  private void logCallerContext() {
    if (!loggingEnabled) {
      return;
    }
    StackTraceElement[] stack = Thread.currentThread().getStackTrace();
    String helperClass = BatchProcessorHelper.class.getName();

    for (int i = 2; i < stack.length; i++) {
      StackTraceElement frame = stack[i];
      String className = frame.getClassName();

      if (!className.equals(helperClass) && !className.startsWith("java.lang.Thread")) {
        logger.debug(
            "StockBatchProcessorHelper called from {}.{} (line {})",
            className,
            frame.getMethodName(),
            frame.getLineNumber());
        break;
      }
    }
  }
}
