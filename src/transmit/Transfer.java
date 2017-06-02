package transmit;

import java.util.Iterator;

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
     * 传输数据包 从一个车到另外一个车， 如果有重复就不传输，带限制条件的传输
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
        Iterator<Packet> it = vehicleFromData.iterator();
        Packet[] toBeTransferred = new Packet[vehicleFromData.getTotalPacketSize()];
        int tupleIndex = 0;
        while(it.hasNext())
        {
            toBeTransferred[tupleIndex++] = it.next();
        }

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
                if(alreadyTransferred >= packetTransferSpeed)
                    break;
            }
        }
    }
}
