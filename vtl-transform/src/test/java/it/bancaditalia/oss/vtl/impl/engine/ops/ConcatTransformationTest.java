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
package it.bancaditalia.oss.vtl.impl.engine.ops;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import it.bancaditalia.oss.vtl.impl.engine.testutils.DummySession;
import it.bancaditalia.oss.vtl.impl.engine.testutils.SampleComponents;
import it.bancaditalia.oss.vtl.impl.transform.ops.ConcatTransformation;
import it.bancaditalia.oss.vtl.impl.transform.ops.VarIDOperand;
import it.bancaditalia.oss.vtl.model.data.ComponentRole.Measure;
import it.bancaditalia.oss.vtl.model.data.DataSet;
import it.bancaditalia.oss.vtl.model.data.DataSet.VTLDataSetMetadata;
import it.bancaditalia.oss.vtl.model.data.DataStructureComponent;

public class ConcatTransformationTest
{
	private final static String RESULT[] = { "AK", "CC", "EG", null, "IA", "KE" };
	
	@Test
	public void test()
	{
		VarIDOperand left = new VarIDOperand("left"), right = new VarIDOperand("right");
		DummySession session = new DummySession();
		session.put("left", SampleComponents.getSample5("STRING", 3));
		session.put("right", SampleComponents.getSample5("STRING", 4));

		ConcatTransformation coTransformation = new ConcatTransformation(left, right);
		
		VTLDataSetMetadata metadata = (VTLDataSetMetadata) coTransformation.getMetadata(session);
		assertTrue(metadata.contains("STRING_2"));
		
		DataSet computedResult = (DataSet) coTransformation.eval(session);
		
		DataStructureComponent<?, ?, ?> id = metadata.getComponent("STRING_1").get();		
		DataStructureComponent<?, ?, ?> res = metadata.getComponent("STRING_2").get();		
		
		DataSet leftD = (DataSet) left.eval(session), 
				rightD = (DataSet) right.eval(session);
		
		computedResult.stream()
			.forEach(dp -> assertEquals(RESULT[dp.get(id).get().toString().charAt(0) - 'A'], dp.get(res).get(), 
					dp.get(id).get().toString() + "(" + leftD.stream()
						.filter(dpl -> dpl.get(id).equals(dp.get(id)))
						.map(dpl -> dpl.getValues(Measure.class).values().iterator().next().toString())
						.findFirst()
						.get() + " && " + 
					rightD.stream()
						.filter(dpr -> dpr.get(id).equals(dp.get(id)))
						.map(dpr -> dpr.getValues(Measure.class).values().iterator().next().toString())
						.findFirst()
						.get() + ")"));
	}
}
