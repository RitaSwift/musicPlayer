package com.joy.player.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.joy.player.R;
import com.joy.player.handler.HandlerUtil;
import com.joy.player.proxy.utils.Constants;
import com.joy.player.widget.SplashScreen;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class LoginActivity extends Activity {
    private static final String CURRENT_THEME = "LoginActivity";
    private SplashScreen splashScreen;
    private Button registerBtn;
    private Button loginBtn;
    private EditText usernameEt;
    private EditText passwordEt;
    private static final int REGISTER_FAIL = 0;
    private static final int REGISTER_SUCCESS = 1;
    private static final int LOGIN_FAIL = 2;
    private static final int LOGIN_SUCCESS = 3;

    private Handler requestHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REGISTER_SUCCESS:
                    Toast.makeText(LoginActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                    saveUserInfo();
                    toMainInter();
                    break;
                case LOGIN_SUCCESS:
                    Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                    saveUserInfo();
                    toMainInter();
                    break;
                case REGISTER_FAIL:
                    Toast.makeText(LoginActivity.this, "注册失败", Toast.LENGTH_SHORT).show();
                    break;
                case LOGIN_FAIL:
                    Toast.makeText(LoginActivity.this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        splashScreen = new SplashScreen(this);
        splashScreen.show(R.drawable.art_login_bg,
                SplashScreen.SLIDE_LEFT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        registerBtn = findViewById(R.id.login_btn_register);
        loginBtn = findViewById(R.id.login_btn_login);
        usernameEt = findViewById(R.id.login_edit_account);
        passwordEt = findViewById(R.id.login_edit_pwd);
        setListeners();
        HandlerUtil.getInstance(this).postDelayed(new Runnable() {
            @Override
            public void run() {
                splashScreen.removeSplashScreen();
            }
        }, 3000);
    }

    //到主界面
    private void toMainInter(){
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
    }

    private void saveUserInfo(){
        SharedPreferences loginInfo = getSharedPreferences(Constants.SP_LOGININFO, Context.MODE_PRIVATE);
        loginInfo.edit().putBoolean(Constants.SP_ISLOGINED,true);
        loginInfo.edit().putString(Constants.SP_USERNAME,usernameEt.getText().toString());
        loginInfo.edit().putString(Constants.SP_PASSWORD,passwordEt.getText().toString());
        loginInfo.edit().commit();
    }

    private void setListeners() {
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performRegisterRequest(usernameEt.getText().toString(),passwordEt.getText().toString());
            }
        });
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performLoginRequest(usernameEt.getText().toString(),passwordEt.getText().toString());
            }
        });
    }

    private void performLoginRequest(String username, String password) {
        Runnable requestTask = new Runnable() {
            @Override
            public void run() {
                Message msg = requestHandler.obtainMessage();
                try {
                    OkHttpClient client = new OkHttpClient();

                    Request request = new Request.Builder()
                            .url("http://www.baidu.com")
                            .build();
                    Call call = client.newCall(request);
                    // 1
                    Response response = call.execute();

                    if (!response.isSuccessful()) {
                        msg.what = LOGIN_FAIL;
                    } else {
                        msg.what = LOGIN_SUCCESS;
                    }
                } catch (IOException ex) {
                    msg.what = LOGIN_FAIL;
                } finally {
                    msg.sendToTarget();
                }
            }
        };

        Thread requestThread = new Thread(requestTask);
        requestThread.start();
    }

    private void performRegisterRequest(String username, String password) {
        Runnable requestTask = new Runnable() {
            @Override
            public void run() {
                Message msg = requestHandler.obtainMessage();
                try {
                    OkHttpClient client = new OkHttpClient();

                    Request request = new Request.Builder()
                            .url("http://www.baidu.com")
                            .build();
                    Call call = client.newCall(request);
                    // 1
                    Response response = call.execute();

                    if (!response.isSuccessful()) {
                        msg.what = REGISTER_FAIL;
                    } else {
                        msg.what = REGISTER_SUCCESS;
                    }
                } catch (IOException ex) {
                    msg.what = REGISTER_FAIL;
                } finally {
                    msg.sendToTarget();
                }
            }
        };

        Thread requestThread = new Thread(requestTask);
        requestThread.start();
    }
}
