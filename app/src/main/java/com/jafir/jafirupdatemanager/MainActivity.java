package com.jafir.jafirupdatemanager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.jafir.updatemanager.UpdateManager;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
        findViewById(R.id.down).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateManager manager = new UpdateManager(MainActivity.this);
                manager.setURL("http://jafir-my-love.oss-cn-shanghai.aliyuncs.com/test_update_info.xml");
                //you can choose native style
//                manager.setProgressStyle(UpdateManager.ProgressStyle.Native);
                //set xml URL on the server
                //set download path, { Environment.getExternalStorageDirectory()+"/download } is default
//                manager.setDownLoadPath(Environment.getExternalStorageDirectory()+"/downloadApk");
                //you can set cool progressbar colors
//                manager.setCoolProgressStyle(
//                        getResources().getColor(R.color.yellow),
//                        getResources().getColor(R.color.colorAccent),
//                        getResources().getColor(R.color.green),
//                        getResources().getColor(R.color.orange)
//                );
                manager.start();
            }
        });
    }
}
