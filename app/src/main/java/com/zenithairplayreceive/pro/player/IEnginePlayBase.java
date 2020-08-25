//@formatter:off
package com.zenithairplayreceive.pro.player;

public interface IEnginePlayBase {
	public void Play();
	public void Pause();
	public void Stop();
	public void SkipTo(int time);
}
