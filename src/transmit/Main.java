package transmit;
import tools.FileIO;
import tools.SQL;

import java.io.File;
import java.util.*;
import java.util.Random;

public class Main {
    private static final int beginTime = 36000;//传输开始的时间
    private static final int endTime = 36200; //传输结束的时间
    private static final String processDate = "2016_03_28";  //处理的是哪一天的数据
    private static final int packetTTL = 20; //包存活的时间
    private static boolean traceHop = false; //如果true记录每个包中间所有的跳数，否则只看最后的结果 当前状态是在车内统计包的情况
    private static final int initPacket = 5000; //网络传输在起始时刻随机给车辆分了多少个包
    private static HashSet<String> vehicleID;  //在这段时间内所有会出现的车辆的ID集合
    //private static int saveObjEverySecond = -1; //在一定的时间后保存对象并清楚记录的数据
    private static final boolean useExtraPacketRecorder = true;
    private static final int saveStatusInterval = -1;  //如果为-1 则表示只在清空数据的时候保存状态 保存当前状态并清空内部的包记录器的时间间隔  相当于随时可以查看当前车辆拥有数据包的情况
    private static int cleanMemoryInterval = 20;  //如果是-1，只在最后保存一次。在一定的时间间隔内，清除数据包，将数据包保存到文件，设置过小可能导致短暂消失的车辆数据包丢失
    private static boolean lowMemoryModel = true; //强行清除内存
    /***Notice: 如果要跑记录包个数的实验，将saveStatusInterval设置为一个大于0的数值；建议不设置saveStatusInterval***/
    RuntimeStatusOperation rso;
    Init init;

    Main(){
        if(useExtraPacketRecorder)
            traceHop = false;//如果启用了外部的包记录器 将禁用内部的包记录器
        if(traceHop==false && useExtraPacketRecorder == false){//如果只是统计车辆携带数据包的个数 那么禁止清除包记录器
            cleanMemoryInterval = -1;
            lowMemoryModel = false;
        }
        vehicleID = SQL.getVehicleID(beginTime,endTime,processDate);//获得当前时段所有车辆的集合
        rso = new RuntimeStatusOperation(vehicleID,packetTTL,traceHop, useExtraPacketRecorder,cleanMemoryInterval,processDate); //初始化运行状态维护，包括状态的保存等
        init = new Init(beginTime,endTime,processDate,traceHop,initPacket); //初始化模块，包含车辆列表的初始化和车上数据的初始化
    }
    /**
     *
     * 传输数据主函数，现在根据  直接对于字典进行修改， 每个字典分别记录每个车的相关的信息
     * 核心控制器
     */
    private void packetExchange(HashMap<String, VehicleCarry> allData) throws Exception {
        AllPacketRecorder externalRecorder = null;
        if(useExtraPacketRecorder)//如果需要启动外部的包状态记录器，需要初始化包记录器
            externalRecorder = new AllPacketRecorder(allData,cleanMemoryInterval);
        for (int i = beginTime;i <= endTime; i++){//从开始时刻到结束时刻开始进行传输
            System.out.println("processing:" + i);
            /********************传输逻辑的控制***********************/
//            cleanOldPacket(allData,i); //在传输的时候删除ttl超时的数据包
            String[] connection = FileIO.connection(processDate, i);
            HashMap<String,int[]> positionInfo = SQL.getPositionInfo(processDate,i);
            for(int j=0;j<connection.length;j++){
                String vehicleIDFrom = connection[j].split("-")[0];
                String vehicleIDTo = connection[j].split("-")[1];
                int[] positionVehicleFrom = positionInfo.get(vehicleIDFrom);
                int[] positionVechcleTo = positionInfo.get(vehicleIDTo);
                VehicleCarry vehicleFromData = allData.get(vehicleIDFrom);
                VehicleCarry vehicleToData = allData.get(vehicleIDTo);
                transferPacket(vehicleFromData,vehicleToData,i,vehicleIDTo, positionVechcleTo,externalRecorder);
                //                generatePacket(vehicleToData, vehicleIDFrom, vehicleIDTo, i, positionVechcleTo); //在传输的过程中产生数据包
            }

            /*******************处理状态保存*************************/
            /*
            状态处理原则，先保存，然后清除对象，然后做好衔接，继续进行就可以了
             */
            String outputFile = processDate + "_init_" + initPacket + "_from_" + beginTime + "_to_" + i + ".obj";
            if(saveStatusInterval > 0 && i % saveStatusInterval == 0)
                rso.saveObjAndCleanInternalRecorder(allData,outputFile);//先保存当前的状态

            if(cleanMemoryInterval > 0 && i % cleanMemoryInterval == 0 && useExtraPacketRecorder == true)
            {
                rso.saveObjAndCleanInternalRecorder(allData,outputFile);//先保存当前的状态
                externalRecorder.saveRecorder(outputFile + ".packetrecorder",allData);//保存外部包索引 并丢弃对象
            }
            /*******开始清除并过度状态***********/


            //再清除allData记录器,并建立新的allData
            if(cleanMemoryInterval > 0 && i % cleanMemoryInterval == 0){
                if(lowMemoryModel){//是否开启低内存状态
                    rso.cleanMemoryStrongly(allData,i); //保留原有的数据包，重新记录新的数据包，finalize原来的数据包
                }
                else{
                    rso.cleanMemory(allData,i);
                }
                System.gc();//强制进行垃圾回收
            }

        }
        String outputFile = processDate + "_init_" + initPacket + "_from_" + beginTime + "_to_" + endTime + ".obj";
        rso.saveObjAndCleanInternalRecorder(allData,outputFile);
    }

    /**
     * 产生一个数据包  在碰面的过程中产生了这个数据包
     * @param vehicleToData
     * @param vehicleIDFrom
     * @param vehicleIDTo
     * @param i
     * @param positionVechcleTo
     */
    private void generatePacket(VehicleCarry vehicleToData,String vehicleIDFrom,
                                       String vehicleIDTo,int i, int[] positionVechcleTo){
        vehicleToData.add(new Packet(vehicleIDFrom, vehicleIDTo,i, positionVechcleTo, traceHop),i);
    }

    /**
     * 传输数据包 从一个车到另外一个车， 如果有重复就不传输
     * @param vehicleFromData
     * @param vehicleToData
     * @param currentTime
     * @param toVehicleID
     * @param positionVehicleTo
     * @throws CloneNotSupportedException
     */
    private void transferPacket(VehicleCarry vehicleFromData, VehicleCarry vehicleToData,
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
                if(useExtraPacketRecorder){
                    externalRecorder.addPacketToVehicle(transferredPacket,toVehicleID,currentTime,positionVehicleTo);
                }
                vehicleToData.add(transferredPacket,currentTime);
                alreadyTransferred++;
            }
        }

    }


    public static void main(String[] args) throws Exception {
        //testUnit();
        Main m = new Main();
        HashMap<String, VehicleCarry> allData = m.init.initDict(vehicleID);//获得当前时段所有车辆的字典
        m.init.initPacket(allData);//初始化数据包 随机分配若干数量的数据包

        m.packetExchange(allData);//传输的主函数
//        String outputFile = processDate + "_init_" + initPacket + "_from_" + beginTime + "_to_" + endTime + ".obj";
//        m.rso.saveObjAndCleanInternalRecorder(allData,outputFile);

    }


}



