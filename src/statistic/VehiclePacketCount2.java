package statistic;

import tools.FileIO;
import transmit.VehicleCarry;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenwuji<chenwuji@mail.ustc.edu.cn> on 2017/5/25 0025.
 * 本类可以提供统计包内每一秒数据信息的类  可以统计出每一秒 每个车上面新获得的包的情况
 * 本程序直接基于运行时候内部包的记录器，记录实时的包的数量，可以看到每一秒每辆车上面的数据包
 */
public class VehiclePacketCount2 {

    private static String folderName = "./notTraceHopObj/";
    private static boolean showAcclu = true;

    public static void main(String args[]) throws Exception {
        String fileName[] = FileIO.getFileName(folderName);
        for(int i = 0;i<fileName.length;i++) {
            System.out.println("Processing:" + fileName[i]);
            int beginTime = Integer.valueOf(fileName[i].split("_from_")[1].split("_to_")[0]);
            int endTime = Integer.valueOf(fileName[i].split("_to_")[1].split(".obj")[0]);
            HashMap<String, VehicleCarry> vehicleStatus = (HashMap<String, VehicleCarry>) FileIO.readObject(new File(folderName + fileName[i]));
            vehiclePacketCount(vehicleStatus, beginTime, endTime, fileName[i]);
        }
    }

    private static void vehiclePacketCount(HashMap<String, VehicleCarry> vehicleStatus,int beginTime, int endTime, String outName){
//        String outputFileName = "vehiclePacketTotal_" + vehicleStatus.size() + "_" + String.valueOf(endTime - beginTime);
        String outputFileName = outName.split("obj")[0] + "vehiclePacketCount_"+ showAcclu+ ".csv";
        System.out.println("vehicle in total:" + vehicleStatus.size());
        for(Map.Entry<String, VehicleCarry> e:vehicleStatus.entrySet()) {
            StringBuffer outputData = new StringBuffer("");
            String vehicleID = e.getKey();
            outputData.append(vehicleID);
            HashMap<Integer, Integer> realTimePacketCount = e.getValue().getPacketCount();
            int intotal = 0;
            for(int i = beginTime;i<endTime+1;i++){
                if(showAcclu){
                    if(realTimePacketCount.containsKey(i)){
                        intotal += realTimePacketCount.get(i);
                    }
                    outputData.append("," + intotal);
                }
                else{
                    if(realTimePacketCount.containsKey(i)){
                        outputData.append("," + realTimePacketCount.get(i));
                    }
                    else{
                        outputData.append(",0");
                    }
                }

            }
            outputData.append("\n");
            FileIO.writeWithAppend(outputFileName, outputData.toString());
        }
    }
}
