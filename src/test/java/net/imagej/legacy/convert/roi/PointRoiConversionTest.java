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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.imagej.legacy.convert.roi.RoiUnwrappers.WrapperToPointRoiConverter;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.roi.geom.real.DefaultPointMask;
import net.imglib2.roi.geom.real.DefaultRealPointCollection;
import net.imglib2.roi.geom.real.PointMask;
import net.imglib2.roi.geom.real.RealPointCollection;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.scijava.Context;
import org.scijava.convert.ConvertService;
import org.scijava.convert.Converter;

import ij.ImagePlus;
import ij.gui.PointRoi;

/**
 * Tests converting between {@link PointRoi} and {@link RealPointCollection} /
 * {@link PointMask}, and the corresponding {@link PointRoiWrapper}.
 *
 * @author Alison Walter
 */
public class PointRoiConversionTest {

	private PointRoi point;
	private RealPointCollection<RealLocalizable> rpc;
	private RealPointCollection<RealLocalizable> wrap;
	private ConvertService convertService;

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@Before
	public void setup() {
		point = new PointRoi(new float[] { 12.125f, 17, 1 }, new float[] { -4, 6.5f,
			30 });
		final List<RealLocalizable> c = new ArrayList<>();
		c.add(new RealPoint(new double[] { 12.125, -4 }));
		c.add(new RealPoint(new double[] { 17, 6.5 }));
		c.add(new RealPoint(new double[] { 1, 30 }));
		rpc = new DefaultRealPointCollection<>(c);
		wrap = new PointRoiWrapper(point);

		final Context context = new Context(ConvertService.class);
		convertService = context.service(ConvertService.class);
	}

	@After
	public void tearDown() {
		convertService.context().dispose();
	}

	// -- PointRoiWrapper --

	@Test
	public void testPointRoiWrapperGetters() {
		Iterator<RealLocalizable> iw = wrap.points().iterator();
		final Iterator<RealLocalizable> irpc = rpc.points().iterator();
		final float[] x = point.getContainedFloatPoints().xpoints;
		final float[] y = point.getContainedFloatPoints().ypoints;

		// Test ImageJ 1.x and wrapper equivalent
		for (int i = 0; i < 3; i++) {
			final RealLocalizable r = iw.next();
			assertEquals(x[i], r.getFloatPosition(0), 0);
			assertEquals(y[i], r.getFloatPosition(1), 0);
		}

		// Test ImgLib2 and wrapper equivalent
		iw = wrap.points().iterator();
		while (irpc.hasNext()) {
			final RealLocalizable w = iw.next();
			final RealLocalizable pc = irpc.next();
			assertEquals(pc.getFloatPosition(0), w.getFloatPosition(0), 0);
			assertEquals(pc.getFloatPosition(1), w.getFloatPosition(1), 0);
		}
	}

	@Test
	public void testPointRoiWrapperAddPoint() {
		wrap.addPoint(new RealPoint(new double[] { -2.25, 13 }));
		final Iterator<RealLocalizable> iw = wrap.points().iterator();
		final RealLocalizable one = iw.next();
		final RealLocalizable two = iw.next();
		final float[] xp = point.getContainedFloatPoints().xpoints;
		final float[] yp = point.getContainedFloatPoints().ypoints;

		assertEquals(xp[0], one.getFloatPosition(0), 0);
		assertEquals(yp[0], one.getFloatPosition(1), 0);
		assertEquals(xp[1], two.getFloatPosition(0), 0);
		assertEquals(yp[1], two.getFloatPosition(1), 0);
	}

	@Test
	public void testPointRoiWrapperRemovePointNoImagePlus() {
		// Throw an exception since wrapped roi has no associated ImagePlus
		exception.expect(UnsupportedOperationException.class);
		wrap.removePoint(new RealPoint(new double[] { 1, 1, }));
	}

	@Test
	public void testPointRoiWrapperRemovePointWithImagePlus() {
		final ImagePlus i = new ImagePlus("http://imagej.net/images/blobs.gif");
		i.setRoi(point);
		point.setImage(i);

		wrap.removePoint(new RealPoint(new double[] { 17, 6.5 }));
		Iterator<RealLocalizable> iw = wrap.points().iterator();
		RealLocalizable one = iw.next();
		RealLocalizable two = iw.next();
		float[] x = point.getContainedFloatPoints().xpoints;
		float[] y = point.getContainedFloatPoints().ypoints;

		// Since the passed point is part of the collection, it should have been
		// removed
		assertEquals(x[0], one.getDoublePosition(0), 0);
		assertEquals(y[0], one.getDoublePosition(1), 0);
		assertEquals(x[1], two.getDoublePosition(0), 0);
		assertEquals(y[1], two.getDoublePosition(1), 0);
		assertEquals(point.getNCoordinates(), 2);
		assertFalse(iw.hasNext());

		wrap.removePoint(new RealPoint(new double[] { 11, 3 }));
		iw = wrap.points().iterator();
		one = iw.next();
		two = iw.next();
		x = point.getContainedFloatPoints().xpoints;
		y = point.getContainedFloatPoints().ypoints;

		// Point was not part of the collection, so no change
		assertEquals(x[0], one.getDoublePosition(0), 0);
		assertEquals(y[0], one.getDoublePosition(1), 0);
		assertEquals(x[1], two.getDoublePosition(0), 0);
		assertEquals(y[1], two.getDoublePosition(1), 0);
		assertEquals(point.getNCoordinates(), 2);
		assertFalse(iw.hasNext());
	}

	@Test
	public void testPointRoiWrapperTest() {
		assertTrue(wrap.test(new RealPoint(new double[] { 12.125, -4 })));
		assertFalse(wrap.test(new RealPoint(new double[] { 8, 15.5 })));
	}

	@Test
	public void testPointRoiWrapperBounds() {
		assertEquals(1, wrap.realMin(0), 0);
		assertEquals(-4, wrap.realMin(1), 0);
		assertEquals(17, wrap.realMax(0), 0);
		assertEquals(30, wrap.realMax(1), 0);
	}

	@Test
	public void testUpdatedAfterPointRoiWrapperModified() {
		final RealPoint remove = new RealPoint(new double[] { 12.125, -4 });
		final RealPoint add = new RealPoint(new double[] { 8, 100.25 });
		assertTrue(wrap.test(remove));
		assertFalse(wrap.test(add));

		// addPoint
		wrap.addPoint(add);
		assertTrue(wrap.test(add));
		assertEquals(100.25, wrap.realMax(1), 0);

		// removePoint
		final ImagePlus i = new ImagePlus("http://imagej.net/images/blobs.gif");
		i.setRoi(point);
		point.setImage(i); // wrapper needs associated ImagePlus in order to
		// removePoint
		wrap.removePoint(remove);
		assertFalse(wrap.test(remove));

		// check the points
		final Iterator<RealLocalizable> iw = wrap.points().iterator();
		final float[] x = point.getContainedFloatPoints().xpoints;
		final float[] y = point.getContainedFloatPoints().ypoints;

		for (int n = 0; n < point.getNCoordinates(); n++) {
			final RealLocalizable pt = iw.next();
			assertEquals(x[n], pt.getDoublePosition(0), 0);
			assertEquals(y[n], pt.getDoublePosition(1), 0);
		}
		assertFalse(iw.hasNext());
	}

	// -- PointRoiToRealPointCollectionConverter tests --

	@Test
	@SuppressWarnings("unchecked")
	public void testPointRoiToRealPointCollectionConverter() {
		final RealPointCollection<RealLocalizable> converted = convertService
			.convert(point, RealPointCollection.class);

		assertTrue(converted instanceof PointRoiWrapper);

		final Iterator<RealLocalizable> ic = converted.points().iterator();
		final Iterator<RealLocalizable> iw = wrap.points().iterator();
		while (ic.hasNext()) {
			final RealLocalizable wrl = iw.next();
			final RealLocalizable crl = ic.next();
			assertEquals(wrl.getFloatPosition(0), crl.getFloatPosition(0), 0);
			assertEquals(wrl.getFloatPosition(1), crl.getFloatPosition(1), 0);
		}
	}

	// -- RealPointCollection to PointRoi converter tests --

	@Test
	public void testRealPointCollectionToPointRoiConverterMatching() {
		final Converter<?, ?> c = convertService.getHandler(rpc, PointRoi.class);
		assertTrue(c instanceof RealPointCollectionToPointRoiConverter);

		final Converter<?, ?> cc = convertService.getHandler(wrap, PointRoi.class);
		assertTrue(cc instanceof WrapperToPointRoiConverter);

		final List<RealLocalizable> pts = new ArrayList<>();
		pts.add(new RealPoint(new double[] { 1, 2, 3 }));
		pts.add(new RealPoint(new double[] { 4, 5, 6 }));
		final Converter<?, ?> ccc = convertService.getHandler(pts, PointRoi.class);
		assertNull(ccc);
	}

	@Test
	public void testRealPointCollectionToPointRoiConverterWithRPC() {
		final PointRoi p = convertService.convert(rpc, PointRoi.class);

		final float[] xp = p.getContainedFloatPoints().xpoints;
		final float[] yp = p.getContainedFloatPoints().ypoints;
		final Iterator<RealLocalizable> points = rpc.points().iterator();
		int count = 0;

		while (points.hasNext()) {
			final RealLocalizable tp = points.next();
			assertEquals(tp.getFloatPosition(0), xp[count], 0);
			assertEquals(tp.getFloatPosition(1), yp[count], 0);
			count++;
		}

		assertEquals(count, p.getNCoordinates());
		assertEquals(rpc.realMin(0), p.getXBase(), 0);
		assertEquals(rpc.realMin(1), p.getYBase(), 0);
		assertEquals(rpc.realMax(0), p.getXBase() + p.getFloatWidth(), 0);
		assertEquals(rpc.realMax(1), p.getYBase() + p.getFloatHeight(), 0);
	}

	@Test
	public void testRealPointCollectionToPointRoiConverterWithWrapper() {
		final PointRoi p = convertService.convert(wrap, PointRoi.class);
		assertTrue(p == point);
	}

	// -- PointMaskToPointRoiConverter tests --

	@Test
	public void testPointMaskToPointRoiConverterMatching() {
		final PointMask p = new DefaultPointMask(new double[] { 20.5, -30 });
		final Converter<?, ?> c = convertService.getHandler(p, PointRoi.class);
		assertTrue(c instanceof PointMaskToPointRoiConverter);
	}

	@Test
	public void testPointMaskToPointRoiConverterWithPointMask() {
		final PointMask pm = new DefaultPointMask(new double[] { 140.25, -0.5 });
		final PointRoi pr = convertService.convert(pm, PointRoi.class);

		assertEquals(1, pr.getNCoordinates());

		assertEquals(pm.getDoublePosition(0), pr
			.getContainedFloatPoints().xpoints[0], 0);
		assertEquals(pm.getDoublePosition(1), pr
			.getContainedFloatPoints().ypoints[0], 0);
	}

}
