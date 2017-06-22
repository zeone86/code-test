package org.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by zhaohu on 2017/6/20.
 */
public class RPNCalculator {
    private Stack<Object> stack = new Stack<>();
    private static Set<String> operatorToken = new HashSet<>();
    private Map<Integer, Integer> opMark = new HashMap<>();

    static {
        operatorToken.add("+");
        operatorToken.add("-");
        operatorToken.add("*");
        operatorToken.add("/");
        operatorToken.add("sqrt");
    }

    RPNCalculator() {
    }

    public void readInput(String input) {
        String[] inputParams = input.split(" ");
        for (int i = 0; i < inputParams.length; i++) {
            if (inputParams[i].trim().equals(""))
                continue;
            String curParam = inputParams[i].trim().toLowerCase();
            BigDecimal num = getNum(curParam);
            boolean isValid = true;
            if (num != null) {
                stack.push(num);
            } else if (operatorToken.contains(curParam)) {
                opMark.put(stack.size(), i);
                stack.push(curParam);
            } else if (curParam.equals("undo")) {
                if (opMark.containsKey(stack.size())) {
                    opMark.remove(stack.size());
                }
                if (stack.size() > 0)
                    stack.pop();

            } else if (curParam.equals("clear")) {
                clearCommand();
            } else {
                isValid = false;
            }

            if (!isValid)
                showError(String.format("invalid input %s", curParam));
        }
    }

    public void runCommand() {
        if (opMark.size() == 0) {
            showStack();
        } else {
            Stack<Object> curStack = new Stack<>();
            String op = null;
            BigDecimal second = null;
            int i = 0;
            int stackSize = stack.size();
            try {
                for (; i < stackSize; i++) {
                    if (!opMark.containsKey(i)) {
                        curStack.push(stack.get(i));
                    } else {
                        op = (String) stack.get(i);

                        BigDecimal result;
                        if (op.equals("sqrt")) {
                            BigDecimal param = (BigDecimal) curStack.pop();
                            result = eval(param, null, op);
                        } else {
                            second = (BigDecimal) curStack.pop();
                            BigDecimal first = (BigDecimal) curStack.pop();
                            result = eval(first, second, op);
                            second = null;
                        }
                        if (result == null) {
                            throw new RuntimeException("invalid result");
                        }
                        curStack.push(result);
                    }

                }
                stack = curStack;
                opMark.clear();
                showStack();
            } catch (EmptyStackException ex) {
                if (second != null)
                    curStack.push(second);
                stack = curStack;
                showWarn(op, opMark.get(i) + 1);
                opMark.clear();
                showStack();
            }
        }
    }

    private void showWarn(String op, Integer idx) {
        System.out.println(String.format("operator %s (position: %d):  insufficient parameters",
                op, idx));
    }

    private BigDecimal eval(BigDecimal first, BigDecimal second, String op) {
        if (op.equals("+")) {
            return first.add(second);
        } else if (op.equals("-")) {
            return first.subtract(second);
        } else if (op.equals("*")) {
            return first.multiply(second);
        } else if (op.equals("/")) {
            try {
                return first.divide(second);
            } catch (ArithmeticException ex) {
                return first.divide(second, 15, BigDecimal.ROUND_DOWN);
            }

        } else if (op.equals("sqrt")) {
            Double result = Math.sqrt(first.doubleValue());
            return new BigDecimal(result.toString());
        } else {
            return null;
        }

    }

    protected void showStack() {
        StringBuilder msg = new StringBuilder();
        for (Object curItem : stack) {
            if (curItem instanceof BigDecimal) {
                String numStr = ((BigDecimal) curItem).toPlainString();
                int scale = ((BigDecimal) curItem).scale();
                if (scale > 10) {
                    numStr = new BigDecimal(numStr).setScale(10, BigDecimal.ROUND_DOWN).toPlainString();
                }
                msg.append(numStr).append(" ");
            } else {
                msg.append(curItem.toString()).append(" ");
            }
        }

        System.out.println(String.format("stack: %s", msg.toString().trim()));
    }

    protected void clearCommand() {
        stack.clear();
        opMark.clear();
    }

    protected void showError(String msg) {
        System.out.println(String.format("%s", msg));
    }

    protected BigDecimal getNum(String input) {
        try {
            return new BigDecimal(input);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static void main(String[] args) {

        RPNCalculator cc = new RPNCalculator();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            while (true) {
                String input = br.readLine();
                cc.readInput(input);
                cc.runCommand();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
