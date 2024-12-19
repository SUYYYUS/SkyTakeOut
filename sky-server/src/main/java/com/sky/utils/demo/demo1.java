package com.sky.utils.demo;

import java.util.Arrays;
import java.util.Scanner;

public class demo1 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int bag = scanner.nextInt();
        int n = scanner.nextInt();

        int[] weight = new int[n]; //物品重量
        for(int i = 0; i < n; i++) weight[i] = scanner.nextInt();

        int[] value = new int[n]; //物品价值
        for(int i = 0; i < n; i++) value[i] = scanner.nextInt();

        int[] k = new int[n]; //物品数量
        for(int i = 0; i < n; i++) k[i] = scanner.nextInt();

        //dp数组含义：装满当前容量最多有多少价值
        int[] dp = new int[bag + 1];
        //初始化
        Arrays.fill(dp, 0);
        //变成01背包来处理
        //先物品
        for(int i = 0; i < n; i++){
            //再背包倒序
            for(int j = bag; j >= weight[i]; j--){
                //把每一个物品的数量拆成一个一个的
                for(int z = 1; z <= k[i] && (j - z * weight[i] >= 0); z++){
                    dp[j] = Math.max(dp[j], dp[j - z * weight[i] ] + z * value[i]);
                }
            }
        }
        System.out.println(dp[bag]);
    }
}
