//@formatter:off
package com.zenithairplayreceive.pro.util;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;


public class UtilImage {
	private static final LogCommon log = LogFactory.CreateLog();

	public static Bitmap CreateBitmapRotateReflect(Bitmap a_bitmapOrg) {
		float fWidth	= ((float)200) / (a_bitmapOrg.getWidth());
		float fHeight	= ((float)200) / (a_bitmapOrg.getHeight());
		Matrix matrix = new Matrix();
		matrix.postScale(fWidth, fHeight);
		a_bitmapOrg = Bitmap.createBitmap(
			a_bitmapOrg,
			0,
			0,
			a_bitmapOrg.getWidth(),
			a_bitmapOrg.getHeight(),
			matrix,
			true
		);
		Bitmap bitmap = CreateBitmapReflect(a_bitmapOrg);
		bitmap = CreateBitmapRotate(bitmap);
		return bitmap;
	}

	public static Bitmap CreateBitmapRotateReflect(Drawable a_drawable) {
		Bitmap bitmap = ((BitmapDrawable)a_drawable).getBitmap();
		if (bitmap != null) {
			log.e("bitmap is not null");
			return CreateBitmapRotateReflect(bitmap);
		}
		return null;
	}

	public static Bitmap CreateBitmapRotate(Bitmap a_bitmapOrg) {
		Camera camera = new Camera();
		camera.save();
		camera.rotateY(10f);

		Matrix matrix = new Matrix();
		camera.getMatrix(matrix);
		camera.restore();

		Bitmap bitmap = Bitmap.createBitmap(
			a_bitmapOrg,
			0,
			0,
			a_bitmapOrg.getWidth(),
			a_bitmapOrg.getHeight(),
			matrix,
			true
		);
		//Bitmap bitmap = Bitmap.createBitmap(a_bitmapOrg, 0, 0, 270, 270, matrix, true);
		return bitmap;
	}

	public static Bitmap CreateBitmapReflect(Bitmap a_bitmapOrg) {
		final int iReflectGap = 4;

		int iWidth		= a_bitmapOrg.getWidth();
		int iHeight		= a_bitmapOrg.getHeight();

		Matrix matrix = new Matrix();

		matrix.preScale(1, -1);
		Bitmap reflectionBitmap = Bitmap.createBitmap(
			a_bitmapOrg,
			0,
			iHeight / 2,
			iWidth,
			iHeight / 2,
			matrix,
			false
		);
		Bitmap bitmapReflect = Bitmap.createBitmap(
			iWidth,
			(iHeight + iHeight / 2 + iReflectGap),
			Config.ARGB_8888
		);

		Canvas canvas = new Canvas(bitmapReflect);
		canvas.drawBitmap(a_bitmapOrg, 0, 0, null);

		Paint paintDefault = new Paint();
		canvas.drawRect(0, iHeight, iWidth, iHeight + iReflectGap, paintDefault);
		canvas.drawBitmap(reflectionBitmap, 0, iHeight + iReflectGap, null);

		Paint paint = new Paint();
		LinearGradient gradientShader = new LinearGradient(
			0,
			a_bitmapOrg.getHeight(),
			0,
			bitmapReflect.getHeight(),
			0x70ffffff,
			0x00ffffff,
			TileMode.MIRROR
		);
		paint.setShader(gradientShader);
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));

		canvas.drawRect(
			0,
			iHeight,
			iWidth,
			bitmapReflect.getHeight(),
			paint
		);

		return bitmapReflect;
	}
}
