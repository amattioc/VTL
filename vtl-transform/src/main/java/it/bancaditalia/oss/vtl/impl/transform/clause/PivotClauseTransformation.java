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
package it.bancaditalia.oss.vtl.impl.transform.clause;

import static java.util.stream.Collectors.groupingByConcurrent;
import static java.util.stream.Collectors.toConcurrentMap;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.bancaditalia.oss.vtl.exceptions.VTLException;
import it.bancaditalia.oss.vtl.exceptions.VTLMissingComponentsException;
import it.bancaditalia.oss.vtl.impl.transform.dataset.LightFDataSet;
import it.bancaditalia.oss.vtl.impl.transform.exceptions.VTLInvalidParameterException;
import it.bancaditalia.oss.vtl.impl.types.dataset.DataPointImpl.DataPointBuilder;
import it.bancaditalia.oss.vtl.impl.types.dataset.DataStructureComponentImpl;
import it.bancaditalia.oss.vtl.impl.types.dataset.DataStructureImpl.Builder;
import it.bancaditalia.oss.vtl.model.data.ComponentRole.Identifier;
import it.bancaditalia.oss.vtl.model.data.ComponentRole.Measure;
import it.bancaditalia.oss.vtl.model.data.DataSet;
import it.bancaditalia.oss.vtl.model.data.DataSet.VTLDataSetMetadata;
import it.bancaditalia.oss.vtl.model.data.DataStructureComponent;
import it.bancaditalia.oss.vtl.model.data.ScalarValue;
import it.bancaditalia.oss.vtl.model.data.VTLValue;
import it.bancaditalia.oss.vtl.model.data.VTLValue.VTLValueMetadata;
import it.bancaditalia.oss.vtl.model.domain.StringCodeList;
import it.bancaditalia.oss.vtl.model.domain.StringDomain;
import it.bancaditalia.oss.vtl.model.transform.TransformationScheme;
import it.bancaditalia.oss.vtl.util.Utils;

public class PivotClauseTransformation extends DatasetClauseTransformation
{
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(PivotClauseTransformation.class);
	private final String identifierName;
	private final String measureName;

	private DataStructureComponent<Measure, ?, ?>                            measure;
	private DataStructureComponent<Identifier, StringCodeList, StringDomain> identifier;

	private static String sanitize(String string)
	{
		return string.replaceAll("^\"(.*)\"$", "$1");
	}
	
	private static String sanitize(ScalarValue<?, ?, ?> value)
	{
		return value.toString().replaceAll("^\"(.*)\"$", "$1");
	}
	
	public PivotClauseTransformation(String identifierName, String measureName)
	{
		this.identifierName = sanitize(identifierName);
		this.measureName = sanitize(measureName);
		
		LOGGER.debug("Pivoting " + measureName + " over " + identifierName);
	}

	@SuppressWarnings("unchecked")
	@Override
	public VTLValueMetadata getMetadata(TransformationScheme session)
	{
		VTLValueMetadata value = getThisMetadata(session);

		if (!(value instanceof VTLDataSetMetadata))
			throw new VTLInvalidParameterException(value, VTLDataSetMetadata.class);

		VTLDataSetMetadata dataset = (VTLDataSetMetadata) value;

		DataStructureComponent<Identifier, ?, ?> tempIdentifier = dataset.getComponent(identifierName, Identifier.class);
		measure = dataset.getComponent(measureName, Measure.class);

		if (tempIdentifier == null)
			throw new VTLMissingComponentsException(identifierName, dataset.getComponents(Identifier.class));
		if (measure == null)
			throw new VTLMissingComponentsException(measureName, dataset.getComponents(Measure.class));
		if (!(tempIdentifier.getDomain() instanceof StringCodeList))
			throw new VTLException("pivot: " + identifier + " must be defined on a CodeList of String.");

		// safe
		identifier = (DataStructureComponent<Identifier, StringCodeList, StringDomain>) tempIdentifier;
		
		return Utils.getStream(((StringCodeList) identifier.getDomain()).getCodeItems())
				.map(i -> new DataStructureComponentImpl<>(i.get(), Measure.class, measure.getDomain()))
				.reduce(new Builder(), Builder::addComponent, Builder::merge)
				.addComponents(dataset.getComponents(Identifier.class))
				.removeComponent(identifier)
				.removeComponent(measure).build();
	}

	@Override
	public VTLValue eval(TransformationScheme session)
	{
		DataSet dataset = (DataSet) getThisValue(session);
		VTLDataSetMetadata structure = dataset.getDataStructure().pivot(identifier, measure);
		Set<DataStructureComponent<Identifier, ?, ?>> ids = new HashSet<>(structure.getComponents(Identifier.class));
		
		return new LightFDataSet<>(structure, ds -> Utils.getStream(ds.stream()
			.collect(groupingByConcurrent(dp -> dp.getValues(ids, Identifier.class)))
			.entrySet())
			.map(group -> new DataPointBuilder(group.getKey()).addAll(Utils.getStream(group.getValue())
					.collect(toConcurrentMap(
							dp -> new DataStructureComponentImpl<>(sanitize(dp.get(identifier)), Measure.class, measure.getDomain()), 
							dp -> dp.get(measure)
					))).build(structure)
			), dataset);
	}

	@Override
	public String toString()
	{
		return "[pivot " + identifierName + ", " + measureName + "]";
	}
}
