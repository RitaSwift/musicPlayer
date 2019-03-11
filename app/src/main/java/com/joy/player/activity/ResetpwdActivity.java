package com.joy.player.activity;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.joy.player.R;
import com.joy.player.proxy.utils.Constants;
import com.squareup.okhttp.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;

public class ResetpwdActivity extends Activity {
    private EditText mAccount;                        //用户名编辑
    private EditText mPwd_old;                        //密码编辑
    private EditText mPwd_new;                        //密码编辑
    private EditText mPwdCheck;                       //密码编辑
    private Button mSureButton;                       //确定按钮
    private Button mCancelButton;                     //取消按钮

    private static final int RESET_SUCCESS = 0;
    private static final int RESET_FAILED = 1;
    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
    private static final String BASE_URL = "http://47.100.245.211:8888";//请求接口根地址

    private Handler requestHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RESET_SUCCESS:
                    Toast.makeText(ResetpwdActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                    saveUserInfo();
                    toMainInter();
                    break;
                case RESET_FAILED:
                    Toast.makeText(ResetpwdActivity.this, "修改失败", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

    //到主界面
    private void toMainInter(){
        Intent intent = new Intent(ResetpwdActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void saveUserInfo(){
        SharedPreferences loginInfo = getSharedPreferences(Constants.SP_LOGININFO, Context.MODE_PRIVATE);
        loginInfo.edit().putBoolean(Constants.SP_ISLOGINED,true);
        loginInfo.edit().commit();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resetpwd);
        mAccount = (EditText) findViewById(R.id.resetpwd_edit_name);
        mPwd_old = (EditText) findViewById(R.id.resetpwd_edit_pwd_old);
        mPwd_new = (EditText) findViewById(R.id.resetpwd_edit_pwd_new);
        mPwdCheck = (EditText) findViewById(R.id.resetpwd_edit_pwd_check);

        mSureButton = (Button) findViewById(R.id.resetpwd_btn_sure);
        mCancelButton = (Button) findViewById(R.id.resetpwd_btn_cancel);

        mSureButton.setOnClickListener(m_resetpwd_Listener);      //注册界面两个按钮的监听事件
        mCancelButton.setOnClickListener(m_resetpwd_Listener);
    }

    View.OnClickListener m_resetpwd_Listener = new View.OnClickListener() {    //不同按钮按下的监听事件选择
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.resetpwd_btn_sure:                       //确认按钮的监听事件
                    resetpwd_check();
                    break;
                case R.id.resetpwd_btn_cancel:                     //取消按钮的监听事件,由注册界面返回登录界面
                    finish();
                    break;
            }
        }
    };

    /**
     * 修改密码网络请求
     */
    public void resetpwd_check() {                                //确认按钮的监听事件
        String userName = mAccount.getText().toString().trim();
        String userPwd_old = mPwd_old.getText().toString().trim();
        String userPwd_new = mPwd_new.getText().toString().trim();
        String userPwdCheck = mPwdCheck.getText().toString().trim();
        if (userPwd_new.equals(userPwdCheck)) {
            resetPwdRequest(userName,userPwd_old,userPwd_new);
        } else {
                Toast.makeText(this, "两次新密码输入不一致", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    private void resetPwdRequest(final String username, final String oldPassword,final String newPassword) {
        Runnable requestTask = new Runnable() {
            @Override
            public void run() {
                Message msg = requestHandler.obtainMessage();
                try {
                    OkHttpClient client = new OkHttpClient();
                    HashMap<String, String> paramsMap = new HashMap<>();
                    paramsMap.put("username", username);
                    paramsMap.put("oldpassword", oldPassword);
                    paramsMap.put("newpassword", newPassword);
                    StringBuilder tempParams = new StringBuilder();
                    int pos = 0;
                    for (String key : paramsMap.keySet()) {
                        if (pos > 0) {
                            tempParams.append("&");
                        }
                        tempParams.append(String.format("%s=%s", key, URLEncoder.encode(paramsMap.get(key), "utf-8")));
                        pos++;
                    }
                    String params = tempParams.toString();
                    RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, params);
                    String requestUrl = String.format("%s/%s", BASE_URL, "resetpassword");
                    final Request request = addHeaders().url(requestUrl).post(body).build();
                    Call call = client.newCall(request);
                    // 1
                    Response response = call.execute();

                    if (!response.isSuccessful()) {
                        msg.what = RESET_FAILED;
                    } else {
                        String string = response.body().string();
                        if ("0".equals(string)) {
                            msg.what = RESET_SUCCESS;
                        } else {
                            msg.what = RESET_FAILED;
                        }
                    }
                } catch (IOException ex) {
                    msg.what = RESET_FAILED;
                } finally {
                    msg.sendToTarget();
                }
            }
        };

        Thread requestThread = new Thread(requestTask);
        requestThread.start();
    }

        private Request.Builder addHeaders() {
            Request.Builder builder = new Request.Builder()
                    .addHeader("Connection", "keep-alive")
                    .addHeader("platform", "2")
                    .addHeader("phoneModel", Build.MODEL)
                    .addHeader("systemVersion", Build.VERSION.RELEASE)
                    .addHeader("appVersion", "1.0.0");
            return builder;
        }
    }
