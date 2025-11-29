package org.example;


import java.util.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        int cntThreads = 3;

        Scanner scanner = new Scanner(System.in);
        String str;
        myList list = new myList();
        List<Node> arrayList = Collections.synchronizedList(new ArrayList<>());
        int cntElems = 0;
        do {
            str = scanner.nextLine();
            if (!str.equals(""))  {
                list.add(new Node(str));
                arrayList.addFirst(new Node(str));
                cntElems++;
            }
        } while (!str.equals(""));

        for (Node node : list) {
            System.out.println(node.str);
        }

        System.out.println("\n\n");
        Sorter sorter = new Sorter(list, cntElems);
        sorter.start(cntThreads);

        System.out.println("\n\n");
        ArrayListSorter arrayListSorter = new ArrayListSorter(arrayList, cntElems);
        arrayListSorter.start(cntThreads);
        ;
    }
}