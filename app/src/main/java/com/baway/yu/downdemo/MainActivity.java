package com.baway.yu.downdemo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.daimajia.numberprogressbar.NumberProgressBar;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
   // private  String urlpath="http://sw.bos.baidu.com/sw-search-sp/software/9a2808964b476/QQ_8.9.3.21169_setup.exe";
    String urlpath = "http://gdown.baidu.com/data/wisegame/74ac7c397e120549/QQ_708.apk";
    private NumberProgressBar progressBar;
    private File file;
    private  int progress;
    private  boolean isDwon;//当前是否 在下载
    private  long size=0;//当前下载长度
    private  long length=0;//下载文件总长度
    private Handler hand=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            progressBar.setProgress(msg.what);
           Log.e("down", "run: "+msg.what +"size = "+size+"length = "+length);
            if(msg.what==100){
                isDwon=false;
                size=0;
                length=0;
                // 核心是下面几句代码
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(file),
                        "application/vnd.android.package-archive");
                startActivity(intent);
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
    }

    private void initData() {
        progressBar = (NumberProgressBar)findViewById(R.id.numberprogress);
        file = new File(getExternalCacheDir(),"QQ.apk");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start:
                startDown();
                break;
            case R.id.pause:
                pauseDown();
                break;
        }
    }
    //暂停下载
    private void pauseDown() {
        isDwon=false;
    }
    //开始下载
    private void startDown() {
        if (isDwon)
            return;
       Thread thread =  new Thread(){
            @Override
            public void run() {
                try {
                    RandomAccessFile raf=new RandomAccessFile(file,"rw");
                    raf.seek(size);//直接将文件指针移到size
                    URL url=new URL(urlpath);
                    HttpURLConnection connect=(HttpURLConnection)url.openConnection();
                    int responCode=200;
                    if (length!=0){
                        //设置断点续传的开始位置
                        connect.setRequestProperty("Range", "bytes=" + size + "-"+length);
                        responCode=206;
                    }
                    if (connect.getResponseCode()==responCode){
                        if (length==0){
                            length=connect.getContentLength();

                        }
                        InputStream in=connect.getInputStream();
                        byte[]buff=new byte[1024];
                        int len=-1;
                        while (isDwon&&(len=in.read(buff))!=-1){
                            raf.write(buff,0,len);
                            size+=len;
                            int pro= (int) (size*100/length);
                            if (pro!=progress){
                                progress = pro;
                                hand.sendEmptyMessage(progress);
                            }
                        }
                    }
                    raf.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        isDwon=true;
        thread.start();
    }
}
