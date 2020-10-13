package com.company;

import java.util.LinkedList;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Main {

    static boolean exit = false;
    static LinkedList<String> tokens;

    public static void main(String[] args) {
        parseInput();
    }

    public static void parseTokens() {
        String fst = tokens.get(0).toLowerCase().trim();
        if(fst.equals("play")) {
            if(tokens.size() != 2) {
                System.out.println("The play command must be in the form: play <song>");
                return;
            } else {
                String song = tokens.get(1);
                // TODO play the song
                System.out.println("Played song: "+song);
            }
        } else if(fst.equals("exit") || fst.equals("quit")) {
            exit = true;
        } else {
            System.out.println("Unrecognized command!\n");
        }
    }

    public static void parseInput() {
        tokens = new LinkedList<String>();
        Scanner s = new Scanner(System.in);
        String input = "";
        char c = '\0';

        while(!exit) {
            System.out.print("--> ");
            input = s.nextLine();
            tokens.clear();
            StringTokenizer multiTokenizer = new StringTokenizer(input, " ");
            while (multiTokenizer.hasMoreTokens())
            {
                tokens.add(multiTokenizer.nextToken());
            }
            parseTokens();
        }
    }
}
