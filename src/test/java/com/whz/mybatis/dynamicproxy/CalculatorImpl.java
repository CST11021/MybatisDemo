package com.whz.mybatis.dynamicproxy;

public class CalculatorImpl implements Calculator {
    @Override
    public int add(int a, int b) {
        System.out.println("执行add方法");
        return a + b;
    }

    @Override
    public int sub(int a, int b) {
        System.out.println("执行sub方法");
        return a - b;
    }
}