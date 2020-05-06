package miui.keyguard.clock;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.miui.system.internal.R;

public class MiuiVerticalClock extends MiuiBaseClock {
    private TextView mTimeText;

    public MiuiVerticalClock(Context context) {
        this(context, (AttributeSet) null);
    }

    public MiuiVerticalClock(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mTimeText = (TextView) findViewById(R.id.current_time);
        updateTime();
    }

    public void setTextColorDark(boolean textDark) {
        super.setTextColorDark(textDark);
        int color = textDark ? getResources().getColor(R.color.miui_common_time_dark_text_color) : -1;
        this.mTimeText.setTextColor(color);
        setInfoDarkMode(color);
    }

    public float getTopMargin() {
        return (float) this.mContext.getResources().getDimensionPixelSize(R.dimen.miui_center_clock_magin_top);
    }

    public void updateTime() {
        int dateResId;
        super.updateTime();
        if (this.m24HourFormat) {
            dateResId = R.string.miui_vertical_time_format_24;
        } else {
            dateResId = R.string.miui_vertical_time_format_12;
        }
        this.mTimeText.setText(this.mCalendar.format(this.mContext.getString(dateResId)));
    }

    /* access modifiers changed from: protected */
    public void updateViewsTextSize() {
        super.updateViewsTextSize();
        this.mTimeText.setTextSize(0, (float) ((int) (this.mScaleRatio * ((float) this.mContext.getResources().getDimensionPixelSize(R.dimen.miui_clock_center_time_text_size)))));
    }

    /* access modifiers changed from: protected */
    public void updateViewsLayoutParams() {
        int i;
        FrameLayout.LayoutParams clockLayoutParams = (FrameLayout.LayoutParams) getLayoutParams();
        if (this.mHasTopMargin) {
            i = (int) (this.mScaleRatio * ((float) this.mResources.getDimensionPixelSize(R.dimen.miui_center_clock_magin_top)));
        } else {
            i = 0;
        }
        clockLayoutParams.topMargin = i;
        setLayoutParams(clockLayoutParams);
        LinearLayout.LayoutParams dateInfoLayoutParams = (LinearLayout.LayoutParams) this.mCurrentDate.getLayoutParams();
        dateInfoLayoutParams.topMargin = (int) (this.mScaleRatio * ((float) this.mResources.getDimensionPixelSize(R.dimen.miui_vertical_clock_date_info_top_margin)));
        this.mCurrentDate.setLayoutParams(dateInfoLayoutParams);
        LinearLayout.LayoutParams lunarCalendarInfoLayoutParams = (LinearLayout.LayoutParams) this.mLunarCalendarInfo.getLayoutParams();
        lunarCalendarInfoLayoutParams.topMargin = (int) (this.mScaleRatio * ((float) this.mResources.getDimensionPixelSize(R.dimen.miui_clock_lunar_calendar_top_margin)));
        this.mLunarCalendarInfo.setLayoutParams(lunarCalendarInfoLayoutParams);
        LinearLayout.LayoutParams ownerInfoLayoutParams = (LinearLayout.LayoutParams) this.mOwnerInfo.getLayoutParams();
        ownerInfoLayoutParams.topMargin = (int) (this.mScaleRatio * ((float) this.mResources.getDimensionPixelSize(R.dimen.miui_clock_owner_info_top_margin)));
        this.mOwnerInfo.setLayoutParams(ownerInfoLayoutParams);
    }
}
