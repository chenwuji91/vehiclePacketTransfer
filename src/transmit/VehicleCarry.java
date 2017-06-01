package transmit;

import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;

import java.io.Serializable;
import java.util.*;

/**
 * Created by Administrator on 2017/5/3 0003.
 */
public class VehicleCarry implements Serializable {
    LinkedHashSet<Packet> packetList;
    HashMap<Integer,Integer> packetCount;

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

    /**
     * 清除
     * @param ttl
     * @param currentTime
     */
    public void cleanDeadPacket(int ttl, int currentTime){
        Iterator<Packet> it = packetList.iterator();
        packetList.removeIf(p->(currentTime - p.getAttainTime()) > ttl);
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
