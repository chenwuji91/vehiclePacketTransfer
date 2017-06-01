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
 * 分开统计每一个的
 */
public class ExternalPacketAnalysisSep {

    /**
     * Main function
     *
     * @param args
     */
    private static final String folderName = "./packetRecorder/";
    private static String outputFileName = "";
    int beginTime;
    int endTime = -1;
    String processDate;
    int initPacket;



    public ExternalPacketAnalysisSep() {

    }

    private void setBasicPara(String oneOfTheFile) {
        int thisEndTime = Integer.valueOf(oneOfTheFile.split("_to_")[1].split(".obj")[0]);
        endTime = thisEndTime;
        beginTime = Integer.valueOf(oneOfTheFile.split("_from_")[1].split("_to_")[0]);
        processDate = oneOfTheFile.split("_init_")[0];
        initPacket = Integer.valueOf(oneOfTheFile.split("_init_")[1].split("_from")[0]);
    }


    public static void main(String args[]) throws Exception {
        ExternalPacketAnalysisSep analy = new ExternalPacketAnalysisSep();
        String[] folderList = analy.folderList();
        for(int i = 0;i<folderList.length;i++){
            HashMap<Packet,HashMap<Integer, HashMap<String,int[]>>> oneResult = (HashMap<Packet,HashMap<Integer, HashMap<String,int[]>>>) FileIO.readObject(new File(folderName + folderList[i]));
            System.out.println("Processing:" + folderList[i]);
            analy.setBasicPara(folderList[i]);
            analy.packetTransmitDistance(oneResult, folderList[i]);
        }

    }

    private void packetTransmitDistance(HashMap<Packet, HashMap<Integer, HashMap<String, int[]>>> oneResult, String outputPath) {
        String folderPath = "./PacketDistanceAnalysisSep/";
        FileIO.createDir(folderPath);
        for (Map.Entry<Packet, HashMap<Integer, HashMap<String, int[]>>> p : oneResult.entrySet()) {
            Packet packet = p.getKey();
            String packetID = packet.hashCode() + "_" + packet.getBornPlace()[0] + "_" + packet.getBornPlace()[1];
            StringBuffer outputStringAvg = new StringBuffer();
            StringBuffer outputStringMax = new StringBuffer();
            outputStringAvg.append(packetID);
            outputStringMax.append(packetID);
            HashMap<Integer, HashMap<String, int[]>> packetStatus = p.getValue();
            for (int i = beginTime; i < endTime; i++) {
                if (packetStatus.containsKey(i)) {
                    HashMap<String, int[]> vehicleDistanceMap = packetStatus.get(i);//依次看每一秒这个包都在哪些车上面
                    float maxDistance = -1;
                    float totalDistance = 0;
                    for (Map.Entry<String, int[]> eachVehicle : vehicleDistanceMap.entrySet()) {
                        float distance = StatisticTools.distanceOfPalces(eachVehicle.getValue(), packet.getBornPlace());
                        if (distance > maxDistance)
                            maxDistance = distance;
                        totalDistance += distance;
                    }
                    outputStringAvg.append("," + totalDistance / vehicleDistanceMap.size());
                    outputStringMax.append("," + maxDistance);
                } else {
                    outputStringAvg.append("," + -1);
                    outputStringMax.append("," + -1);
                }
            }
            outputStringAvg.append("\n");
            outputStringMax.append("\n");
            FileIO.writeWithAppend(folderPath + outputPath + "_avg.csv", outputStringAvg.toString());
            FileIO.writeWithAppend(folderPath + outputPath + "_max.csv", outputStringMax.toString());
        }
    }

    /**
     * 读取整个目录的对象并整个 返回整个的对象
     *
     * @return 完整的包记录器，记录每个时间所有数据包的位置
     * @throws Exception
     */
    private String[] folderList() throws Exception {
        String[] folderList = FileIO.getFileName(folderName);
        /******顺便初始化起始时间和结束时间********/
        return folderList;
    }

}

