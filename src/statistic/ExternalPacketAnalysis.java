package statistic;

import tools.FileIO;
import transmit.Packet;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenwuji<chenwuji@mail.ustc.edu.cn> on 2017/5/18 0018.
 * @Funciton: 外部数据包记录分析器，根据外部包分析包的传输距离等 包括在不同的时间传输的覆盖范围是什么样的
 * 需要看几个东西，看在一定的时间内包能够传输多远 还有看传输一定的距离需要多长时间
 */
public class ExternalPacketAnalysis {

    /**
     * Main function
     * @param args
     */
    private static final String folderName = "./packetRecorder/";
    private static String outputFileName = "";
    int beginTime;
    int endTime = -1;
    String processDate;
    int initPacket;
    public ExternalPacketAnalysis(){

    }

    private void setBasicPara(String oneOfTheFile){
        int thisEndTime = Integer.valueOf(oneOfTheFile.split("_to_")[1].split(".obj")[0]);
        if(thisEndTime < endTime)
            return;
        endTime = thisEndTime;
        beginTime = Integer.valueOf(oneOfTheFile.split("_from_")[1].split("_to_")[0]);
        processDate = oneOfTheFile.split("_init_")[0];
        initPacket = Integer.valueOf(oneOfTheFile.split("_init_")[1].split("_from")[0]);
    }


    public static void main(String args[]) throws Exception {
        ExternalPacketAnalysis analy = new ExternalPacketAnalysis();
        HashMap<Packet,HashMap<Integer, HashMap<String,int[]>>> allResult = analy.combineSeparatedObj();
    }

    private void packetTransmitDistance(HashMap<Packet,HashMap<Integer, HashMap<String,int[]>>> allResult){
        String folderPath = "./PacketDistanceAnalysis/";
        FileIO.createDir(folderPath);
        String outputPath = folderPath + processDate + "_" + initPacket + "_from_" + beginTime + "_to_" + endTime;
        for(Map.Entry<Packet,HashMap<Integer, HashMap<String,int[]>>> p: allResult.entrySet()){
            Packet packet = p.getKey();
            String packetID = packet.hashCode() + "_" + packet.getBornPlace()[0]+ "_" + packet.getBornPlace()[1];
            StringBuffer outputStringAvg = new StringBuffer();
            StringBuffer outputStringMax = new StringBuffer();
            outputStringAvg.append(packetID);
            outputStringMax.append(packetID);
            HashMap<Integer, HashMap<String,int[]>> packetStatus = p.getValue();
            for(int i = beginTime;i<endTime;i++){
                if(packetStatus.containsKey(i)){
                    HashMap<String,int[]> vehicleDistanceMap = packetStatus.get(i);//依次看每一秒这个包都在哪些车上面
                    float maxDistance = -1;
                    float totalDistance = 0;
                    for(Map.Entry<String, int[]> eachVehicle:vehicleDistanceMap.entrySet()){
                        float distance = StatisticTools.distanceOfPalces(eachVehicle.getValue(),packet.getBornPlace());
                        if(distance > maxDistance)
                            maxDistance = distance;
                        totalDistance += distance;
                    }
                    outputStringAvg.append("," + totalDistance/vehicleDistanceMap.size());
                    outputStringMax.append("," + maxDistance);
                }
            }
            outputStringAvg.append("\n");
            outputStringMax.append("\n");
            FileIO.writeWithAppend(outputPath + "_avg.csv",outputStringAvg.toString());
            FileIO.writeWithAppend(outputPath + "_max.csv",outputStringMax.toString());
        }
    }

    /**
     * 读取整个目录的对象并整个 返回整个的对象
     * @return 完整的包记录器，记录每个时间所有数据包的位置
     * @throws Exception
     */
    private HashMap<Packet,HashMap<Integer, HashMap<String,int[]>>> combineSeparatedObj() throws Exception {
        String[] folderList = FileIO.getFileName(folderName);
        /******顺便初始化起始时间和结束时间********/
        setBasicPara(folderList[0]);
        HashMap<Packet,HashMap<Integer, HashMap<String,int[]>>> packetStatus = (HashMap<Packet,HashMap<Integer, HashMap<String,int[]>>>) FileIO.readObject(new File(folderName + folderList[0]));
        for(int i = 1;i < folderList.length;i++){
            HashMap<Packet,HashMap<Integer, HashMap<String,int[]>>> packetStatusI = (HashMap<Packet,HashMap<Integer, HashMap<String,int[]>>>) FileIO.readObject(new File(folderName + folderList[i]));
            combineTwoPacketRecorder(packetStatus, packetStatusI);
            setBasicPara(folderList[i]);
        }
        return packetStatus;

    }


    /**
     * 虽然在AllPacketRecorder里面定义了一个包记录器，但是因为保存的对象是HashMap所以那个不建议直接调用，而是在这里调用
     * 调用该函数后，把第二个包对象清除，只保留第一个包对象的内容
     * @param packetStatus2
     */
    private void combineTwoPacketRecorder(HashMap<Packet, HashMap<Integer, HashMap<String, int[]>>> packetStatus,HashMap<Packet, HashMap<Integer, HashMap<String, int[]>>> packetStatus2){
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
}
