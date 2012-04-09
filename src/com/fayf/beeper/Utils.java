package com.fayf.beeper;

public class Utils {
	public static int dp2px(int dp){
		return (int) (dp * G.density + 0.5f);
	}
}
