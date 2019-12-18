package de.unierlangen.hscd12.dse;

import static org.junit.Assert.*;

import java.util.HashSet;

import org.junit.Test;

import net.sf.opendse.model.Routings;

public class RoutingEncodingNoneTest {

	@Test
	public void test() {
		RoutingEncodingNone encoding = new RoutingEncodingNone();
		assertEquals(0, encoding.toConstraints(new HashSet<>(), new HashSet<>(), new Routings<>()).size());
	}

}
