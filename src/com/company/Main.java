package com.company;

import java.util.LinkedList;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Main {

    static boolean exit = false;
    static LinkedList<String> tokens;

    static DBController db = new DBController();
    static String user = "";

    public static void main(String[] args) {
        parseInput();
    }

    // TODO
    public static void parseTokens() {
        String fst = tokens.get(0).toLowerCase().trim();
        if(fst.equals("play")) {
            if (tokens.size() == 3) {
                String flag = tokens.get(1);
                if(!flag.equals("-id")) {
                    System.out.println("The play command must be in the form: play <song | -id id>");
                }
                String id_str = tokens.get(2);
                int id = -1;
                try {
                    id = Integer.parseInt(id_str);
                } catch (final NumberFormatException e) {
                    System.out.println("Song id must be an integer!");
                    return;
                }
                if(!db.playSong(id)) {
                    System.out.println("Unable to play song with id: "+id_str); // TODO move this
                } else {
                    System.out.println("Played song with id: "+id_str); // TODO move this
                }
            } else if(tokens.size() == 2) {
                String song = tokens.get(1);

                if(!db.playSong(song)) {
                    System.out.println("Unable to play song: "+song); // TODO move this
                } else {
                    System.out.println("Played song: " + song); // TODO move this
                }
            } else {
                System.out.println("The play command must be in the form: play <song | -id id>");
            }
        } else if(fst.equals("list")) {
            // can list albums or artists
            // list with no parameters lists your collection
            if (tokens.size() == 1) {
                //if(user == "") {
                //   System.out.println("You must login before trying to access your collection!");
                // return;
                //}
                if (!db.listCollection(user)) {
                    System.out.println("Unhandled error listing collection for user: " + user);
                }
                return;
            } else if (tokens.size() == 3) {
                String snd = tokens.get(1).toLowerCase().trim();
                String third = tokens.get(2).toLowerCase().trim();
                if (snd.equals("artist")) {
                    System.out.println("Artist: " + third); // TODO maybe move this
                    if (!db.listArtist(third)) {
                        System.out.println("Error listing artist: " + third); // TODO maybe move this error to listArtist
                    }
                } else if (snd.equals("album")) {
                    System.out.println("Album: " + third); // TODO maybe move this
                    if (!db.listAlbum(third)) {
                        System.out.println("Error listing album: " + third); // TODO maybe move this error to listAlbum
                    }
                } else {
                    System.out.println("The list command must be in the form: list <album | artist> <name>");
                }
                return;
            } else if(tokens.size() == 4) {
                if(tokens.get(2).equals("-id")) {
                    String id_str = tokens.get(3);
                    int id = -1;
                    try {
                        id = Integer.parseInt(id_str);
                    } catch (final NumberFormatException e) {
                        System.out.println("Song id must be an integer!");
                    }

                    if(tokens.get(1).equals("album")) {
                        db.listAlbum(id);
                    } else if(tokens.get(1).equals("artist")) {
                        db.listArtist(id);
                    }
                }
            }
            System.out.println("The list command must be in the form: list [<album | artist> <-id id | name>]\n" +
                    "If no album/artist is specified, it will display the logged in user's collection.\n" +
                    "The -id flag can be used to specify an integer id rather than a name.");

        } else if(fst.equals("search")) {
            if(tokens.size() == 2) {
                db.search(tokens.get(1));
            } else if(tokens.size() == 3 && tokens.get(1).equals("collection")) {
                db.searchCollection(user, tokens.get(2));
            } else {
                System.out.println("The search command must be in the form: search [collection] search_term");
            }
        } else if(fst.equals("info")) {
            // NOTE: song, album, artist names can't start with '-' (currently)
            if(tokens.size() == 2 && !tokens.get(1).startsWith("-")) {
                db.dispInfo(tokens.get(1));
                return;
            } else if(tokens.size() == 3) {
                String id_str = tokens.get(2);
                int id = -1;
                try {
                    id = Integer.parseInt(id_str);
                } catch (final NumberFormatException e) {
                    System.out.println("id must be an integer!");
                    return;
                }
                if (tokens.get(1).equals("-song")) {
                    db.dispSongInfo(id);
                    return;
                } else if (tokens.get(1).equals("-artist")) {
                    db.dispArtistInfo(id);
                    return;
                } else if (tokens.get(1).equals("-album")) {
                    db.dispAlbumInfo(id);
                    return;
                }
            }
            System.out.println("The info command can be used in the following ways:");
            System.out.println("\tinfo search_name");
            System.out.println("\tinfo -song song_id");
            System.out.println("\tinfo -album album_id");
            System.out.println("\tinfo -artist artist_id\n");
        } else if(fst.equals("help")) {
            printHelpMessage();
        } else if(fst.equals("exit") || fst.equals("quit") || fst.equals("logout")) {
            exit = true;
        } else {
            System.out.println("Unrecognized command!\n");
        }
    }

    public static void printHelpMessage() {
        // TODO
    }

    public static void parseInput() {
        tokens = new LinkedList<String>();
        Scanner s = new Scanner(System.in);
        String input = "";
        char c = '\0';

        System.out.println("Please Login!");
        System.out.print("username: ");
        user = s.nextLine();
        System.out.println("Welcome: "+user+"\n");

        while(!exit) {
            System.out.print("--> ");
            input = s.nextLine();
            // TODO add input to history?

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