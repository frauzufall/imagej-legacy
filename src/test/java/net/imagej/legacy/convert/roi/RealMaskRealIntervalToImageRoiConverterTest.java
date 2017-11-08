/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
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

package net.imagej.legacy.convert.roi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.RealPositionable;
import net.imglib2.realtransform.AffineTransform2D;
import net.imglib2.roi.RealMaskRealInterval;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.convert.ConvertService;
import org.scijava.convert.Converter;

import ij.gui.ImageRoi;
import ij.gui.Roi;

/**
 * Tests for {@link RealMaskRealIntervalToImageRoiConverter}.
 *
 * @author Alison Walter
 */
public class RealMaskRealIntervalToImageRoiConverterTest {

	private ConvertService convertService;

	@Before
	public void setup() {
		final Context context = new Context();
		convertService = context.service(ConvertService.class);
	}

	@After
	public void tearDown() {
		convertService.context().dispose();
	}

	@Test
	public void testConverterMatching() {
		final RealMaskRealInterval mri = new TestMask(new double[] { 0, 0 },
			new double[] { 12, 24 });
		final Converter<?, ?> c = convertService.getHandler(mri, Roi.class);

		assertTrue(c instanceof RealMaskRealIntervalToImageRoiConverter);
	}

	@Test
	public void testConversion() {
		final RealMaskRealInterval mri = new TestMask(new double[] { 15, 20 },
			new double[] { 50, 72 });
		final ImageRoi ir = convertService.convert(mri, ImageRoi.class);

		assertEquals(mri.realMin(0), ir.getXBase(), 0);
		assertEquals(mri.realMin(1), ir.getYBase(), 0);
		assertEquals(mri.realMax(0), ir.getXBase() + ir.getFloatWidth() - 1, 0);
		assertEquals(mri.realMax(1), ir.getYBase() + ir.getFloatHeight() - 1, 0);

		final AffineTransform2D transform = new AffineTransform2D();
		transform.translate(new double[] { -15, -20 });
		final double[] pos = new double[] { 20, 30 };
		final double[] tpos = new double[2];

		transform.apply(pos, tpos);
		assertEquals(mri.test(new RealPoint(pos)), ir.getProcessor().getPixelValue(
			(int) tpos[0], (int) tpos[1]) == 255);

		pos[0] = 43;
		pos[1] = 62;
		transform.apply(pos, tpos);
		assertEquals(mri.test(new RealPoint(pos)), ir.getProcessor().getPixelValue(
			(int) tpos[0], (int) tpos[1]) == 255);
	}

	// -- Helper classes --

	private class TestMask implements RealMaskRealInterval {

		private final double[] tmin;
		private final double[] tmax;

		public TestMask(final double[] min, final double[] max) {
			tmin = min;
			tmax = max;
		}

		@Override
		public int numDimensions() {
			return 2;
		}

		@Override
		public boolean test(final RealLocalizable t) {
			final double x = t.getDoublePosition(0);
			final double y = t.getDoublePosition(1);

			return (x + y) % 2 == 0;
		}

		@Override
		public double realMin(final int d) {
			return tmin[d];
		}

		@Override
		public void realMin(final double[] min) {
			System.arraycopy(tmin, 0, min, 0, tmin.length);
		}

		@Override
		public void realMin(final RealPositionable min) {
			min.setPosition(tmin);
		}

		@Override
		public double realMax(final int d) {
			return tmax[d];
		}

		@Override
		public void realMax(final double[] max) {
			System.arraycopy(tmax, 0, max, 0, tmax.length);
		}

		@Override
		public void realMax(final RealPositionable max) {
			max.setPosition(tmax);
		}

	}
}
