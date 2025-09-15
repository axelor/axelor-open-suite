package com.axelor.apps.base.db.repo;

import com.axelor.apps.base.db.MapView;
import com.axelor.apps.base.service.mapConfigurator.MapViewService;
import com.google.inject.Inject;

public class MapViewBaseRepository extends MapViewRepository {

  protected final MapViewService mapViewService;

  @Inject
  public MapViewBaseRepository(MapViewService mapViewService) {
    this.mapViewService = mapViewService;
  }

  @Override
  public MapView save(MapView mapView) {
    mapViewService.computeMapActionView(mapView);
    return super.save(mapView);
  }
}
