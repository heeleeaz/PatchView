package com.heeleeaz.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class PatchView extends LinearLayout {

	public static final int MATCH_PARENT = LayoutParams.MATCH_PARENT;
	public static final int WRAP_CONTENT = LayoutParams.WRAP_CONTENT;

	public static final String TAG = PatchView.class.getSimpleName();

	public PatchView(Context context) {
		super(context);
	}

	public PatchView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PatchView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * add a patch view the view is automatically updated on change note:
	 * default layout_width=MATCH_PARENT and layout_height=MATCH_PATENT
	 * 
	 * @param patch
	 */
	public void setPatchedView(final PatchBase patch) {
		setPatchedView(patch, MATCH_PARENT, MATCH_PARENT);
	}

	/**
	 * add a patch view the view is automatically updated on change
	 * 
	 * @param patch
	 */
	public void setPatchedView(final PatchBase patch, int w, int h) {
		final LayoutParams params = new LayoutParams(w, h);

		removeAllViews();
		addView(patch.getView(), 0, params);

		patch.setViewListener(new PatchViewListener() {
			@Override
			public void onChange(View v) {
				removeAllViews();
				addView(v, 0, params);
			}
		});
	}

	public interface PatchViewListener {
		void onChange(View v);
	}
}
