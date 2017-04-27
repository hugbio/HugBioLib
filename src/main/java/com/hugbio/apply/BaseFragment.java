package com.hugbio.apply;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import com.hugbio.core.AndroidEventManager;
import com.hugbio.core.Event;
import com.hugbio.core.EventManager;
import com.hugbio.utils.ErrorMsgException;
import com.hugbio.utils.NetException;
import com.hugbio.utils.ToastManager;
import com.hugbio.utils.ViewTools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * 作者： huangbiao
 * 时间： 2017-04-25
 */
public class BaseFragment extends Fragment implements EventManager.OnEventListener {

    protected boolean mIS_Dialog = true;
    protected ToastManager mToastManager;

    protected AndroidEventManager mEventManager = AndroidEventManager
            .getInstance();

    private SparseArray<EventManager.OnEventListener> mMapCodeToListener;
    private HashMap<Event, Event> mMapPushEvents;
    private SparseIntArray mMapDismissProgressDialogEventCode;
    private HashMap<Event, Boolean> mMapEventToProgressBlock;
    private SparseArray<List<Runnable>> mMapCodeToEventEndRunnable;
    private SparseArray<List<TriggerEvent>> mMapListenCodeToTriggerEvent;

    protected ProgressDialog mProgressDialog;
    protected View mViewXProgressDialog;
    protected int mXProgressDialogShowCount = 0;
    protected boolean mIsXProgressDialogShowing;
    private boolean mIsXProgressAdded;
    private int mProgressDialogSize = 0;

    private boolean mIsResume;
    public boolean isToast = true;// 控制是否需要toast显示


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToastManager = ToastManager.getInstance(getActivity().getApplicationContext());
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMapCodeToListener != null) {
            final int nCount = mMapCodeToListener.size();
            for (int nIndex = 0; nIndex < nCount; ++nIndex) {
                int nCode = mMapCodeToListener.keyAt(nIndex);
                mEventManager.removeEventListener(nCode,
                        mMapCodeToListener.get(nCode));
            }
            mMapCodeToListener.clear();
        }

        if (mMapPushEvents != null) {
            for (Event e : mMapPushEvents.keySet()) {
                mEventManager.removeEventListener(e.getEventCode(), this);
                mEventManager.removeEventListenerEx(e, this);
            }
            mMapPushEvents.clear();
        }

        if (mMapCodeToEventEndRunnable != null) {
            mMapCodeToEventEndRunnable.clear();
        }
        while (mIsXProgressDialogShowing) {
            dismissXProgressDialog();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mIsResume = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        mIsResume = false;
    }

    public boolean isResume() {
        return mIsResume;
    }

    protected Event pushEvent(int eventCode, Object... params) {
        return pushEventEx(eventCode, true, false, null, params);
    }

    protected Event pushEventBlock(int eventCode, Object... params) {
        return pushEventEx(eventCode, true, true, null, params);
    }

    protected Event pushEventNoProgress(int eventCode, Object... params) {
        return pushEventEx(eventCode, false, false, null, params);
    }

    @SuppressLint("UseSparseArrays")
    protected Event pushEventEx(int eventCode, boolean bShowProgress,
                                boolean bBlock, String progressMsg, Object... params) {
        Event e = null;
        if (mMapCodeToListener == null) {
            mMapCodeToListener = new SparseArray<EventManager.OnEventListener>();
        }
        if (mEventManager.isEventRunning(eventCode, params)) {
            e = new Event(eventCode, params);
            if (mMapCodeToListener.get(eventCode) == null
                    && (mMapPushEvents == null || !mMapPushEvents
                    .containsKey(e))) {
                e = mEventManager.pushEventEx(eventCode, this, params);
                if (mMapPushEvents == null) {
                    mMapPushEvents = new HashMap<Event, Event>();
                }
                mMapPushEvents.put(e, e);
            }
        } else {
            if (mMapCodeToListener.get(eventCode) != null) {
                e = mEventManager.pushEvent(eventCode, params);
            } else {
                e = mEventManager.pushEventEx(eventCode, this, params);
                if (mMapPushEvents == null) {
                    mMapPushEvents = new HashMap<Event, Event>();
                }
                mMapPushEvents.put(e, e);
            }
        }

        if (mMapEventToProgressBlock == null) {
            mMapEventToProgressBlock = new HashMap<Event, Boolean>();
        }

        if (!mMapEventToProgressBlock.containsKey(e)) {
            if (bShowProgress) {
                if (bBlock) {
                    showProgressDialog(null, progressMsg);
                } else {

                    if (mIS_Dialog) {
                        showXProgressDialog();
                    } else {
                        mIS_Dialog = true;
                    }
                }

                mMapEventToProgressBlock.put(e, bBlock);
            }
        }

        return e;
    }

    protected void addAndManageEventListener(int eventCode) {
        addAndManageEventListener(eventCode, false);
    }

    protected void addAndManageEventListener(int eventCode,
                                             boolean bDismissProgressDialog) {
        if (mMapCodeToListener == null) {
            mMapCodeToListener = new SparseArray<EventManager.OnEventListener>();
        }
        if (mMapCodeToListener.get(eventCode) == null) {
            mMapCodeToListener.put(eventCode, this);

            mEventManager.addEventListener(eventCode, this, false);
        }

        if (bDismissProgressDialog) {
            if (mMapDismissProgressDialogEventCode == null) {
                mMapDismissProgressDialogEventCode = new SparseIntArray();
            }
            mMapDismissProgressDialogEventCode.put(eventCode, eventCode);
        }
    }

    protected void removeEventListener(int eventCode) {
        if (mMapCodeToListener == null) {
            return;
        }
        mMapCodeToListener.remove(eventCode);

        mEventManager.removeEventListener(eventCode, this);
    }

    protected void bindEventListenerRunnable(int eventCode, Runnable run) {
        if (mMapCodeToEventEndRunnable == null) {
            mMapCodeToEventEndRunnable = new SparseArray<List<Runnable>>();
        }
        List<Runnable> listRunnable = mMapCodeToEventEndRunnable.get(eventCode);
        if (listRunnable == null) {
            listRunnable = new LinkedList<Runnable>();
            mMapCodeToEventEndRunnable.put(eventCode, listRunnable);
        }
        listRunnable.add(run);
    }

    protected void unbindEventListenerRunnable(int eventCode, Runnable run) {
        if (mMapCodeToEventEndRunnable == null) {
            return;
        }
        List<Runnable> listRunnable = mMapCodeToEventEndRunnable.get(eventCode);
        if (listRunnable != null) {
            listRunnable.remove(run);
        }
    }

    protected void bindTriggerEventCode(int listenCode, int triggerCode) {
        bindTriggerEvent(listenCode, triggerCode, false);
    }

    protected void bindTriggerEvent(int listenCode, int triggerCode,
                                    boolean bShowProgress, Object... params) {
        if (mMapListenCodeToTriggerEvent == null) {
            mMapListenCodeToTriggerEvent = new SparseArray<List<TriggerEvent>>();
        }
        List<TriggerEvent> tes = mMapListenCodeToTriggerEvent.get(listenCode);
        if (tes == null) {
            tes = new ArrayList<TriggerEvent>();
            mMapListenCodeToTriggerEvent.put(listenCode, tes);
        }
        final TriggerEvent te = new TriggerEvent(triggerCode, params,
                bShowProgress);
        tes.add(te);
        addAndManageEventListener(listenCode);
    }

    @Override
    public void onEventRunEnd(Event event) {
        final int code = event.getEventCode();
        if (mIsResume) {
            if (!event.isSuccess()) {
                final Exception e = event.getFailException();
                if (e != null) {
                    if (e instanceof NetException) {
                        onHandleStringIdException((NetException) e);
                    } else if (e instanceof ErrorMsgException) {
                        onHandleErrorMsgException((ErrorMsgException) e);
                    }
                }
            }
        }
        if (mMapListenCodeToTriggerEvent != null) {
            if (event.isSuccess()) {
                final List<TriggerEvent> tes = mMapListenCodeToTriggerEvent
                        .get(code);
                if (tes != null) {
                    for (TriggerEvent te : tes) {
                        if (te.mIsShowProgress) {
                            pushEvent(te.mEventCode, te.mParams);
                        } else {
                            pushEventNoProgress(te.mEventCode, te.mParams);
                        }
                    }
                }
            }
        }
        if (mMapDismissProgressDialogEventCode != null
                && mMapDismissProgressDialogEventCode.get(code, -1) != -1) {
            dismissProgressDialog();
            dismissXProgressDialog();
        }

        if (mMapPushEvents != null) {
            mMapPushEvents.remove(event);
        }

        if (mMapEventToProgressBlock != null) {
            Boolean block = mMapEventToProgressBlock.remove(event);
            if (block != null) {
                if (block.booleanValue()) {
                    dismissProgressDialog();
                } else {
                    dismissXProgressDialog();
                }
            }
        }
        if (mMapCodeToEventEndRunnable != null) {
            List<Runnable> listRunnable = mMapCodeToEventEndRunnable.get(code);
            if (listRunnable != null) {
                for (Runnable run : listRunnable) {
                    run.run();
                }
            }
        }
    }

    protected void onHandleStringIdException(NetException e) {
        final int id = e.getStringId();
        if (isToast) {
            mToastManager.show(id);
        }
    }

    protected void onHandleErrorMsgException(ErrorMsgException e) {
        String resultCode = e.getResultCode();
    }

    private void showErrorDialog(ErrorMsgException e) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("提示:");
        builder.setMessage(e.getErrorMsg());
        builder.setNegativeButton("确定", (DialogInterface.OnClickListener)null);
        builder.create();
        builder.show();
    }

    protected static interface EventRunnable {
        public void onEventRunEnd(Event event);
    }

    private static class TriggerEvent {
        public final int mEventCode;
        public final Object mParams[];
        public final boolean mIsShowProgress;

        public TriggerEvent(int code, Object params[], boolean bShowProgress) {
            mEventCode = code;
            mParams = params;
            mIsShowProgress = bShowProgress;
        }
    }

    protected void showProgressDialog(String strTitle, String strMessage) {
        if (mProgressDialog == null) {
            Context context;
            if (getActivity().getParent() != null) {
                context = getActivity().getParent();
            } else {
                context = getActivity();
            }
            mProgressDialog = ProgressDialog.show(context, strTitle, strMessage,
                    true, false);
        }
    }

    protected void showXProgressDialog() {
        showXProgressDialog(null);
    }

    protected void showXProgressDialog(String text) {
        ++mXProgressDialogShowCount;
        if (mIsXProgressDialogShowing) {
            return;
        }
        if (mIsXProgressAdded) {
            mViewXProgressDialog.setVisibility(View.VISIBLE);
            mIsXProgressDialogShowing = true;
        } else {
            final View layout =  ViewTools.createXProgressDialog(getActivity());
            if(mProgressDialogSize == 0){
                mProgressDialogSize = ViewTools.dipToPixel(getActivity(), 70);
            }
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(mProgressDialogSize,
                    mProgressDialogSize);
            lp.gravity = Gravity.CENTER;
            getActivity().addContentView(layout, lp);
            mViewXProgressDialog = layout;
            mIsXProgressDialogShowing = true;
            mIsXProgressAdded = true;
        }

    }

    protected void dismissXProgressDialog() {
        if (mIsXProgressDialogShowing) {
            if (--mXProgressDialogShowCount == 0) {
                mViewXProgressDialog.setVisibility(View.GONE);

                mIsXProgressDialogShowing = false;
            }
        }
    }

    protected void dismissProgressDialog() {
        try {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }
        } catch (Exception e) {
        }
        mProgressDialog = null;
    }
}
