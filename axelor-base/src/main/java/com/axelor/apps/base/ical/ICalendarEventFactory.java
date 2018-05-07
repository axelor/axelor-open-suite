package com.axelor.apps.base.ical;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.axelor.apps.base.db.ICalendar;
import com.axelor.apps.base.db.ICalendarEvent;
import com.axelor.apps.base.db.repo.ICalendarRepository;


public class ICalendarEventFactory {

	private static final Map<String, Supplier<ICalendarEvent>> map = new HashMap<>();

	static {
		map.put(ICalendarRepository.ICAL_ONLY, ICalendarEvent::new);
	}

	public static ICalendarEvent getNewIcalEvent(ICalendar calendar) {
		Supplier<ICalendarEvent> supplier = map.getOrDefault(calendar.getSynchronizationSelect(), ICalendarEvent::new);
		return supplier.get();
	}
	
	public static void register(String selection, Supplier<ICalendarEvent> eventSupplier) {
		map.put(selection, eventSupplier);
	}
}
