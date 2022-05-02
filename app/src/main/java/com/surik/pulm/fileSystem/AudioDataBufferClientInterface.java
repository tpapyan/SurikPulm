package com.surik.pulm.fileSystem;
/**
 * Confidential and Proprietary
 * Copyright ©2016 HeadSense Medical Ltd.
 * All rights reserved.
 */
/**
 * Interface for monitoring audio states
 */
public interface AudioDataBufferClientInterface {

	/**
	 * @param status get the list of statuses
	 */
	public void recordStatusChanged(AudioDataBufferEnum status);
}
