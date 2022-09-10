package com.bdns.daqdemojava.manager;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;


public class NetWorkServiceNtrip {
    private String GGA = "";
    private String init_gga = "$GPGGA,121452.60,2309.5808817,N,11320.5346890,E,1,35,0.5,34.8435,M,0.000,M,,*52\r\n";
    private static String request2Server = "GET / HTTP/1.0\r\n" +
            "User-Agent: NTRIP GNSSInternetRadio/1.4.10\r\n" +
            "Accept: */*\r\n" +
            "Connection: close\r\n" +
            "\r\n";
    private String mIP;
    private String mPort;
    private String mUserID;
    private String mPwd;
    private String mMountedpoint;
    private Socket mSocket;
    private Handler mHandler;
    private DataOutputStream dos; // 与差分服务器的Socket的数据输出流
    private DataInputStream dis;  // 与差分服务器的Socket的数据输入流
    //获取挂载点线程
    private UpdateSourceTableThread mUpdateSourceTableThread;
    private ReportGGA2Service mReportGGA2Service;
    //获取差分数据线程
    private AcquireDataThread mAcquireDataThread;
    //获取挂载点
    private ArrayList<String> mountedPoints = null;
    private String feedBackState = null;
    //获取连接状态
    public String getFeedBackState() {
        return this.feedBackState;
    }
    public ArrayList<String> getMountedPoints() {
        return this.mountedPoints;
    }
    private Context mcontext;

    //构造函数
    public NetWorkServiceNtrip(Context context, String ipAdress, String port, String userID, String password, String mountedpoint) {
        this.mcontext = context;
        this.mIP = ipAdress;
        this.mPort = port;
        this.mUserID = userID;
        this.mPwd = password;
        this.mMountedpoint = mountedpoint;
    }

    //连接差分服务器并获取挂载点列表
    public synchronized void connect2Server(){
        if(this.mUpdateSourceTableThread != null){
            this.mUpdateSourceTableThread.release();
            this.mUpdateSourceTableThread = null;
        }
        this.mUpdateSourceTableThread = new UpdateSourceTableThread((UpdateSourceTableThread)null);
        this.mUpdateSourceTableThread.start();
    }
    //连接差分服务器获取差分数据
    public synchronized void getDifferentialData(){
        if (this.mAcquireDataThread != null) {
            this.mAcquireDataThread.cancle();
            this.mAcquireDataThread = null;
        }
        this.mAcquireDataThread = new AcquireDataThread((AcquireDataThread) null);
        this.mAcquireDataThread.start();
    }
    //连接差分服务器
    private void getCorsServiceSocket(String ip, String port){
        try {
            if(this.mSocket == null){
                InetAddress e = Inet4Address.getByName(ip);
                this.mSocket = new Socket(e, Integer.parseInt(port));
            }
            if(this.dos == null){
                this.dos = new DataOutputStream(this.mSocket.getOutputStream());
            }
            if(this.dis == null){
                this.dis = new DataInputStream(this.mSocket.getInputStream());
            }
            Log.d("getCorsServiceSocket", "Successful");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //获取差分数据线程
    private class AcquireDataThread extends Thread {
        private boolean _run;
        private byte[] buffer;

        public AcquireDataThread(AcquireDataThread acquireDataThread) {
            this._run = true;
            this.buffer = new byte[256];
        }
        @Override
        public void run() {
            if(NetWorkServiceNtrip.this.mSocket != null){
                NetWorkServiceNtrip.this.mSocket = null;
            }
            try {
                NetWorkServiceNtrip.this.getCorsServiceSocket(NetWorkServiceNtrip.this.mIP
                        , NetWorkServiceNtrip.this.mPort);
                if(NetWorkServiceNtrip.this.dos != null){
                    //这里将发送的请求参数封装成Ntrip协议格式
                    NetWorkServiceNtrip.this.dos.write(NtripUtils.CreateHttpRequsets(NetWorkServiceNtrip.this.mMountedpoint
                            , NetWorkServiceNtrip.this.mUserID, NetWorkServiceNtrip.this.mPwd).getBytes());
                }
                boolean e = true;
                while (this._run){
                    if(NetWorkServiceNtrip.this.dis != null){ //这里需要判断一下网络是否连接
                        int e1 = NetWorkServiceNtrip.this.dis.read(this.buffer, 0, this.buffer.length);
                        //自己的业务逻辑中将差分数据大小存入了SharePreference中
                        //这里有两行代码
                        if(e1 >= 1){
                            String e1x = new String(this.buffer);
                            if(e1x.startsWith("ICY 200 OK")){
                                Log.d("TAG:", "ICY successfully!");
                                //NetWorkServiceNtrip.access$402(NetWorkServiceNtrip.this, true);
                                if(NetWorkServiceNtrip.this.mReportGGA2Service == null) {
                                    NetWorkServiceNtrip.this.mReportGGA2Service = NetWorkServiceNtrip.this.new ReportGGA2Service(NetWorkServiceNtrip.this.dos,(ReportGGA2Service)null);
                                    NetWorkServiceNtrip.this.mReportGGA2Service.start();
                                    Log.d("TAG:", "GGA successfully!");
                                }
                            } else if(e1x.contains("401 Unauthorized")){
                                NetWorkServiceNtrip.this.feedBackState = "401 UNAUTHORIZED";
                            } else {
                                NetWorkServiceNtrip.this.feedBackState = "SUCCESSFUL";
                                if (null != onDataReceiveListener) {
                                    onDataReceiveListener.OnDataReceive(this.buffer, this.buffer.length);
                                }
                            }
                        }
                    }
                }
            } catch (UnknownHostException var5) {
                var5.printStackTrace();
            } catch (IOException var6) {
                var6.printStackTrace();
                try {
                    NetWorkServiceNtrip.this.dos.close();
                    NetWorkServiceNtrip.this.dis.close();
                } catch (IOException var4) {
                    var4.printStackTrace();
                }
            }
        }
        public void cancle() {
            try {
                this._run = false;
                NetWorkServiceNtrip.this.mSocket.close();
            } catch (IOException var2) {
                var2.printStackTrace();
            }
        }
    }


    private class ReportGGA2Service extends Thread {
        private DataOutputStream dos;
        private boolean _run;
        public ReportGGA2Service(DataOutputStream dos, ReportGGA2Service reportGGA2Service) {
            this.dos = dos;
            this._run = false;
            this.dos = dos;
        }
        @Override
        public void run() {
            while(!this._run){
                try {
                    this.dos.write((GGA + "\r\n").getBytes());   //在哪里获取GGA
                    Thread.sleep(1000);
                } catch (Exception e) {
                    this.Cancle();
                }
            }
        }
        public void Cancle() {
            try {
                this._run = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private class UpdateSourceTableThread extends Thread {
        public UpdateSourceTableThread(UpdateSourceTableThread updateSourceTableThread) {
        }
        @Override
        public void run() {
            try{
                NetWorkServiceNtrip.this.getCorsServiceSocket(NetWorkServiceNtrip.this.mIP
                        , NetWorkServiceNtrip.this.mPort);
                if(NetWorkServiceNtrip.this.dos == null){
                    NetWorkServiceNtrip.this.mSocket.setSoTimeout(5000);
                    NetWorkServiceNtrip.this.dos = (DataOutputStream) NetWorkServiceNtrip.this.mSocket.getOutputStream();
                }
                NetWorkServiceNtrip.this.dos.write(request2Server.getBytes()); //获取源列表
                byte[] e = new byte[1024];
                StringBuilder sb = new StringBuilder();
                boolean len = true;
                String sourceString;
                int var14;
                while ((var14 = NetWorkServiceNtrip.this.dis.read(e, 0, e.length)) != -1){
                    sourceString = new String(e, 0, var14);
                    sb.append(sourceString);
                }
                sourceString = sb.toString();
                if(sourceString.startsWith("SOURCETABLE 200 OK")) {
                    ArrayList mountPoints = new ArrayList();
                    String[] linStrings = sourceString.split("\r\n");
                    String[] var10 = linStrings;
                    int var9 = linStrings.length;
                    for(int var8 = 0; var8 < var9; ++var8){
                        String line = var10[var8];
                        if(line.startsWith("STR")){
                            String[] dataStrings = line.trim().split(";");
                            mountPoints.add(dataStrings[1]);
                        }
                    }
                    NetWorkServiceNtrip.this.mountedPoints = mountPoints;
                }
                this.release();
                NetWorkServiceNtrip.this.mUpdateSourceTableThread = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        private void release() {
            try {
                if(NetWorkServiceNtrip.this.dos != null) {
                    NetWorkServiceNtrip.this.dos.close();
                }
                if(NetWorkServiceNtrip.this.dis != null){
                    NetWorkServiceNtrip.this.dis.close();
                }
                if(NetWorkServiceNtrip.this.mSocket != null){
                    NetWorkServiceNtrip.this.mSocket.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setGGA(String GGA) {
        this.GGA = GGA;
    }

    /*
    * socket response data listener
    */
    private OnDataReceiveListener onDataReceiveListener = null;
    public interface OnDataReceiveListener {
        void OnDataReceive(byte[] buffer, int size);
    }

    public void setOnDataReceiveListener(OnDataReceiveListener dataReceiveListener) {
        this.onDataReceiveListener = dataReceiveListener;
    }


}
