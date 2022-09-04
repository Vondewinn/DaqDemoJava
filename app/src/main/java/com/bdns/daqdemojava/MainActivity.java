package com.bdns.daqdemojava;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.android.canapi.DataType;
import com.lc31.uartsdk.ProcessData;
import com.lc31.uartsdk.SerialHelper;
import com.scau.cansdk.CanBusHelper;

import java.util.Arrays;


public class MainActivity extends AppCompatActivity {

    private SerialHelper ttyS0;
    private String strData;

    private Button btnSend, btn250K, btn500K;
    private byte[] data = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x00};
    private CanBusHelper canBusHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        canBusHelper = CanBusHelper.getInstance(this);
        canBusHelper.open();
        canBusHelper.getData(new com.scau.cansdk.ProcessData() {
            @Override
            public void process(byte[] bytes, DataType dataType) {
                // 处理接收数据
                handleCanData(bytes);
            }
        });

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

    private void handleCanData(byte[] bytes) {
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
        System.out.println("handleCanData 通道【" + bytes[0] + "】," + (frameFormat == 0 ? "标准帧" : "扩展帧") + " " + (frameType == 0 ? "数据帧" : "远程帧") + " id:[0x" + String.format("%08x",extendid) + "] " + (data == null ? "" : "data:[0x" + DataUtils.saveHex2String(data) + "]"));
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