
package com.team16.swipeinvite;



public class Date {
	int year;
	int month;
	int day;
	int hours;
	int minutes;
	
	public Date(int dateyear, int datemonth, int dateday, int datehour, int datemin) {
		this.year = dateyear;
		this.month = datemonth;
		this.day = dateday;
		this.hours = datehour;
		this.minutes = datemin;
	}
	
	public int getYear()
	{
		return year;
		
	}
	
	public int getMonth()
	{
		return month;
		
	}
	
	public int getDay()
	{
		return day;
		
	}
	
	public int getHour()
	{
		return hours;
		
	}
	
	public int getMinutes()
	{
		return minutes;
		
	}
	
	public void setYear(int yr)
	{
		year = yr;
		
	}
	
	public void setMonth(int mnth)
	{
		month = mnth;
		
	}
	
	public void setDay(int dy)
	{
		day = dy;
		
	}
	
	public void setHour(int hr)
	{
		hours= hr;
		
	}
	
	public void setMinutes(int min)
	{
		minutes = min;
		
	}

}
