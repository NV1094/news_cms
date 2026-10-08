package cn.lvdou.vod.ui.lvdou;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ToastUtils;

import org.jetbrains.annotations.NotNull;

import cn.lvdou.vod.ApiConfig;
import cn.lvdou.vod.MainActivity;
import cn.lvdou.vod.R;
import cn.lvdou.vod.ui.feedback.FeedbackActivity;
import cn.lvdou.vod.ui.login.LoginActivity;
import cn.lvdou.vod.ui.widget.HitDialog;
import cn.lvdou.vod.utils.MMkvUtils;
import kotlin.jvm.internal.Intrinsics;

public class TanChu extends AppCompatActivity {

    private AlertDialog alertDialog1; //信息框
    private AlertDialog alertDialog2; //单选框
    private AlertDialog alertDialog3; //多选框

    private Button button1;
    private Button button2;
    private Button button3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.itme_menu);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setStatusBarColor(getColor(R.color.transparent));
        }
        init();
    }

    public void init(){
        button1 = (Button) findViewById(R.id.button1);
        //button2 = (Button) findViewById(R.id.button2);
        //button3 = (Button) findViewById(R.id.button3);
    }

    //@Override
    //public boolean onCreateOptionsMenu(Menu menu) {
    //    getMenuInflater().inflate(R.menu.mian, menu);
    //    return true;
    //}

    public void showList(View view){
        final String[] items = {"IQIYIUI", "YOUKUUI", "KATONGUI", "BLBLUI", "BLBLUI2", "AQCUI"};
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("常规主题选择");
        alertBuilder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 1){
                    MMkvUtils.Companion.Builds().saveThemeStyle("1");
                }else if (i == 2){
                    MMkvUtils.Companion.Builds().saveThemeStyle("2");
                }else if (i == 3){
                    MMkvUtils.Companion.Builds().saveThemeStyle("3");
                }else if (i == 4){
                    MMkvUtils.Companion.Builds().saveThemeStyle("4");
                }else if (i == 5){
                    MMkvUtils.Companion.Builds().saveThemeStyle("5");
                }else{
                    MMkvUtils.Companion.Builds().saveThemeStyle("0");
                }

                alertDialog1.dismiss();
                (new HitDialog(TanChu.this)).setTitle("提示").setMessage("主题切换成功，在APP重启后生效").setOnHitDialogClickListener(new HitDialog.OnHitDialogClickListener() {
                    public void onCancelClick(@NotNull HitDialog dialog) {
                        Intrinsics.checkParameterIsNotNull(dialog, "dialog");
                        super.onCancelClick(dialog);
                    }
                    public void onOkClick(@NotNull HitDialog dialog) {
                        Intrinsics.checkParameterIsNotNull(dialog, "dialog");
                        super.onOkClick(dialog);
                        ToastUtils.showShort("请手动重启APP");
//自动重启，有BUG
//                        final Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
//                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                        startActivity(intent);
//                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                }).show();
            }
        });
        alertDialog1 = alertBuilder.create();
        alertDialog1.show();
    }

    public void showSingleAlertDialog(View view){
        final String[] items = {"暂无"};
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("经典主题");
        alertBuilder.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(TanChu.this, items[i], Toast.LENGTH_SHORT).show();
            }
        });

        alertBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertDialog2.dismiss();
            }
        });

        alertBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertDialog2.dismiss();
            }
        });

        alertDialog2 = alertBuilder.create();
        alertDialog2.show();
    }

    public void showMutilAlertDialog(View view){
        final String[] items = {"暂无"};
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("卡通主题");
        /**
         *第一个参数:弹出框的消息集合，一般为字符串集合
         * 第二个参数：默认被选中的，布尔类数组
         * 第三个参数：勾选事件监听
         */
        alertBuilder.setMultiChoiceItems(items, null, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i, boolean isChecked) {
                if (isChecked){
                    Toast.makeText(TanChu.this, "选择" + items[i], Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(TanChu.this, "取消选择" + items[i], Toast.LENGTH_SHORT).show();
                }
            }
        });
        alertBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertDialog3.dismiss();
            }
        });

        alertBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertDialog3.dismiss();
            }
        });


        alertDialog3 = alertBuilder.create();
        alertDialog3.show();
    }
}
