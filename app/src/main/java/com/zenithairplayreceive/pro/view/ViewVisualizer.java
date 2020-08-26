//@formatter:off
package com.zenithairplayreceive.pro.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.zenithairplayreceive.pro.R;


public class ViewVisualizer extends View {
	private byte[]		m_vcModel;
	private float[]		m_vfPoint;
	private Rect	m_rect			= new Rect();
	private Paint	m_paintFore		= new Paint();
	private int		m_iNumSpectrum = 64;

	public ViewVisualizer(Context context) {
		super(context);
		Init();
	}

	public ViewVisualizer(Context context, AttributeSet attrs) {
		super(context, attrs);
		Init();
	}

	private void Init() {
		m_vcModel = null;

		m_paintFore.setStrokeWidth(3f);
		m_paintFore.setAntiAlias(true);
		m_paintFore.setColor(getResources().getColor(R.color.visualize_fx));
	}

	public void UpdateVisualizer(byte[] a_vcFft) {
		byte[] vcModel = new byte[a_vcFft.length / 2 + 1];

		vcModel[0] = (byte)Math.abs(a_vcFft[0]);
		for (int i = 2, j = 1; j < m_iNumSpectrum; ) {
			vcModel[j] = (byte)Math.hypot(a_vcFft[i], a_vcFft[i + 1]);
			i += 2;
			j++;
		}
		m_vcModel = vcModel;
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (m_vcModel == null) {
			return;
		}

		if (m_vfPoint == null || m_vfPoint.length < m_vcModel.length * 4) {
			m_vfPoint = new float[m_vcModel.length * 4];
		}

		m_rect.set(0, 0, getWidth(), getHeight());

		final int baseX = m_rect.width() / m_iNumSpectrum;
		final int height = m_rect.height();

		for (int i = 0; i < m_iNumSpectrum; i++) {
			if (m_vcModel[i] < 0) {
				m_vcModel[i] = 127;
			}

			final int xi = baseX * i + baseX / 2;

			m_vfPoint[i * 4] = xi;
			m_vfPoint[i * 4 + 1] = height;

			m_vfPoint[i * 4 + 2] = xi;
			m_vfPoint[i * 4 + 3] = height - m_vcModel[i] * 2;
		}

		canvas.drawLines(m_vfPoint, m_paintFore);
	}
}