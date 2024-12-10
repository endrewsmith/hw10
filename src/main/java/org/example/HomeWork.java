package org.example;


import lombok.SneakyThrows;

import java.io.*;
import java.util.stream.IntStream;

public class HomeWork {

    /**
     * <h1>Задание 1.</h1>
     * Решить задачу https://codeforces.com/contest/356/problem/A
     */
    @SneakyThrows
    public void championship(InputStream in, OutputStream out) {
        // Создадим красно-черное дерево рыцарей(из вебинара)
        RedBlackTree<Integer> knights = new RedBlackTree<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        PrintWriter pw = new PrintWriter(out);

        String[] strings = br.readLine().split(" ");
        int n = Integer.parseInt(strings[0]);
        int m = Integer.parseInt(strings[1]);

        // Заполним дерево
        for (int i = 1; i <= n; i++) {
            knights.add(i);
        }

        for (int i = 0; i < m; i++) {
            String[] data = br.readLine().split(" ");
            int l = Integer.parseInt(data[0]);
            int r = Integer.parseInt(data[1]);
            int win = Integer.parseInt(data[2]);
            knights.setK(win, IntStream.range(l, r + 1).boxed().toArray(Integer[]::new));
        }

        for (int i = 1; i <= n; i++) {
            pw.print(knights.getByKeyForWinner(i) == null ? 0 : knights.getByKeyForWinner(i));
            if (i < n) {
                pw.print(" ");
            }
        }
        pw.flush();
    }
}
