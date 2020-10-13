package com.company;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBController {
  public void connect() {
    Connection connection;
    try {
      connection =
          DriverManager.getConnection(
              "//jdbc:postgresql://reddwarf.cs.rit.edu/databases/p320_18currentSchema=myschema",
              "p320_18",
              "ieshoocaiDeipi0iev1v");

    } catch (SQLException throwable) {
      throwable.printStackTrace();
      System.exit(0);
    }
  }
}
