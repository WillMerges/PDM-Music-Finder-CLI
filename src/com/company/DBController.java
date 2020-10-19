package com.company;

import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;

public class DBController {
  private Connection connection = null;
  private final String dbString = "p320_18";

  public void setConnection() {
    try {
      connection =
          DriverManager.getConnection(
              "jdbc:postgresql://reddwarf.cs.rit.edu/p320_18?currentSchema=myschema",
              "p320_18",
              "ieshoocaiDeipi0iev1v");

    } catch (SQLException throwable) {
      throwable.printStackTrace();
      System.exit(0);
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
            statement.executeQuery("SELECT Song FROM " + dbString + " WHERE Title = " + song);

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
                    "SELECT Artist FROM " + dbString + " WHERE aid = " + resultSet.getInt("aid"));

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
            statement.executeQuery("SELECT Song FROM " + dbString + " WHERE sid = " + sid);

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
        throwable.printStackTrace();
      }
    }
  }

  // add song to collection
  public boolean addSong(String user, int sid) {
    // TODO
    return true;
  }

  // add album to collection
  public boolean addAlbum(String user, int aid) {
    // TODO
    return true;
  }

  public boolean addArtist(String user, int arid) {
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
    // TODO
    if (connection == null) {
      return false;
    }
    return true;
  }

  public boolean listAlbum(String album) {
    // TODO
    if (connection == null) {
      return false;
    }
    return true;
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
    // TODO
    return true;
  }

  //  public boolean dispSongInfo(String song) {
  //    // TODO
  //    // display all info, including play record
  //    return true;
  //  }

  // display a "last played" time
  public boolean dispSongInfo(int sid) {
    // TODO
    return true;
  }

  //  public boolean dispArtistInfo(String artist) {
  //    // TODO
  //    return true;
  //  }

  public boolean dispArtistInfo(int arid) {
    // TODO
    return true;
  }

  //  public boolean dispAlbumInfo(String album) {
  //    // TODO
  //    return true;
  //  }

  public boolean dispAlbumInfo(int aid) {
    // TODO
    return true;
  }

  // search for anything matching string tok
  public boolean search(String tok) {
    // TODO
    return true;
  }

  // search a user's collection
  public boolean searchCollection(String user, String tok) {
    // TODO
    return true;
  }
}
