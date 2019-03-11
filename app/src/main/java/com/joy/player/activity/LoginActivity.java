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
import android.widget.TextView;
import android.widget.Toast;
import com.joy.player.R;
import com.joy.player.handler.HandlerUtil;
import com.joy.player.proxy.utils.Constants;
import com.joy.player.widget.SplashScreen;
import com.squareup.okhttp.*;
import net.youmi.android.normal.spot.SpotManager;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;

public class LoginActivity extends Activity {
    private static final String CURRENT_THEME = "LoginActivity";
    private SplashScreen splashScreen;
    private Button registerBtn;
    private Button loginBtn;
    private EditText usernameEt;
    private EditText passwordEt;
    private TextView changePwd;
    private static final int REGISTER_FAIL = 0;
    private static final int REGISTER_SUCCESS = 1;
    private static final int LOGIN_FAIL = 2;
    private static final int LOGIN_SUCCESS = 3;
    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");//mdiatype 这个需要和服务端保持一致
    private static final MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("text/plain; charset=UTF-8");//mdiatype 这个需要和服务端保持一致
    private static final String BASE_URL = "http://47.100.245.211:8888";//请求接口根地址

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
        changePwd = findViewById(R.id.login_text_change_pwd);
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
        finish();
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
        changePwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this,ResetpwdActivity.class);
                startActivity(intent);
            }
        });
    }

    private void performLoginRequest(final String username, final String password) {
        Runnable requestTask = new Runnable() {
            @Override
            public void run() {
                Message msg = requestHandler.obtainMessage();
                try {
//                    OkHttpClient client = new OkHttpClient();
////                    String url = "139.199.126.198:8888/login?username="+username+"&password="+password;
//                    String url = "http://www.baidu.com/";
//                    Request request = new Request.Builder()
//                            .url(url)
//                            .build();
//                    Call call = client.newCall(request);
//                    // 1
//                    Response response = call.execute();
//
//                    if (!response.isSuccessful()) {
//                        msg.what = LOGIN_FAIL;
//                    } else {
//                        msg.what = LOGIN_SUCCESS;
//                    }
                    OkHttpClient client = new OkHttpClient();
                    HashMap<String, String> paramsMap = new HashMap<>();
                    paramsMap.put("username",username);
                    paramsMap.put("password",password);
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
                    String requestUrl = String.format("%s/%s", BASE_URL, "login");
                    final Request request = addHeaders().url(requestUrl).post(body).build();
                    Call call = client.newCall(request);
                    // 1
                    Response response = call.execute();

                    if (!response.isSuccessful()) {
                        msg.what = LOGIN_FAIL;
                    } else {
                        String string = response.body().string();
                        if("0".equals(string)){
                            msg.what = LOGIN_SUCCESS;
                        }
                        else
                        {
                            msg.what = LOGIN_FAIL;
                        }
                    }
                } catch (Exception ex) {
                    msg.what = LOGIN_FAIL;
                }
                finally {
                    msg.sendToTarget();
                }
            }
        };

        Thread requestThread = new Thread(requestTask);
        requestThread.start();
    }

    private void performRegisterRequest(final String username, final String password) {
        Runnable requestTask = new Runnable() {
            @Override
            public void run() {
                Message msg = requestHandler.obtainMessage();
                try {
                    OkHttpClient client = new OkHttpClient();
                    HashMap<String, String> paramsMap = new HashMap<>();
                    paramsMap.put("username",username);
                    paramsMap.put("password",password);
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
                    String requestUrl = String.format("%s/%s", BASE_URL, "register");
                    final Request request = addHeaders().url(requestUrl).post(body).build();
                    Call call = client.newCall(request);
                    // 1
                    Response response = call.execute();

                    if (!response.isSuccessful()) {
                        msg.what = REGISTER_FAIL;
                    } else {
                        String string = response.body().string();
                        if("0".equals(string)){
                            msg.what = REGISTER_SUCCESS;
                        }
                        else
                        {
                            msg.what = REGISTER_FAIL;
                        }
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

    private Request.Builder addHeaders() {
        Request.Builder builder = new Request.Builder()
                .addHeader("Connection", "keep-alive")
                .addHeader("platform", "2")
                .addHeader("phoneModel", Build.MODEL)
                .addHeader("systemVersion", Build.VERSION.RELEASE)
                .addHeader("appVersion", "1.0.0");
        return builder;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
