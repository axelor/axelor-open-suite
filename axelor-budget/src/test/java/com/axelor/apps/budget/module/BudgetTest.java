/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.budget.module;

import com.axelor.db.JPA;
import com.axelor.db.JpaSupport;
import com.axelor.test.GuiceExtension;
import com.axelor.test.GuiceModules;
import javax.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GuiceExtension.class)
@GuiceModules(BudgetTestModule.class)
@TestMethodOrder(MethodOrderer.MethodName.class)
public abstract class BudgetTest extends JpaSupport {
  @AfterAll
  public static void tearDownClass() {
    // Close the entity manager factory else when tests ends, the connection
    // aren't resealed. After many tests there is too many clients
    EntityManagerFactory managerFactory = JPA.em().getEntityManagerFactory();
    if (managerFactory != null) {
      managerFactory.close();
    }
  }
}
