package com.hugbio.apply;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import com.hugbio.core.AndroidEventManager;
import com.hugbio.core.Event;
import com.hugbio.core.EventManager.OnEventListener;
import com.hugbio.utils.ErrorMsgException;
import com.hugbio.utils.NetException;
import com.hugbio.utils.ToastManager;
import com.hugbio.utils.ViewTools;

public class BaseActivity extends Activity implements OnEventListener {

    protected boolean mIS_Dialog = true;
    protected ToastManager mToastManager;

    protected AndroidEventManager mEventManager = AndroidEventManager
            .getInstance();

    private SparseArray<OnEventListener> mMapCodeToListener; // 额外的事件完成的回调函数集
    private HashMap<Event, Event> mMapPushEvents; // 当前正在执行的事件集
    private SparseIntArray mMapDismissProgressDialogEventCode; // 用于关闭连接提示对话框
    private HashMap<Event, Boolean> mMapEventToProgressBlock; // 当前启动了连接提示对话框的事件集
    private SparseArray<List<Runnable>> mMapCodeToEventEndRunnable; // 触发线程。由指定事件完成后启动
    private SparseArray<List<TriggerEvent>> mMapListenCodeToTriggerEvent; // 触发事件。由指定事件完成后驱动

    protected ProgressDialog mProgressDialog;
    protected View mViewXProgressDialog;
    protected int mXProgressDialogShowCount = 0;
    protected boolean mIsXProgressDialogShowing;
    private boolean mIsXProgressAdded;
    private int mProgressDialogSize = 0;

    private boolean mIsResume;
    public boolean isToast = true;// 控制是否需要toast显示

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToastManager = ToastManager.getInstance(getApplicationContext());
    }

    @Override
    protected void onDestroy() {
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
    protected void onResume() {
        super.onResume();
        mIsResume = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsResume = false;
    }

    public boolean isResume() {
        return mIsResume;
    }

    public Event pushEvent(int eventCode, Object... params) {
        return pushEventEx(eventCode, true, false, null, params);
    }

    public Event pushEventBlock(int eventCode, Object... params) {
        return pushEventEx(eventCode, true, true, "加载中...", params);
    }

    public Event pushEventBlockM(int eventCode, String progressMsg, Object... params) {
        return pushEventEx(eventCode, true, true, progressMsg, params);
    }

    public Event pushEventNoProgress(int eventCode, Object... params) {
        return pushEventEx(eventCode, false, false, null, params);
    }

    @SuppressLint("UseSparseArrays")
    protected Event pushEventEx(int eventCode, boolean bShowProgress,
                                boolean bBlock, String progressMsg, Object... params) {
        Event e = null;
        if (mMapCodeToListener == null) {
            mMapCodeToListener = new SparseArray<OnEventListener>();
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
            mMapCodeToListener = new SparseArray<OnEventListener>();
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
        if (isToast && id > 0) {
            mToastManager.show(id);
        }
    }

    protected void onHandleErrorMsgException(ErrorMsgException e) {
        String resultCode = e.getResultCode();
//		switch (type) {
//		case 0:
//
//			break;
//
//		case 1:
//			// alertDialog提示
//			showErrorDialog(e);
//			break;
//
//		case 2:
//			if (!e.getErrorMsg().equals("") && e.getErrorMsg() != null) {
//				mToastManager.show(e.getErrorMsg());
//			}
//			break;
//		}
    }

    private void showErrorDialog(ErrorMsgException e) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示:");
        builder.setMessage(e.getErrorMsg());
        builder.setNegativeButton("确定", null);
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
            mProgressDialog = ProgressDialog.show(this, strTitle, strMessage,
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
            final View layout = ViewTools.createXProgressDialog(this);
            if (mProgressDialogSize == 0) {
                mProgressDialogSize = ViewTools.dipToPixel(this, 70);
            }
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(mProgressDialogSize,
                    mProgressDialogSize);
            lp.gravity = Gravity.CENTER;
            addContentView(layout, lp);
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
