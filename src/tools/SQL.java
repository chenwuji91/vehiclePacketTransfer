package tools;

import java.sql.*;
import java.util.HashSet;
import java.util.HashMap;

/**
 * Created by chenwuji on 2017/4/26.
 */
public class SQL {
    private static String connectionInfo = "jdbc:mysql://192.168.10.3:3306/vehicle?characterEncoding=utf8&useSSL=false";
    private static String userName = "root";
    private static String password = "fuckyou321";
    public static HashSet<String> getVehicleID(int begintime, int endtime, String currentDate){//2016_03_28
        HashSet<String> vehicleID = new HashSet<>();
        Connection con = null;
        Statement sta = null;
        ResultSet res = null;
        String sql = null;
        try{
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(connectionInfo,userName,password);
            sta = con.createStatement();
            sql = new String("select distinct(vid) from vehicle_position_" + currentDate + " where timenow>=" + String.valueOf(begintime) +
                    " and timenow<="+String.valueOf(endtime)+";");
            res = sta.executeQuery(sql);
            while(res.next()){
                vehicleID.add(res.getString("vid"));
            }
        }catch (ClassNotFoundException e){
            System.out.println("ClassNotFoundException");

        }catch (SQLException a){
            a.printStackTrace();
        }
        finally {
            try{
                if(con != null)
                    con.close();
                if (sta != null)
                    sta.close();
                if(res != null)
                    res.close();
            }catch (Exception e){

            }
        }
        System.out.println("vehicle in total:" + vehicleID.size());
        return vehicleID;
    }

    public static HashMap<String,int[]> getPositionInfo(String currentDate,int currentTime){//2016_03_28
        HashMap<String,int[]> positionDict = new HashMap<>();
        Connection con = null;
        Statement sta = null;
        ResultSet res = null;
        String sql = null;
        try{
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(connectionInfo,userName,password);
            sta = con.createStatement();
            sql = new String("select vid,X,Y from vehicle_position_" + currentDate + " where timenow=" + String.valueOf(currentTime) +";");
            res = sta.executeQuery(sql);
            while(res.next()){
                String vid = res.getString(1);
                int X = res.getInt(2);
                int Y = res.getInt(3);
                positionDict.put(vid,new int[]{X,Y});
            }
        }catch (ClassNotFoundException e){
            System.out.println("ClassNotFoundException");

        }catch (SQLException a){
            a.printStackTrace();
        }
        finally {
            try{
                if(con != null)
                    con.close();
                if (sta != null)
                    sta.close();
                if(res != null)
                    res.close();
            }catch (Exception e){

            }
        }
        System.out.println("vehicle position in total:" + positionDict.size());
        return positionDict;
    }
}
