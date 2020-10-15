package com.company;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBController {
  public static void main(String[] args) {
    Connection connection;
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
    return true;
  }

  public boolean playSong(int sid) {
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

  public boolean listArtist(int arid) {
    // TODO
    return true;
  }

  public boolean listAlbum(int aid) {
    // TODO
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
