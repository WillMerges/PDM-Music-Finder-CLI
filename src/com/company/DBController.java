package com.company;

import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;

public class DBController {
  private Connection connection = null;

  public DBController() {
    try {
      connection =
          DriverManager.getConnection(
              "jdbc:postgresql://reddwarf.cs.rit.edu/p320_18?currentSchema=public",
              "p320_18",
              "ieshoocaiDeipi0iev1v");

    } catch (SQLException throwable) {
      throwable.printStackTrace();
      System.exit(0);
    }
  }

  // for DEBUG only
  private void printResults(ResultSet resultSet) throws SQLException {
    ResultSetMetaData rsmd = resultSet.getMetaData();
    int columnsNumber = rsmd.getColumnCount();
    while (resultSet.next()) {
      for (int i = 1; i <= columnsNumber; i++) {
        if (i > 1) System.out.print(",  ");
        String columnValue = resultSet.getString(i);
        System.out.print(columnValue + " " + rsmd.getColumnName(i));
      }
      System.out.println("");
    }
  }

  // check if a user exists, if not create a collection for them
  public boolean userExists(String user) {
    // TODO
    return true;
  }

  public void playSong(String song, String username) {
    if (connection != null) {
      // Create and execute the SQL query
      try {
        Statement statement = connection.createStatement();
        // Selecting from song based on the inputted title
        ResultSet resultSet =
            statement.executeQuery("SELECT sid FROM Song WHERE Title = " + song);

        int numResults = 0;
        while (resultSet.next()) {
          numResults++;
        }

        if (numResults == 0) {
          System.out.println("Unable to play " + song);
        } else if (numResults == 1) {
          System.out.println("Played " + song);
          // TODO check this properly inserts
          int sid = resultSet.getInt("sid");
          PreparedStatement insertStatement =
              connection.prepareStatement("INSERT INTO PlayRecords VALUES ?, ?, ?");
          insertStatement.setObject(1, username);
          insertStatement.setObject(2, sid);
          insertStatement.setObject(3, LocalDate.now());
          insertStatement.execute();
          insertStatement.close();
        } else {
          System.out.println(
              "Multiple songs with that name found. Please select the SID of the desired song to be played");

          // Go over the multiple matches and output them in the format: "sid Title by artist name
          while (resultSet.next()) {
            // Fetch the artist name from the aid associated with the song
            Statement artistStatement = connection.createStatement();
            ResultSet artist =
                artistStatement.executeQuery(
                    "SELECT name FROM Artist WHERE aid = " + resultSet.getInt("aid"));

            // Output the song information for the user
            System.out.println(
                resultSet.getInt("sid: ")
                    + resultSet.getString("Title")
                    + " by "
                    + artist.getString("name"));

            artistStatement.close();
          }

          Scanner scanner = new Scanner(System.in);
          // Get sid from user
          int sid = scanner.nextInt();
          scanner.close();

          // Check for a matching sid within the set of songs and if the inputted sid is not within
          // the output ask for another
          boolean songPlayed = false;
          while (!songPlayed) {
            // Checked the original list of songs for the inputted sid
            while (resultSet.next()) {
              if (resultSet.getInt("sid") == sid) {
                System.out.println("Played song: " + resultSet.getString("Title"));
                songPlayed = true;
                // TODO check that this correctly inserts a PlayRecord entry
                PreparedStatement insertStatement =
                    connection.prepareStatement("INSERT INTO PlayRecords VALUES ?, ?, ?");
                insertStatement.setObject(1, username);
                insertStatement.setObject(2, sid);
                insertStatement.setObject(3, LocalDate.now());
                insertStatement.execute();
                insertStatement.close();
                break;
              }
            }
            // No matching sid was found
            System.out.println("Please enter an sid from the list ");
          }
        }

        statement.close();
        resultSet.close();

      } catch (SQLException throwable) {
        throwable.printStackTrace();
      }
    }
  }

  public void playSong(int sid, String username) {
    // TODO
    if (connection != null) {
      try {
        Statement statement = connection.createStatement();
        // Selecting from song based on the inputted sid
        ResultSet resultSet =
            statement.executeQuery("SELECT sid FROM Song WHERE sid = " + sid);

        int numResults = 0;
        while (resultSet.next()) {
          numResults++;
        }

        if (numResults == 0) {
          System.out.println(sid + " is not a valid song sid within the database");
        } else if (numResults == 1) {
          System.out.println("Played the song of sid: " + sid);
          // TODO check this properly inserts
          PreparedStatement insertStatement =
              connection.prepareStatement("INSERT INTO PlayRecords VALUES ?, ?, ?");
          insertStatement.setObject(1, username);
          insertStatement.setObject(2, sid);
          insertStatement.setObject(3, LocalDate.now());
          insertStatement.execute();
          insertStatement.close();
        }

        statement.close();
        resultSet.close();

      } catch (SQLException throwable) {
        System.out.println("An error occurred while trying to play the selected song. Please try again");
      }
    }
  }

  // add song to collection
  public boolean addSong(String user, int sid) {
    // TODO
    if (connection == null) {
      return false;
    }
    try {
      Statement statement = connection.createStatement();
      ResultSet resultSet
              = statement.executeQuery("SELECT cid FROM \"Collection\" WHERE Username = " + user);
      int cid = resultSet.getInt("cid");
      resultSet.close();
      PreparedStatement insertStatement =
              connection.prepareStatement("INSERT INTO \"ConsistsOfSong\" VALUES ?, ?");
      insertStatement.setObject(1, sid);
      insertStatement.setObject(2, cid);
      insertStatement.execute();
      insertStatement.close();

    } catch (SQLException throwable) {
      throwable.printStackTrace();
    }
    return true;
  }

  // add album to collection
  public boolean addAlbum(String user, int aid) {
    // TODO
    if (connection == null) {
      return false;
    }
    try {
      Statement statement = connection.createStatement();
      ResultSet resultSet
              = statement.executeQuery("SELECT cid FROM \"Collection\" WHERE Username = " + user);
      int cid = resultSet.getInt("cid");
      resultSet.close();
      PreparedStatement insertStatement =
              connection.prepareStatement("INSERT INTO \"ConsistsOfAlbum\" VALUES ?, ?");
      insertStatement.setObject(1, aid);
      insertStatement.setObject(2, cid);
      insertStatement.execute();
      insertStatement.close();

    } catch (SQLException throwable) {
      throwable.printStackTrace();
    }
    return true;
  }

  // add artist to collection
  public boolean addArtist(String user, int arid) {
    // TODO
    if (connection == null) {
      return false;
    }
    try {
      Statement statement = connection.createStatement();
      ResultSet resultSet
              = statement.executeQuery("SELECT cid FROM \"Collection\" WHERE Username = " + user);
      int cid = resultSet.getInt("cid");
      resultSet.close();
      PreparedStatement insertStatement =
              connection.prepareStatement("INSERT INTO \"ConsistsOfArtist\" VALUES ?, ?");
      insertStatement.setObject(1, arid);
      insertStatement.setObject(2, cid);
      insertStatement.execute();
      insertStatement.close();

    } catch (SQLException throwable) {
      throwable.printStackTrace();
    }
    return true;
  }

  // TODO we DONT need the remove methods?

  // remove song from collection
  public boolean removeSong(String user, int sid) {
    // TODO
    return true;
  }

  // remove album from collection
  public boolean removeAlbum(String user, int aid) {
    // TODO
    return true;
  }

  // remove artist from eollection
  public boolean removeArtist(String user, int arid) {
    // TODO
    return true;
  }

  public boolean listCollection(String user) {
    // TODO
    if (connection == null) {
      return false;
    }
    return true;
  }

  public boolean listArtist(String artist) {
    System.out.println("This function is not yet implemented yet, please use the id version.");
    return false;
  }

  public boolean listAlbum(String album) {
    System.out.println("This function is not yet implemented yet, please use the id version.");
    return false;
  }

  public boolean listArtist(int arid) {
    // TODO
    if (connection == null) {
      return false;
    }
    return true;
  }

  public boolean listAlbum(int aid) {
    // TODO
    if (connection == null) {
      return false;
    }
    return true;
  }

  // display info about a song,album,or artist with name 'name'
  public boolean dispInfo(String name) {
    System.out.println("This function is not yet implemented yet, please use the id version.");
    return false;
  }

  // display a "last played" time
  public boolean dispSongInfo(int sid) {
    // TODO
    if (connection == null) {
      return false;
    }

    // Create and execute the SQL query
    try {
      Statement statement = connection.createStatement();
      // Selecting from song based on the inputted sid
      ResultSet resultSet =
              statement.executeQuery("SELECT Title FROM Song WHERE sid = " + sid);
      System.out.println("Title: " + resultSet.getString("Title"));
      resultSet = statement.executeQuery("SELECT TrackNum FROM Song WHERE sid = " + sid);
      int trackNum = resultSet.getInt("TrackNum");
      resultSet = statement.executeQuery("SELECT aid FROM Song WHERE sid = " + sid);
      int aid = resultSet.getInt("aid");
      resultSet = statement.executeQuery("SELECT arid FROM PublishesAlbum WHERE aid = " + aid);
      int arid = resultSet.getInt("arid");
      resultSet = statement.executeQuery("SELECT name FROM Artist WHERE arid = " + arid);
      System.out.println("Artist: " + resultSet.getString("name"));
      resultSet = statement.executeQuery("SELECT title FROM Album WHERE aid = " + aid);
      System.out.print("Album: " + resultSet.getString("title"));
      System.out.println("Track Number:" + trackNum);
      resultSet = statement.executeQuery("SELECT Time FROM PlayRecords WHERE sid = " + sid);
      System.out.println("Last played: " + resultSet.getTimestamp("Time"));
      resultSet.close();

    } catch (SQLException throwable) {
      throwable.printStackTrace();
    }
    return true;
  }

  public boolean dispArtistInfo(int arid) {
    //TODO
    if (connection == null) {
      return false;
    }
    // Create and execute the SQL query
    try {
      Statement statement = connection.createStatement();
      ResultSet resultSet
              = statement.executeQuery("SELECT name FROM \"Artist\" WHERE arid = " + arid);
      resultSet.next();
      System.out.println("Artist's name is: " + resultSet.getString("name"));
      System.out.println("They published the following albums:\n");
      resultSet = statement.executeQuery("" +
              "SELECT a.aid, a.title FROM \"Album\" a, \"PublishesAlbum\" p WHERE a.aid = p.aid AND p.arid = "+Integer.toString(arid));
      while(resultSet.next()) {
        String title = resultSet.getString("title");
        int aid = resultSet.getInt("aid");
        System.out.println(title+"  :  id: "+Integer.toString(aid));
      }
      resultSet.close();

    } catch (SQLException throwable) {
      throwable.printStackTrace();
    }

    return true;
  }

  public boolean dispAlbumInfo(int aid) {
    // TODO
    if (connection == null) {
      return false;
    }
    // Create and execute the SQL query
    try {
      Statement statement = connection.createStatement();
      // Selecting from song based on the inputted title
      ResultSet resultSet =
              statement.executeQuery("SELECT Title FROM Album WHERE aid = " + aid);
      System.out.print("Album's title is: " + resultSet.getString("title"));
      resultSet = statement.executeQuery("SELECT releasedate FROM Album WHERE aid = " + aid);
      System.out.println(" and the release date is: " + resultSet.getDate("releasedate"));
      resultSet = statement.executeQuery("SELECT arid FROM PublishesAlbum WHERE aid = " + aid);
      int arid = resultSet.getInt("arid");
      resultSet = statement.executeQuery("SELECT name FROM Artist WHERE arid = " + arid);
      System.out.println("Artist: " + resultSet.getString("name"));
      resultSet.close();

    } catch (SQLException throwable) {
      throwable.printStackTrace();
    }
    return true;
  }

  // search for anything matching string tok
  public boolean search(String tok) {
    // TODO
    // return all id'scanner of albums, artists, songs matching name
    // then call dispInfo
    // profit
    if (connection == null) {
      return false;
    }
    // Create and execute the SQL query
    try {
      Statement statement = connection.createStatement();
      ResultSet resultSet =
              statement.executeQuery("SELECT arid FROM Artist WHERE name = " + tok);
      while (resultSet.next()){
        dispArtistInfo(resultSet.getInt("arid"));
      }
      resultSet = statement.executeQuery("SELECT aid FROM Album WHERE Title = " + tok);
      while (resultSet.next()){
        dispAlbumInfo(resultSet.getInt("aid"));
      }
      resultSet = statement.executeQuery("SELECT sid FROM Song WHERE Title = " + tok);
      while (resultSet.next()){
        dispSongInfo(resultSet.getInt("sid"));
      }
      resultSet.close();

    } catch (SQLException throwable) {
      throwable.printStackTrace();
    }
    return true;
  }

  // search a user'scanner collection
  public boolean searchCollection(String user, String tok) {
    // TODO
    return true;
  }

  public boolean importSong() {
    // TODO
    return true;
  }

  public boolean importArtist() {
    // TODO
    return true;
  }

  public boolean importAlbum() {
    // TODO
    return true;
  }
}
