package com.bdns.daqdemojava.bean;

public class GpsDataBean {

    private String gpgga;
    private String gpvtg;
    private String gptra;
    private String time;
    private double gpsLat = 0;         //纬度
    private double gpsLng = 0;         //经度
    private int    SatelliteNumber = 0;//卫星数量
    private int    gpsRtkQuality = 0;  //差分质量
    private float  heightValue = 0;    //高程值
    private float  heightFactor = 0;   //高程因子
    private float  diffTimeout = 0;    //差分延时
    private float  speed = 0;          //速度
    private float  roll  = 0;          //横滚角/俯仰角
    private float  yaw = 0;            //航向角

    public String getGpgga() {
        return gpgga;
    }

    public void setGpgga(String gpgga) {
        this.gpgga = gpgga;
    }

    public String getGpvtg() {
        return gpvtg;
    }

    public void setGpvtg(String gpvtg) {
        this.gpvtg = gpvtg;
    }

    public String getGptra() {
        return gptra;
    }

    public void setGptra(String gptra) {
        this.gptra = gptra;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public double getGpsLat() {
        return gpsLat;
    }

    public void setGpsLat(double gpsLat) {
        this.gpsLat = gpsLat;
    }

    public double getGpsLng() {
        return gpsLng;
    }

    public void setGpsLng(double gpsLng) {
        this.gpsLng = gpsLng;
    }

    public int getSatelliteNumber() {
        return SatelliteNumber;
    }

    public void setSatelliteNumber(int satelliteNumber) {
        SatelliteNumber = satelliteNumber;
    }

    public int getGpsRtkQuality() {
        return gpsRtkQuality;
    }

    public void setGpsRtkQuality(int gpsRtkQuality) {
        this.gpsRtkQuality = gpsRtkQuality;
    }

    public float getHeightValue() {
        return heightValue;
    }

    public void setHeightValue(float heightValue) {
        this.heightValue = heightValue;
    }

    public float getHeightFactor() {
        return heightFactor;
    }

    public void setHeightFactor(float heightFactor) {
        this.heightFactor = heightFactor;
    }

    public float getDiffTimeout() {
        return diffTimeout;
    }

    public void setDiffTimeout(float diffTimeout) {
        this.diffTimeout = diffTimeout;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getRoll() {
        return roll;
    }

    public void setRoll(float roll) {
        this.roll = roll;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    @Override
    public String toString() {
        return "GpsDataBean{" +
                "gpsLat=" + gpsLat +
                ", gpsLng=" + gpsLng +
                ", SatelliteNumber=" + SatelliteNumber +
                ", gpsRtkQuality=" + gpsRtkQuality +
                ", heightValue=" + heightValue +
                ", diffTimeout=" + diffTimeout +
                ", speed=" + speed +
                ", roll=" + roll +
                ", yaw=" + yaw +
                '}';
    }

}
