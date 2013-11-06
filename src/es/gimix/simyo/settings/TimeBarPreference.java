package es.gimix.simyo.settings;

import android.content.Context;
import android.util.AttributeSet;

public class TimeBarPreference extends SeekBarPreference {
	
	private final TimeCalculator calculator;

	public TimeBarPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		calculator = new TimeCalculator(context);
		super.setMax(calculator.getTimeOptionCount() - 1); // max value is included
	}
	
	@Override
	public String getLabel(int value) {
		return calculator.getLabelForOption(value);
	}

}
