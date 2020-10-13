package com.company;

import java.util.Scanner;

public class Main {


    static boolean exit = false;

    public static void main(String[] args) {
        parseInput();
    }

    public static void parseToken(String token) {
        token = token.toLowerCase().trim();
        if(token.startsWith("play")) {
            // do stuff, then more stuff
        } else if(token.equals("exit") || token.equals("quit")) {
            exit = true;
        } else {
            System.out.println("Unrecognized command!\n");
        }
    }

    public static void parseInput() {
        Scanner s = new Scanner(System.in);
        String token = "";
        char c = '\0';

        while(!exit) {
            System.out.print("--> ");
            token = s.nextLine();
            parseToken(token);
        }
    }
}
