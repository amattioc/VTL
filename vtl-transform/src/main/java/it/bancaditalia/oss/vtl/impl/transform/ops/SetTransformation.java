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
package it.bancaditalia.oss.vtl.impl.transform.ops;

import static it.bancaditalia.oss.vtl.util.Utils.not;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import it.bancaditalia.oss.vtl.impl.transform.dataset.LightFDataSet;
import it.bancaditalia.oss.vtl.impl.types.dataset.LightDataSet;
import it.bancaditalia.oss.vtl.model.data.ComponentRole.Identifier;
import it.bancaditalia.oss.vtl.model.data.DataSet;
import it.bancaditalia.oss.vtl.model.data.DataSet.VTLDataSetMetadata;
import it.bancaditalia.oss.vtl.model.data.DataStructureComponent;
import it.bancaditalia.oss.vtl.model.data.ScalarValue;
import it.bancaditalia.oss.vtl.model.data.VTLValue.VTLValueMetadata;
import it.bancaditalia.oss.vtl.model.transform.LeafTransformation;
import it.bancaditalia.oss.vtl.model.transform.Transformation;
import it.bancaditalia.oss.vtl.model.transform.TransformationScheme;

public class SetTransformation extends TransformationImpl
{
	private static final long serialVersionUID = 1L;

	public enum SetOperator
	{
		UNION((left, right) -> structure -> new LightFDataSet<>(structure, left::concatDataPoints, setDiff(right, left))),
		INTERSECT((left, right) -> structure -> setDiff(left, setDiff(right, left))), 
		SETDIFF((left, right) -> structure -> setDiff(left, right)), 
		SYMDIFF((left, right) -> structure -> new LightFDataSet<>(structure,  
				setDiff(left, right)::concatDataPoints, setDiff(right, left)));

		private final BiFunction<DataSet, DataSet, Function<VTLDataSetMetadata, DataSet>> reducer;

		SetOperator(BiFunction<DataSet, DataSet, Function<VTLDataSetMetadata, DataSet>> reducer)
		{
			this.reducer = reducer;
		}
		
		public BinaryOperator<DataSet> getReducer(VTLDataSetMetadata metadata)
		{
			return (left, right) -> reducer.apply(left, right).apply(metadata);
		}
		
		private static DataSet setDiff(DataSet a, DataSet b)
		{
			return setDiff("ACCUMULATOR", a, b);
		}

		private static DataSet setDiff(String name, DataSet a, DataSet b)
		{
			Set<DataStructureComponent<Identifier, ?, ?>> ids = b.getComponents(Identifier.class);
			Set<Map<DataStructureComponent<Identifier, ?, ?>, ScalarValue<?, ?, ?>>> leftValues = b.stream()
					.map(dp -> dp.getValues(ids, Identifier.class))
					.collect(toSet());
			
			return a.filter(not(leftValues::contains));
		}
	}

	private final List<Transformation> operands;
	private final SetOperator setOperator;

	private VTLDataSetMetadata metadata;
	
	public SetTransformation(SetOperator setOperator, List<Transformation> operands)
	{
		this.operands = operands;
		this.setOperator = setOperator;
	}

	@Override
	public DataSet eval(TransformationScheme scheme)
	{
		return operands.stream().sequential()
				.map(t -> t.eval(scheme))
				.map(DataSet.class::cast)
				.reduce((left, right) -> setOperator.getReducer(left.getDataStructure()).apply(left, right))
				.orElse(new LightDataSet(metadata, Stream::empty));
	}

	@Override
	public VTLDataSetMetadata getMetadata(TransformationScheme scheme)
	{
		List<VTLValueMetadata> meta = operands.stream()
				.map(t -> t.getMetadata(scheme))
				.collect(toList());
		
		if (!(meta.get(0) instanceof VTLDataSetMetadata))
			throw new UnsupportedOperationException("In set operation expected all datasets but found a scalar"); 
			
		if (meta.stream().distinct().limit(2).count() != 1)
			throw new UnsupportedOperationException("In set operation expected all datasets with equal structure but found: " + meta); 

		metadata = (VTLDataSetMetadata) meta.get(0);
		return metadata;	
	}
	
	@Override
	public boolean isTerminal()
	{
		return false;
	}
	
	@Override
	public Set<LeafTransformation> getTerminals()
	{
		return operands.stream().flatMap(t -> t.getTerminals().stream()).collect(toSet());
	}

	@Override
	public String toString()
	{
		return operands.stream().map(Object::toString).collect(Collectors.joining(", ", setOperator + "(", ")"));
	}
}
