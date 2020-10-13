package com.company;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        parseInput();
    }

    public static void parseToken(String token) {
        token = token.toLowerCase().trim();
        if(token.startsWith("play")) {
            // do stuff, then more stuff
        } else {
            System.out.println("Unrecognized command!\n");
        }
    }

    public static void parseInput() {
        Scanner s = new Scanner(System.in);
        String token = "";
        char c = '\0';

        boolean exit = false;
        while(!exit) {
            System.out.print("--> ");
            token = s.nextLine();
            parseToken(token);
        }
    }
}
