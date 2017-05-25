package tools;
/**
 * Created by chenwuji on 2017/4/26.
 */
import java.io.*;


public class FileIO {

    /**
     * 读取filePath的文件，将文件中的数据按照行读取到String数组中
     * @param filePath    文件的路径
     * @return            文件中一行一行的数据
     */
    private static final String BASEPATH = "E:/transmit/connection_file/";
    FileIO(){
        createDir("./packetRecorder");
        createDir("./notTraceHopObj/");
        createDir("./traceHopObj/");
    }

    public static String [] getFileName(String path)
    {
        File file = new File(path);
        String [] fileName = file.list();
        return fileName;
    }

    public static String[] connection(String currentDate, int currenttime){
        String meetingPair = readToString(currentDate, currenttime);
        return meetingPair.split(",");
    }
    private static String readToString(String currentDate, int current_time1)
    {
        currentDate = currentDate.replace('_','-');
        int hour1 = current_time1 / 3600;
        int min1 = current_time1 % 3600 / 60;
        int seconds1 = current_time1 % 3600 % 60;
        String hour = String.valueOf(hour1);
        String min = String.valueOf(min1);
        String second = String.valueOf(seconds1);
        if(hour1<10)
            hour = "0" + hour;
        if(min1<10)
            min = "0" + min;
        if(seconds1<10)
            second = "0" + second;
        String current_time =hour  + "_" +  min + "_" + second;
        System.out.println(current_time);
        File file = new File(BASEPATH + currentDate + "/" + currentDate + " " + current_time);
        Long filelength = file.length(); // 获取文件长度
        byte[] filecontent = new byte[filelength.intValue()];
        try
        {
            FileInputStream in = new FileInputStream(file);
            in.read(filecontent);
            in.close();
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        String[] fileContentArr = new String(filecontent).split("\n");
//        System.out.println(fileContentArr[0]);
        return fileContentArr[0];// 返回文件内容,默认编码
    }

    /**
     *将对象序列化到磁盘文件中
     *@paramo
     *@throwsException
     */
    public static void writeObject(Object o,String fileName) throws Exception{
        File f = new File(fileName);
        if(f.exists()){
            f.delete();
        }
        FileOutputStream os=new FileOutputStream(f);
        //ObjectOutputStream 核心类
        ObjectOutputStream oos=new ObjectOutputStream(os);
        oos.writeObject(o);
        oos.close();
        os.close();
    }


    /**
     *反序列化,将磁盘文件转化为对象
     *@paramf
     *@return
     *@throwsException
     */

    public static Object readObject(File f) throws Exception{
        InputStream is=new FileInputStream(f);
        //ObjectOutputStream 核心类
        ObjectInputStream ois=new ObjectInputStream(is);
        return ois.readObject();

    }

    /**
     * 使用bufferedWriter写入文件，将数据写入到磁盘
     * @param fileName
     */
    public static void writeToFile(String fileName, String writeData)
    {
           try
           {
               BufferedWriter out=new BufferedWriter(new FileWriter(fileName));
               out.write(writeData);
               out.close();
           } catch (IOException e)
           {
               e.printStackTrace();
           }
       }

    public static void writeWithAppend(String fileName, String writeData)
    {
        try
        {
            //使用这个构造函数时，如果存在kuka.txt文件，
            // 则直接往kuka.txt中追加字符串
            FileWriter writer=new FileWriter(fileName,true);
            writer.write(writeData);
            writer.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    public static boolean createDir(String destDirName) {
        File dir = new File(destDirName);
        if (dir.exists()) {
            System.out.println("创建目录" + destDirName + "失败，目标目录已经存在");
            return false;
        }
        if (!destDirName.endsWith(File.separator)) {
            destDirName = destDirName + File.separator;
        }
        //创建目录
        if (dir.mkdirs()) {
            System.out.println("创建目录" + destDirName + "成功！");
            return true;
        } else {
            System.out.println("创建目录" + destDirName + "失败！");
            return false;
        }
    }

    public static void main(String args[]){
//        writeToFile("test.txt","fuckyou");
//        writeWithAppend("test.txt","fuckyou");
//        readToString("2016_03_28",360);
        String filelist[] = getFileName("notTraceHopObj/");
        for(int i = 0;i<filelist.length;i++){
            System.out.println(filelist[i]);
        }
    }
}


