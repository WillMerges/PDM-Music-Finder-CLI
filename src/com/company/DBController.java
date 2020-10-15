package com.company;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBController {
  private static Connection connection = null;


  public static void main(String[] args) {
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
    try {
      Statement statement = connection.createStatement();
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
