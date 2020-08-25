//@formatter:off
package com.zenithairplayreceive.pro.player;

import com.zenithairplayreceive.pro.center.DlnaMediaModel;

public interface ListenerEnginePlay {
	public void OnTrackPlay(DlnaMediaModel itemInfo);
	public void OnTrackStop(DlnaMediaModel itemInfo);
	public void OnTrackPause(DlnaMediaModel itemInfo);
	public void OnTrackPrepareSync(DlnaMediaModel itemInfo);
	public void OnTrackPrepareComplete(DlnaMediaModel itemInfo);
	public void OnTrackStreamError(DlnaMediaModel itemInfo);
	public void OnTrackPlayComplete(DlnaMediaModel itemInfo);
}
