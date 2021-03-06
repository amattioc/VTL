/*******************************************************************************
 * Copyright 2020, Bank Of Italy
 *
 * Licensed under the EUPL, Version 1.2 (the "License");
 * You may not use this work except in compliance with the
 * License.
 * You may obtain a copy of the License at:
 *
 * https://joinup.ec.europa.eu/sites/default/files/custom-page/attachment/2020-03/EUPL-1.2%20EN.txt
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the License is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 *
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *******************************************************************************/
package it.bancaditalia.oss.vtl.impl.types.data.date;

import static java.time.temporal.ChronoUnit.DAYS;

import java.time.LocalDate;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.bancaditalia.oss.vtl.impl.types.domain.Duration;

class YearMonthDayHolder extends DateHolder<LocalDate>
{
	private final static Logger LOGGER = LoggerFactory.getLogger(YearMonthDayHolder.class);
	private static final long serialVersionUID = 1L;

	private final LocalDate date;

	public YearMonthDayHolder(int year, int month, int day)
	{
		date = LocalDate.of(year, month, day);
	}
	
	public YearMonthDayHolder(LocalDate date)
	{
		this.date = date;
	}
	
	@Override
	public long getLong(TemporalField field)
	{
		return date.getLong(field);
	}
	
	@Override
	public boolean isSupported(TemporalField field)
	{
		return date.isSupported(field);
	}
	
	@Override
	public int compareTo(DateHolder<?> other)
	{
		int c = date.compareTo(LocalDate.from(other));
		LOGGER.trace("Comparing {} and {} yield {}.", date, other, c);
		return c;
	}
	
	@Override
	public TemporalUnit getPeriod()
	{
		return DAYS;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		YearMonthDayHolder other = (YearMonthDayHolder) obj;
		if (date == null)
		{
			if (other.date != null)
				return false;
		}
		else if (!date.equals(other.date))
			return false;
		return true;
	}
	
	@Override
	public String toString()
	{
		return date.toString();
	}

	@Override
	public YearMonthDayHolder increment(long amount)
	{
		return new YearMonthDayHolder(date.plusDays(amount));
	}

	@Override
	public PeriodHolder<?> wrap(Duration frequency)
	{
		switch (frequency)
		{
			case A: return new YearPeriodHolder(this);
			case S: return new YearSemesterPeriodHolder(this);
			case Q: return new YearQuarterPeriodHolder(this);
			case M: return new YearMonthPeriodHolder(this);
		default:
			throw new UnsupportedOperationException("Cannot wrap " + this + " with duration " + frequency + " or wrapping time_period not implemented"); 
		}
	}
}
