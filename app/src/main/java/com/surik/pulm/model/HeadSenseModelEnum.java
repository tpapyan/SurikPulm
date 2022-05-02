package com.surik.pulm.model;
/**
 * Confidential and Proprietary
 * Copyright ©2016 HeadSense Medical Ltd.
 * All rights reserved.
 */
/**
 * HeadSense Model Enum provides main states of app
 */
public enum HeadSenseModelEnum {
	ALGORITHM_HAS_FINISHED_WITH_SUCCESS,
	ALGORITHM_HAS_FINISHED_WITH_FAILURE,
	AUDIO_RECORD_STOPPED,
	AUDIO_RECORD_FAILED,
	FILES_WERE_SAVED,

	ALGORITHM_START
}
