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
import com.axelor.apps.app.db.repo.AppRepository;
import com.axelor.db.Query;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.axelor.meta.db.repo.MetaModuleRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TestAppServiceImpl {

  private AppRepository appRepo;
  private AppServiceImpl appService;
  private List<String> savedCodes;

  @BeforeEach
  void prepare() {
    appRepo = Mockito.mock(AppRepository.class);
    savedCodes = new ArrayList<>();
    Mockito.when(appRepo.save(Mockito.any(App.class)))
        .thenAnswer(
            invocation -> {
              App app = invocation.getArgument(0);
              savedCodes.add(app.getCode());
              return app;
            });
    appService =
        new AppServiceImpl(
            appRepo,
            Mockito.mock(MetaFiles.class),
            Mockito.mock(AppVersionService.class),
            Mockito.mock(MetaModuleRepository.class),
            Mockito.mock(MetaFileRepository.class));
  }

  @Test
  void testInstallAppInstallsDependenciesFirst() throws Exception {
    App base = createApp(1L, "base", 1);
    App crm = createApp(2L, "crm", 2);
    App sale = createApp(3L, "sale", 3);
    crm.setDependsOnSet(Set.of(base));
    sale.setDependsOnSet(Set.of(base, crm));

    appService.installApp(sale, "fr");

    Assertions.assertEquals(List.of("base", "crm", "sale"), savedCodes);
    for (App app : List.of(base, crm, sale)) {
      Assertions.assertTrue(app.getActive());
      Assertions.assertTrue(app.getInitDataLoaded());
      Assertions.assertEquals("fr", app.getLanguageSelect());
    }
  }

  @Test
  void testInstallAppSkipsInstalledDependencies() throws Exception {
    App base = createApp(1L, "base", 1);
    base.setActive(true);
    App sale = createApp(2L, "sale", 2);
    sale.setDependsOnSet(Set.of(base));

    appService.installApp(sale, null);

    Assertions.assertEquals(List.of("sale"), savedCodes);
    Assertions.assertTrue(sale.getActive());
  }

  @Test
  void testInstallAppAlreadyActiveDoesNothing() throws Exception {
    App sale = createApp(1L, "sale", 1);
    sale.setActive(true);

    appService.installApp(sale, "en");

    Assertions.assertTrue(savedCodes.isEmpty());
  }

  @Test
  void testBulkInstallFollowsInstallOrder() throws Exception {
    App base = createApp(1L, "base", 1);
    App sale = createApp(2L, "sale", 5);
    App crm = createApp(3L, "crm", 3);

    appService.bulkInstall(List.of(sale, crm, base), false, "en");

    Assertions.assertEquals(List.of("base", "crm", "sale"), savedCodes);
  }

  @Test
  void testUnInstallAppRefusedWhileAnInstalledAppDependsOnIt() {
    App base = createApp(1L, "base", 1);
    base.setActive(true);
    App sale = createApp(2L, "sale", 2);
    sale.setActive(true);
    mockInstalledChildren(List.of(sale));

    Assertions.assertThrows(IllegalStateException.class, () -> appService.unInstallApp(base));
    Assertions.assertTrue(base.getActive());
    Assertions.assertTrue(savedCodes.isEmpty());
  }

  @Test
  void testUnInstallAppWithoutInstalledChildren() {
    App sale = createApp(1L, "sale", 2);
    sale.setActive(true);
    mockInstalledChildren(List.of());

    appService.unInstallApp(sale);

    Assertions.assertFalse(sale.getActive());
    Assertions.assertEquals(List.of("sale"), savedCodes);
  }

  @SuppressWarnings("unchecked")
  private void mockInstalledChildren(List<App> children) {
    Query<App> query = Mockito.mock(Query.class);
    Mockito.when(appRepo.all()).thenReturn(query);
    Mockito.doReturn(query).when(query).filter(Mockito.anyString(), Mockito.<Object>any());
    Mockito.doReturn(children).when(query).fetch();
  }

  private App createApp(Long id, String code, int installOrder) {
    App app = new App();
    app.setId(id);
    app.setCode(code);
    app.setName(code);
    app.setActive(false);
    app.setInstallOrder(installOrder);
    app.setLanguageSelect("en");
    Mockito.when(appRepo.find(id)).thenReturn(app);
    return app;
  }
}
