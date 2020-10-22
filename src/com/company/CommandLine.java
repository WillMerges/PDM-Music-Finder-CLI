package com.company;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.StringTokenizer;

public class CommandLine {

    static boolean exit = false;
    static LinkedList<String> tokens;
    Scanner scanner;

    static DBController db;
    static String user = "";
    String input;

    public CommandLine() {
        db = new DBController();
        tokens = new LinkedList<String>();
        scanner = new Scanner(System.in);
        input = "";
    }

    public void run() {
        System.out.println("Welcome! Please enter your username to log in.");
        System.out.print("Username: ");
        user = scanner.nextLine();

        System.out.println("Welcome, " + user + "!");
        if (!db.userExists(user)) {
            System.out.println("It seems like this is your first time here, so let's get you set up!");
            db.createUser(user);
            System.out.println("Setup complete!");
        }

        System.out.println("Please enter a command. For a list of possible commands, type 'help', and hit Enter.");

        while(!exit) {
            System.out.print("--> ");
            try {
                input = scanner.nextLine();
            } catch(NoSuchElementException e) { // EOF
                return;
            }

            tokens.clear();
            StringTokenizer multiTokenizer = new StringTokenizer(input, " ");
            while (multiTokenizer.hasMoreTokens())
            {
                tokens.add(multiTokenizer.nextToken());
            }
            parseTokens();
        }
    }

    private void parseTokens() {
        String first = tokens.get(0).toLowerCase().trim();

        switch (first) {
            case "import":
                importOptions();
                break;
            case "add":
                addOptions();
                break;
            case "play":
                playOptions();
                break;
            case "list":
                listOptions();
                break;
            case "search":
                searchOptions();
                break;
            case "info":
                infoOptions();
                break;
            case "help":
                printHelpMessage();
                break;
            case "exit":
            case "quit":
            case "logout":
                exit = true;
                break;
            default:
                System.out.println(first + " is not a recognized command.");
        }
    }

    private void importOptions() {
        if(tokens.size() == 2) {
            String modifier = tokens.get(1);

            switch (modifier) {
                case "song":
                    db.importSong();
                    break;
                case "artist":
                    db.importArtist();
                    break;
                case "album":
                    db.importAlbum();
                    break;
                default:
                    System.out.println(modifier + " is not a recognized modifier for the input command.");
            }
        }
        else {
            importHelp();
        }

    }

    private void addOptions() {
        if(tokens.size() == 3) {
            String id_str = tokens.get(2);
            String modifier = tokens.get(1);
            int id = -1;
            try {
                id = Integer.parseInt(id_str);
            } catch (final NumberFormatException e) {
                System.out.println("id must be an integer!");
                return;
            }

            switch (modifier) {
                case "-song":
                    db.addSong(user, id);
                    break;
                case "-album":
                    db.addAlbum(user, id);
                    break;
                case "-artist":
                    db.addArtist(user, id);
                    break;
                default:
                    System.out.println(modifier + " is not a recognized modifier for the add command");
                    break;
            }
        }
        else {
            addHelp();
        }
    }

    private void playOptions() {
        if (tokens.size() == 3) {
            String flag = tokens.get(1);
            if(!flag.equals("-id")) {
                playHelp();
            }
            String id_str = tokens.get(2);
            int id = -1;
            try {
                id = Integer.parseInt(id_str);
            } catch (final NumberFormatException e) {
                System.out.println("Song id must be an integer!");
                return;
            }
            db.playSong(id, user);
        } else if(tokens.size() == 2 && !tokens.get(1).equals("-id")) {
            String song = tokens.get(1);
            db.playSong(song, user);
        } else {
            playHelp();
        }
    }

    private void listOptions() {
        // can list albums or artists
        // list with no parameters lists your collection
        if (tokens.size() == 1) {
            db.listCollection(user);
            return;
        } else if (tokens.size() == 3) {
            String second = tokens.get(1).toLowerCase().trim();
            String third = tokens.get(2).toLowerCase().trim();
            if (second.equals("artist")) {
                db.listArtist(third);
                return;
            } else if (second.equals("album")) {
                db.listAlbum(third);
                return;
            }
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
                    return;
                } else if(tokens.get(1).equals("artist")) {
                    db.listArtist(id);
                    return;
                }
            }
        }
        listHelp();
    }

    private void searchOptions() {
        if(tokens.size() == 2) {
            db.search(tokens.get(1));
        } else if(tokens.size() == 3 && tokens.get(1).equals("collection")) {
            db.searchCollection(user, tokens.get(2));
        } else {
            searchHelp();
        }
    }

    private void infoOptions() {
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

        //Seems like this belongs more in the help message to me
        infoHelp();
    }

    private void printHelpMessage() {
        if(tokens.size() == 1) {
            showBasicHelp();
        }
        else if(tokens.size() == 2) {
            showAdvHelp();
        } else {
            System.out.println("Help command only takes 0 or 1 arguments.");
        }
    }

    private void showBasicHelp() {
        System.out.println("Possible commands are:");
        System.out.println("\tplay");
        System.out.println("\tinfo");
        System.out.println("\tlist");
        System.out.println("\tsearch");
        System.out.println("\tadd");
        System.out.println("\timport");
        System.out.println("\texit, quit, or logout");
        System.out.println("\thelp (shows this menu)");
        System.out.println("\nuse 'help [command]' for more information about a particular command.");
    }

    private void showAdvHelp() {
        String modifier = tokens.get(1);

        switch (modifier) {
            case "play":
                playHelp();
                break;
            case "info":
                infoHelp();
                break;
            case "list":
                listHelp();
                break;
            case "search":
                searchHelp();
                break;
            case "add":
                addHelp();
                break;
            case "import":
                importHelp();
                break;
            case "exit":
            case "quit":
            case "logout":
                exitHelp();
                break;
            default:
                System.out.println(modifier + " is not a recognized command. Can't help you there.");
                break;
        }
    }

    private void playHelp() {
        System.out.println("The play command must be in the form: play <song | -id id>");
    }

    private void infoHelp() {
        System.out.println("The info command can be used in the following ways:");
        System.out.println("\tinfo search_name");
        System.out.println("\tinfo -song song_id");
        System.out.println("\tinfo -album album_id");
        System.out.println("\tinfo -artist artist_id");
    }

    private void listHelp() {
        System.out.println("The list command must be in the form: list [<album | artist> <-id id | name>]\n" +
                "If no album/artist is specified, it will display the logged in user'scanner collection.\n" +
                "The -id flag can be used to specify an integer id rather than a name.");
    }

    private void searchHelp() {
        System.out.println("The search command must be in the form: search [collection] search_term");
    }

    private void addHelp() {
        //System.out.println("The add command is used to add a song, album or artist to your collection.");
        System.out.println("The add command must be in the form: add <-song | -album | -artist> <id>");
    }
    private void importHelp() {
        System.out.println("import command must be in the form: import <song | album | artist>");
    }
    private void exitHelp() {
        System.out.println("Use exit, quit, or logout to terminate the application.");
    }
}
