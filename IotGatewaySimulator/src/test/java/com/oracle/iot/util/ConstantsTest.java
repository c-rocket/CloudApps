package com.oracle.iot.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ConstantsTest {

	@Test
	public void testWithinVariation_IsWithin() throws Exception {
		// setup
		Double value1 = 99d;
		Double setValue = 100d;

		// execute
		boolean isWithin = Constants.isWithinVariation(value1, setValue, 0.02d);

		// assert
		assertTrue(isWithin);
	}

	@Test
	public void testWithinVariation_NotWithin() throws Exception {
		// setup
		Double value1 = 97d;
		Double setValue = 100d;

		// execute
		boolean isWithin = Constants.isWithinVariation(value1, setValue, 0.02d);

		// assert
		assertFalse(isWithin);
	}

	@Test
	public void testWithinVariation_IsWithinNegative() throws Exception {
		// setup
		Double value1 = -99d;
		Double setValue = -100d;

		// execute
		boolean isWithin = Constants.isWithinVariation(value1, setValue, 0.02d);

		// assert
		assertTrue(isWithin);
	}

	@Test
	public void testWithinVariation_NotWithinNegative() throws Exception {
		// setup
		Double value1 = -97d;
		Double setValue = -100d;

		// execute
		boolean isWithin = Constants.isWithinVariation(value1, setValue, 0.02d);

		// assert
		assertFalse(isWithin);
	}

	@Test
	public void testWithin_NoVariance() throws Exception {
		// setup
		Double value1 = -97d;
		Double setValue = -97d;

		// execute
		boolean isWithin = Constants.isWithinVariation(value1, setValue, 0d);

		// assert
		assertTrue(isWithin);
	}
}
