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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.roi.geom.real.DefaultPolyline;
import net.imglib2.roi.geom.real.Polyline;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.scijava.Context;
import org.scijava.convert.ConvertService;

import ij.ImagePlus;
import ij.gui.PolygonRoi;
import ij.gui.Roi;

/**
 * Tests converting {@link PolygonRoi} to {@link Polyline} and the corresponding
 * {@link PolylineRoiWrapper}.
 *
 * @author Alison Walter
 */
public class PolylineRoiConversionTest {

	private PolygonRoi poly;
	private Polyline<RealPoint> wrap;
	private Polyline<RealPoint> dp;
	private ConvertService convertService;

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@Before
	public void setup() {
		poly = new PolygonRoi(new float[] { 1.25f, 20, 50, 79 }, new float[] {
			1.25f, 20, -30, -1 }, Roi.POLYLINE);
		wrap = new PolylineRoiWrapper(poly);
		final List<RealPoint> pts = new ArrayList<>(4);
		pts.add(new RealPoint(new double[] { 1.25, 1.25 }));
		pts.add(new RealPoint(new double[] { 20, 20 }));
		pts.add(new RealPoint(new double[] { 50, -30 }));
		pts.add(new RealPoint(new double[] { 79, -1 }));
		dp = new DefaultPolyline(pts);

		final Context context = new Context(ConvertService.class);
		convertService = context.service(ConvertService.class);
	}

	@After
	public void tearDown() {
		convertService.context().dispose();
	}

	@Test
	public void testPolylineRoiWrapperGetters() {
		assertEquals(4, wrap.numVertices());
		final float[] x = poly.getFloatPolygon().xpoints;
		final float[] y = poly.getFloatPolygon().ypoints;

		for (int i = 0; i < 4; i++) {
			// compare ImageJ 1.x to Wrapper
			assertEquals(x[i], wrap.vertex(i).getDoublePosition(0), 0);
			assertEquals(y[i], wrap.vertex(i).getDoublePosition(1), 0);

			// compare ImgLib2 to Wrapper
			assertEquals(dp.vertex(i).getDoublePosition(1), wrap.vertex(i)
				.getDoublePosition(1), 0);
			assertEquals(dp.vertex(i).getDoublePosition(1), wrap.vertex(i)
				.getDoublePosition(1), 0);
		}
	}

	@Test
	public void testPolylineRoiWrapperSetVertex() {
		exception.expect(UnsupportedOperationException.class);
		wrap.vertex(2).setPosition(new double[] { 1, -3 });
	}

	@Test
	public void testPolylineRoiWrapperAddVertex() {
		exception.expect(UnsupportedOperationException.class);
		wrap.addVertex(3, new RealPoint(new double[] { 0, 0 }));
	}

	@Test
	public void testPolylineRoiWrapperRemoveVertexNoImagePlus() {
		exception.expect(UnsupportedOperationException.class);
		wrap.removeVertex(0);
	}

	@Test
	public void testPolylineRoiWrapperRemoveVertexWithImagePlus() {
		final ImagePlus i = new ImagePlus("http://imagej.net/images/blobs.gif");
		i.setRoi(poly);
		poly.setImage(i);

		wrap.removeVertex(3);
		assertEquals(3, wrap.numVertices());

		// Check that backing PolygonRoi was updated
		assertEquals(3, poly.getNCoordinates());
	}

	@Test
	public void testPolylineRoiWrapperTest() {
		assertTrue(wrap.test(new RealPoint(new double[] { 10, 10 })));
		assertTrue(wrap.test(new RealPoint(new double[] { 35, -5 })));
		assertTrue(wrap.test(new RealPoint(new double[] { 51.25, -28.75 })));
		assertFalse(wrap.test(new RealPoint(new double[] { 0, 0 })));
		assertFalse(wrap.test(new RealPoint(new double[] { 17, 25 })));
	}

	@Test
	public void testPolylineRoiWrapperBounds() {
		assertEquals(1.25, wrap.realMin(0), 0);
		assertEquals(-30, wrap.realMin(1), 0);
		assertEquals(79, wrap.realMax(0), 0);
		assertEquals(20, wrap.realMax(1), 0);
	}

	@Test
	public void testUpdatedAfterPolylineRoiWrapperModified() {
		final ImagePlus i = new ImagePlus("http://imagej.net/images/blobs.gif");
		i.setRoi(poly);
		poly.setImage(i);

		final RealLocalizable test = new RealPoint(new double[] { 51.25, -28.75 });
		assertTrue(wrap.test(test));

		wrap.removeVertex(3);
		assertFalse(wrap.test(test));

		// Check boundaries updated
		assertEquals(1.25, wrap.realMin(0), 0);
		assertEquals(-30, wrap.realMin(1), 0);
		assertEquals(50, wrap.realMax(0), 0);
		assertEquals(20, wrap.realMax(1), 0);
	}

	@Test
	public void testPolylineRoiToPolylineConverter() {
		final Polyline<?> converted = convertService.convert(poly, Polyline.class);

		assertTrue(converted instanceof PolylineRoiWrapper);

		assertEquals(wrap.vertex(0).getDoublePosition(0), converted.vertex(0)
			.getDoublePosition(0), 0);
		assertEquals(wrap.vertex(0).getDoublePosition(1), converted.vertex(0)
			.getDoublePosition(1), 0);
		assertEquals(wrap.vertex(1).getDoublePosition(0), converted.vertex(1)
			.getDoublePosition(0), 0);
		assertEquals(wrap.vertex(1).getDoublePosition(1), converted.vertex(1)
			.getDoublePosition(1), 0);
		assertEquals(wrap.vertex(2).getDoublePosition(0), converted.vertex(2)
			.getDoublePosition(0), 0);
		assertEquals(wrap.vertex(2).getDoublePosition(1), converted.vertex(2)
			.getDoublePosition(1), 0);
		assertEquals(wrap.vertex(3).getDoublePosition(0), converted.vertex(3)
			.getDoublePosition(0), 0);
		assertEquals(wrap.vertex(3).getDoublePosition(1), converted.vertex(3)
			.getDoublePosition(1), 0);
	}

	@Test
	public void testPolylineRoiToPolylineConverterWithFreeLine() {
		final PolygonRoi free = new PolygonRoi(new float[] { 1, 2, 2.5f, 3, 3.1f,
			3.5f, 4, 5, 5.5f, 6, 7, 8, 9, 10, 11, 12, 13, 14, 14.5f, 14, 14.25f,
			14.4f, 14.3f, 14, 14.5f }, new float[] { 1, 1, 1.25f, 1, 1.5f, 1.5f, 1, 1,
				1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 11.4f, 12, 13, 14.5f, 15, 15.25f },
			Roi.FREELINE);
		final Polyline<?> converted = convertService.convert(free, Polyline.class);

		assertTrue(converted == null);
	}

	@Test
	public void testPolylineRoiToPolylineConverterWithWidth() {
		poly.setStrokeWidth(15.5);
		final Polyline<?> converted = convertService.convert(poly, Polyline.class);

		assertTrue(converted == null);
	}

	@Test
	public void testPolylineRoiToPolylineConverterWithSpline() {
		poly.fitSpline();
		final Polyline<?> converted = convertService.convert(poly, Polyline.class);

		assertTrue(converted == null);
	}
}
