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
}
