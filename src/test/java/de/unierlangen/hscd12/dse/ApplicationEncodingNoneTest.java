package de.unierlangen.hscd12.dse;

import static org.junit.Assert.*;

import org.junit.Test;

import net.sf.opendse.model.Application;

public class ApplicationEncodingNoneTest {

	@Test
	public void test() {
		ApplicationEncodingNone encoding = new ApplicationEncodingNone();
		assertEquals(0, encoding.toConstraints(new Application<>()).size());
	}
}
