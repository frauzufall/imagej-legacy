/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2017 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package org.scijava.search.tmp;

import java.util.List;

import org.scijava.Context;
import org.scijava.search.SearchOperation;
import org.scijava.search.SearchResult;
import org.scijava.search.SearchService;

/**
 * Test UI for search.
 *
 * @author Curtis Rueden
 */
public class SearchDemoCLI {

	public static void main(final String... args) throws InterruptedException {
		Context ctx = new Context();
		final SearchService ss = ctx.service(SearchService.class);
		final SearchOperation operation = ss.search(//
			event -> System.out.println("[RESULT] searcher=" + //
				event.searcher().title() + ", results=" + s(event.results())));
		type(operation, "B", 10);
		type(operation, "Back", 100);
		type(operation, "Backgrou", 400);
		type(operation, "Background", 400);
		type(operation, "Background Subtraction", 2000);
		System.out.println("[INFO] Terminating the search!");
		operation.terminate();
		System.out.println("[INFO] Search terminated.");
		ctx.dispose();
	}

	private static void type(SearchOperation sop, String text, int wait)
		throws InterruptedException
	{
		sop.search(text);
		System.out.println("[INFO] Query -> " + text);
		Thread.sleep(wait);
	}

	private static String s(List<SearchResult> results) {
		final StringBuilder sb = new StringBuilder("[");
		results.forEach(result -> sb.append("\n\t" + result.name()));
		sb.append("\n]");
		return sb.toString();
	}
}
