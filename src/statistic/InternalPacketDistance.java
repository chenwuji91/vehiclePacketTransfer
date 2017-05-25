package statistic;

/**
 * Created by Administrator on 2017/5/4 0004.
 * 该统计程序是基于车内部的包记录器进行的，如果启动内部的包记录器，将使用本类进行相关的统计
 */
import tools.FileIO;
import transmit.VehicleCarry;
import java.io.File;
import java.util.*;
import transmit.Packet;

public class InternalPacketDistance {

    /**
     * 根据对象序列化的结果统计一些基本的变量，统计传输的一些标准
     * @param args
     */
    private static String folderName = "notTraceHopObj/";
    private static String fileName = "2016_03_28_init_1000_from_30000_to_30250.obj";
    private static int beginTime = Integer.valueOf(fileName.split("_from_")[1].split("_to_")[0]);
    private static int endTime = Integer.valueOf(fileName.split("_to_")[1].split(".obj")[0]);
    public static void main(String args[]) throws Exception {
        HashMap<String, VehicleCarry> vehicleStatus = (HashMap<String, VehicleCarry>) FileIO.readObject(new File(folderName + fileName));
        HashMap<Packet,LinkedHashMap<Integer, ArrayList<Float>>> packetStatus = travelDistance(vehicleStatus);

    }

    /**
     * 计算数据包的传输距离 需要读取数据包的传输距离的参数，看看在传输的过程中每一秒传输了多远的距离
     * 设计思路，可以是每个packet对应一个HashMap 然后这个map对应着这个包所有的travel距离  对应着一个list 表示这个数据包的距离
     * @param vehicleStatus
     */
    private static HashMap<Packet,LinkedHashMap<Integer, ArrayList<Float>>> travelDistance(HashMap<String, VehicleCarry> vehicleStatus){
        HashMap<Packet,LinkedHashMap<Integer, ArrayList<Float>>> packetStatus = new HashMap<>();
        for(Map.Entry<String, VehicleCarry> eachVehicle:vehicleStatus.entrySet()){
            LinkedHashSet<Packet> packetSetThisVehicle = eachVehicle.getValue().getPacketList();//首先取出来每个车的数据包的列表
            for(Packet p:packetSetThisVehicle){//观察每一个数据包，看看它每一条的数据
                ArrayList<Integer> hopTimeList = p.getHopTimeList();
                ArrayList<int[]> hopPlaceList = p.getHopPlaceList();
                int[] bornPlace = p.getBornPlace();
                if(!packetStatus.containsKey(p)){//如果包状态里面不包含这个包
                    packetStatus.put(p,new LinkedHashMap<>());
                }
                LinkedHashMap<Integer, ArrayList<Float>> currentPacketStatus = packetStatus.get(p);
                for(int i = 0;i<hopTimeList.size();i++)//对于每一秒 或者说是传输过程中的每一跳 来看看每一次的传输距离是多少  记录下来
                {
                    int currentTime = hopTimeList.get(i);
                    int[] currentPlace = hopPlaceList.get(i);
                    if(!currentPacketStatus.containsKey(currentTime)){
                        currentPacketStatus.put(currentTime, new ArrayList<Float>());
                    }
                    ArrayList<Float> currentDistanceList = currentPacketStatus.get(currentTime);
                    currentDistanceList.add((float) (Math.sqrt(Math.pow((Double.valueOf(bornPlace[0]) - Double.valueOf(currentPlace[0])),2.0)
                                                + Math.pow((Double.valueOf(bornPlace[0]) - Double.valueOf(currentPlace[0])),2.0)) * 5));
                }

            }

        }
        return packetStatus;
    }

    /**
     * 根据统计的数据包的传输的结果，将结果最后输出到文件
     * @param packetStatus
     */
    private void packetStatusOutput(HashMap<Packet,LinkedHashMap<Integer, ArrayList<Float>>> packetStatus){
        for(Map.Entry<Packet,LinkedHashMap<Integer, ArrayList<Float>>> e: packetStatus.entrySet()){
            int currentPacketID = e.getKey().hashCode();
            LinkedHashMap<Integer, ArrayList<Float>> distance = e.getValue();
            StringBuffer outputOnePacket = new StringBuffer();
            outputOnePacket.append(currentPacketID);
            for(int i = beginTime;i<= endTime;i++){
                float maxDistance = -1;
                float avgDistance = -1;
                float stdDistance = -1;
                if(distance.containsKey(beginTime)){
                    ArrayList<Float> spreadTime = distance.get(i);
                    Float[] spreadTime2 = spreadTime.toArray(new Float[spreadTime.size()]);
                    maxDistance = StatisticTools.getMax(spreadTime2);
                }
            }
        }
    }
}
