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
package com.axelor.apps.base.service.app;

import com.axelor.apps.app.db.App;
import com.axelor.meta.db.MetaModule;
import com.axelor.meta.db.repo.MetaModuleRepository;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TestAppVersionServiceImpl {

  @Test
  void testAppVersionIsTheHighestModuleVersion() {
    MetaModuleRepository metaModuleRepository = Mockito.mock(MetaModuleRepository.class);
    mockModule(metaModuleRepository, "axelor-sale", "9.2.0-SNAPSHOT");
    mockModule(metaModuleRepository, "axelor-crm", "9.1.3");
    App app = new App();
    app.setModules("axelor-crm,axelor-sale,axelor-unknown");

    String version = new AppVersionServiceImpl(metaModuleRepository).getAppVersion(app);

    Assertions.assertEquals("9.2.0", version);
  }

  @Test
  void testAppWithoutModuleHasNoVersion() {
    App app = new App();

    String version =
        new AppVersionServiceImpl(Mockito.mock(MetaModuleRepository.class)).getAppVersion(app);

    Assertions.assertNull(version);
  }

  @Test
  void testFindMaxVersionIgnoresNonSemanticVersions() {
    Assertions.assertEquals(
        "4.0.7", AppVersionServiceImpl.findMaxVersion(List.of("dev", "4.0.7", "3.5.9")));
    Assertions.assertNull(AppVersionServiceImpl.findMaxVersion(List.of("dev")));
  }

  private void mockModule(MetaModuleRepository repository, String name, String version) {
    MetaModule module = new MetaModule();
    module.setName(name);
    module.setModuleVersion(version);
    Mockito.when(repository.findByName(name)).thenReturn(module);
  }
}
