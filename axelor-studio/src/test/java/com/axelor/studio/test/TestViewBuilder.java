package com.axelor.studio.test;

import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.studio.db.ViewBuilder;
import com.axelor.studio.db.repo.ViewBuilderRepository;
import com.axelor.test.GuiceModules;
import com.axelor.test.GuiceRunner;
import java.lang.invoke.MethodHandles;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(GuiceRunner.class)
@GuiceModules(TestModule.class)
public class TestViewBuilder {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Test
  public void test() throws AxelorException {

    List<ViewBuilder> viewBuilders = Beans.get(ViewBuilderRepository.class).all().fetch();

    for (ViewBuilder viewBuilder : viewBuilders) {
      String xml = null;
      switch (viewBuilder.getViewType()) {
          //    	case "kanban":
          //    		xml = Beans.get(KanbanBuilderService.class).build(viewBuilder, "axelor-base");
          //    		break;
          //    	case "calendar":
          //    		xml = Beans.get(CalendarBuilderService.class).build(viewBuilder, "axelor-base");
          //    		break;
          //        case "cards":
          //          xml = Beans.get(CardsBuilderService.class).build(viewBuilder, "axelor-base");
          //          break;
      }
      log.debug("View xml generated: {}", xml);
    }
  }
}
