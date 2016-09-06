package com.bj.loadbalance;

/**
 * 加权轮询调度
 *
 * @author 谢俊权
 * @create 2016/5/6 14:28
 */
public class WRRLoadBalance implements LoadBalance {

    private int lastServerIndex = 0;

    private int serverSize;

    private int[] weights;

    private int currentWeight = 0;

    public WRRLoadBalance(int[] weights) {
        this.weights = weights;
        this.serverSize = weights.length;
    }

    @Override
    public int getIndex(String key) {
        int maxWeight = max();
        int gcd = gcd();
        while(true && this.serverSize > 0) {
            this.lastServerIndex = (this.lastServerIndex + 1) % this.serverSize;
            if(this.lastServerIndex == 0){
                this.currentWeight = this.currentWeight - gcd;
                if(this.currentWeight <= 0){
                    this.currentWeight = maxWeight;
                    if(this.currentWeight == 0)
                        return 0;
                }
            }
            if(this.weights[this.lastServerIndex] >= this.currentWeight)
                return this.lastServerIndex;
        }
        return 0;
    }

    protected int max(){
        int maxWeight = 0;
        for(int i = 0; i < this.serverSize; i++){
            int weight = this.weights[i];
            if(weight > maxWeight){
                maxWeight = weight;
            }
        }
        return maxWeight;
    }

    protected int gcd(){
        int gcd = 0;
        if(this.weights.length > 0){
            gcd = this.weights[this.lastServerIndex];
            for(int i = 0; i < this.weights.length - 1; i++){
                int temp = gcdBetweenTwoNum(this.weights[i], this.weights[i+1]);
                if(gcd > temp){
                    gcd = temp;
                }
            }
        }
        return gcd;
    }

    protected int gcdBetweenTwoNum(int numa, int numb){
        if(numa < numb){
            int temp = numa;
            numa = numb;
            numb = temp;
        }
        while(numb != 0){
            int temp = numa % numb;
            numa = numb;
            numb = temp;
        }
        return numa;
    }

    public static void main(String[] args){

        int[] times = new int[]{0,0,0};
        WRRLoadBalance wrr = new WRRLoadBalance(new int[]{4, 8, 12});
        for(int i = 0; i < 100; i++){
            int index = wrr.getIndex(null);
            times[index]++;
        }
        System.out.println(times[0] + "," + times[1] + "," + times[2]);
    }
}
