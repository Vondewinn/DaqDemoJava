package com.bdns.daqdemojava;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.canapi.Command;
import com.android.canapi.DataType;
import com.android.canapi.FrameFormat;
import com.lc31.uartsdk.ProcessData;
import com.lc31.uartsdk.SerialHelper;
import com.scau.cansdk.CanBusHelper;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static com.bdns.daqdemojava.DataUtils.int2byte;


public class MainActivity extends AppCompatActivity {
    private CanBusHelper canBusHelper;
    private SerialHelper ttyS0;
    private String strData;

    private ListView listData;
    private MyAdapter myAdapter = null;
    private List<Data> mData = null;
    private Context mContext = null;
    private int flag;
    private int revDataNum = 0;
    private boolean _isStd = true;

    private TextView tvRevDataNum;
    private EditText etID, etData;
    private Button btnSend, btn250K, btn500K, btnOpenCan, btnCloseCan, btnClean, btnStd, btnExt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);    // 默认在初始化时不弹出软键盘
        mContext = MainActivity.this;
        bindViews();
        mData = new LinkedList<Data>();
        myAdapter = new MyAdapter(mContext, (LinkedList<Data>)mData);
        listData.setAdapter(myAdapter);

        canBusHelper = CanBusHelper.getInstance(this);
        canBusHelper.getData(new com.scau.cansdk.ProcessData() {
            @Override
            public void process(byte[] bytes, DataType dataType) {
                // 处理接收数据
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        myAdapter.add(new Data(handleCanData(bytes)));
                        revDataNum ++;
                        tvRevDataNum.setText(revDataNum + "");
                        flag ++;
                        listData.smoothScrollToPosition(myAdapter.getCount());
                        if (flag == 1000) { // 当显示数量大于1000个时自动清空界面
                            flag = 0;
                            myAdapter.clear();
                        }
                    }
                });
            }
        });

        setInnerOnClickListener();

        ttyS0 = new SerialHelper("ttyS0", 115200);
        ttyS0.open();
        ttyS0.uartRevData(new ProcessData() {
            @Override
            public void process(byte[] revData, int len) {
                String dataOut = "";
                String aa = new String(revData, 0, len);
                strData = strData + aa;
                int strGGA = strData.indexOf("$GPGGA,");
                String subData = strData.substring(strGGA+"$GPGGA,".length());
                if (subData.contains("$GPGGA,")){
                    dataOut = strData.substring(0, strGGA+subData.indexOf("$GPGGA,") + "$GPGGA,".length());
                    strData = subData.substring(subData.indexOf("$GPGGA,"));
                }
                if (!dataOut.equals("")) {
                    Log.i("TAG", "process: " + dataOut);
                }
            }
        });

    }

    private void bindViews() {
        listData = findViewById(R.id.list_data);
        btn250K = findViewById(R.id.set250kbtn);
        btn500K = findViewById(R.id.set500kbtn);
        btnOpenCan = findViewById(R.id.open_can_btn);
        btnCloseCan = findViewById(R.id.close_can_btn);
        btnClean = findViewById(R.id.clean_btn);
        btnStd = findViewById(R.id.std_btn);
        btnExt = findViewById(R.id.ext_btn);
        etID = findViewById(R.id.can_id_et);
        etData = findViewById(R.id.can_data_et);
        btnSend = findViewById(R.id.send_btn);
        tvRevDataNum = findViewById(R.id.rev_num_tv);
    }

    /*
    * @fun 设置内部点击事件
    * */

    private void setInnerOnClickListener() {
        btn250K.setOnClickListener(v -> CanBusHelper.sendCommand(Command.Send.Switch250K()));
        btn500K.setOnClickListener(v -> CanBusHelper.sendCommand(Command.Send.Switch500K()));
        btnOpenCan.setOnClickListener(v -> canBusHelper.open());
        btnCloseCan.setOnClickListener(v -> canBusHelper.close());
        btnClean.setOnClickListener((v) -> {
            revDataNum = 0;
            tvRevDataNum.setText(revDataNum + "");
            myAdapter.clear();
        });
        btnStd.setOnClickListener(v -> _isStd = true);
        btnExt.setOnClickListener(v -> _isStd = false);
        btnSend.setOnClickListener((v) -> {
            if (canBusHelper.isOpen()) {
                if (_isStd) {
                    sendCanData(FrameFormat.stdFormat);
                } else {
                    sendCanData(FrameFormat.extFormat);
                }
            } else {
                Toast.makeText(mContext, "CAN未打开", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /*
     * @fun 发送CAN数据
     * */
    private void sendCanData(FrameFormat frameFormat) {
        String strID = etID.getText().toString();
        String strData = etData.getText().toString();
        Log.d("TAG", "sendCanData: " + Integer.parseInt(strID.substring(2), 16));
        if (strID.length() % 2 == 0 || strID.length() != 8) {
            if (strData.length() % 2 == 0 || strData.length() != 16) {
                CanBusHelper.sendCanData(Integer.parseInt(strID.substring(2), 16), int2byte(strData, 8), frameFormat);
            } else {
                Toast.makeText(this, "error data", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "error id", Toast.LENGTH_SHORT).show();
        }
    }


    private String handleCanData(byte[] bytes) {
        byte[] id = new byte[4];
        System.arraycopy(bytes, 1, id, 0, id.length);//ID
        byte[] data = null;
        int frameFormatType = (id[3] & 0x06);
        int frameFormat = 0;
        int frameType = 0;
        long extendid = 0;
        switch (frameFormatType) {
            case 0://标准数据
                Log.i("TAG", "handleCanData: " + Arrays.toString(bytes));
                frameFormat = 0;
                frameType = 0;
                extendid = (((((id[0]&0xff)<<24)|((id[1]&0xff)<<16)|((id[2]&0xff)<<8)|((id[3]&0xff)))&0xFFFFFFFFl)>>21);//bit31-bit21: 标准ID
                int dataLength = bytes[5];
                data = new byte[dataLength];
                System.arraycopy(bytes, 6, data, 0, dataLength);
                break;
            case 2://标准远程
                frameFormat = 0;
                frameType = 1;
                extendid = (((((id[0]&0xff)<<24)|((id[1]&0xff)<<16)|((id[2]&0xff)<<8)|((id[3]&0xff)))&0xFFFFFFFFl)>>21);//bit31-bit21: 标准ID
                break;
            case 4://扩展数据
                Log.i("TAG", "handleCanData: " + Arrays.toString(bytes));
                frameFormat = 1;
                frameType = 0;
                extendid = (((((id[0]&0xff)<<24)|((id[1]&0xff)<<16)|((id[2]&0xff)<<8)|((id[3]&0xff)))&0xFFFFFFFFl)>>3);//bit31-bit3: 扩展ID
                int dataLengthExtra = bytes[5];
                data = new byte[dataLengthExtra];
                System.arraycopy(bytes, 6, data, 0, dataLengthExtra);
                break;
            case 6://扩展远程
                frameFormat = 1;
                frameType = 1;
                extendid = (((((id[0]&0xff)<<24)|((id[1]&0xff)<<16)|((id[2]&0xff)<<8)|((id[3]&0xff)))&0xFFFFFFFFl)>>3);//bit31-bit3: 扩展ID
                break;
        }
        return ("通道【" + bytes[0] + "】," + (frameFormat == 0 ? "标准帧" : "扩展帧") + " " + (frameType == 0 ? "数据帧" : "远程帧") + " id: [0x" + String.format("%08x",extendid) + "]  " + (data == null ? "" : "data: [" + DataUtils.saveHex2String(data) + "]"));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        canBusHelper.close();
        ttyS0.close();
    }


}