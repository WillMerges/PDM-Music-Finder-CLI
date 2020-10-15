package com.company;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBController {
  public static void main(String[] args) {
    Connection connection;
    try {
      Class.forName("lib.postgresql-42.2.17.jar");
      connection =
          DriverManager.getConnection(
              "//jdbc:postgresql://reddwarf.cs.rit.edu/databases/p320_18?currentSchema=myschema",
              "p320_18",
              "ieshoocaiDeipi0iev1v");

    } catch (SQLException | ClassNotFoundException throwable) {
      throwable.printStackTrace();
      System.exit(0);
    }
  }

  public boolean playSong(String song) {
    // TODO
    return true;
  }

  public boolean listCollection(String user) {
    // TODO
    return true;
  }

  public boolean listArtist(String artist) {
    // TODO
    return true;
  }

  public boolean listAlbum(String album) {
    // TODO
    return true;
  }

}
