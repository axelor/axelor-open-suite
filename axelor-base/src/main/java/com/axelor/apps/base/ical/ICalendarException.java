package com.axelor.apps.base.ical;

@SuppressWarnings("serial")
public class ICalendarException extends Exception {

	public ICalendarException() {
	}

	public ICalendarException(String message, Throwable cause) {
		super(message, cause);
	}

	public ICalendarException(String message) {
		super(message);
	}

	public ICalendarException(Throwable cause) {
		super(cause);
	}
}
