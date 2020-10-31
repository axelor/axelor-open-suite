/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.service.administration.sequence;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.inject.Beans;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SequenceProvider {

  private static final Logger LOG = LoggerFactory.getLogger(SequenceProvider.class);

  private static volatile Map<Long, SequenceProvider> instances = Maps.newHashMap();

  private final Lock lock = new ReentrantLock(true);
  private final Condition condition1 = lock.newCondition();
  private final Condition condition2 = lock.newCondition();

  private final ExecutorService executor = Executors.newSingleThreadExecutor();

  private Long sequenceId;
  private boolean inProcess = false;
  private boolean autoUnlock = true;
  private int processus = 0;

  private SequenceProvider(Sequence sequence) {
    this.sequenceId = sequence.getId();
  }

  public static boolean exist(String code) {
    return Beans.get(SequenceRepository.class).findByCodeSelect(code) != null;
  }

  public static boolean exist(String code, Company company) {
    return Beans.get(SequenceRepository.class).find(code, company) != null;
  }

  protected static SequenceProvider synchronizedInstances(Sequence sequence) {
    if (!instances.containsKey(sequence.getId())) {
      LOG.info("SYNCHRONIZED SEQUENCE ::: {}", sequence);
      synchronized (SequenceProvider.class) {
        if (!instances.containsKey(sequence.getId())) {
          instances.put(sequence.getId(), new SequenceProvider(sequence));
        }
      }
    }

    return instances.get(sequence.getId());
  }

  public void setAutoUnlock(boolean autoUnlock) {
    this.autoUnlock = autoUnlock;
  }

  public boolean getAutoUnlock() {
    return this.autoUnlock;
  }

  protected void addProcess() {
    processus++;
    LOG.debug("NB PROCESSUS AFTER ADD ::: {}", processus);
  }

  protected void delProcess() {
    processus--;
    LOG.debug("NB PROCESSUS AFTER DEL ::: {}", processus);
  }

  private ExecutorService getExecutorService() {
    return executor;
  }

  private Long getSequenceId() {
    return sequenceId;
  }

  public static SequenceProvider get(String code) {
    Sequence sequence = Beans.get(SequenceRepository.class).findByCodeSelect(code);
    checkNotNull(sequence, "Unknown sequence for code %s", code);
    return synchronizedInstances(sequence);
  }

  public static SequenceProvider get(String code, Company company) {
    Sequence sequence = Beans.get(SequenceRepository.class).find(code, company);
    checkNotNull(sequence, "Unknown sequence for code %s and company %s", code, company.getName());
    return synchronizedInstances(sequence);
  }

  public static SequenceProvider get(Sequence sequence) {
    Preconditions.checkNotNull(sequence);
    synchronizedInstances(sequence);
    return synchronizedInstances(sequence);
  }

  public String next() {
    lock();
    try {
      return compute();
    } finally {
      if (autoUnlock) {
        unlock();
      }
    }
  }

  public void reset() {
    lock();
    try {
      setSequence();
    } finally {
      unlock();
    }
  }

  public static void resetAll() {
    JPA.runInTransaction(
        () -> {
          Query query =
              JPA.em()
                  .createNativeQuery(
                      "UPDATE base_sequence SET next_num = 1 WHERE yearly_reset_ok = true");
          query.executeUpdate();
        });
  }

  protected void lock() {
    lock.lock();
    try {
      addProcess();
      while (inProcess) {
        condition1.await();
      }
      inProcess = true;
      condition2.signal();
    } catch (InterruptedException interruptedException) {
      throw new RuntimeException(interruptedException);
    } finally {
      lock.unlock();
    }
  }

  public void unlock() {
    unlock(false);
  }

  public void unlock(boolean procInError) {
    LOG.debug("AUTO UNLOCK ::: {}", autoUnlock);
    LOG.debug("PROC IN ERROR ::: {}", procInError);
    lock.lock();
    try {
      while (!inProcess) {
        condition2.await();
      }
      if (procInError) {
        rollBack();
      }
      delProcess();
      inProcess = false;
      shutdown(false);
      condition1.signal();

    } catch (InterruptedException interruptedException) {
      throw new RuntimeException(interruptedException);
    } finally {
      lock.unlock();
    }
  }

  protected void setSequence() {
    try {
      executor.submit(Beans.get(SequenceReseter.class).init(sequenceId)).get();
    } catch (Exception e) {
      propagateException(e);
    }
  }

  protected void rollBack() {
    try {
      executor.submit(Beans.get(SequenceRollback.class).init(sequenceId)).get();
    } catch (Exception e) {
      propagateException(e);
    }
  }

  protected String compute() {
    String sequence = null;

    try {
      sequence = executor.submit(Beans.get(SequenceComputer.class).init(sequenceId)).get();
    } catch (Exception e) {
      propagateException(e);
    }

    return sequence;
  }

  public void shutdown(boolean force) {
    LOG.debug("RUNNING PROCESSUS ::: {}", processus);
    if (processus == 0 || force) {
      LOG.info("!!! Shutdown sequence provider {} !!!", getSequenceId());
      getExecutorService().shutdown();
      instances.remove(getSequenceId());
    }
  }

  protected static void checkNotNull(Sequence sequence, String tmpl, Object... args) {
    if (sequence != null) {
      return;
    }

    throw new RuntimeException(
        new AxelorException(
            Sequence.class, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, tmpl, args));
  }

  protected void propagateException(Exception exception) {
    Sequence sequence = Beans.get(SequenceRepository.class).find(sequenceId);

    throw new RuntimeException(
        new AxelorException(
            exception,
            sequence,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            "Anomaly on sequence %s",
            sequence.getName()));
  }

  public static void shutdownAll() {
    for (SequenceProvider provider : instances.values()) {
      provider.shutdown(false);
    }
  }
}
