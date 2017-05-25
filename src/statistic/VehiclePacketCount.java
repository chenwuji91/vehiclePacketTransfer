package statistic;

/**
 * Created by Administrator on 2017/5/4 0004.
 * 直接基于notTracedHop的HashMap对象进行统计 主要是统计并且输出当前时刻的全部包的信息
 */

import tools.FileIO;
import transmit.VehicleCarry;

import java.io.File;
import java.util.*;

public class VehiclePacketCount {

    /**
     * 根据对象序列化的结果统计一些基本的变量，统计传输的一些标准
     * 本类主要是统计在当前对象下，有多少的车，每个车上有多少的包。
     * @param args
     */
    private static String folderName = "notTraceHopObj/";
//    private static String fileName = "2016_03_28_init_1000_from_30000_to_30250.obj";
//    private static int beginTime = Integer.valueOf(fileName.split("_from_")[1].split("_to_")[0]);
//    private static int endTime = Integer.valueOf(fileName.split("_to_")[1].split(".obj")[0]);

    public static void main(String args[]) throws Exception {
        String fileName[] = FileIO.getFileName(folderName);
        for(int i = 0;i<fileName.length;i++){
            System.out.println("Processing:" + fileName[i]);
            int beginTime = Integer.valueOf(fileName[i].split("_from_")[1].split("_to_")[0]);
            int endTime = Integer.valueOf(fileName[i].split("_to_")[1].split(".obj")[0]);
            HashMap<String, VehicleCarry> vehicleStatus = (HashMap<String, VehicleCarry>) FileIO.readObject(new File(folderName + fileName[i]));
            vehiclePacketCount(vehicleStatus,beginTime,endTime,fileName[i]);
        }
    }
    /**
     * 统计每个车上的数据包的个数 基于hashset的个数
     * @param vehicleStatus
     */
    private static void vehiclePacketCount(HashMap<String, VehicleCarry> vehicleStatus,int beginTime, int endTime, String outName){
        StringBuffer outputData = new StringBuffer("");
//        String outputFileName = "vehiclePacketTotal_" + vehicleStatus.size() + "_" + String.valueOf(endTime - beginTime);
        String outputFileName = outName.split("obj")[0] + "vehiclePacketCount.csv";
        System.out.println("vehicle in total:" + vehicleStatus.size());
        for(Map.Entry<String, VehicleCarry> e:vehicleStatus.entrySet()) {
            System.out.println(e.getKey() + "," + e.getValue().getTotalPacketSize());
            outputData.append(e.getKey() + "," + e.getValue().getTotalPacketSize() + "\n");
        }
        FileIO.writeToFile(outputFileName, outputData.toString());
    }


}
