package com.company;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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

    // DEBUG only
    public void test() {
        db.getArtistGenreScores("test.csv");
    }

    public void run() {
        //test();
        user = "";
        System.out.println("Welcome! Please enter your username to log in.");
        try {
            while(user.equals("")) {
                System.out.print("Username: ");
                user = scanner.nextLine().trim();
                if(user.contains(" ")) {
                    System.out.println("Usernames cannot contain spaces!");
                    user = "";
                }
                if(user.equals("")) {
                    System.out.println("Please try again.");
                }
            }
        } catch(NoSuchElementException e) {
            System.exit(1);
        }

        System.out.println("Welcome, " + user + "!");
        if (false || !db.userExists(user)) {
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
                System.exit(1);
            }

            tokens.clear();
            StringTokenizer multiTokenizer = new StringTokenizer(input, " ");
            while (multiTokenizer.hasMoreTokens())
            {
                tokens.add(multiTokenizer.nextToken().trim());
            }
            parseTokens();
        }
        exit = false; // reset for next loop
    }

    private String concatRest(int n) {
        String ret = "";
        for(int i = n; i < tokens.size(); i++) {
            ret += tokens.get(i) + " ";
        }
        return ret.trim();
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
                System.exit(1);
            case "logout":
                exit = true;
                break;
            case "analytic":
                analyticsOptions();
                break;
            default:
                System.out.println(first + " is not a recognized command.");
        }
    }

    private void analyticsOptions() {
        if(tokens.size() >= 2) {
            String analytic = tokens.get(1);

            switch (analytic) {
                case "similar_artist":
                    db.getArtistGenreScores("artist_genre_scores.csv");
                    break;
                case "top_ten":
                    if (tokens.size() == 4) {
                        String startStr = tokens.get(2);
                        String endStr = tokens.get(3);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");
                        try {
                            Date startDate = dateFormat.parse(startStr);
                            Date endDate = dateFormat.parse(endStr);
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(endDate);
                            calendar.add(Calendar.YEAR, 1);
                            Date end = calendar.getTime();
                            db.topTenArtists(new Timestamp(startDate.getTime()), new Timestamp(end.getTime()));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println("top_ten must be run as \"top_ten <starting date> <end date>");
                    }
                    break;
                case "top_genre":
                    if (tokens.size() == 3) {
                        String yearStr = tokens.get(2);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");
                        try {
                            Date year = dateFormat.parse(yearStr);
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(year);
                            calendar.add(Calendar.YEAR, 1);
                            Date end = calendar.getTime();
                            db.topGenre(new Timestamp(year.getTime()), new Timestamp(end.getTime()));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println("top_genre must be run as \"top_genre <year>");
                    }
                    break;
                case "similar_users":
                    db.getUserGenreScores("user_genre_scores.csv");
                    break;
                case "cult_artists":
                    db.getCultArtists("cult_artists.csv");
                    break;
                case "top_artist_by_genre":
                    db.writeTopArtistByGenre("top_artist_by_genre.csv");
                    break;
                default:
                    System.out.println(analytic+" is not a valid analytic.");
            }
        } else {
            System.out.println("Analytics must be run as \"analytic <analytic name> [other options]\"");
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
                case "song":
                    db.addSong(user, id);
                    break;
                case "album":
                    db.addAlbum(user, id);
                    break;
                case "artist":
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
                return;
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
        } else if(tokens.size() == 3) {
            String id_str = tokens.get(2);
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
        listHelp();
    }

    private void searchOptions() {
        if(tokens.size() < 2) {
            searchHelp();
            return;
        }
        if(tokens.get(1).equals("collection")) {
            db.searchCollection(user, concatRest(2));
        } else {
            db.search(concatRest(1));
        }
    }

    private void infoOptions() {
        if(tokens.size() >= 3) {
            String id_str = tokens.get(2);
            int id = -1;
            try {
                id = Integer.parseInt(id_str);
            } catch (final NumberFormatException e) {
                System.out.println("id must be an integer!");
                return;
            }
            if (tokens.get(1).equals("song")) {
                db.dispSongInfo(id);
                return;
            } else if (tokens.get(1).equals("artist")) {
                db.dispArtistInfo(id);
                return;
            } else if (tokens.get(1).equals("album")) {
                db.dispAlbumInfo(id);
                return;
            }
        }
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
                System.out.println("The play command let's you play a song.");
                playHelp();
                break;
            case "info":
                System.out.println("The info command let's you see information about a song, album, or artist.");
                infoHelp();
                break;
            case "list":
                System.out.println("The list command let's you list songs in an album or albums from an artist.");
                listHelp();
                break;
            case "search":
                System.out.println("The search command let's you search the entire database or just your collection.");
                searchHelp();
                break;
            case "add":
                System.out.println("The add command let's you add songs, artists, and albums to your collection.");
                addHelp();
                break;
            case "import":
                System.out.println("Import let's you add songs, albums, and artists to the database.");
                importHelp();
                break;
            case "exit":
            case "quit":
                exitHelp();
                break;
            case "logout":
                logoutHelp();
                break;
            case "help":
                System.out.println("You have already found what you seek...");
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
        System.out.println("\tinfo song song_id");
        System.out.println("\tinfo album album_id");
        System.out.println("\tinfo artist artist_id");
    }

    private void listHelp() {
        System.out.println("The list command must be in the form: list [<album | artist> <id>]\n" +
                "If no album/artist is specified, it will display the logged in user's collection.");
    }

    private void searchHelp() {
        System.out.println("The search command must be in the form: search [collection] search_term");
    }

    private void addHelp() {
        System.out.println("The add command must be in the form: add <song | album | artist> <id>");
    }

    private void importHelp() {
        System.out.println("import command must be in the form: import <song | album | artist>");
    }

    private void exitHelp() {
        System.out.println("Use exit or quit to terminate the application.");
    }

    private void logoutHelp() {
        System.out.println("Use logout to sign out of the current user and try to re-login.");
    }
}
