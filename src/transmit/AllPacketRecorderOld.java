package transmit;

import tools.FileIO;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * Created by Administrator on 2017/5/10 0010.
 * 专用的包记录器，记录产生的每一个包，以及这个包的传输情况，包括这个包经过了哪些地方  每次什么车获得了这些包
 * 该模块提供 保存 清空 增加 融合等功能，作为一个共用模块提供给统计和产生的模块
 */
public class AllPacketRecorderOld {

    private static HashMap<Packet,HashMap<String, HashMap<Integer,int[]>>> packetStatus = new HashMap<>();//初始化包状态记录，<包名称，HashMap<车ID，HashMap<时间，地点>>>
    private static int cleanMemoryInterval;  //在一定的时间间隔内，清除数据包，将数据包保存到文件，设置过小可能导致短暂消失的车辆数据包丢失

    /**
     * 初始化所有包的记录模块  在包完成初始化之后调用该模块 建立包状态记录器
     * <包名称，HashMap<车ID，HashMap<时间，地点>>>
     *
     */
    AllPacketRecorderOld(HashMap<String, VehicleCarry> allData, int cleanMemoryInterval){
        initPacketStatus(allData);
        this.cleanMemoryInterval = cleanMemoryInterval;
        System.out.println("Finish init external recorder~");
    }

    public int packetSize(){
        return packetStatus.size();
    }

    public int packetXVehicleSize(){
        int size = 0;
        for(Map.Entry<Packet,HashMap<String, HashMap<Integer,int[]>>> e:packetStatus.entrySet()){
            size+=e.getValue().size();
        }
        return size;
    }

    public void initPacketStatus(HashMap<String, VehicleCarry> allData){
        for(Map.Entry<String, VehicleCarry> eachVehicle:allData.entrySet()) {//对于每一辆车来执行相关的操作
            String vehicleId = eachVehicle.getKey();  //车辆的ID
            LinkedHashSet<Packet> packetSetThisVehicle = eachVehicle.getValue().getPacketList();//首先取出来每个车的数据包的列表
            for (Packet p : packetSetThisVehicle) {//对每一个车上的每一个数据包来做下列的操作
                if (!packetStatus.containsKey(p)) {
                    packetStatus.put(p, new HashMap<>());
                }
                HashMap<String, HashMap<Integer, int[]>> currentPacketStatus = packetStatus.get(p);
                if (!currentPacketStatus.containsKey(vehicleId)) {
                    currentPacketStatus.put(vehicleId, new HashMap<>());
                }
                HashMap<Integer, int[]> currentPacketOnVehicleStatus = currentPacketStatus.get(vehicleId);
                if (!currentPacketOnVehicleStatus.containsKey(p.getBornTime())) {
                    currentPacketOnVehicleStatus.put(p.getBornTime(), p.getBornPlace());
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
        if(!packetStatus.get(packet).containsKey(vehicleID))
            packetStatus.get(packet).put(vehicleID, new HashMap<>());
        HashMap<Integer,int[]> packetStatuasOfThisVehicle = packetStatus.get(packet).get(vehicleID);
        packetStatuasOfThisVehicle.put(time,place);
    }

    /**
     * 返回当前所有车的记录集合
     * @return
     */
    public HashMap<Packet,HashMap<String, HashMap<Integer,int[]>>> getPacketStatus()
    {
        return packetStatus;
    }

    /**
     * 将两个记录状态的包进行合并 注意：尽量是把少的作为参数向里面传递，都则严重影响效率
     * @param packetStatus2 另外一个状态记录包
     */
    public void combineTwoPacketRecorder(HashMap<Packet,HashMap<String, HashMap<Integer,int[]>>> packetStatus2){
        for(Map.Entry<Packet,HashMap<String, HashMap<Integer,int[]>>> eachRecord:packetStatus2.entrySet()){
            Packet currentPacket = eachRecord.getKey();
            if(!packetStatus.containsKey(currentPacket)){//如果不包含这个包，直接就整个拿过来好了
                packetStatus.put(currentPacket,eachRecord.getValue());
            }
            else{
                HashMap<String, HashMap<Integer,int[]>> currentPacketCarry1 = packetStatus.get(currentPacket);
                HashMap<String, HashMap<Integer,int[]>> currentPacketCarry2 = packetStatus2.get(currentPacket);
                for(Map.Entry<String,HashMap<Integer,int[]>> eachVehicle:currentPacketCarry2.entrySet()){
                    String currentVehicle2 = eachVehicle.getKey();
                    if(!currentPacketCarry1.containsKey(currentVehicle2)){//如果记录里面没有这个车的记录，就直接拿过来
                        currentPacketCarry1.put(currentVehicle2,eachVehicle.getValue());
                    }
                    else{
                        HashMap<Integer,int[]> currentVehicleCarry2 = eachVehicle.getValue();
                        HashMap<Integer,int[]> currentVehicleCarry1 = currentPacketCarry1.get(currentVehicle2);
                        currentVehicleCarry1.putAll(currentVehicleCarry2);//将一个车上面的所有的时间地点的信息进行合并
                    }
                }

            }
        }
        packetStatus2.clear();// 合并完了强行清空读入的数据，避免内存溢出
    }

    public void saveRecorder(String filepath,HashMap<String, VehicleCarry> allData) throws Exception {
        String folderObj = "./packetRecorder/";
        FileIO.writeObject(packetStatus,folderObj + filepath);
        packetStatus.clear();
        System.out.println("Successfully save");
    }

    @Override
    public void finalize() throws Throwable {
//        packetStatus.clear();
        super.finalize();

    }

}
