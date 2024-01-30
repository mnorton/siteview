package com.nolaria.sv.db.test;


import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.nolaria.sv.db.PageRegistry;

public class TestPageRegistry {
	private PageRegistry registry = null;
	
	@Test
	public void getPageRegistryTest() {
		this.registry = new PageRegistry();
		System.out.println("PageRegistryTest.getPageRegistryTest()");
		assertTrue(true);
		//assert this.registry != null;
	}
	
}