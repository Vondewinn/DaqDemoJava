package com.bdns.daqdemojava.dataprocessing;

import com.bdns.daqdemojava.bean.GpsDataBean;
import com.bdns.daqdemojava.manager.NetWorkServiceNtrip;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Gps {

    public static GpsDataBean dataAnalysis(String gpsData) {
        if(null == gpsData){
            return null;
        }
        GpsDataBean gpsDataBean = new GpsDataBean();
        int ggaLength = gpsData.indexOf("$GPGGA,");
        if(-1 != ggaLength) {
            String string = gpsData.substring(ggaLength);
            ggaLength = string.indexOf("\r\n");
            if(-1 != ggaLength){
                string = string.substring(0, ggaLength);
                gpsDataBean.setGpgga(string);
                String str = string.substring(0, string.length() - 1);
                List<String> gpsDataList = new ArrayList<>();
                String[] split = str.split(",");
                for (String s : split) {
                    gpsDataList.add(s);
                }
                if(gpsDataList.size() == 15) {

                    if (!gpsDataList.get(1).isEmpty()) {
                        gpsDataBean.setTime(gpsDataList.get(1));
                    }

                    if (!gpsDataList.get(13).isEmpty()) {
                        gpsDataBean.setDiffTimeout(Float.parseFloat(gpsDataList.get(13)));
                    } else {
                        gpsDataBean.setDiffTimeout(0f);
                    }
                    if (!gpsDataList.get(6).isEmpty()) {
                        gpsDataBean.setGpsRtkQuality(Integer.parseInt(gpsDataList.get(6)));
                    }
                    if (!gpsDataList.get(7).isEmpty()) {
                        gpsDataBean.setSatelliteNumber(Integer.parseInt(gpsDataList.get(7)));
                    }
                    if (!gpsDataList.get(9).isEmpty()) {
                        gpsDataBean.setHeightValue(Float.parseFloat(gpsDataList.get(9)) + Float.parseFloat(gpsDataList.get(11)));
                    }
                    if (!gpsDataList.get(11).isEmpty()) {
                        gpsDataBean.setHeightFactor(Float.parseFloat(gpsDataList.get(11)));
                    }
                    if(gpsDataList.get(2).length() >= 12){
                        //纬度DDMM.MMMMMMM转为DDX.XXXXXXX
                        String wd_dd = gpsDataList.get(2).substring(0, 2);
                        String wd_mm_mmmmmmm = gpsDataList.get(2).substring(2, 11);
                        BigDecimal wd_miao = new BigDecimal(wd_mm_mmmmmmm);
                        BigDecimal v_60 = new BigDecimal("60");  //数值60
                        BigDecimal wd_miao_60 = wd_miao.divide(v_60, 8, BigDecimal.ROUND_HALF_UP);  //mm.mmmmmmm除以60，保留8位小数，四舍五入
                        String s_wd_miao = wd_miao_60.toPlainString();
                        String gpsLat = wd_dd + s_wd_miao.substring(1);
                        gpsDataBean.setGpsLat(Double.parseDouble(gpsLat));
                    }
                    if (gpsDataList.get(4).length() >= 13) {
                        //经度DDDMM.MMMMMMM转为DDDX.XXXXXXX
                        String jd_ddd = gpsDataList.get(4).substring(0, 3);
                        String jd_mm_mmmmmmm = gpsDataList.get(4).substring(3, 12);
                        BigDecimal jd_miao = new BigDecimal(jd_mm_mmmmmmm);
                        BigDecimal v_60 = new BigDecimal("60");  //数值60
                        BigDecimal jd_miao_60 = jd_miao.divide(v_60, 8, BigDecimal.ROUND_HALF_UP);  //mm.mmmmmmm除以60，保留8位小数，四舍五入
                        String s_jd_miao = jd_miao_60.toPlainString();
                        String gpsLng = jd_ddd + s_jd_miao.substring(1);
                        gpsDataBean.setGpsLng(Double.parseDouble(gpsLng));
                    }
                }
            }
        }
        //GPVTG
        int vtgLength = gpsData.indexOf("$GPVTG,");
        if(-1 != vtgLength){
            String string = gpsData.substring(vtgLength);
            vtgLength = string.indexOf("\r\n");
            if(-1 != vtgLength) {
                string = string.substring(0, vtgLength);
                gpsDataBean.setGpvtg(string);
                //System.out.println(string);
                String str = string.substring(0, string.length() - 1);
                List<String> gpsDataList = new ArrayList<>();
                String[] split = str.split(",");
                for (String s : split) {
                    gpsDataList.add(s);
                }
                if(gpsDataList.size() == 10){
                    if(gpsDataList.get(7).length() > 0){
                        String speed = String.format("%.2f", Float.parseFloat(gpsDataList.get(7)));
                        gpsDataBean.setSpeed(Float.parseFloat(speed));
                    }
                }
            }
        }
        //GPTRA
        int traLength = gpsData.indexOf("$GPTRA,");
        if(-1 != traLength){
            String string = gpsData.substring(traLength);
            traLength = string.indexOf("\r\n");
            if(-1 != traLength) {
                string = string.substring(0, traLength);
                gpsDataBean.setGptra(string);
                //System.out.println(string);
                String str = string.substring(0, string.length() - 1);
                List<String> gpsDataList = new ArrayList<>();
                String[] split = str.split(",");
                for (String s : split) {
                    gpsDataList.add(s);
                }
                if(gpsDataList.size() == 9) {
                    if(!gpsDataList.get(2).isEmpty()){
                        String yaw = String.format("%.2f", Float.parseFloat(gpsDataList.get(2)));
                        gpsDataBean.setYaw(Float.parseFloat(yaw));
                    } else {
                        gpsDataBean.setYaw(0f);
                    }
                }
            }
        }

        return gpsDataBean;

    }

}
