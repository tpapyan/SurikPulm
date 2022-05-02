package com.surik.pulm;
/**
 * Confidential and Proprietary
 * Copyright ©2016 HeadSense Medical Ltd.
 * All rights reserved.
 */

/**
 * Algorithm params
 */
public class Params {
	
	public static final int FS = 11025;//2205;//3675;
	
	public static final int    F0 = 621;
	public static final double K0 = 0.1; 
	public static final double cc0 = 3.5; 
	public static final int    nsQ0 = 2; 
	public static final int    nsQ20 = 4; 
	public static final double mQs120 = 0.03; 
	public static final int    lenRecord = 6; 
	public static final double klen_sec = 3.5; 
	public static final int    lenSp_sec = 200;
	
	public static final double Fsv1 = 0.5; 
	public static final double Fsv2 = 0.5; 
	public static final double Fsv3 = 15;
	public static final double Fsv4 = 0.5; 
	public static final double Fsv5 = 25;
	public static final double Fsv6 = 0.5; 
	public static final double Fsv7 = 45;
	public static final double Fsv8 = 0.5; 
	public static final double Fsv9 = 75;
	public static final double Fsv10 = 53;
	public static final double Fsv11 = 90;
	public static final double Fsv12 = F0 - 3;
	public static final double Fsv13 = F0 + 3;
	public static final double Fsv14 = 630;
	
	public static final double iFS1s = 0.5; 
	public static final double iFS1e = 25; 
	public static final double iFS2s = 153;
	public static final double iFS2e = 180;

//	public static final String kmString = "-385.1307 0 -45.8352 0.2740 -1.4022 0.9359 -0.5547 -33.9872 -33.4166 34.5560 -13.0942 2.5705 -102.3281 29.7351 -16.3565 45.8662 4.9822 243.4824";
//	public static final String kmString = "164.5709 575.1024 39.5447 -0.0139 1.4897 -1.4151 0.3978 13.5176 37.8854 -64.0183 37.2355 -1.6069 59.7137 -34.1793 31.0879 -46.8207 -3.9993 -113.9611";
//	public static final String kmString = "132.9155 -106.5686 34.9475 -0.0268 1.5360 -1.3463 0.5032 10.7378 35.1791 -53.9074 29.4526 -1.5558 47.0154 -32.2763 23.5691 -37.8743 -5.4208 -94.3795";
	public static final String kmString = "170.51 -417.66 20.284 -0.32865 1.3906 -0.80544 0.55611 -5.3001 29.287 -30.954 9.231 -2.8623 106.78 -24.129 11.746 -17.447 -4.7257 -89.534";

}
