package transmit;

import tools.FileIO;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;

/**
 * Created by Administrator on 2017/5/15 0015.
 */
public class TestUnit {
    /**
     * 测试单元
     * @throws Exception
     */
    public static void testUnit() throws Exception {
        HashMap<String, LinkedHashSet<Packet>> obj1 =(HashMap<String, LinkedHashSet<Packet>>) FileIO.readObject(new File("test.obj"));
        Packet packet1 = new Packet("123","456",360,new int[]{12,31},true);
        Packet packet2 = new Packet("123","456",360,new int[]{12,31},true);
        System.out.println(packet1.hashCode());
        System.out.println(packet2.hashCode());
        HashSet<Packet> s = new HashSet<>();
        s.add(packet1);
        System.out.println(s.contains(packet2));
        s.add(packet2);
        System.out.println(s.size());
        System.out.println(packet1 == packet2);
        Packet packet3 = (Packet) packet1.clone();
        packet1.addInfo(22,new int[]{1,2},"213");
        System.out.println(s.contains(packet3));
        System.exit(0);
    }
}
