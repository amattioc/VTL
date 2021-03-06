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
package it.bancaditalia.oss.vtl.impl.types.data;

import static it.bancaditalia.oss.vtl.impl.types.domain.Domains.DURATIONDS;

import java.time.temporal.TemporalUnit;
import java.util.HashMap;
import java.util.Map;

import it.bancaditalia.oss.vtl.impl.types.domain.Duration;
import it.bancaditalia.oss.vtl.impl.types.exceptions.VTLCastException;
import it.bancaditalia.oss.vtl.model.data.ScalarValue;
import it.bancaditalia.oss.vtl.model.data.ValueDomain;
import it.bancaditalia.oss.vtl.model.data.ValueDomainSubset;
import it.bancaditalia.oss.vtl.model.domain.DurationDomain;
import it.bancaditalia.oss.vtl.model.domain.DurationDomainSubset;

public class DurationValue extends BaseScalarValue<Duration, DurationDomainSubset, DurationDomain>
{
	private static final long serialVersionUID = 1L;
	private static final Map<Duration, DurationValue> DURATIONS = new HashMap<>(); 
	private static final Map<TemporalUnit, Duration> DURATIONS_BY_UNIT = new HashMap<>(); 

	static {
		for (Duration duration: Duration.values())
		{
			DURATIONS.put(duration, new DurationValue(duration));
			DURATIONS_BY_UNIT.put(duration.getUnit(), duration);
		}
	}

	private DurationValue(Duration duration)
	{
		super(duration, DURATIONDS);
	}

	public static DurationValue of(Duration duration)
	{
		return DURATIONS.get(duration);
	}

	public static DurationValue of(String duration)
	{
		return DURATIONS.get(Duration.valueOf(duration));
	}

	public static DurationValue of(TemporalUnit unit)
	{
		return DURATIONS.get(DURATIONS_BY_UNIT.get(unit));
	}

	@Override
	public VTLScalarValueMetadata<DurationDomainSubset> getMetadata()
	{
		return () -> DURATIONDS;
	}

	@Override
	public int compareTo(ScalarValue<?, ? extends ValueDomainSubset<?>, ? extends ValueDomain> o)
	{
		if (o instanceof DurationValue)
			return get().compareTo((Duration) o.get());
		else
			throw new VTLCastException(DURATIONDS, o);
	}
}
