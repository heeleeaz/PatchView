package com.heeleeaz.android.view;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.heeleeaz.android.view.PatchView.PatchViewListener;

public abstract class PatchBase {
	private Context context;
	private View view, initialViewSwap;
	protected PatchViewListener viewListener;

	protected boolean isSwappedPatch = false;

	// private OnPatchEvent onPatchEvent;
	private Object[] instanceObjects;
	private PatchBundle bundle;
	private PatchSession patchSession;

	private void init(boolean eventlyCreated, Context context,
			PatchBundle bundle, Object... instanceObjects) throws Exception {
		if (context == null)
			throw new Exception("Context cannot be null");

		this.context = context;
		this.instanceObjects = instanceObjects;
		this.bundle = bundle;

		// call onCreate and onStart firstly
		onCreate(eventlyCreated, bundle, instanceObjects);
		onStart();
	}

	/**
	 * called if PatchSession instantiates this class
	 *
	 * @throws Exception
	 */
	protected PatchBase callContruct(boolean eventlyCreated,
			PatchSession session, PatchBundle bundle, Object... instanceObjects)
			throws Exception {
		this.patchSession = session;
		init(eventlyCreated, session.getContext(), bundle, instanceObjects);
		return this;
	}

	public PatchSession getPatchSession() {
		return patchSession;
	}

	/** set patch view */
	public void setContentView(int resid) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.view = inflater.inflate(resid, null, false);
	}

	public void setContentView(View view) {
		this.view = view;
	}

	/** get the patch view child */
	public View findViewById(int id) {
		if (view != null)
			return view.findViewById(id);
		return null;
	}

	/**
	 * called firstly, to do all Patch initialization. onStart is called next
	 *
	 * @param bundle
	 *            Bundle that contains data
	 * @param sessionObject
	 *            may contain controller etc
	 */
	protected void onCreate(boolean eventlyCreated, PatchBundle bundle,
			Object... sessionObject) {
	}

	/**
	 * recreate instance of this class with specified PatchBundle as additional
	 * argument
	 *
	 * @param bundle
	 * @throws Exception
	 */
	protected void recreatePatch(PatchBundle bundle, Object instanceObjects)
			throws Exception {
		init(false, this.context, bundle, instanceObjects);
	}

	/**
	 * recreate instance of this class using already initialized bundle and
	 * saved instance argument
	 *
	 * @throws Exception
	 */
	protected void recreatePatch() throws Exception {
		init(false, this.context, this.bundle, this.instanceObjects);
	}

	/**
	 * recreate instance of this class with specified PatchBundle as additional
	 * argument
	 *
	 * @param bundle
	 * @throws Exception
	 */
	protected void recreatePatch(PatchBundle bundle) throws Exception {
		init(false, this.context, bundle, this.instanceObjects);
	}

	/**
	 * called to do initialization.
	 */
	protected void onStart() {
	}

	/**
	 * called when application resume back to view after pausing
	 * */

	protected void onResume() {
	}

	/**
	 * called if patch is going of view.
	 */
	protected void onPause() {
	}

	/**
	 * called if application as gone out of view
	 */
	protected void onStop() {
	}

	/**
	 * called if activity as been destroyed, or completely moved out of stack
	 */
	protected void onDestroy() {
	}

	/** run the specified Runnable in main thread Handler */
	protected void runOnUIThread(Runnable runnable) {
		Handler handler = new Handler(context.getMainLooper());
		handler.post(runnable);
	}

	/** get the PatchView set by setContentView */
	public View getView() {
		return this.view;
	}

	/** get base context */
	public Context getContext() {
		return context;
	}

	// /** set on patch event */
	// protected void setOnPatchEvent(OnPatchEvent onPatchEvent) {
	// this.onPatchEvent = onPatchEvent;
	// }

	/** set on view change listener */
	public void setViewListener(PatchViewListener listener) {
		this.viewListener = listener;
	}

	/**
	 * change the patch(view), and notify OnViewChanged listener
	 *
	 * @param patch
	 *            switch to a new defined PatchBase
	 * @param bundle
	 *            data to send to patch
	 * @throws Exception
	 */
	public void swapPatch(Class<? extends PatchBase> swapTo,
			PatchBundle bundle, Object... instanceObjects) throws Exception {

		// change to the new patch(swapTo) view
		PatchBase swappingPatch = patchSession.patch(swapTo, bundle, false,
				instanceObjects);
		swappingPatch.isSwappedPatch = true;

		// set initial initialViewSwap to view of the initial patch, in other to
		// be able to restore the view from the new patch
		this.initialViewSwap = getView();
		Log.d("called", "trigger onChange");
		if (viewListener != null) {
			viewListener.onChange(swappingPatch.getView());
		}
	}

	public void swapPatch(Class<? extends PatchBase> swapTo) throws Exception {
		swapPatch(swapTo, bundle, instanceObjects);
	}

	/**
	 * restore to the initial view of the patch
	 */
	public void restoreSwappedPatch() {
		if (viewListener != null) {
			viewListener.onChange(initialViewSwap);
		}
	}

	public void initNewPatch(Class<? extends PatchBase> patch,
			PatchBundle bundle, Object... instanceObjects) throws Exception {
		patchSession.patch(patch, bundle, true, instanceObjects);
	}

	protected void finish() {
		patchSession.dispatchFinishEvent(this);
	}

	/**
	 * dispatch an event to specified PatchBase session base patch
	 *
	 * @throws Exception
	 *             if error is found in creating patch
	 */
	public void dispatchEvent(String event, Class<? extends PatchBase> toPatch,
			PatchBundle bundle, boolean forceCreate, boolean notifyFrame)
			throws Exception {
		patchSession.dispatchEvent(toPatch, bundle, event, forceCreate,
				notifyFrame, instanceObjects);
	}

	public void dispatchEventToFrame(String event, PatchBundle bundle) {
		patchSession.dispatchEventToFrame(this, event, bundle);
	}

	/** called when event is fired by other patch class of same class */
	public void onEventReceived(String event, PatchBundle bundle) {
	}

}
