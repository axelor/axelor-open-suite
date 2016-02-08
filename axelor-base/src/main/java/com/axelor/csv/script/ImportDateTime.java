/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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
package com.axelor.csv.script;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.LocalDateTime;

import com.google.common.base.Strings;


public class ImportDateTime {
	String pat = "((\\+|\\-)?[0-9]{1,%s}%s)";
	String dt = "[0-9]{4}-[0-9]{2}-[0-9]{2}";
	Pattern  patternYear = Pattern.compile("[0-9]{1,4}");
	Pattern  patternMonth = Pattern.compile("[0-9]{1,2}");
	
	public LocalDateTime updateYear(LocalDateTime dateTime, String year){
		if(!Strings.isNullOrEmpty(year)){
			Matcher matcher = patternYear.matcher(year);
			if(matcher.find()){
				Integer years = Integer.parseInt(matcher.group());
				if(year.startsWith("+"))
					dateTime = dateTime.plusYears(years);
				else if(year.startsWith("-"))
					dateTime = dateTime.minusYears(years);
				else
					dateTime = dateTime.withYear(years);
			}
		}
		return dateTime;
	}
	
	public LocalDateTime updateMonth(LocalDateTime dateTime, String month){
		if(!Strings.isNullOrEmpty(month)){
			Matcher matcher = patternMonth.matcher(month);
			if(matcher.find()){
				Integer months = Integer.parseInt(matcher.group());
				if(month.startsWith("+"))
					dateTime = dateTime.plusMonths(months);
				else if(month.startsWith("-"))
					dateTime = dateTime.minusMonths(months);
				else
					dateTime = dateTime.withMonthOfYear(months);
			}
		}
		return dateTime;
	}
	
	public LocalDateTime updateDay(LocalDateTime dateTime, String day){
		if(!Strings.isNullOrEmpty(day)){
			Matcher matcher = patternMonth.matcher(day);
			if(matcher.find()){
				Integer days = Integer.parseInt(matcher.group());
				if(day.startsWith("+"))
					dateTime = dateTime.plusDays(days);
				else if(day.startsWith("-"))
					dateTime = dateTime.minusDays(days);
				else
					dateTime = dateTime.withDayOfMonth(days);
			}
		}
		return dateTime;
	}
	
	public LocalDateTime updateHour(LocalDateTime dateTime, String hour){
		if(!Strings.isNullOrEmpty(hour)){
			Matcher matcher = patternMonth.matcher(hour);
			if(matcher.find()){
				Integer hours = Integer.parseInt(matcher.group());
				if(hour.startsWith("+"))
					dateTime = dateTime.plusHours(hours);
				else if(hour.startsWith("-"))
					dateTime = dateTime.minusHours(hours);
				else
					dateTime = dateTime.withHourOfDay(hours);
			}
		}
		return dateTime;
	}
	
	public LocalDateTime updateMinute(LocalDateTime dateTime, String minute){
		if(!Strings.isNullOrEmpty(minute)){
			Matcher matcher = patternMonth.matcher(minute);
			if(matcher.find()){
				Integer minutes = Integer.parseInt(matcher.group());
				if(minute.startsWith("+"))
					dateTime = dateTime.plusMinutes(minutes);
				else if(minute.startsWith("-"))
					dateTime = dateTime.minusMinutes(minutes);
				else
					dateTime = dateTime.withMinuteOfHour(minutes);
			}
		}
		return dateTime;
	}
	
	public LocalDateTime updateSecond(LocalDateTime dateTime, String second){
		if(!Strings.isNullOrEmpty(second)){
			Matcher matcher = patternMonth.matcher(second);
			if(matcher.find()){
				Integer seconds = Integer.parseInt(matcher.group());
				if(second.startsWith("+"))
					dateTime = dateTime.plusSeconds(seconds);
				else if(second.startsWith("-"))
					dateTime = dateTime.minusSeconds(seconds);
				else
					dateTime = dateTime.withSecondOfMinute(seconds);
			}
		}
		return dateTime;
	}
	
	public String importDate(String inputDate) {
		
		String patDate = "("+dt+"|TODAY)(\\[("+String.format(pat,4,"y")+
											"?"+String.format(pat,2,"M")+
										    "?"+String.format(pat,2,"d")+"?"+")\\])?";
		try{
			if(!Strings.isNullOrEmpty(inputDate) && inputDate.matches(patDate)){
				List<String> dates = Arrays.asList(inputDate.split("\\["));
				inputDate = dates.get(0).equals("TODAY") ? new LocalDateTime().toString("yyyy-MM-dd") : dates.get(0);
				if(dates.size() > 1){
					LocalDateTime dateTime = new LocalDateTime(inputDate);
					Matcher matcher = Pattern.compile(String.format(pat,4,"y")).matcher(dates.get(1));
					if(matcher.find())
						dateTime = updateYear(dateTime, matcher.group());
					matcher = Pattern.compile(String.format(pat,2,"M")).matcher(dates.get(1));
					if(matcher.find())
						dateTime = updateMonth(dateTime,matcher.group());
					matcher = Pattern.compile(String.format(pat,2,"d")).matcher(dates.get(1));
					if(matcher.find())
							dateTime = updateDay(dateTime, matcher.group());
					return dateTime.toLocalDate().toString();
				}else return inputDate;
			}else return null;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	} 
	
	public String importDateTime(String inputDateTime) {
		String tm = "[0-9]{2}:[0-9]{2}:[0-9]{2}";
		String patTime = "("+dt+" "+tm+"|NOW)(\\[(" +String.format(pat,4,"y")+"?"
													+String.format(pat,2,"M")+"?"
													+String.format(pat,2,"d")+"?"
													+String.format(pat,2,"H")+"?"
													+String.format(pat,2,"m")+"?"
													+String.format(pat,2,"s")+"?"
													+")\\])?";
		try{
			if(!Strings.isNullOrEmpty(inputDateTime) && inputDateTime.matches(patTime)){
				List<String> timeList = Arrays.asList(inputDateTime.split("\\["));
				inputDateTime = timeList.get(0).equals("NOW") ? new LocalDateTime().toString("yyyy-MM-dd HH:mm:ss") : timeList.get(0);
				if(timeList.size() > 1){
					LocalDateTime dateTime = new LocalDateTime(inputDateTime.replace(" ","T"));
					Matcher matcher = Pattern.compile(String.format(pat,4,"y")).matcher(timeList.get(1));
					if(matcher.find())
						dateTime = updateYear(dateTime, matcher.group());
					matcher = Pattern.compile(String.format(pat,2,"M")).matcher(timeList.get(1));
					if(matcher.find())
						dateTime = updateMonth(dateTime,matcher.group());
					matcher = Pattern.compile(String.format(pat,2,"d")).matcher(timeList.get(1));
					if(matcher.find())
						dateTime = updateDay(dateTime,matcher.group());
					matcher = Pattern.compile(String.format(pat,2,"H")).matcher(timeList.get(1));
					if(matcher.find())
						dateTime = updateHour(dateTime,matcher.group());
					matcher = Pattern.compile(String.format(pat,2,"m")).matcher(timeList.get(1));
					if(matcher.find())
						dateTime = updateMinute(dateTime,matcher.group());
					matcher = Pattern.compile(String.format(pat,2,"s")).matcher(timeList.get(1));
					if(matcher.find())
						dateTime = updateSecond(dateTime,matcher.group());
					return dateTime.toString();
				}
				return inputDateTime.replace(" ","T");
			}
			return null;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
		
}
