package transmit;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * Created by chenwuji<chenwuji@mail.ustc.edu.cn> on 2017/6/1 0001.
 * 数据包传输专用类  控制所有数据包的传输  主要是各种传输策略的控制
 */
public class Transfer {

    private boolean traceHop;
    private boolean useExtraPacketRecorder;
    private int refreshExternalRecorderInterval;
    private int vehiclePacketLimit;
    private int packetTransferSpeed;

    /**
     * @param traceHop
     * @param useExtraPacketRecorder
     * @param refreshExternalRecorderInterval
     * @param vehiclePacketLimit
     * @param packetTransferSpeed
     */
    public Transfer(boolean traceHop, boolean useExtraPacketRecorder, int refreshExternalRecorderInterval, int vehiclePacketLimit, int packetTransferSpeed) {
        this.traceHop = traceHop;
        this.useExtraPacketRecorder = useExtraPacketRecorder;
        this.refreshExternalRecorderInterval = refreshExternalRecorderInterval;
        this.vehiclePacketLimit = vehiclePacketLimit;
        this.packetTransferSpeed = packetTransferSpeed;
    }

    /**
     * 传输数据包 从一个车到另外一个车， 如果有重复就不传输  没有任何的限制  广播传输
     * @param vehicleFromData
     * @param vehicleToData
     * @param currentTime
     * @param toVehicleID
     * @param positionVehicleTo
     * @throws CloneNotSupportedException
     */
    protected void transferPacket(VehicleCarry vehicleFromData, VehicleCarry vehicleToData,
                                int currentTime, String toVehicleID, int[] positionVehicleTo,
                                AllPacketRecorder externalRecorder) throws CloneNotSupportedException {
        int transferredPacketAllowed = vehicleFromData.getTotalPacketSize() - vehicleFromData.getPacketNum(currentTime);//总的数据包个数减去这一秒收到的个数
        Iterator<Packet> it = vehicleFromData.iterator();
        int alreadyTransferred = 0;
        while(it.hasNext()){
            if(alreadyTransferred >= transferredPacketAllowed)//如果已经传输的包大于允许传输的包
                break;
            Packet currentPacket = it.next();
            if(!vehicleToData.contains(currentPacket)){//这个时候是过滤掉一跳的包的
                Packet transferredPacket = currentPacket;
                if(traceHop){
                    transferredPacket = (Packet) currentPacket.clone();
                    transferredPacket.addInfo(currentTime, positionVehicleTo, toVehicleID);
                }
                if(useExtraPacketRecorder && refreshExternalRecorderInterval != 1){//注意 如果每秒都刷新位置的话 就不用在交换的时候再进行记录
                    externalRecorder.addPacketToVehicle(transferredPacket,toVehicleID,currentTime,positionVehicleTo);
                }
                vehicleToData.add(transferredPacket,currentTime);
                alreadyTransferred++;
            }
        }
    }


    /**
     * 传输数据包 从一个车到另外一个车， 如果有重复就不传输，带限制条件的传输  同时在传输的过程中 需要维护所有的全局变量
     * @param vehicleFromData
     * @param vehicleToData
     * @param currentTime
     * @param toVehicleID
     * @param positionVehicleTo
     * @throws CloneNotSupportedException
     */
    protected void transferPacketWithLimitationRemoveOldIfFull(VehicleCarry vehicleFromData, VehicleCarry vehicleToData,
                                  int currentTime, String toVehicleID, int[] positionVehicleTo,
                                  AllPacketRecorder externalRecorder) throws CloneNotSupportedException {
        int transferredPacketAllowed = vehicleFromData.getTotalPacketSize() - vehicleFromData.getPacketNum(currentTime);//总的数据包个数减去这一秒收到的个数
        Iterator<Packet> it = vehicleToData.iterator();
        //需要移到的车上的所有的数据包的列表
        Packet[] existPacket = new Packet[vehicleToData.getTotalPacketSize()];
        int tupleIndex = 0;
        while(it.hasNext())
        {
            existPacket[tupleIndex++] = it.next();
        }
        tupleIndex = 0;
        LinkedHashSet<Packet> vehicleToDataSet = vehicleToData.getPacketList();
        LinkedHashMap<Integer, Integer> vehicleToDataCountMap = vehicleToData.getPacketCount();
        int[] timeList = new int[vehicleToDataCountMap.size()];
        Iterator<Map.Entry<Integer,Integer>> itt = vehicleToDataCountMap.entrySet().iterator();
        for(int i = 0;i < vehicleToDataCountMap.size();i++)
            timeList[i] = itt.next().getKey();
        int minTimeCount = 0;
        int alreadyTransferred = 0;
        it = vehicleFromData.iterator();
        while(it.hasNext()){
            if(alreadyTransferred >= transferredPacketAllowed)//如果已经传输的包大于允许传输的包
                break;
            Packet currentPacket = it.next();
            if(!vehicleToData.contains(currentPacket)){//看看目标车没有这个数据包的时候 才进行传输
                while(vehicleToData.getTotalPacketSize() >= vehiclePacketLimit)  //如果这个车满了 需要做额外处理
                {
                    Packet packetNeedToRemove = existPacket[tupleIndex++];
                    vehicleToDataSet.remove(packetNeedToRemove);
                    if(useExtraPacketRecorder){//清除外部记录器的包
                        externalRecorder.removePacketFromVehicle(packetNeedToRemove,toVehicleID,timeList[minTimeCount]);
                    }
                    //更新包数量记录器
                    int currentCountTemp;
                    currentCountTemp = vehicleToDataCountMap.get(timeList[minTimeCount]);
                    if(currentCountTemp == 1)
                        vehicleToDataCountMap.remove(timeList[minTimeCount++]);
                    else
                        vehicleToDataCountMap.replace(timeList[minTimeCount], --currentCountTemp);

                }
                Packet transferredPacket = currentPacket;
                if(traceHop){
                    transferredPacket = (Packet) currentPacket.clone();
                    transferredPacket.addInfo(currentTime, positionVehicleTo, toVehicleID);
                }
                if(useExtraPacketRecorder && refreshExternalRecorderInterval != 1){//注意 如果每秒都刷新位置的话 就不用在交换的时候再进行记录
                    externalRecorder.addPacketToVehicle(transferredPacket,toVehicleID,currentTime,positionVehicleTo);
                }
                vehicleToData.add(transferredPacket,currentTime);
                alreadyTransferred++;
                if(alreadyTransferred >= packetTransferSpeed)
                    break;
            }
        }
    }
}
