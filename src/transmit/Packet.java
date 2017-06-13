package transmit;

import exception.ExceptionUser;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Administrator on 2017/5/3 0003.
 */
public class Packet implements Cloneable, Serializable {
    //    private String id;
    private int bornTime;
    private int[] bornPlace;
    private ArrayList<String> hopVehicleList;
    private ArrayList<int[]> hopPlaceList;
    private ArrayList<Integer> hopTimeList;
    private String fromVehicle;
    private String toVehicle;
    private boolean traceHop;
    private int attainTime;//当前车辆获得这个数据包的时间




    Packet(String fromVehicle, String toVehicle, int bornTime, int[] bornPlace, boolean traceHop) {
        this.fromVehicle = fromVehicle;
        this.toVehicle = toVehicle;
//        this.id = fromVehicle + "-" + toVehicle + "-" + String.valueOf(bornTime) + "-" + String.valueOf(bornPlace[0]) + String.valueOf(bornPlace[1]);
        this.bornTime = bornTime;
        this.bornPlace = bornPlace;
        this.traceHop = traceHop;
        if (traceHop) { //看情况决定是否记录中间所有的路由
            hopVehicleList = new ArrayList<>();
            hopPlaceList = new ArrayList<>();
            hopTimeList = new ArrayList<>();
            hopTimeList.add(bornTime);
            hopPlaceList.add(bornPlace);
            hopVehicleList.add(toVehicle); //最后是放在哪个车上面 这个就attach哪个ID
        }
    }

    public void setBornTime(int bornTime) {
        this.bornTime = bornTime;
    }

    public void setBornPlace(int[] bornPlace) {
        this.bornPlace = bornPlace;
    }

    public String getFromVehicle() {
        return fromVehicle;
    }

    public void setFromVehicle(String fromVehicle) {
        this.fromVehicle = fromVehicle;
    }

    public String getToVehicle() {
        return toVehicle;
    }

    public void setToVehicle(String toVehicle) {
        this.toVehicle = toVehicle;
    }

    public int getAttainTime() {
        return attainTime;
    }

    public void setAttainTime(int attainTime) {
        this.attainTime = attainTime;
    }

    public int getBornTime(){
        return this.bornTime;
    }

    public ArrayList<Integer> getHopTimeList(){
        return this.hopTimeList;
    }

    public  ArrayList<int[]> getHopPlaceList(){
        return this.hopPlaceList;
    }

    public int[] getBornPlace(){
        return this.bornPlace;
    }

    public ArrayList<String> getHopVehicleList(){
        return this.hopVehicleList;
    }


    /**
     * 清楚所有记录跳数的数据
     */
    public void clearHopInfo(){
        if(traceHop){
            hopVehicleList.clear();
            hopPlaceList.clear();
            hopTimeList.clear();
        }
        else{
            return;
        }
    }

    /**
     * 将两个连续时间的数据包传输的内容进行叠加，得到一个完整的带有跳数的内容
     * @param p
     */
    public void addAnotherPacketHopInfo(Packet p) {
        if(this.hashCode() != p.hashCode()){
            throw new ExceptionUser("Adding not same packet hop");
        }
        this.hopVehicleList.addAll(p.getHopVehicleList());
        this.hopTimeList.addAll(p.getHopTimeList());
        this.hopPlaceList.addAll(p.getHopPlaceList());
    }


    public void addInfo(int transferTime, int[] transferPosition, String toVehicleID) {
        hopTimeList.add(transferTime);
        hopPlaceList.add(transferPosition);
        hopVehicleList.add(toVehicleID);
    }

    //禁用掉override 不能开启内部记录器
//    @Override
//    public boolean equals(Object o) {
//        Packet s = (Packet) o;
//        if(s.fromVehicle.equals(this.fromVehicle) && s.toVehicle.equals(this.toVehicle)
//                && s.bornTime == this.bornTime && s.bornPlace[0] == this.bornPlace[0] && s.bornPlace[1] == this.bornPlace[1]){
//            return true;
//        }
//        else
//            return false;
////        return s.id.equals(this.id);
//    }

//    @Override
//    public int hashCode() {
//        int result = Integer.valueOf(this.fromVehicle) + Integer.valueOf(this.toVehicle) * 12345 +
//                this.bornPlace[0] * 567 + this.bornPlace[1] * 789 + this.bornTime * 97531;
//        return result;
//    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        Packet packet = (Packet) super.clone();
        if(traceHop) {
            packet.hopVehicleList = (ArrayList<String>) hopVehicleList.clone();
            packet.hopPlaceList = (ArrayList<int[]>) hopPlaceList.clone();
            packet.hopTimeList = (ArrayList<Integer>) hopTimeList.clone();
        }
        return packet;
    }
}
