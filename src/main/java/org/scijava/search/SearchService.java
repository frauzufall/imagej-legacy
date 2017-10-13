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

package org.scijava.search;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.scijava.plugin.SingletonService;
import org.scijava.service.SciJavaService;

/**
 * Interface for service that manages search results.
 *
 * @author Curtis Rueden
 */
public interface SearchService extends SingletonService<Searcher>,
	SciJavaService
{

	default List<SearchResult> search(final String text, final boolean fuzzy) {
		final List<SearchResult> results = new ArrayList<>();
		for (final Searcher searcher : getInstances()) {
			results.addAll(searcher.search(text, fuzzy));
		}
		return results;
	}

	default List<SearchAction> actions(final SearchResult result) {
		return pluginService().createInstancesOfType(SearchActionFactory.class)
			.stream().filter(factory -> factory.supports(result)).map(
				factory -> factory.create(result)).collect(Collectors.toList());
	}

	@Override
	default Class<Searcher> getPluginType() {
		return Searcher.class;
	}
}