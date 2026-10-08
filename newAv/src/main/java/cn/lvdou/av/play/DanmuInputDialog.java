package cn.lvdou.av.play;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDialog;

import cn.lvdou.av.R;
import cn.lvdou.av.widget.A;
import cn.lvdou.av.widget.KeyboardChangeListener;

/**
 * 评论输入弹窗
 */
public class DanmuInputDialog extends AppCompatDialog {

    private EditText mInputView;
    private View btnSend;
    private KeyboardChangeListener mKeyboardChangeListener;
    private String mTextValue;
    private OnSendClickListener onSendClickListener;
    LinearLayout textColorRadioGroup;

    public DanmuInputDialog(Context context) {
        super(context, R.style.bottomDialog);
    }

    public DanmuInputDialog(Context context, int theme) {
        super(context, theme);
    }

    protected DanmuInputDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public void setOnSendClickListener(OnSendClickListener listener) {
        onSendClickListener = listener;
    }

    public int getScreenWidth(Context context) {
        WindowManager manager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        return display.getWidth();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pop_layout_danmaku);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = getScreenWidth(getContext());
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.dimAmount = 0.5f;
        params.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        getWindow().setAttributes(params);
        getWindow().setGravity(Gravity.BOTTOM);
        getWindow().setSoftInputMode(params.SOFT_INPUT_STATE_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        textColorRadioGroup = findViewById(R.id.textcolorgroup);
        for (int i = 0; i < textColorRadioGroup.getChildCount(); i++) {
            CheckBox checkBox = (CheckBox) textColorRadioGroup.getChildAt(i);
            checkBox.setOnCheckedChangeListener((compoundButton, b) -> {
                if(b){
                    resetTextColor(compoundButton);
                }
            });
        }
        btnSend = findViewById(R.id.btn_pop_danmaku);
        btnSend.setEnabled(false);
        mInputView = findViewById(R.id.et_pop_danmaku);
        mInputView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    dismiss();
                }
            }
        });
        mInputView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                btnSend.setEnabled(s != null && s.length() > 0);
            }
        });
        if (mTextValue != null) {
            mInputView.setText(mTextValue);
        }
        mInputView.requestFocus();
        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (mKeyboardChangeListener != null) {
                    mKeyboardChangeListener.destroy();
                }
                if (mInputView.getWindowToken() != null) {
                    InputMethodManager imm = (InputMethodManager)
                            mInputView.getContext().getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mInputView.getWindowToken(), 0);
                }
            }
        });
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendComment();//发送
            }
        });
    }

    public void show(String defaultValue) {
        this.mTextValue = defaultValue;
        if (mInputView != null) {
            mInputView.setText(mTextValue);
        }
        show();
    }

    void resetTextColor(View checkedView){
        if(textColorRadioGroup != null){
            for (int i = 0; i < textColorRadioGroup.getChildCount(); i++) {
                CheckBox checkBox = (CheckBox) textColorRadioGroup.getChildAt(i);
                if (checkBox != checkedView) {
                    checkBox.setChecked(false);
                }
            }
        }
    }

    @Override
    public void show() {
        if (mKeyboardChangeListener != null) {
            mKeyboardChangeListener.destroy();
        }
        mKeyboardChangeListener = KeyboardChangeListener.create(this);
        mKeyboardChangeListener.setKeyboardListener(new KeyboardChangeListener.KeyboardListener() {
            @Override
            public void onKeyboardChange(boolean isShow, int keyboardHeight) {
                if (!isShow) {
                    dismiss();
                }
            }
        });
        resetTextColor(null);
        super.show();
    }

    private String getTextColor() {
        for (int i = 0; i < textColorRadioGroup.getChildCount(); i++) {
            CheckBox checkBox = (CheckBox) textColorRadioGroup.getChildAt(i);
            if (checkBox.isChecked()) {
                return checkBox.getTag() + "";
            }
        }
        return "#FFFFFF";
    }

    /**
     * 发送评论
     */
    private void sendComment() {
        if (onSendClickListener != null && !TextUtils.isEmpty(mInputView.getText().toString())) {
            onSendClickListener.onSend(mInputView.getText().toString(), getTextColor());
            mInputView.setText(null);
            dismiss();
        }
    }


    interface OnSendClickListener {
        void onSend(String value, String textcolor);
    }

}
