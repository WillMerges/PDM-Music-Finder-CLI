package com.company;

import java.util.LinkedList;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Main {

    static boolean exit = false;
    static LinkedList<String> tokens;

    static DBController db = new DBController();

    public static void main(String[] args) {
        parseInput();
    }

    public static void parseTokens() {
        String fst = tokens.get(0).toLowerCase().trim();
        if(fst.equals("play")) {
            if (tokens.size() != 2) {
                System.out.println("The play command must be in the form: play <song>");
                return;
            } else {
                String song = tokens.get(1);
                if(!db.playSong(song)) {
                    System.out.println("Unable to play song: "+song);
                } else {
                    System.out.println("Played song: " + song);
                }
            }
        } else if(fst.equals("list")) {
            // can list albums or artists
            if(tokens.size() != 3) {
                System.out.println("The list command must be in the form: list <album | artist> <name>");
                return;
            } else {
                String snd = tokens.get(1).toLowerCase().trim();
                String third = tokens.get(2).toLowerCase().trim();
                if(snd.equals("artist")) {
                    System.out.println("Artist: "+third);
                    // TODO list artists' albums/songs
                } else if(snd.equals("album")){
                    System.out.println("Album: "+third+" By: TODO"); // TODO get artist of song
                    // TODO list songs in album
                } else {
                    System.out.println("The list command must be in the form: list <album | artist> <name>");
                }
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
            // TODO add input to history

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
