package fansirsqi.xposed.sesame.util;

/**平均值计算工具类*/
public class Average {

    /** 使用一个循环队列来存储固定数量的数值*/
    private final CircularFifoQueue<Integer> queue;

    /** 数值的总和，用于计算平均值*/
    private double sum;

    /** 当前的平均值*/
    private double average;

    /** 构造函数，初始化队列大小，初始总和和平均值*/
    public Average(int size) {
        this.queue = new CircularFifoQueue<>(size); // 创建一个固定大小的循环队列
        this.sum = 0.0; // 初始化总和为 0
        this.average = 0.0; // 初始化平均值为 0
    }

    /**
     * 计算下一个数值加入后的新平均值
     *
     * @param value 新加入的数值
     * @return 当前的平均值
     */
    public double nextDouble(int value) {
        // 将新值添加到队列中，并移除队列中的旧值（如果有的话）
        Integer last = queue.push(value);

        // 如果队列中有旧值，则从总和中减去它
        if (last != null) {
            sum -= last;
        }

        // 将新值加入到总和中
        sum += value;

        // 计算并返回新的平均值
        return average = sum / queue.size();
    }

    /**
     * 计算下一个数值加入后的新平均值（返回整数）
     *
     * @param value 新加入的数值
     * @return 当前的平均值（整数）
     */
    public int nextInteger(int value) {
        // 使用 nextDouble 方法计算平均值，然后强制转换为整数
        return (int) nextDouble(value);
    }

    /**
     * 获取当前的平均值（浮动型）
     *
     * @return 当前的平均值
     */
    public double averageDouble() {
        return average;
    }

    /**
     * 获取当前的平均值（整数型）
     *
     * @return 当前的平均值（整数）
     */
    public int getAverageInteger() {
        return (int) average;
    }

    /**
     * 清除队列和重置所有统计数据
     */
    public void clear() {
        // 清空队列
        queue.clear();
        // 重置总和和平均值
        sum = 0.0;
        average = 0.0;
    }

}
