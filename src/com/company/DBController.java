package com.company;

import java.sql.*;

public class DBController {
  private Connection connection = null;
  private String dbString = "p320_18";


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

  public boolean playSong(String song) {
    // TODO
    if (connection == null) {
      return false;
    }
    // Create and execute the SQL query
    try {
      Statement statement = connection.createStatement();
      // Selecting from song based on the inputted title
      ResultSet resultSet = statement.executeQuery("SELECT song FROM " + dbString + "WHERE Title = " + song);
      // Output the returned values
      while (resultSet.next()) {
        System.out.println(resultSet.getString("Title"));
      }
      statement.close();
      resultSet.close();

    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }
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

  public boolean dispSongInfo(String song) {
    // TODO
    // display all info, including play record
    return true;
  }

  public boolean dispSongInfo(int sid) {
    // TODO
    return true;
  }

  public boolean dispArtistInfo(String artist) {
    // TODO
    return true;
  }

  public boolean dispArtistInfo(int arid) {
    // TODO
    return true;
  }

  public boolean dispAlbumInfo(String album) {
    // TODO
    return true;
  }

  public boolean dispAlbumInfo(int aid) {
    // TODO
    return true;
  }

  // search for anything matching string tok
  public boolean search(String tok) {
    // TODO
    return true;
  }
}
