//@formatter:off
package com.zenithairplayreceive.pro.view;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.xindawn.airgl.NativeVideo;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;


public class ViewGl extends GLSurfaceView {

	private static class ContextFactory implements GLSurfaceView.EGLContextFactory {
		private static int EGL_CONTEXT_CLIENT_VERSION = 0x3098;

		@Override
		public EGLContext createContext(EGL10 egl, EGLDisplay eglDisplay, EGLConfig eglConfig) {
			Log.w(TAG, "creating OpenGL ES 2.0 context");
			CheckErrorEgl("Before eglCreateContext", egl);
			int[] viAttr = {EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE};
			EGLContext eglContext = egl.eglCreateContext(eglDisplay, eglConfig, EGL10.EGL_NO_CONTEXT, viAttr);
			CheckErrorEgl("After eglCreateContext", egl);
			return eglContext;
		}

		@Override
		public void destroyContext(EGL10 egl, EGLDisplay eglDisplay, EGLContext eglContext) {
			egl.eglDestroyContext(eglDisplay, eglContext);
		}
	}


	private static class ConfigChooser implements GLSurfaceView.EGLConfigChooser {
		/* This EGL config specification is used to specify 2.0 rendering.
		 * We use a minimum size of 4 bits for red/green/blue, but will
		 * perform actual matching in ChooseConfig() below.
		 */
		private static int EGL_OPENGL_ES2_BIT = 4;

		private static int[] m_viConfigAttr = {
			EGL10.EGL_RED_SIZE,			4,
			EGL10.EGL_GREEN_SIZE,		4,
			EGL10.EGL_BLUE_SIZE,		4,
			EGL10.EGL_RENDERABLE_TYPE,	EGL_OPENGL_ES2_BIT,
			EGL10.EGL_NONE
		};

		// Subclasses can adjust these values:
		protected int m_iSizeRed;
		protected int m_iSizeGreen;
		protected int m_iSizeBlue;
		protected int m_iSizeAlpha;
		protected int m_iSizeDepth;
		protected int m_iSizeStencil;
		private int[] m_viValue = new int[1];

		public ConfigChooser(int r, int g, int b, int a, int depth, int stencil) {
			m_iSizeRed = r;
			m_iSizeGreen = g;
			m_iSizeBlue = b;
			m_iSizeAlpha = a;
			m_iSizeDepth = depth;
			m_iSizeStencil = stencil;
		}

		@Override
		public EGLConfig chooseConfig(EGL10 egl, EGLDisplay eglDisplay) {
			// Get the number of minimally matching EGL configurations
			int[] vNumConfig = new int[1];
			egl.eglChooseConfig(eglDisplay, m_viConfigAttr, null, 0, vNumConfig);

			int iNumConfig = vNumConfig[0];

			if (iNumConfig <= 0) {
				throw new IllegalArgumentException("No configs match configSpec");
			}

			// Allocate then read the array of minimally matching EGL configs
			EGLConfig[] vConfig = new EGLConfig[iNumConfig];
			egl.eglChooseConfig(eglDisplay, m_viConfigAttr, vConfig, iNumConfig, vNumConfig);

			if (DEBUG) {
				PrintConfig(egl, eglDisplay, vConfig);
			}
			// Now return the "best" one
			return ChooseConfig(egl, eglDisplay, vConfig);
		}

		public EGLConfig ChooseConfig(EGL10 egl, EGLDisplay eglDisplay, EGLConfig[] vEglConfig) {
			for (EGLConfig config : vEglConfig) {
				int iD = FindConfigAttrib(egl, eglDisplay, config, EGL10.EGL_DEPTH_SIZE, 0);
				int iS = FindConfigAttrib(egl, eglDisplay, config, EGL10.EGL_STENCIL_SIZE, 0);

				// We need at least m_iSizeDepth and m_iSizeStencil bits
				if (iD < m_iSizeDepth || iS < m_iSizeStencil)
					continue;

				// We want an *exact* match for red/green/blue/alpha
				int iR = FindConfigAttrib(egl, eglDisplay, config, EGL10.EGL_RED_SIZE, 0);
				int iG = FindConfigAttrib(egl, eglDisplay, config, EGL10.EGL_GREEN_SIZE, 0);
				int iB = FindConfigAttrib(egl, eglDisplay, config, EGL10.EGL_BLUE_SIZE, 0);
				int iA = FindConfigAttrib(egl, eglDisplay, config, EGL10.EGL_ALPHA_SIZE, 0);

				if (iR == m_iSizeRed && iG == m_iSizeGreen && iB == m_iSizeBlue && iA == m_iSizeAlpha)
					return config;
			}
			return null;
		}

		private int FindConfigAttrib(EGL10 egl, EGLDisplay eglDisplay, EGLConfig eglConfig, int iAttr, int iDefault) {
			if (egl.eglGetConfigAttrib(eglDisplay, eglConfig, iAttr, m_viValue)) {
				return m_viValue[0];
			}
			return iDefault;
		}

		private void PrintConfig(EGL10 egl, EGLDisplay eglDisplay, EGLConfig[] vEglConfig) {
			int numConfigs = vEglConfig.length;
			Log.w(TAG, String.format("%d configurations", numConfigs));
			for (int i = 0; i < numConfigs; i++) {
				Log.w(TAG, String.format("Configuration %d:\n", i));
				PrintConfig(egl, eglDisplay, vEglConfig[i]);
			}
		}

		private void PrintConfig(EGL10 egl, EGLDisplay eglDisplay, EGLConfig eglConfig) {
			int[] viAttr = {
				EGL10.EGL_BUFFER_SIZE,
				EGL10.EGL_ALPHA_SIZE,
				EGL10.EGL_BLUE_SIZE,
				EGL10.EGL_GREEN_SIZE,
				EGL10.EGL_RED_SIZE,
				EGL10.EGL_DEPTH_SIZE,
				EGL10.EGL_STENCIL_SIZE,
				EGL10.EGL_CONFIG_CAVEAT,
				EGL10.EGL_CONFIG_ID,
				EGL10.EGL_LEVEL,
				EGL10.EGL_MAX_PBUFFER_HEIGHT,
				EGL10.EGL_MAX_PBUFFER_PIXELS,
				EGL10.EGL_MAX_PBUFFER_WIDTH,
				EGL10.EGL_NATIVE_RENDERABLE,
				EGL10.EGL_NATIVE_VISUAL_ID,
				EGL10.EGL_NATIVE_VISUAL_TYPE,
				0x3030, // EGL10.EGL_PRESERVED_RESOURCES,
				EGL10.EGL_SAMPLES,
				EGL10.EGL_SAMPLE_BUFFERS,
				EGL10.EGL_SURFACE_TYPE,
				EGL10.EGL_TRANSPARENT_TYPE,
				EGL10.EGL_TRANSPARENT_RED_VALUE,
				EGL10.EGL_TRANSPARENT_GREEN_VALUE,
				EGL10.EGL_TRANSPARENT_BLUE_VALUE,
				0x3039, // EGL10.EGL_BIND_TO_TEXTURE_RGB,
				0x303A, // EGL10.EGL_BIND_TO_TEXTURE_RGBA,
				0x303B, // EGL10.EGL_MIN_SWAP_INTERVAL,
				0x303C, // EGL10.EGL_MAX_SWAP_INTERVAL,
				EGL10.EGL_LUMINANCE_SIZE,
				EGL10.EGL_ALPHA_MASK_SIZE,
				EGL10.EGL_COLOR_BUFFER_TYPE,
				EGL10.EGL_RENDERABLE_TYPE,
				0x3042 // EGL10.EGL_CONFORMANT
			};
			String[] vsName = {
				"EGL_BUFFER_SIZE",
				"EGL_ALPHA_SIZE",
				"EGL_BLUE_SIZE",
				"EGL_GREEN_SIZE",
				"EGL_RED_SIZE",
				"EGL_DEPTH_SIZE",
				"EGL_STENCIL_SIZE",
				"EGL_CONFIG_CAVEAT",
				"EGL_CONFIG_ID",
				"EGL_LEVEL",
				"EGL_MAX_PBUFFER_HEIGHT",
				"EGL_MAX_PBUFFER_PIXELS",
				"EGL_MAX_PBUFFER_WIDTH",
				"EGL_NATIVE_RENDERABLE",
				"EGL_NATIVE_VISUAL_ID",
				"EGL_NATIVE_VISUAL_TYPE",
				"EGL_PRESERVED_RESOURCES",
				"EGL_SAMPLES",
				"EGL_SAMPLE_BUFFERS",
				"EGL_SURFACE_TYPE",
				"EGL_TRANSPARENT_TYPE",
				"EGL_TRANSPARENT_RED_VALUE",
				"EGL_TRANSPARENT_GREEN_VALUE",
				"EGL_TRANSPARENT_BLUE_VALUE",
				"EGL_BIND_TO_TEXTURE_RGB",
				"EGL_BIND_TO_TEXTURE_RGBA",
				"EGL_MIN_SWAP_INTERVAL",
				"EGL_MAX_SWAP_INTERVAL",
				"EGL_LUMINANCE_SIZE",
				"EGL_ALPHA_MASK_SIZE",
				"EGL_COLOR_BUFFER_TYPE",
				"EGL_RENDERABLE_TYPE",
				"EGL_CONFORMANT"
			};

			int[] viValue = new int[1];
			for (int i = 0; i < viAttr.length; i++) {
				int iAttr = viAttr[i];
				String sName = vsName[i];
				if (egl.eglGetConfigAttrib(eglDisplay, eglConfig, iAttr, viValue)) {
					Log.w(TAG, String.format("  %s: %d\n", sName, viValue[0]));
				}
				else {
					// Log.w(TAG, String.format("  %s: failed\n", name));
					while (egl.eglGetError() != EGL10.EGL_SUCCESS) ;
				}
			}
		}
	}


	private static class Renderer implements GLSurfaceView.Renderer {
		public void onDrawFrame(GL10 gl) {
			NativeVideo.draw();
		}

		public void onSurfaceChanged(GL10 gl, int iWidth, int iHeight) {
			NativeVideo.init(iWidth, iHeight);
		}

		public void onSurfaceCreated(GL10 gl, EGLConfig eglConfig) {
			// Do nothing.
			Log.e(TAG, "onSurfaceCreated");
		}
	}


	private static String TAG = "AirPlayViewGl";
	private static final boolean DEBUG = false;

	public ViewGl(Context context) {
		super(context);
		Init(false, 0, 0);
	}

	public ViewGl(Context context, boolean bTranslucent, int iDepth, int iStencil) {
		super(context);
		Init(bTranslucent, iDepth, iStencil);
	}

	private void Init(boolean bTranslucent, int iDepth, int iStencil) {
		/* By default, GLSurfaceView() creates a RGB_565 opaque surface.
		 * If we want a translucent one, we should change the surface's
		 * format here, using PixelFormat.TRANSLUCENT for GL Surfaces
		 * is interpreted as any 32-bit surface with alpha by SurfaceFlinger.
		 */
		if (bTranslucent)
			getHolder().setFormat(PixelFormat.TRANSLUCENT);

		/* Setup the context factory for 2.0 rendering.
		 * See ContextFactory class definition below
		 */
		setEGLContextFactory(new ContextFactory());

		/* We need to choose an EGLConfig that matches the format of
		 * our surface exactly. This is going to be done in our
		 * custom config chooser. See ConfigChooser class definition
		 * below.
		 */
		setEGLConfigChooser(bTranslucent
			? new ConfigChooser(8, 8, 8, 8, iDepth, iStencil)
			: new ConfigChooser(5, 6, 5, 0, iDepth, iStencil)
		);

		/* Set the renderer responsible for frame rendering */
		setRenderer(new Renderer());
	}

	private static void CheckErrorEgl(String sPrompt, EGL10 egl) {
		int iError;
		while ((iError = egl.eglGetError()) != EGL10.EGL_SUCCESS) {
			Log.e(TAG, String.format("%s: EGL error: 0x%x", sPrompt, iError));
		}
	}

//	@Override
//	public void surfaceDestroyed(SurfaceHolder holder) {
//		// Do nothing.
//		NativeVideo.deinit();
//		Log.e(TAG, "surfaceDestroyed");
//	}
}
