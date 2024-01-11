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
package com.axelor.apps.base.callable;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionResponse;
import com.axelor.utils.exception.ToolExceptionMessage;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Tool class to call specific callable services in a controller.
 *
 * @param <V> the type of the callable service.
 */
public class ControllerCallableTool<V> {

  /**
   * Run the given callable in a separate thread. display any occurring exception in the given
   * response. If the thread is not over before the timeout, send a notification to the user.
   *
   * @param callable a callable service
   * @param response a response available in a controller
   * @return what is returned by the service
   */
  public V runInSeparateThread(Callable<V> callable, ActionResponse response) {
    V result = null;
    ExecutorService executor = Executors.newSingleThreadExecutor();

    // Start thread
    Future<V> future = executor.submit(callable);

    int processTimeout = Beans.get(AppBaseService.class).getProcessTimeout();
    // Wait processTimeout seconds
    try {
      result = future.get(processTimeout, TimeUnit.SECONDS);
    } catch (ExecutionException e) {
      // cause already traced in traceback
      response.setInfo(e.getCause().getMessage());
    } catch (TimeoutException e) {
      response.setNotify(I18n.get(ToolExceptionMessage.PROCESS_BEING_COMPUTED));
    } catch (InterruptedException e) {
      TraceBackService.trace(e);
      Thread.currentThread().interrupt();
    }
    return result;
  }
}
