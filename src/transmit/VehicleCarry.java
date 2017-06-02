package transmit;

import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;

import java.io.Serializable;
import java.util.*;

/**
 * Created by Administrator on 2017/5/3 0003.
 */
public class VehicleCarry implements Serializable {
    private LinkedHashSet<Packet> packetList;
    private HashMap<Integer,Integer> packetCount;
    VehicleCarry(){
        packetList = new LinkedHashSet<Packet>();
        packetCount = new HashMap<>();
    }

    public HashMap<Integer, Integer> getPacketCount() {
        return packetCount;
    }

    /**
     * 删除这个车上面的所有数据包
     */
    public void clearVehicle(){
        packetList.clear();
        packetCount.clear();
        packetList = null;
        packetCount = null;

    }

//    /**
//     * 清除
//     * @param ttl
//     * @param currentTime
//     */
//    @Deprecated
//    public void cleanDeadPacket(int ttl, int currentTime){
//        Iterator<Packet> it = packetList.iterator();
//        packetList.removeIf(p->(currentTime - p.getAttainTime()) > ttl);
//    }

    /***
     * 清除这个车上面超过ttl的数据包 这个程序必须每秒都执行一次 都则会出现计数异常
     * 20170601修改
     * @param ttl
     * @param currentTime
     */
    public void cleanDeprecatedPackets(int ttl, int currentTime){
        int cleanTimeOfpackets = currentTime - ttl;
        if(packetCount.containsKey(cleanTimeOfpackets)){
            int removePacketCount = packetCount.get(cleanTimeOfpackets);
            Packet[] deadPacket = new Packet[removePacketCount];
            Iterator<Packet> it = packetList.iterator();
            while(it.hasNext())
            {
                deadPacket[--removePacketCount] = it.next();
            }
            for(int i = 0;i < deadPacket.length;i++){
                packetList.remove(deadPacket[i]);
            }
            packetCount.remove(cleanTimeOfpackets);
        }
    }


    public LinkedHashSet<Packet> getPacketList(){
        return this.packetList;
    }

    public int getTotalPacketSize(){
        return packetList.size();
    }

    public int getPacketNum(int queryTime){
        if(packetCount.containsKey((Integer) queryTime)){
            return packetCount.get(queryTime);
        }
        else
            return 0;
    }

    public boolean contains(Packet packet){
        return packetList.contains(packet);
    }

//    public void add(Packet packet){
//        packetList.add(packet);
//    }

    public void add(Packet packet, int time){
        packetList.add(packet);
        if(packetCount.containsKey((Integer) time)){
            packetCount.replace(time, packetCount.get(time) + 1);
        }
        else
            packetCount.put(time,1);
    }

    public Iterator<Packet> iterator(){
        return packetList.iterator();
    }

    @Override
    public void finalize() throws Throwable {
//        clearVehicle();
        try {
            super.finalize();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public static void main(String args[]){
        System.out.println("车的载体");
    }

}
