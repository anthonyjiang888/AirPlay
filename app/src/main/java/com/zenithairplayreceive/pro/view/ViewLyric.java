//@formatter:off
package com.zenithairplayreceive.pro.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.zenithairplayreceive.pro.R;
import com.zenithairplayreceive.pro.player.DataLyric;
import com.zenithairplayreceive.pro.util.UtilMusic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ViewLyric extends View {

	private final static String TAG = "LyricView";

	public static final int INTERVAL = 15;	// Gap of the Single Line

	private TreeMap<Integer, DataLyric> m_mapLyric = new TreeMap<Integer, DataLyric>();
	private float		m_fX;			// Center of the X in the Screen
	private float		m_fOffsetY;		// Y offset, this is getting smaller by scrolling
	private boolean		m_bLyric = false;
	private float		m_fTouchY;				// Touch Y
	private int			m_iIndexLyric = 0;		// Store TreeMap DropDown
	private int			m_iSizeWord = 22;		// Size of the Character
	private int			m_iSizeWordHl = 27;		// Size of the Character, Highlight
	private Paint		m_paint = new Paint();		// Paint
	private Paint		m_paintHl = new Paint();	// Paint, Highlight
	private String		m_sTitle = "";
	private String		m_sArtist = "";

	public ViewLyric(Context context) {
		super(context);
		Init();
	}

	public ViewLyric(Context context, AttributeSet attrs) {
		super(context, attrs);
		Init();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (m_bLyric) {
			DataLyric dataLyric = m_mapLyric.get(m_iIndexLyric);
			if (dataLyric.sLyric != null) {
				canvas.drawText(
					dataLyric.sLyric, m_fX, m_fOffsetY + (m_iSizeWord + INTERVAL) * m_iIndexLyric, m_paintHl
				);
			}

			// Paint before Current Paint
			for (int i = m_iIndexLyric - 1; i >= 0; i--) {
				dataLyric = m_mapLyric.get(i);
				if (m_fOffsetY + (m_iSizeWord + INTERVAL) * i < 0) {
					break;
				}
				if (dataLyric.sLyric != null) {
					canvas.drawText(dataLyric.sLyric, m_fX, m_fOffsetY + (m_iSizeWord + INTERVAL) * i, m_paint);
				}
			}
			// Paint after Current Paint
			for (int i = m_iIndexLyric + 1; i < m_mapLyric.size(); i++) {
				dataLyric = m_mapLyric.get(i);
				if (m_fOffsetY + (m_iSizeWord + INTERVAL) * i > 600) {
					break;
				}
				if (dataLyric.sLyric != null) {
					canvas.drawText(dataLyric.sLyric, m_fX, m_fOffsetY + (m_iSizeWord + INTERVAL) * i, m_paint);
				}
			}
		}
		else {
			m_paint.setTextSize(m_iSizeWord);
			canvas.drawText(m_sTitle, m_fX, 220, m_paint);
			String sArtist = "";
			if (m_sArtist != null && m_sArtist.length() > 0
			&&	!MediaStore.UNKNOWN_STRING.equals(m_sArtist)
				) {
				sArtist = m_sArtist;
			}
			else {
				sArtist = getResources().getString(R.string.mp_unknown_artist);
			}
			canvas.drawText(sArtist, m_fX, 260, m_paint);
			canvas.drawText(
				getResources().getString(R.string.mp_cant_find_lyrics),
				m_fX,
				310,
				m_paint
			);
		}
		super.onDraw(canvas);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		m_fX = w * 0.5f;
		super.onSizeChanged(w, h, oldw, oldh);
	}

//	@Override
//	public boolean onTouchEvent(MotionEvent event) {
//		float fY = event.getY();
//		if (!m_bLyric) {
//			return super.onTouchEvent(event);
//		}
//		switch (event.getAction()) {
//			case MotionEvent.ACTION_DOWN:
//				break;
//			case MotionEvent.ACTION_MOVE:
//				m_fTouchY = fY - m_fTouchY;
//				m_fOffsetY = m_fOffsetY + m_fTouchY;
//				break;
//			case MotionEvent.ACTION_UP:
//				break;
//		}
//		m_fTouchY = fY;
//		return true;
//	}

	public void Init() {
		m_fOffsetY = 320;
		m_paint = new Paint();
		m_paint.setTextAlign(Paint.Align.CENTER);
		m_paint.setColor(getResources().getColor(R.color.lyrics));
		m_paint.setTextSize(m_iSizeWord);
		m_paint.setAntiAlias(true);
		m_paint.setDither(true);
		m_paint.setAlpha(180);

		m_paintHl = new Paint();
		m_paintHl.setTextAlign(Paint.Align.CENTER);

		m_paintHl.setColor(getResources().getColor(R.color.lyrics_hl));
		m_paintHl.setTextSize(m_iSizeWordHl);
		m_paintHl.setAntiAlias(true);
		m_paintHl.setAlpha(255);
	}

	/**
	 * Set Char Size according to long sentence
	 */
	public void SetTextSize() {
//		 if (!m_bLyric) {
//			 return;
//		 }
//		 int iMax = m_mapLyric.get(0).sLyric.length();
//		 for (int i = 1; i < m_mapLyric.size(); i++) {
//			 DataLyric dataLyric = m_mapLyric.get(i);
//			 if (iMax < dataLyric.sLyric.length()) {
//				 iMax = dataLyric.sLyric.length();
//			 }
//		 }
//		 m_iSizeWord = 320 / iMax;
	}

	/**
	 * Speed of the Lyric
	 *
	 * @return Speed of the Scroll Lyric
	 */
	public Float SpeedLyric() {
		float fSpeed = 0;
		if (m_fOffsetY + (m_iSizeWord + INTERVAL) * m_iIndexLyric > 220) {
			fSpeed = ((m_fOffsetY + (m_iSizeWord + INTERVAL) * m_iIndexLyric - 220) / 20);
		}
		else if (m_fOffsetY + (m_iSizeWord + INTERVAL) * m_iIndexLyric < 120) {
			Log.i("speed", "speed is too fast!!!");
			fSpeed = 0;
		}
//		if(speed<0.2){
//			fSpeed=0.2f;
//		}
		return fSpeed;
	}

	/**
	 * Receive Lyrics according to time of the radio
	 *
	 * @param a_iTime current Play time of the current song
	 * @return index of the current Lyrics
	 */
	public int SelectIndex(int a_iTime) {
		if (!m_bLyric) {
			return 0;
		}
		int iIndex = 0;
		for (int i = 0; i < m_mapLyric.size(); i++) {
			DataLyric dataLyric = m_mapLyric.get(i);
			if (dataLyric.iTimeBegin < a_iTime) {
				++iIndex;
			}
		}
		m_iIndexLyric = iIndex - 1;
		if (m_iIndexLyric < 0) {
			m_iIndexLyric = 0;
		}
		return m_iIndexLyric;

	}

	/**
	 * Read Lyric File
	 */
	public void Read(String a_sSong, String a_sArtist) {
		TreeMap<Integer, DataLyric> mapLyricRead = new TreeMap<Integer, DataLyric>();
		String sFileLyric = UtilMusic.GetLyricFile(a_sSong, a_sArtist);
		String sData = "";

		m_sTitle = a_sSong;
		m_sArtist = a_sArtist;

		if (sFileLyric != null) {
			File fileSave = new File(sFileLyric);
			if (fileSave.isFile()) {
				m_bLyric = true;
				try {
					FileInputStream streamInput = new FileInputStream(fileSave); // context.openFileInput(file);

					BufferedReader readerBuffered = new BufferedReader(
						new InputStreamReader(streamInput, "gb18030")
					);
					Pattern pattern = Pattern.compile("\\d{2}");
					while ((sData = readerBuffered.readLine()) != null) {
						if (sData.startsWith("[ti")) {
							String sTitle = sData.substring(4, sData.length() - 1);
							DataLyric dataLyric = new DataLyric();
							dataLyric.iTimeBegin = 0;
							dataLyric.sLyric = sTitle;
							mapLyricRead.put(0, dataLyric);
							continue;
						}
						else if (sData.startsWith("[ar")) {
							String sArtist = sData.substring(4, sData.length() - 1);
							DataLyric dataLyric = new DataLyric();
							dataLyric.iTimeBegin = 1;
							dataLyric.sLyric = sArtist;
							mapLyricRead.put(1, dataLyric);
							continue;
						}
						else if (sData.startsWith("[al")) {
							String sAlbum = sData.substring(4, sData.length() - 1);
							DataLyric dataLyric = new DataLyric();
							dataLyric.iTimeBegin = 2;
							dataLyric.sLyric = sAlbum;
							mapLyricRead.put(2, dataLyric);

							DataLyric dataLyricTips = new DataLyric();
							dataLyricTips.iTimeBegin = 3;
							dataLyricTips.sLyric = getContext().getString(R.string.mp_lyrics_tips);
							mapLyricRead.put(3, dataLyricTips);
							continue;
						}
						sData = sData.replace("[", "");	// Modify front to back
						sData = sData.replace("]", "@");
						String splitdata[] = sData.split("@");
						if (sData.endsWith("@")) {
							for (int k = 0; k < splitdata.length; k++) {
								String sTemp = splitdata[k];
								sTemp = sTemp.replace(":", ".");
								sTemp = sTemp.replace(".", "@");
								String vsTime[] = sTemp.split("@");
								Matcher matcher = pattern.matcher(vsTime[0]);
								if (vsTime.length == 3 && matcher.matches()) {
									int m = Integer.parseInt(vsTime[0]);		// Minute
									int s = Integer.parseInt(vsTime[1]);		// Second
									int ms = Integer.parseInt(vsTime[2]);		// MilliSecond
									int currTime = (m * 60 + s) * 1000 + ms * 10;
									if (currTime == 0) {
										currTime = 10;
									}
									DataLyric dataLyric = new DataLyric();
									dataLyric.iTimeBegin = currTime;
									dataLyric.sLyric = "";
									mapLyricRead.put(currTime, dataLyric);
								}
							}
						}
						else {
							String sLyric = splitdata[splitdata.length - 1];
							for (int j = 0; j < splitdata.length - 1; j++) {
								String sTemp = splitdata[j];
								sTemp = sTemp.replace(":", ".");
								sTemp = sTemp.replace(".", "@");
								String vsTime[] = sTemp.split("@");
								Matcher matcher = pattern.matcher(vsTime[0]);
								if (vsTime.length == 3 && matcher.matches()) {
									int iMin = Integer.parseInt(vsTime[0]);		// Min
									int iSec = Integer.parseInt(vsTime[1]);		// Sec
									int iMil = Integer.parseInt(vsTime[2]);		// MilliSec
									int iTimeCurrent = (iMin * 60 + iSec) * 1000 + iMil * 10;
									if (iTimeCurrent == 0) {
										iTimeCurrent = 20;
									}
									DataLyric dataLyric = new DataLyric();
									dataLyric.iTimeBegin = iTimeCurrent;
									dataLyric.sLyric = sLyric;
									mapLyricRead.put(iTimeCurrent, dataLyric);
								}
							}
						}
					}
					streamInput.close();
				}
				catch (IOException e) {
					Log.e(TAG, "Lyric IOException", e);
				}
				/*
				 * Calc time per lyric according to HashMap
				 */
				m_mapLyric.clear();
				sData = "";
				Iterator<Integer> iterator = mapLyricRead.keySet().iterator();
				DataLyric dataLyricOld = null;
				int i = 0;
				while (iterator.hasNext()) {
					Object obj = iterator.next();

					DataLyric dataLyric = mapLyricRead.get(obj);

					if (dataLyricOld == null) {
						dataLyricOld = dataLyric;
					}
					else {
						DataLyric dataLyricTemp = new DataLyric();
						dataLyricTemp = dataLyricOld;
						dataLyricTemp.iTimeline = dataLyric.iTimeBegin - dataLyricOld.iTimeBegin;
						m_mapLyric.put(Integer.valueOf(i), dataLyricTemp);
						i++;
						dataLyricOld = dataLyric;
					}
					if (!iterator.hasNext()) {
						m_mapLyric.put(Integer.valueOf(i), dataLyric);
					}
				}
			}
			else {
				m_bLyric = false;
			}
		}
		else {
			m_bLyric = false;
		}
	}

	public boolean IsLyric() {
		return m_bLyric;
	}

	public float GetOffsetY() {
		return m_fOffsetY;
	}

	public void SetOffsetY(float a_fOffsetY) {
		m_fOffsetY = a_fOffsetY;
	}

	public int GetSizeWord() {
		return m_iSizeWord;
	}

	public void SetSizeWord(int a_iSizeWord) {
		m_iSizeWord = a_iSizeWord;
	}
}
