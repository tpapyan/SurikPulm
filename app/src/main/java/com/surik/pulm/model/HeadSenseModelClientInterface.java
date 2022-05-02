package com.surik.pulm.model;
/**
 * Confidential and Proprietary
 * Copyright ©2016 HeadSense Medical Ltd.
 * All rights reserved.
 */
/**
 * provides main interface for model
 */
public interface HeadSenseModelClientInterface {

	/**
	 * @return status Return the new status after a change
	 */
	public void headSenseModelStatusChanged(HeadSenseModelEnum status);
}
