package transmit;

import tools.FileIO;

import java.util.*;

/**
 * Created by Administrator on 2017/5/10 0010.
 * 专用的包记录器，记录产生的每一个包，以及这个包的传输情况，包括这个包经过了哪些地方  每次什么车获得了这些包
 * 该模块提供 保存 清空 增加 融合等功能，作为一个共用模块提供给统计和产生的模块
 */
public class AllPacketRecorder {

    private static HashMap<Packet,HashMap<Integer, HashMap<String,int[]>>> packetStatus = new HashMap<>();//初始化包状态记录，<包名称，HashMap<时间，地点>>
    private static int cleanMemoryInterval;  //在一定的时间间隔内，清除数据包，将数据包保存到文件，设置过小可能导致短暂消失的车辆数据包丢失

    /**
     * 初始化所有包的记录模块  在包完成初始化之后调用该模块 建立包状态记录器
     * <包名称，HashMap<车ID，HashMap<时间，地点>>>
     *
     */
    AllPacketRecorder(HashMap<String, VehicleCarry> allData, int cleanMemoryInterval){
//        initPacketStatus(allData);
        this.cleanMemoryInterval = cleanMemoryInterval;
        System.out.println("Finish init external recorder~");
    }


    /**
     * 本函数用于在每一秒结束的时候 刷新车的状态 以更新数据包的状态
     * @param currentTime 当前时间
     * @param positionInfo 当前时间所有车辆的位置信息
     * @param allData 当前所有的车辆携带的数据包的信息
     */
    public void refreshStatus(int currentTime, HashMap<String, int[]> positionInfo, HashMap<String, VehicleCarry> allData){
        for(Map.Entry<String, VehicleCarry> eachV: allData.entrySet()){
            String vehicleID = eachV.getKey();
            if(!positionInfo.containsKey(vehicleID))//如果这个车在这一秒里面没有出现 那自然也不用考虑它
                continue;
            int[] vehiclePosition = positionInfo.get(vehicleID);
            VehicleCarry vc = eachV.getValue();
            LinkedHashSet<Packet> packetlist = vc.getPacketList();
            for(Packet p:packetlist) {
                addPacketToVehicle(p,vehicleID,currentTime,vehiclePosition);
            }
        }
    }

    public int packetSize(){
        return packetStatus.size();
    }

    public int packetXVehicleSize(){
        int size = 0;
        for(Map.Entry<Packet, HashMap<Integer, HashMap<String, int[]>>> e:packetStatus.entrySet()){
            size+=e.getValue().size();
        }
        return size;
    }


    @Deprecated
    public void initPacketStatus(HashMap<String, VehicleCarry> allData){
        for(Map.Entry<String, VehicleCarry> eachVehicle:allData.entrySet()) {//对于每一辆车来执行相关的操作
            String vehicleId = eachVehicle.getKey();  //车辆的ID
            LinkedHashSet<Packet> packetSetThisVehicle = eachVehicle.getValue().getPacketList();//首先取出来每个车的数据包的列表
            for (Packet p : packetSetThisVehicle) {//对每一个车上的每一个数据包来做下列的操作
                if (!packetStatus.containsKey(p)) {
                    packetStatus.put(p, new HashMap<>());
                }
                HashMap<Integer, HashMap<String, int[]>> currentPacketStatus = packetStatus.get(p);
                if (!currentPacketStatus.containsKey(p.getBornTime())) {
                    currentPacketStatus.put(p.getBornTime(), new HashMap<>());
                }
                HashMap<String, int[]> currentPacketOnVehicleStatus = currentPacketStatus.get(p.getBornTime());
                if (!currentPacketOnVehicleStatus.containsKey(vehicleId)) {
                    currentPacketOnVehicleStatus.put(vehicleId, p.getBornPlace());
                }
            }
        }
    }

    /**
     * 新增加包 在传输过程中需要调用，即某个车获得一个新的包，改变相关的记录
     * @param packet 需要传输的数据包
     * @param place 数据包交换的地点
     * @param time 数据包交换的时间
     * @param vehicleID 当前接收数据包的车辆ID
     */
    public void addPacketToVehicle(Packet packet, String vehicleID, int time, int[] place){
        if(!packetStatus.containsKey(packet))
            packetStatus.put(packet,new HashMap<>());
        if(!packetStatus.get(packet).containsKey(time))
            packetStatus.get(packet).put(time, new HashMap<>());
        HashMap<String, int[]> packetStatuasOfThisVehicle = packetStatus.get(packet).get(time);
        packetStatuasOfThisVehicle.put(vehicleID,place);
    }

    /**
     * 返回当前所有车的记录集合
     * @return
     */
    public HashMap<Packet, HashMap<Integer, HashMap<String, int[]>>> getPacketStatus()
    {
        return packetStatus;
    }

    /**
     * 将两个记录状态的包进行合并 注意：尽量是把少的作为参数向里面传递，都则严重影响效率
     * @param packetStatus2 另外一个状态记录包
     */
    public void combineTwoPacketRecorder(HashMap<Packet, HashMap<Integer, HashMap<String, int[]>>> packetStatus2){
        for(Map.Entry<Packet, HashMap<Integer, HashMap<String, int[]>>> eachRecord:packetStatus2.entrySet()){
            Packet currentPacket = eachRecord.getKey();
            if(!packetStatus.containsKey(currentPacket)){//如果不包含这个包，直接就整个拿过来好了
                packetStatus.put(currentPacket,eachRecord.getValue());
            }
            else{
                HashMap<Integer, HashMap<String, int[]>> currentPacketCarry1 = packetStatus.get(currentPacket);
                HashMap<Integer, HashMap<String, int[]>> currentPacketCarry2 = packetStatus2.get(currentPacket);
                for(Map.Entry<Integer, HashMap<String, int[]>> eachVehicle:currentPacketCarry2.entrySet()){
                    Integer currentVehicle2 = eachVehicle.getKey();
                    currentPacketCarry1.put(currentVehicle2,eachVehicle.getValue());//直接拿过来 时间一定不会重叠
                }
            }
        }
        packetStatus2.clear();// 合并完了强行清空读入的数据，避免内存溢出
    }

    public void saveRecorder(String filepath,HashMap<String, VehicleCarry> allData) throws Exception {
        String folderObj = "./packetRecorder/";
        FileIO.writeObject(packetStatus,folderObj + filepath);
        packetStatus.clear();
        System.out.println("Successfully save and clear");
    }

    @Override
    public void finalize() throws Throwable {
//        packetStatus.clear();
        super.finalize();

    }

}
