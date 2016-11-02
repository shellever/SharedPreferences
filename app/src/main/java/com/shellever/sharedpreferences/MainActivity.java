package com.shellever.sharedpreferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String PREFS_ACCOUNT = "account";
    private static final String KEY_ACCOUNT_USERNAME = "username";
    private static final String KEY_ACCOUNT_PASSWORD = "password";
    private static final String KEY_ACCOUNT_GENDER = "gender";
    private static final String KEY_ACCOUNT_AGE = "age";

    private SharedPreferences sp;

    private EditText mUsernameEt;
    private EditText mPasswordEt;
    private EditText mAgeEt;
    private RadioGroup mGenderRg;
    private RadioButton mMaleGenderRb;
    private RadioButton mFemaleGenderRb;
    private Button mResetBtn;
    private Button mSaveBtn;
    private Button mRestoreBtn;

    private String username;
    private String password;
    private boolean gender;     // Female: false(default)
    private int age;

    private TextView mXMLTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        mXMLTv = (TextView) findViewById(R.id.tv_xml);

        mUsernameEt = (EditText) findViewById(R.id.et_account_username);
        mPasswordEt = (EditText) findViewById(R.id.et_account_password);
        mAgeEt = (EditText) findViewById(R.id.et_account_age);
        mGenderRg = (RadioGroup) findViewById(R.id.rg_account_gender);
        mMaleGenderRb = (RadioButton) findViewById(R.id.rb_account_gender_male);
        mFemaleGenderRb = (RadioButton) findViewById(R.id.rb_account_gender_female);
        mResetBtn = (Button) findViewById(R.id.btn_reset);
        mSaveBtn = (Button) findViewById(R.id.btn_save);
        mRestoreBtn = (Button) findViewById(R.id.btn_restore);

        mGenderRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_account_gender_male:
                        gender = true;
                        break;
                    case R.id.rb_account_gender_female:
                        gender = false;
                        break;
                }
            }
        });

        mResetBtn.setOnClickListener(this);
        mSaveBtn.setOnClickListener(this);
        mRestoreBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_reset:
                reset();            // 复位各个控件
                Toast.makeText(this, "Reset Account Okay", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_save:
                // 获取EditText控件上的文本并转成字符串，同时去掉首尾两端的空白符
                username = mUsernameEt.getText().toString().trim();
                if (TextUtils.isEmpty(username)) {      // return if null or ""
                    Toast.makeText(this, "Username is empty.", Toast.LENGTH_SHORT).show();
                    return;
                }

                password = mPasswordEt.getText().toString().trim();
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(this, "Password is empty.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String ageString = mAgeEt.getText().toString().trim();
                if (TextUtils.isEmpty(ageString)) {
                    Toast.makeText(this, "Age is empty.", Toast.LENGTH_SHORT).show();
                    return;
                }
                age = Integer.valueOf(ageString);

                save();             // 读取并判断各个控件的数据，并保存到SharedPreferences中
                Toast.makeText(this, "Save Account Okay", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_restore:
                restore();          // 读取保存在SharedPreferences中的数据，并依次显示在控件上
                Toast.makeText(this, "Restore Account Okay", Toast.LENGTH_SHORT).show();

                // 读取SharedPreferences保存数据而生成的xml文件内容并显示在TextView上
                testSharedPreferencesXML();
                break;
        }
    }

    private void restore() {
        sp = getSharedPreferences(PREFS_ACCOUNT, MODE_PRIVATE);
        username = sp.getString(KEY_ACCOUNT_USERNAME, "");  // 读取字符串数据，默认为""
        password = sp.getString(KEY_ACCOUNT_PASSWORD, "");
        gender = sp.getBoolean(KEY_ACCOUNT_GENDER, false);  // 读取布尔数据，默认为false
        age = sp.getInt(KEY_ACCOUNT_AGE, -1);               // 读取整型数据，默认为-1

        mUsernameEt.setText(username);
        mPasswordEt.setText(password);
        mMaleGenderRb.setChecked(gender);
        mFemaleGenderRb.setChecked(!gender);
        mAgeEt.setText(age < 0 ? "" : String.valueOf(age)); // 若小于0，则复位为""
    }

    private boolean save() {
        sp = getSharedPreferences(PREFS_ACCOUNT, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();        // 获取Editor对象
        editor.putString(KEY_ACCOUNT_USERNAME, username);   // 保存字符串数据
        editor.putString(KEY_ACCOUNT_PASSWORD, password);
        editor.putBoolean(KEY_ACCOUNT_GENDER, gender);      // 保存布尔数据
        editor.putInt(KEY_ACCOUNT_AGE, age);                // 保存整型数据
        editor.apply();             // 异步提交方法
        return true;
    }

    private void reset() {
        mUsernameEt.setText("");
        mPasswordEt.setText("");
        mMaleGenderRb.setChecked(false);
        mFemaleGenderRb.setChecked(true);
        mAgeEt.setText("");
        mXMLTv.setText("");
    }

    // getPackageName() - com.shellever.sharedpreferences
    // getFilesDir() - /data/data/com.shellever.sharedpreferences/files
    // FilesDir's Parent: /data/data/com.shellever.sharedpreferences
    // File: /data/data/com.shellever.sharedpreferences/shared_prefs/account.xml
    private void testSharedPreferencesXML() {
        // String file = getSharedPrefsFileString(this, PREFS_ACCOUNT);
        File file = getSharedPrefsFile(this, PREFS_ACCOUNT);
        String text = file.getPath();
        text += "\n\n";

        FileInputStream fis = null;
        ByteArrayOutputStream out = null;
        try {
            fis = new FileInputStream(file);        // 构建文件输入流
            out = new ByteArrayOutputStream();      // 构建字节数组输出流
            int len;                                // 记录每次读取数据的大小，-1表示结束
            byte[] buffer = new byte[10 * 1024];    // 定义10kB大小的缓冲区
            while ((len = fis.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            text += out.toString();
            mXMLTv.setText(text);                   // 将xml内容设置到TextView
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // /Sdk/sources/android-21/android/app/ContextImpl.java
    // 895 public File getSharedPrefsFile(String name);
    //     |--- makeFilename(getPreferencesDir(), name + ".xml");
    // 942 private File getPreferencesDir();
    //     |--- mPreferencesDir = new File(getDataDirFile(), "shared_prefs");
    //1077 public File getFilesDir();
    //     |--- mFilesDir = new File(getDataDirFile(), "files");
    //2177 private File getDataDirFile();
    //     |--- mPackageInfo.getDataDirFile();
    //2185 public File getDir(String name, int mode);
    //     |--- makeFilename(getDataDirFile(), name);
    //2362 private File makeFilename(File base, String name);
    //     |--- new File(base, name);
    private String getSharedPrefsFileString(Context context, String name) {
        String path = context.getFilesDir().getParent();// 获取文件目录的父目录路径
        return path + "/shared_prefs/" + name + ".xml"; // SharedPreferences生成的xml数据文件
    }

    private File getSharedPrefsFile(Context context, String name) {
        String dirPath = context.getDir("shared_prefs", Context.MODE_PRIVATE).getPath();
        // getDir()中会在目录名称前自动加上app_变成app_shared_prefs
        dirPath = dirPath.replace("app_", "");
        return new File(dirPath, name + ".xml");
    }

}
