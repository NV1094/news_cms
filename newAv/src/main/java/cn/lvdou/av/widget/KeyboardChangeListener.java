package cn.lvdou.av.widget;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.view.Window;

import java.lang.reflect.Method;


/**
 * 监听键盘弹出隐藏   需要dialog或者activity设置adjustresize
 */
public class KeyboardChangeListener implements ViewTreeObserver.OnGlobalLayoutListener {
    private static final String TAG = "KeyboardChangeListener";
    public int MIN_KEYBOARD_HEIGHT;
    private KeyboardListener mKeyboardListener;
    private boolean mShowFlag = false;
    private Window mWindow;
    private View mContentView;
    private int mScreenHeight;
    private Rect mRect = new Rect();

    public void destroy() {
        if (mContentView != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mContentView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
            mContentView.removeCallbacks(refreshRunnable);
        }
        MIN_KEYBOARD_HEIGHT = 0;
    }

    public void setKeyboardListener(KeyboardListener keyboardListener) {
        this.mKeyboardListener = keyboardListener;
    }

    public static KeyboardChangeListener create(Activity activity) {
        return new KeyboardChangeListener(activity);
    }

    public static KeyboardChangeListener create(Dialog dialog) {
        return new KeyboardChangeListener(dialog);
    }

    private KeyboardChangeListener(Object contextObj) {
        if (contextObj == null) {
            Log.d(TAG, "contextObj is null");
            return;
        }
        if (contextObj instanceof Activity) {
            mContentView = findContentView((Activity) contextObj);
            mWindow = ((Activity) contextObj).getWindow();
        } else if (contextObj instanceof Dialog) {
            mContentView = findContentView((Dialog) contextObj);
            mWindow = ((Dialog) contextObj).getWindow();
        }
        if (mContentView != null && mWindow != null) {
            MIN_KEYBOARD_HEIGHT = dp2px(mContentView.getContext(), 100);
            addContentTreeObserver();
            mContentView.postDelayed(refreshRunnable, 100);
        }

    }

    Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            onGlobalLayout();
            if (mContentView != null)
                mContentView.postDelayed(refreshRunnable, 100);
        }
    };


    private View findContentView(Activity contextObj) {
        return contextObj.getWindow().getDecorView();
//        return contextObj.findViewById(android.R.id.content);
    }

    private View findContentView(Dialog contextObj) {
        return contextObj.getWindow().getDecorView();
//        return contextObj.findViewById(android.R.id.content);
    }

    private void addContentTreeObserver() {
        mContentView.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }


    @Override
    public void onGlobalLayout() {
        if (mContentView == null || mWindow == null) {
            return;
        }
        int currentViewHeight = mContentView.getHeight();
        if (currentViewHeight == 0) {
            Log.d(TAG, "currHeight is 0");
            return;
        }
//        if (mScreenHeight == 0) {
//            mScreenHeight = getScreenHeight();
//        }
        int keyboardHeight;
        mWindow.getDecorView().getWindowVisibleDisplayFrame(mRect);
        if (mScreenHeight == 0) {
            mScreenHeight = mRect.bottom;
        }

        if (mKeyboardListener != null) {
            keyboardHeight = mScreenHeight - mRect.bottom;
            boolean currentShow = keyboardHeight > MIN_KEYBOARD_HEIGHT;
            Log.d(TAG, "onKeyboardChange() called  isShow: " + currentShow + "   keyboardHeight " + keyboardHeight);
            if (mShowFlag != currentShow) {
                mShowFlag = currentShow;
                mKeyboardListener.onKeyboardChange(currentShow, keyboardHeight);
            }
        }
    }

    private int getScreenHeight() {
        Display defaultDisplay = mWindow.getWindowManager().getDefaultDisplay();
        Point point = new Point();
        defaultDisplay.getRealSize(point);
        return point.y - getNavigationBarHeight(mWindow.getContext());
    }

    public int getNavigationBarHeight(Context context) {
        Resources res = context.getResources();
        int result = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            if (hasNavBar(context)) {
                String key;
                boolean mInPortrait = (res.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
                if (mInPortrait) {
                    key = "navigation_bar_height";
                } else {
                    key = "navigation_bar_height_landscape";
                }
                return getInternalDimensionSize(res, key);
            }
        }
        return result;
    }

    public boolean hasNavBar(Context context) {
        Resources res = context.getResources();
        int resourceId = res.getIdentifier("config_showNavigationBar", "bool", "android");
        if (resourceId != 0) {
            boolean hasNav = res.getBoolean(resourceId);
            String sNavBarOverride = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                try {
                    Class c = Class.forName("android.os.SystemProperties");
                    Method m = c.getDeclaredMethod("get", String.class);
                    m.setAccessible(true);
                    sNavBarOverride = (String) m.invoke(null, "qemu.hw.mainkeys");
                } catch (Throwable e) {
                    sNavBarOverride = null;
                }
            }
            // check override flag (see static block)
            if ("1".equals(sNavBarOverride)) {
                hasNav = false;
            } else if ("0".equals(sNavBarOverride)) {
                hasNav = true;
            }
            return hasNav;
        } else { // fallback
            return !ViewConfiguration.get(context).hasPermanentMenuKey();
        }
    }

    private int getInternalDimensionSize(Resources res, String key) {
        int result = 0;
        int resourceId = res.getIdentifier(key, "dimen", "android");
        if (resourceId > 0) {
            result = res.getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public interface KeyboardListener {
        /**
         * call back
         *
         * @param isShow         true is show else hidden
         * @param keyboardHeight keyboard height
         */
        void onKeyboardChange(boolean isShow, int keyboardHeight);
    }
}
