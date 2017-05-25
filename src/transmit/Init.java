package transmit;

import tools.FileIO;
import tools.SQL;

import java.util.*;

/**
 * Created by Administrator on 2017/5/15 0015.
 */
public class Init {

    private static int beginTime;//传输开始的时间
    private static int endTime; //传输结束的时间
    private static String processDate;  //处理的是哪一天的数据
    private static boolean traceHop; //如果true记录每个包中间所有的跳数，否则只看最后的结果 当前状态是在车内统计包的情况
    private static int initPacket; //网络传输在起始时刻随机给车辆分了多少个包

    Init(int beginTime,int endTime, String processDate, boolean traceHop, int initPacket){
        this.beginTime = beginTime;
        this.endTime = endTime;
        this.traceHop = traceHop;
        this.processDate = processDate;
        this.initPacket = initPacket;
        FileIO.createDir("./packetRecorder");
        FileIO.createDir("./notTraceHopObj/");
        FileIO.createDir("./traceHopObj/");
    }

    /**
     * 初始化车辆VehicleCarry字典
     * @param vechileID
     * @return
     */
    public HashMap<String, VehicleCarry> initDict(HashSet<String> vechileID){
        HashMap<String, VehicleCarry> allData = new HashMap<>();
        Iterator<String> it = vechileID.iterator();
        while(it.hasNext()){
            allData.put(it.next(),new VehicleCarry());
        }
        return allData;
    }

    /**
     * 数据包的初始化函数  对于起始的包随机分配
     * @param allData
     */
    public void initPacket(HashMap<String, VehicleCarry> allData){
        HashMap<String,int[]> positionInfo = SQL.getPositionInfo(processDate,beginTime);
        HashSet<String> vehicleID = SQL.getVehicleID(beginTime,beginTime,processDate); //在起始的时刻 网络中拥有的车辆集合
        ArrayList<String> vehicleArrayID = new ArrayList<>(vehicleID);
        Random rd = new Random();
        for(int i = 0;i < initPacket; i++){
            String sendToVehicle = vehicleArrayID.get(rd.nextInt(vehicleArrayID.size()));
            VehicleCarry receiceVehicleCarry = allData.get(sendToVehicle);
            receiceVehicleCarry.add(new Packet("1", sendToVehicle,beginTime, positionInfo.get(sendToVehicle), traceHop),beginTime);
        }
    }
}
