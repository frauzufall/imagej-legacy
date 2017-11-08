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

import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.roi.geom.GeomMaths;
import net.imglib2.roi.geom.real.Polyline;
import net.imglib2.util.Intervals;

import ij.ImagePlus;
import ij.gui.PolygonRoi;
import ij.gui.Roi;

/**
 * Wraps an ImageJ 1.x {@link PolygonRoi} as an ImgLib2 {@link Polyline}.
 *
 * @author Alison Walter
 */
public class PolylineRoiWrapper extends AbstractPolygonRoiWrapper implements
	Polyline<RealPoint>
{

	/**
	 * Creates an ImageJ 1.x {@link PolygonRoi} and wraps it as an ImgLib2
	 * {@link Polyline}.
	 *
	 * @param xPoints x coordinates of the vertices
	 * @param yPoints y coordinates of the vertices
	 * @param nPoints number of vertices
	 */
	public PolylineRoiWrapper(final int[] xPoints, final int[] yPoints,
		final int nPoints)
	{
		super(xPoints, yPoints, nPoints, Roi.POLYLINE);
	}

	/**
	 * Creates an ImageJ 1.x {@link PolygonRoi} and wraps it as an ImgLib2
	 * {@link Polyline}.
	 *
	 * @param xPoints x coordinates of the vertices
	 * @param yPoints y coordinates of the vertices
	 * @param nPoints number of vertices
	 */
	public PolylineRoiWrapper(final float[] xPoints, final float[] yPoints,
		final int nPoints)
	{
		super(xPoints, yPoints, nPoints, Roi.POLYLINE);
	}

	/**
	 * Creates an ImageJ 1.x {@link PolygonRoi} and wraps it as an ImgLib2
	 * {@link Polyline}. The length of {@code xPoints} will be used to determine
	 * the number of vertices this {@code Polyline} has.
	 *
	 * @param xPoints x coordinates of the vertices
	 * @param yPoints y coordinates of the vertices
	 */
	public PolylineRoiWrapper(final float[] xPoints, final float[] yPoints) {
		super(xPoints, yPoints, Roi.POLYLINE);
	}

	/**
	 * Wraps an ImageJ 1.x {@link PolygonRoi} as an ImgLib2 {@link Polyline}.
	 *
	 * @param poly the {@code PolygonRoi} to be wrapped
	 */
	public PolylineRoiWrapper(final PolygonRoi poly) {
		super(poly);
		if (poly.getType() != Roi.POLYLINE) throw new IllegalArgumentException(
			"Cannot wrap " + poly.getTypeAsString() + " as Polyline");
		if (poly.getStrokeWidth() != 0) throw new IllegalArgumentException(
			"Cannot wrap polylines with non-zero width");
		if (poly.isSplineFit()) throw new IllegalArgumentException("Cannot wrap " +
			"spline fitted polylines");
	}

	@Override
	public boolean test(final RealLocalizable t) {
		if (Intervals.contains(this, t)) {
			final float[] x = getRoi().getFloatPolygon().xpoints;
			final float[] y = getRoi().getFloatPolygon().ypoints;

			for (int i = 1; i < numVertices(); i++) {
				final double[] start = new double[] { x[i - 1], y[i - 1] };
				final double[] end = new double[] { x[i], y[i] };
				final boolean testLineContains = GeomMaths.lineContains(start, end, t,
					2);
				if (testLineContains) return true;
			}
		}
		return false;
	}

	/**
	 * This will <strong>always</strong> throw an
	 * {@code UnsupportedOperationException}.
	 *
	 * @throws UnsupportedOperationException cannot add a new vertex to the
	 *           underlying {@link PolygonRoi}
	 */
	@Override
	public void addVertex(final int index, final RealLocalizable vertex) {
		throw new UnsupportedOperationException("addVertex");
	}

	/**
	 * If the wrapped {@link PolygonRoi} is not associated with an
	 * {@link ImagePlus}, then this method will always throw an
	 * {@code UnsupportedOperationException}. Otherwise, the vertex will be
	 * removed provided the index is valid.
	 */
	@Override
	public void removeVertex(final int index) {
		if (getRoi().getImage() != null) {
			final double x = getRoi().getFloatPolygon().xpoints[index];
			final double y = getRoi().getFloatPolygon().ypoints[index];
			getRoi().deleteHandle(x, y);
		}
		else throw new UnsupportedOperationException("removeVertex");
	}
}
