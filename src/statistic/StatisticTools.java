package statistic;

/**
 * Created by Administrator on 2017/5/7 0007.
 * 统计工具类，计算一个数组的最大值最小值标准差等等
 */
public class StatisticTools {
    public static void main(String[] args) {
        Float [] testData=new Float[]{new Float(1),new Float(2),new Float(3),new Float(4)};
        System.out.println("最大值："+getMax(testData));
        System.out.println("最小值："+getMin(testData));
        System.out.println("计数："+getCount(testData));
        System.out.println("求和："+getSum(testData));
        System.out.println("求平均："+getAverage(testData));
        System.out.println("方差："+getVariance(testData));
        System.out.println("标准差："+getStandardDiviation(testData));

    }
    /**
     * 求给定双精度数组中值的最大值
     *
     * @param inputData
     *            输入数据数组
     * @return 运算结果,如果输入值不合法，返回为-1
     */
    public static Float getMax(Float[] inputData) {
        if (inputData == null || inputData.length == 0)
            return new Float(-1);
        int len = inputData.length;
        Float max = inputData[0];
        for (int i = 0; i < len; i++) {
            if (max < inputData[i])
                max = inputData[i];
        }
        return max;
    }
    /**
     * 求求给定双精度数组中值的最小值
     *
     * @param inputData
     *            输入数据数组
     * @return 运算结果,如果输入值不合法，返回为-1
     */
    public static Float getMin(Float[] inputData) {
        if (inputData == null || inputData.length == 0)
            return new Float(-1);
        int len = inputData.length;
        Float min = inputData[0];
        for (int i = 0; i < len; i++) {
            if (min > inputData[i])
                min = inputData[i];
        }
        return min;
    }
    /**
     * 求给定双精度数组中值的和
     *
     * @param inputData
     *            输入数据数组
     * @return 运算结果
     */
    public static Float getSum(Float[] inputData) {
        if (inputData == null || inputData.length == 0)
            return new Float(-1);
        int len = inputData.length;
        Float sum = new Float(0);
        for (int i = 0; i < len; i++) {
            sum = sum + inputData[i];
        }
        return sum;
    }
    /**
     * 求给定双精度数组中值的数目
     *
     * @param inputData 输入数据数组
     * @return 运算结果
     */
    public static int getCount(Float[] inputData) {
        if (inputData == null)
            return -1;
        return inputData.length;
    }
    /**
     * 求给定双精度数组中值的平均值
     *
     * @param inputData
     *            输入数据数组
     * @return 运算结果
     */
    public static Float getAverage(Float[] inputData) {
        if (inputData == null || inputData.length == 0)
            return new Float(-1);
        int len = inputData.length;
        Float result;
        result = getSum(inputData) / len;

        return result;
    }
    /**
     * 求给定双精度数组中值的平方和
     *
     * @param inputData 输入数据数组
     * @return 运算结果
     */
    public static Float getSquareSum(Float[] inputData) {
        if(inputData==null||inputData.length==0)
            return new Float(-1);
        int len=inputData.length;
        Float sqrsum = new Float(0);
        for (int i = 0; i <len; i++) {
            sqrsum = sqrsum + inputData[i] * inputData[i];
        }

        return sqrsum;
    }
    /**
     * 求给定双精度数组中值的方差
     *
     * @param inputData
     *            输入数据数组
     * @return 运算结果
     */
    public static Float getVariance(Float[] inputData) {
        int count = getCount(inputData);
        Float sqrsum = getSquareSum(inputData);
        Float average = getAverage(inputData);
        Float result;
        result = (sqrsum - count * average * average) / count;
        return result;
    }
    /**
     * 求给定双精度数组中值的标准差
     *
     * @param inputData
     *            输入数据数组
     * @return 运算结果
     */
    public static Float getStandardDiviation(Float[] inputData) {
        Float result;
        //绝对值化很重要
        result = new Float(Math.sqrt(Math.abs(getVariance(inputData))));
        return result;
    }


    public static float distanceOfPalces(int[] place1, int[] place2){
        float distance = (float)(Math.sqrt(Math.pow((Double.valueOf(place1[0]) - Double.valueOf(place2[0])),2.0)
                + Math.pow((Double.valueOf(place1[1]) - Double.valueOf(place2[1])),2.0)) * 5);
        return distance;
    }
}