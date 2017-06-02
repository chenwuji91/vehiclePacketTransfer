package transmit;

import tools.FileIO;
import tools.SQL;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Administrator on 2017/5/15 0015.
 * 维护整个运行状态的基本操作，包括数据包的定期存储等
 */
public class RuntimeStatusOperation {
    private static int packetTTL; //包存活的时间
    private static boolean traceHop; //如果true记录每个包中间所有的跳数，否则只看最后的结果 当前状态是在车内统计包的情况
    private static boolean useExtraPacketRecorder;
    private static HashSet<String> vehicleID;
    private static String processDate;  //处理的是哪一天的数据
    private static int cleanMemoryInterval;  //在一定的时间间隔内，清除数据包，将数据包保存到文件，设置过小可能导致短暂消失的车辆数据包丢失
    RuntimeStatusOperation(HashSet<String> vehicleID, int packetTTL, boolean traceHop, boolean useExtraPacketRecorder, int cleanMemoryInterval, String processDate){
        this.vehicleID = vehicleID;
        this.packetTTL = packetTTL;
        this.traceHop = traceHop;
        this.useExtraPacketRecorder = useExtraPacketRecorder;
        this.cleanMemoryInterval = cleanMemoryInterval;
        this.processDate = processDate;

    }

    /**
     * 把在未来一个时间间隔内没有出现的数据包，从历史数据中清除。 即把那些没有跑的车上的包给清除掉
     * @param allData
     * @param currentTime
     */
    public void cleanMemory(HashMap<String, VehicleCarry> allData,int currentTime){
        HashSet<String> nextIntervalAppear = SQL.getVehicleID(currentTime,currentTime + cleanMemoryInterval,processDate);
        HashSet<String> needToRemove = new HashSet<>();
        needToRemove.addAll(vehicleID);
        needToRemove.removeAll(nextIntervalAppear);
        for(String vehicleID:needToRemove){
            allData.get(vehicleID).clearVehicle();//把下一个时间间隔不再出现的死包 给清除掉
        }
    }

    /**
     * 强力整理内存 每次只处理并且保存有限个数的数据包
     * @param allData
     * @param currentTime
     */
    public void cleanMemoryStrongly(HashMap<String, VehicleCarry> allData,int currentTime){
        HashSet<String> nextIntervalAppear = SQL.getVehicleID(currentTime,currentTime + cleanMemoryInterval,processDate);
        HashSet<String> needToRemove = new HashSet<>();
        needToRemove.addAll(vehicleID);
        needToRemove.removeAll(nextIntervalAppear);//后面的每次任务就是删除掉在下一步里面没有的，然后加上新来的
        System.out.println("清除之前：" + allData.size());
        tools.RuntimeTest.printReaminMemory();//看剩余内存
        for(String vehicleID1:needToRemove){
            if(allData.containsKey(vehicleID1))
                try {
                    allData.get(vehicleID1).clearVehicle();
                    allData.get(vehicleID1).finalize();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            allData.remove(vehicleID1);//不该出现的去死
        }
        System.out.println("清除之后" + allData.size());
        System.gc();
        tools.RuntimeTest.printReaminMemory();//看剩余内存
        for(String vehicleID:nextIntervalAppear){
            if(!allData.containsKey(vehicleID)){
                allData.put(vehicleID,new VehicleCarry());
            }
        }
        System.out.println("加入增量后" + allData.size());
        tools.RuntimeTest.printReaminMemory();//看剩余内存
    }


    /***
     * 清除每个车上面过期的数据包
     * @param allData
     * @param currentTime
     */
    public void cleanDeprecatedPacket(HashMap<String, VehicleCarry> allData, int currentTime){
        Iterator<Map.Entry<String,VehicleCarry>> it = allData.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<String,VehicleCarry> nowVehicle = it.next();
            nowVehicle.getValue().cleanDeprecatedPackets(packetTTL,currentTime);
        }
    }

    /**
     * 保存现在传输的跳数的相关记录，保存到文件，然后继续
     * 目前暂时没有做外部数据记录包的处理
     * @param objPath
     */
    public HashMap<String, VehicleCarry> getObjButCleanTrace(String objPath) throws Exception{
        HashMap<String, VehicleCarry> obj = (HashMap<String, VehicleCarry>) FileIO.readObject(new File(objPath));
        for(Map.Entry<String,VehicleCarry> eachpair: obj.entrySet()){//清空数据
            VehicleCarry v = eachpair.getValue();
            Iterator<Packet> it = v.getPacketList().iterator();
            while(it.hasNext())
                it.next().clearHopInfo();
        }
        return obj;
    }

    /**
     * 保存对象并且将对象中记录的一部分数据清空
     * @param allData
     * @param objFilePath
     * @throws Exception
     */
    public void saveObjAndCleanInternalRecorder(HashMap<String, VehicleCarry> allData,String objFilePath) throws Exception {
        String folderObj = "./notTraceHopObj/";
        if(traceHop)
            folderObj = "./traceHopObj/";

        FileIO.writeObject(allData,folderObj + objFilePath);
        if(!traceHop)//如果不记录中间跳数的话 直接返回
            return;
        for(Map.Entry<String,VehicleCarry> eachpair: allData.entrySet()){//清空数据
            VehicleCarry v = eachpair.getValue();
            Iterator<Packet> it = v.getPacketList().iterator();
            while(it.hasNext())
                it.next().clearHopInfo();
        }
    }

}
