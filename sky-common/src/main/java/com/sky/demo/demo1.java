package com.sky.demo;

import java.util.Scanner;

public class demo1 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int n = scanner.nextInt();
        int m = scanner.nextInt(); //一次最多爬 1 到 m 阶之间
        int[] step = new int[m];
        for (int i = 0; i < step.length; i++) {
            step[i] = i + 1; //第0个物品是爬一个台阶
        }
        //dp数组含义，走到几阶有多少种走法，i表示背包容量，也就是还需要爬多少个台阶
        int[] dp = new int[n + 1];
        dp[0] = 1; //初始化
        //遍历
        //因为是有排序的，所以先背包再物品
        for (int i = 1; i < dp.length; i++){
            for(int j = 0; j < step.length; j++){
                if(step[j] <= i){
                    dp[i] = dp[i] + dp[i - step[j]];
                }
            }
        }
        System.out.println(dp[n]);
    }
}
