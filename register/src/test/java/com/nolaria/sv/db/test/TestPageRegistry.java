package com.nolaria.sv.db.test;


import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.nolaria.sv.db.PageRegistry;

public class TestPageRegistry {
	
	@Test
	public void getPageRegistryTest() {
		PageRegistry registry = new PageRegistry();
		System.out.println(registry.toString());
		assertTrue(registry != null);
		//assert this.registry != null;
	}
	
}