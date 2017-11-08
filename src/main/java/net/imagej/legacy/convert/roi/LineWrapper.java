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

import net.imglib2.Localizable;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.RealPositionable;
import net.imglib2.roi.geom.GeomMaths;
import net.imglib2.roi.geom.real.Line;

/**
 * Wraps an ImageJ 1.x {@link ij.gui.Line Line} as an ImgLib2 {@link Line}.
 * <p>
 * This implementation does not support lines with widths.
 * </p>
 *
 * @author Alison Walter
 */
public class LineWrapper implements IJRoiWrapper<ij.gui.Line>, Line<RealPoint> {

	private final ij.gui.Line line;

	/**
	 * Creates a new ImageJ 1.x {@link ij.gui.Line Line} with endpoints at the
	 * specified integer coordinates, and then wraps this as an ImgLib2
	 * {@link Line}.
	 *
	 * @param x1 x coordinate of the first endpoint.
	 * @param y1 y coordinate of the first endpoint.
	 * @param x2 x coordinate of the second endpoint.
	 * @param y2 y coordinate of the second endpoint.
	 */
	public LineWrapper(final int x1, final int y1, final int x2, final int y2) {
		line = new ij.gui.Line(x1, y1, x2, y2);
	}

	/**
	 * Creates a new ImageJ 1.x {@link ij.gui.Line Line} with endpoints at the
	 * specified real coordinates, and then wraps this as an ImgLib2 {@link Line}.
	 *
	 * @param x1 x coordinate of the first endpoint.
	 * @param y1 y coordinate of the first endpoint.
	 * @param x2 x coordinate of the second endpoint.
	 * @param y2 y coordinate of the second endpoint.
	 */
	public LineWrapper(final double x1, final double y1, final double x2,
		final double y2)
	{
		line = new ij.gui.Line(x1, y1, x2, y2);
	}

	/**
	 * Wraps the given ImageJ 1.x {@link ij.gui.Line Line} as an ImgLib2
	 * {@link Line}.
	 *
	 * @param line imageJ 1.x line to be wrapped
	 */
	public LineWrapper(final ij.gui.Line line) {
		if (ij.gui.Line.getWidth() > 1) throw new IllegalArgumentException(
			"Cannot wrap lines with width > 1");
		this.line = line;
	}

	@Override
	public boolean test(final RealLocalizable t) {
		// NB: ImageJ 1.x contains(...) is not used due to the limitations of
		// integer coordinates. Due to this, ImageJ 1.x contains(...) always
		// returns false for lines with width = 1.
		return GeomMaths.lineContains(new double[] { line.x1d, line.y1d },
			new double[] { line.x2d, line.y2d }, t, 2);
	}

	@Override
	public double realMin(final int d) {
		if (d != 0 && d != 1) throw new IllegalArgumentException(
			"Invalid dimension " + d);
		return d == 0 ? Math.min(line.x1d, line.x2d) : Math.min(line.y1d, line.y2d);
	}

	@Override
	public double realMax(final int d) {
		if (d != 0 && d != 1) throw new IllegalArgumentException(
			"Invalid dimension " + d);
		return d == 0 ? Math.max(line.x1d, line.x2d) : Math.max(line.y1d, line.y2d);
	}

	@Override
	public RealPoint endpointOne() {
		return new LineEndPoint(line.x1d, line.y1d);
	}

	@Override
	public RealPoint endpointTwo() {
		return new LineEndPoint(line.x2d, line.y2d);
	}

	@Override
	public ij.gui.Line getRoi() {
		return line;
	}

	/**
	 * This {@link RealPoint} throws {@link UnsupportedOperationException}s for
	 * all {@link RealPositionable} methods, because the endpoints of the
	 * underlying {@link ij.gui.Line line} cannot be modified.
	 */
	private class LineEndPoint extends RealPoint {

		public LineEndPoint(final double x, final double y) {
			super(new double[] { x, y });
		}

		@Override
		public void move(final float distance, final int d) {
			throw new UnsupportedOperationException("move");
		}

		@Override
		public void move(final double distance, final int d) {
			throw new UnsupportedOperationException("move");
		}

		@Override
		public void move(final RealLocalizable localizable) {
			throw new UnsupportedOperationException("move");
		}

		@Override
		public void move(final float[] distance) {
			throw new UnsupportedOperationException("move");
		}

		@Override
		public void move(final double[] distance) {
			throw new UnsupportedOperationException("move");
		}

		@Override
		public void setPosition(final RealLocalizable localizable) {
			throw new UnsupportedOperationException("setPosition");
		}

		@Override
		public void setPosition(final float[] position) {
			throw new UnsupportedOperationException("setPosition");
		}

		@Override
		public void setPosition(final double[] position) {
			throw new UnsupportedOperationException("setPosition");
		}

		@Override
		public void setPosition(final float position, final int d) {
			throw new UnsupportedOperationException("setPosition");
		}

		@Override
		public void setPosition(final double position, final int d) {
			throw new UnsupportedOperationException("setPosition");
		}

		@Override
		public void fwd(final int d) {
			throw new UnsupportedOperationException("fwd");
		}

		@Override
		public void bck(final int d) {
			throw new UnsupportedOperationException("bck");
		}

		@Override
		public void move(final int distance, final int d) {
			throw new UnsupportedOperationException("move");
		}

		@Override
		public void move(final long distance, final int d) {
			throw new UnsupportedOperationException("move");
		}

		@Override
		public void move(final Localizable localizable) {
			throw new UnsupportedOperationException("move");
		}

		@Override
		public void move(final int[] distance) {
			throw new UnsupportedOperationException("move");
		}

		@Override
		public void move(final long[] distance) {
			throw new UnsupportedOperationException("move");
		}

		@Override
		public void setPosition(final Localizable localizable) {
			throw new UnsupportedOperationException("setPosition");
		}

		@Override
		public void setPosition(final int[] position) {
			throw new UnsupportedOperationException("setPosition");
		}

		@Override
		public void setPosition(final long[] position) {
			throw new UnsupportedOperationException("setPosition");
		}

		@Override
		public void setPosition(final int position, final int d) {
			throw new UnsupportedOperationException("setPosition");
		}

		@Override
		public void setPosition(final long position, final int d) {
			throw new UnsupportedOperationException("setPosition");
		}

	}

}
