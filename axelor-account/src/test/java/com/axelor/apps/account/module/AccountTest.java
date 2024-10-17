package com.axelor.apps.account.module;

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
@GuiceModules(AccountTestModule.class)
@TestMethodOrder(MethodOrderer.MethodName.class)
public abstract class AccountTest extends JpaSupport {
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
