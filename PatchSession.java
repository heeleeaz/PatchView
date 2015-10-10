package com.heeleeaz.android.view;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;

/**
 * 
 * @author elias igbalajobi an abstract class. that keeps session and bind all
 *         registered classes for one single instance of an object. this class
 *         is meant to be subclassed by a class that is willing to keep instance
 *         of PatchBase instances and share that single instance
 * 
 */
public class PatchSession {

	/** all registered patches list */

	/** all registered base patches list */
	private List<PatchBase> basePatches;
	private Context context;
	private PatchEventNotifierListener eventNotifierListener;

	public void setPatchEventNotifierListener(
			PatchEventNotifierListener eventNotifierListener) {
		this.eventNotifierListener = eventNotifierListener;
	}

	public PatchSession(Context context) {
		this.context = context;
		basePatches = new ArrayList<PatchBase>();
	}

	public Context getContext() {
		return context;
	}

	/**
	 * initialize base patch
	 * 
	 * @param cls
	 *            PatchBase class
	 * @param ctx
	 *            Argument for PatchBase contructor
	 * @return
	 * @throws Exception
	 *             if base patch has not been registered
	 */
	private PatchBase initBasePatch(PatchSession session, Class<PatchBase> cls,
			PatchBundle bundle, Object... objects) throws Exception {
		Constructor<? extends PatchBase> constr = cls.getDeclaredConstructor();
		PatchBase patchBase = constr.newInstance();
		basePatches.add(patchBase);
		return patchBase;
	}

	/**
	 * get patch bundle of the specified class
	 * 
	 * @param cls
	 *            class object for the specified PatchBase
	 * @return
	 * @throws Exception
	 *             if base patch as not been registered
	 */
	public PatchBase getBasePatch(Class<PatchBase> cls) {
		for (PatchBase patchBase : basePatches) {
			if (patchBase.getClass().getName() == cls.getName())
				return patchBase;
		}
		return null;
	}

	/**
	 * get the specified patch bundle view
	 * 
	 * @param cls
	 *            PatchBundle in which view is requested
	 * @return
	 * @throws Exception
	 *             if patch bundle has not been registered
	 */
	public View getBasePatchView(Class<PatchBase> cls) throws Exception {
		for (PatchBase patchBase : basePatches) {
			if (patchBase.getClass().getName() == cls.getName())
				return patchBase.getView();
		}
		return null;
	}

	/**
	 * get all registered base patchs
	 * 
	 * @return {@code List<PatchBase>}
	 */
	public List<PatchBase> getBasePatches() {
		return basePatches;
	}

	protected PatchBase getPatchBase(Class<? extends PatchBase> patch) {
		if (basePatches.size() <= 0)
			return null;

		for (PatchBase pb : basePatches) {
			if (pb.getClass().getName() == patch.getName()) {
				return pb;
			}
		}
		return null;
	}

	/**
	 * get all base patch view
	 * 
	 * @return {@code View[]}
	 */
	public View[] getBasePatchesView() {
		View[] v = new View[basePatches.size()];
		for (int i = 0; i < v.length; i++)
			v[i] = basePatches.get(i).getView();
		return v;
	}

	@SuppressWarnings("unchecked")
	public <T extends PatchBase> T patch(Class<? extends PatchBase> cls,
			PatchBundle bundle, boolean notifyFrame, Object... objects)
			throws Exception {
		Class<PatchBase> bpCls = (Class<PatchBase>) cls;
		PatchBase pb = initBasePatch(this, bpCls, bundle, objects)
				.callContruct(false, this, bundle, objects);
		if (notifyFrame && eventNotifierListener != null) {
			eventNotifierListener.onNewPatchInit(pb);
			return (T) getBasePatch(bpCls);
		} else {
			return (T) getBasePatch(bpCls);
		}
	}

	@SuppressWarnings("unchecked")
	protected void dispatchEvent(Class<? extends PatchBase> toPatch,
			final PatchBundle bundle, String event, boolean forceCreate,
			boolean notifyFrame, Object... instanceObjects) throws Exception {
		PatchBase pb = getPatchBase(toPatch);
		if (pb == null) {
			initBasePatch(this, (Class<PatchBase>) toPatch, bundle,
					instanceObjects).callContruct(true, this, bundle,
					instanceObjects);
			dispatchEvent(toPatch, bundle, event, forceCreate, notifyFrame,
					instanceObjects);
			return;
		}

		pb.onEventReceived(event, bundle);// perform notification
		if (eventNotifierListener != null && notifyFrame) {
			eventNotifierListener.onPatchEvent(pb, event, bundle);
		}
	}

	protected void dispatchEventToFrame(PatchBase fromPatch, String event,
			PatchBundle bundle) {
		if (eventNotifierListener != null) {
			eventNotifierListener.onEventToFrame(fromPatch, event, bundle);
		}
	}

	public void dispatchFinishEvent(PatchBase patchBase) {
		if (eventNotifierListener != null) {
			eventNotifierListener.onPatchFinish(patchBase);
		}
	}

	/**
	 * call this to dispatch onStop protected method of all registered
	 * BasePatches
	 */
	protected void dispatchOnStop() {
		try {
			for (PatchBase patchBase : basePatches) {
				Method method = patchBase.getClass().getSuperclass()
						.getDeclaredMethod("onStop");
				method.setAccessible(true);
				method.invoke(patchBase);
			}
		} catch (Exception e) {
		}
	}

	/**
	 * call this to dispatch onDestroy protected method of all registered
	 * BasePatches
	 */
	protected void dispatchOnDestroy() {
		try {
			for (PatchBase patchBase : basePatches) {
				Method method = patchBase.getClass().getSuperclass()
						.getDeclaredMethod("onDestroy");
				method.setAccessible(true);
				method.invoke(patchBase);
			}
		} catch (Exception e) {
		}
	}

	/**
	 * call this to dispatch onPause event to all registered BasePatches.
	 * 
	 * @param bundle
	 */
	protected void dispatchOnPause() {
		try {
			for (PatchBase patchBase : basePatches) {
				Method method = patchBase.getClass().getSuperclass()
						.getDeclaredMethod("onPause", PatchBundle.class);
				method.setAccessible(true);
				method.invoke(patchBase);
			}
		} catch (Exception e) {
		}
	}

	/**
	 * call this to dispatch onResume event to all registered BasePatches.
	 * 
	 * @param bundle
	 */
	protected void dispatchOnResume() {
		try {
			for (PatchBase patchBase : basePatches) {
				Method method = patchBase.getClass().getSuperclass()
						.getDeclaredMethod("onResume", PatchBundle.class);
				method.setAccessible(true);
				method.invoke(patchBase);
			}
		} catch (Exception e) {
		}
	}

	public interface PatchEventNotifierListener {
		void onPatchEvent(PatchBase toPatch, String event, PatchBundle bundle);

		void onEventToFrame(PatchBase fromPatch, String event,
				PatchBundle bundle);

		void onNewPatchInit(PatchBase patch);

		void onPatchFinish(PatchBase patch);
	}

	protected interface PatchEventListener {
		void onEvent(String fromPatch, PatchBundle bundle,
				Class<? extends PatchBase> toPatch);
	}
}
