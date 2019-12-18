package de.unierlangen.hscd12.dse;

import static org.junit.Assert.*;

import java.util.HashSet;

import org.junit.Test;

import net.sf.opendse.model.Architecture;

public class AllocationEncodingNoneTest {

	@Test
	public void test() {
		AllocationEncodingNone encoding = new AllocationEncodingNone();
		assertEquals(0, encoding.toConstraints(new HashSet<>(), new HashSet<>(), new Architecture<>()).size());
	}

}
