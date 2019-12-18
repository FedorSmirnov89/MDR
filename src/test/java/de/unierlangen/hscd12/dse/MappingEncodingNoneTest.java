package de.unierlangen.hscd12.dse;

import static org.junit.Assert.*;

import java.util.HashSet;

import org.junit.Test;

import net.sf.opendse.model.Mappings;

public class MappingEncodingNoneTest {

	@Test
	public void test() {
		MappingEncodingNone encoding = new MappingEncodingNone();
		assertEquals(0, encoding.toConstraints(new Mappings<>(), new HashSet<>()).size());
	}

}
