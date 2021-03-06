package com.company;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.InputMismatchException;
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
      System.out.println(
          "Unable to connect to DB, please check you are on the correct network then try again.");
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

  // Calculate who the most-played artist is by genre of music, then write the results to a .csv file
  public void writeTopArtistByGenre(String outfile) {
    FileWriter file;

    try {
      Files.deleteIfExists(Paths.get(outfile));
      file = new FileWriter(outfile);

      Statement statement = connection.createStatement();

      ResultSet resultSet =
              statement.executeQuery(
                      "SELECT t1.genre, t1.name " +
                              "FROM " +
                              "(SELECT COUNT(s.title) plays, ar.name, a.genre " +
                              "FROM \"PlayRecords\" pr, \"Artist\" ar, \"Song\" s, \"Album\" a, \"PublishesAlbum\" p " +
                              "WHERE s.aid = a.aid AND p.aid = a.aid AND p.arid = ar.arid AND pr.sid = s.sid " +
                              "GROUP BY ar.name, a.genre " +
                              "ORDER BY plays) AS t1 " +
                              "LEFT OUTER JOIN " +
                              "(SELECT COUNT(s.title) plays, ar.name, a.genre " +
                              "FROM \"PlayRecords\" pr, \"Artist\" ar, \"Song\" s, \"Album\" a, \"PublishesAlbum\" p " +
                              "WHERE s.aid = a.aid AND p.aid = a.aid AND p.arid = ar.arid AND pr.sid = s.sid " +
                              "GROUP BY ar.name, a.genre " +
                              "ORDER BY plays) AS t2 " +
                              "ON t2.genre = t1.genre " +
                              "AND t2.plays > t1.plays " +
                              "WHERE t2.plays IS NULL " +
                              "ORDER BY t1.genre");

      String genre;
      String name;
      file.write("genre, name\n");
      System.out.println("Top Artist by Genre:");
      System.out.println("=================================================================");
      System.out.println("Genre   :   Artist");
      while (resultSet.next()) {
        genre = resultSet.getString("genre");
        name = resultSet.getString("name");
        System.out.println(genre + "   :   " + name);
        file.write(genre + "," + name + "\n");
      }

      System.out.println("Wrote data to: " + outfile);
      statement.close();
      resultSet.close();
      file.close();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void getCultArtists(String outfile){
    FileWriter file = null;
    try {
      Files.deleteIfExists(Paths.get(outfile));
      file = new FileWriter(outfile);

      Statement statement = connection.createStatement();
      ResultSet resultSet = statement.executeQuery("select uc.name artist, pc.plays/uc.users::double precision ratio  from " +
                "(select COUNT(s.title) plays, ar.name  from \"PlayRecords\" pr, \"Artist\" ar, \"Song\" s, \"Album\" a, \"PublishesAlbum\" p " +
                "where s.aid = a.aid AND p.aid = a.aid AND p.arid = ar.arid AND pr.sid = s.sid " +
                "GROUP BY ar.name ) as pc " +
                "INNER JOIN " +
                "(select count(upa.username) users, upa.name from " +
                "(select ar.name, pr.username from  \"PlayRecords\" pr, \"Artist\" ar, \"Song\" s, \"Album\" a, \"PublishesAlbum\" p " +
                "where s.aid = a.aid AND p.aid = a.aid AND p.arid = ar.arid AND pr.sid = s.sid " +
                "GROUP BY pr.username,  ar.name) as upa " +
                "GROUP BY upa.name) as uc " +
                "on pc.name = uc.name " +
                "order by ratio desc");

        String name = "";
        double ratio = 0.0;
        while(resultSet.next()) {
          name = resultSet.getString("artist");
          ratio = resultSet.getDouble("ratio");
          file.write(name + "," + ratio + "\n");
        }
        System.out.println("Wrote data to: "+outfile);
      }

     catch(Exception e) {
      e.printStackTrace();
      System.out.println("Error fetching cult artist scores.");
    } finally {
      try {
        file.close();
      } catch (Exception e) {}
    }
  }

  public void getArtistGenreScores(String outfile) {
    FileWriter file = null;
    try {
      Files.deleteIfExists(Paths.get(outfile));
      file = new FileWriter(outfile);

      Statement statement = connection.createStatement();
      ResultSet resultSet =
              statement.executeQuery("select distinct arid, name from \"Artist\"");
      int arid = -1;
      String name = "";
      while(resultSet.next()) {
        arid = resultSet.getInt("arid");
        name = resultSet.getString("name");
        name = name.replaceAll(",", "\006"); // can't pass a comma in to a csv so use ACK char
        file.write(arid+": "+name+"\n");

        ResultSet genres = statement.executeQuery("select a.genre, a.releasedate, pr.num " +
                "from \"PublishesAlbum\" p, \"Album\" a, " +
                "(select COUNT(pr.time) num, a.aid from \"PlayRecords\" pr, " +
                "\"Album\" a, \"Song\" s where s.aid = a.aid GROUP BY a.aid) as pr " +
                "where p.arid="+arid+" and a.aid = p.aid and pr.aid = p.aid " +
                "order by a.releasedate DESC");

        String genre = "";
        Timestamp t = null;
        int playcount = 0;
        while(genres.next()) {
          t = genres.getTimestamp("releasedate");
          genre = genres.getString("genre");
          playcount = genres.getInt("num");
          file.write(genre + "," + t.getTime() + "," + playcount + "\n");
        }
      }
      System.out.println("Wrote data to: "+outfile);
    } catch(Exception e) {
      e.printStackTrace();
      System.out.println("Error fetching artist genre scores.");
    } finally {
      try {
        file.close();
      } catch (Exception e) {}
    }
  }

  public void getUserGenreScores(String outfile) {
    FileWriter file = null;
    try {
      Files.deleteIfExists(Paths.get(outfile));
      file = new FileWriter(outfile);

      Statement statement = connection.createStatement();
      ResultSet resultSet =
              statement.executeQuery("select distinct username from \"User\" u");

      String username = "";
      while(resultSet.next()) {
        username = resultSet.getString("username");
        String name = username.replaceAll(",", "\006"); // can't pass a comma in to a csv so use ACK char
        file.write(name+"\n");

        ResultSet genres = statement.executeQuery("select a.genre, pr.lastplayed, pr.numplayed from \"Album\" a, " +
                        "(select COUNT(pr.time) numplayed, MAX(pr.time) lastplayed, s.aid from \"PlayRecords\" pr, " +
                        "\"Song\" s, \"User\" u where pr.username='"+username+"' and pr.sid = s.sid group by s.aid) as pr " +
                        "where pr.aid = a.aid " +
                        "order by pr.lastplayed DESC");

        String genre = "";
        Timestamp t = null;
        int playcount = 0;
        while(genres.next()) {
          t = genres.getTimestamp("lastplayed");
          genre = genres.getString("genre");
          playcount = genres.getInt("numplayed");
          file.write(genre + "," + t.getTime() + "," + playcount + "\n");
        }
      }
      System.out.println("Wrote data to: "+outfile);
    } catch(Exception e) {
      e.printStackTrace();
      System.out.println("Error fetching user genre scores.");
    } finally {
      try {
        file.close();
      } catch (Exception e) {}
    }
  }


  // check if a user exists in the database
  public boolean userExists(String user) {
    boolean found = false;
    String sql = "SELECT username FROM \"User\" WHERE username = ?";

    try {
      PreparedStatement statement = connection.prepareStatement(sql);
      statement.setString(1, user);

      ResultSet resultSet = statement.executeQuery();

      while (resultSet.next()) {
        String foundName = resultSet.getString("username");

        if (user.equals(foundName)) {
          found = true;
        }
      }

      statement.close();
    } catch (SQLException throwable) {
      System.out.println("userExists() has encountered an error!");
      throwable.printStackTrace();
    }
    return found;
  }

  // Create a new user and add them to the User and Collection tables of the database.
  public void createUser(String user) {
    String insertUserSql = "INSERT INTO \"User\" (username) VALUES (?)";
    String selectCollectionSql = "SELECT MAX(cid) FROM \"Collection\"";
    String insertCollectionSql = "INSERT INTO \"Collection\" (cid, username) VALUES (?,?)";

    try {
      PreparedStatement insertUserStatement = connection.prepareStatement(insertUserSql);
      PreparedStatement selectCollectionStatement =
          connection.prepareStatement(selectCollectionSql);
      PreparedStatement insertCollectionStatement =
          connection.prepareStatement(insertCollectionSql);

      ResultSet idResult = selectCollectionStatement.executeQuery();
      int maxID = -1;

      while (idResult.next()) {
        maxID = idResult.getInt("max");
      }

      insertUserStatement.setString(1, user);

      insertCollectionStatement.setInt(1, maxID + 1);
      insertCollectionStatement.setString(2, user);

      insertUserStatement.executeUpdate();
      insertCollectionStatement.executeUpdate();

      insertUserStatement.close();
      selectCollectionStatement.close();
      insertCollectionStatement.close();
    } catch (SQLException throwable) {
      System.out.println("createUser() has encountered an error!");
      throwable.printStackTrace();
    }
  }

  public void topTenArtists(Timestamp start, Timestamp end) {
    if (connection != null) {
      try {
        PreparedStatement searchStatement =
            connection.prepareStatement("SELECT COUNT(PR.time) AS C, PA.arid FROM \"PlayRecords\" PR, " +
                "\"PublishesAlbum\" PA, \"Song\" S WHERE PR.sid = S.sid AND S.aid = PA.aid AND " +
                "PR.time BETWEEN ? AND ? GROUP BY PA.arid ORDER BY C DESC");
        searchStatement.setObject(1, start);
        searchStatement.setObject(2, end);
        ResultSet resultSet = searchStatement.executeQuery();

        System.out.println("Ranking | ARID | Play Count");

        int i = 1;
        while (resultSet.next() && i <= 10) {
          System.out.println(i + ":\t" + resultSet.getString("arid") + "\t" +
              resultSet.getString("C"));
          i++;
        }

      } catch (SQLException throwable) {
        throwable.printStackTrace();
      }
    }
  }

  public void topGenre(Timestamp start, Timestamp end) {
    if (connection != null) {
      try {
        PreparedStatement preparedStatement =
            connection.prepareStatement("SELECT COUNT(PR.time) AS C, A.genre FROM \"PlayRecords\" PR, " +
                "\"Album\" A, \"Song\" S WHERE PR.sid = S.sid AND S.aid = A.aid AND PR.time BETWEEN ? AND ? " +
                "GROUP BY A.genre ORDER BY C DESC");
        preparedStatement.setObject(1, start);
        preparedStatement.setObject(2, end);
        ResultSet resultSet = preparedStatement.executeQuery();

        System.out.println("Ranking | Genre | Play Count");

        int i = 1;
        while (resultSet.next() && i <= 10) {
          System.out.println(i + ":\t" + resultSet.getString("genre") + "\t" +
              resultSet.getString("C"));
          i++;
        }
      } catch (SQLException throwable) {
        throwable.printStackTrace();
      }
    }
  }

  public void playSong(String song, String username) {
    System.out.println("Not implemented at this time, please use the sid version.");
    return;
//    if (connection != null) {
//      // Create and execute the SQL query
//      try {
//        Statement statement = connection.createStatement();
//        // Selecting from song based on the inputted title
//        ResultSet resultSet =
//            statement.executeQuery("SELECT \"sid\" FROM \"Song\" WHERE \"title\" = " + song);
//
//        int numResults = 0;
//        while (resultSet.next()) {
//          numResults++;
//        }
//
//        if (numResults == 0) {
//          System.out.println("Unable to play " + song);
//        } else if (numResults == 1) {
//          System.out.println("Played " + song);
//          // Insert the play record entry
//          int sid = resultSet.getInt("sid");
//          PreparedStatement insertStatement =
//              connection.prepareStatement("INSERT INTO \"PlayRecords\" VALUES ?, ?, ?");
//          insertStatement.setObject(1, username);
//          insertStatement.setObject(2, sid);
//          insertStatement.setObject(3, Timestamp.valueOf(LocalDateTime.now()));
//          insertStatement.execute();
//          insertStatement.close();
//        } else {
//          System.out.println(
//              "Multiple songs with that name found. Please select the SID of the desired song to be played");
//
//          resultSet.first();
//          // Go over the multiple matches and output them in the format: "sid Title by artist name
//          while (resultSet.next()) {
//            // Fetch the artist name from the aid associated with the song
//            Statement artistStatement = connection.createStatement();
//            ResultSet artist =
//                artistStatement.executeQuery(
//                    "SELECT \"name\" FROM \"Artist\" WHERE \"aid\" = " + resultSet.getInt("aid"));
//
//            // Output the song information for the user
//            System.out.println(
//                resultSet.getInt("sid: ")
//                    + resultSet.getString("title")
//                    + " by "
//                    + artist.getString("name"));
//
//            artistStatement.close();
//          }
//
//          Scanner scanner = new Scanner(System.in);
//          // Get sid from user
//          int sid = scanner.nextInt();
//          scanner.close();
//
//          // Check for a matching sid within the set of songs and if the inputted sid is not within
//          // the output ask for another
//          boolean songPlayed = false;
//          while (!songPlayed) {
//            // Checked the original list of songs for the inputted sid
//            while (resultSet.next()) {
//              if (resultSet.getInt("sid") == sid) {
//                System.out.println("Played song: " + resultSet.getString("title"));
//                songPlayed = true;
//                // Insert the play record entry
//                PreparedStatement insertStatement =
//                    connection.prepareStatement("INSERT INTO \"PlayRecords\" VALUES ?, ?, ?");
//                insertStatement.setObject(1, username);
//                insertStatement.setObject(2, sid);
//                insertStatement.setObject(3, Timestamp.valueOf(LocalDateTime.now()));
//                insertStatement.execute();
//                insertStatement.close();
//                break;
//              }
//            }
//            // No matching sid was found
//            System.out.println("Please enter an sid from the list");
//          }
//        }
//
//        statement.close();
//        resultSet.close();
//
//      } catch (SQLException throwable) {
//        throwable.printStackTrace();
//      }
//    }
  }

  public void playSong(int sid, String username) {
    if (connection != null) {
      try {
        Statement statement = connection.createStatement();
        // Selecting from song based on the inputted sid
        ResultSet resultSet =
            statement.executeQuery("SELECT \"sid\" FROM \"Song\" WHERE \"sid\" = " + sid);

        int numResults = 0;
        while (resultSet.next()) {
          numResults++;
        }

        if (numResults == 0) {
          System.out.println(sid + " is not a valid song sid within the database");
        } else if (numResults == 1) {
          System.out.println("Played the song of sid: " + sid);
          // Insert the play record entry
          PreparedStatement insertStatement =
              connection.prepareStatement(
                  "INSERT INTO \"PlayRecords\" (username, sid, time) VALUES (?, ?, ?)");
          insertStatement.setObject(1, username);
          insertStatement.setObject(2, sid);
          insertStatement.setObject(3, Timestamp.valueOf(LocalDateTime.now()));
          insertStatement.execute();
          insertStatement.close();
        }

        statement.close();
        resultSet.close();

      } catch (SQLException throwable) {
        System.out.println(
            "An error occurred while trying to play the selected song. Please try again");
      }
    }
  }

  // add song to collection
  public void addSong(String user, int sid) {
    if (connection != null) {
      try {
        Statement statement = connection.createStatement();
        ResultSet resultSet =
            statement.executeQuery(
                "SELECT cid FROM \"Collection\" WHERE \"username\" = \'" + user + "\'");
        if(!resultSet.next()) {
          System.out.println("Unexpected error, no matching cid to username: "+user);
          return;
        }
        int cid = resultSet.getInt("cid");

        resultSet = statement.executeQuery("SELECT sid FROM \"ConsistsOfSong\" WHERE cid = "+cid+" AND sid = "+sid);
        if(resultSet.next()) {
          System.out.println("This song is already in your collection!");
          return;
        }
        resultSet.close();

        PreparedStatement insertStatement =
            connection.prepareStatement("INSERT INTO \"ConsistsOfSong\"(sid, cid) VALUES(?, ?)");
        insertStatement.setObject(1, sid);
        insertStatement.setObject(2, cid);
        insertStatement.execute();
        insertStatement.close();

      } catch (SQLException throwable) {
        throwable.printStackTrace();
      }
    }
  }

  // add album to collection
  public void addAlbum(String user, int aid) {
    if (connection != null) {
      try {
        Statement statement = connection.createStatement();
        ResultSet resultSet =
            statement.executeQuery(
                "SELECT cid FROM \"Collection\" WHERE \"username\" = \'" + user + "\'");
        resultSet.next();
        int cid = resultSet.getInt("cid");

        resultSet = statement.executeQuery("SELECT aid FROM \"ConsistsOfAlbum\" WHERE cid = "+cid+" AND aid = "+aid);
        if(resultSet.next()) {
          System.out.println("This album is already in your collection!");
          return;
        }
        resultSet.close();

        PreparedStatement insertStatement =
            connection.prepareStatement("INSERT INTO \"ConsistsOfAlbum\"(aid, cid) VALUES(?, ?)");
        insertStatement.setObject(1, aid);
        insertStatement.setObject(2, cid);
        insertStatement.execute();
        insertStatement.close();

      } catch (SQLException throwable) {
        throwable.printStackTrace();
      }
    }
  }

  // add artist to collection
  public void addArtist(String user, int arid) {
    if (connection != null) {
      try {
        Statement statement = connection.createStatement();
        ResultSet resultSet =
            statement.executeQuery(
                "SELECT cid FROM \"Collection\" WHERE \"username\" = \'" + user + "\'");
        resultSet.next();
        int cid = resultSet.getInt("cid");

        resultSet = statement.executeQuery("SELECT arid FROM \"ConsistsOfArtist\" WHERE cid = "+cid+" AND arid = "+arid);
        if(resultSet.next()) {
          System.out.println("This artist is already in your collection!");
          return;
        }
        resultSet.close();

        PreparedStatement insertStatement =
            connection.prepareStatement("INSERT INTO \"ConsistsOfArtist\"(ArID, cid) VALUES(?, ?)");
        insertStatement.setObject(1, arid);
        insertStatement.setObject(2, cid);
        insertStatement.execute();
        insertStatement.close();

      } catch (SQLException throwable) {
        throwable.printStackTrace();
      }
    }
  }

  // list the collection of a user--------------------------------------------------------------------------------------------
  public void listCollection(String user) {
    String songSql = "SELECT S.sid, S.title FROM \"Song\" S, \"Collection\" C, \"ConsistsOfSong\" CS" +
            " WHERE S.sid = CS.sid AND CS.cid = C.cid AND C.username = ?";
    String albumSql = "Select A.aid, A.title FROM \"Album\" A, \"Collection\" C, \"ConsistsOfAlbum\" CA" +
            " WHERE A.aid = CA.aid AND CA.cid = C.cid AND C.username = ?";
    String artistSql= "Select A.arid, A.name FROM \"Artist\" A, \"Collection\" C, \"ConsistsOfArtist\" CA" +
            " WHERE A.arid = CA.arid AND CA.cid = C.cid AND C.username = ?";

    try {
      PreparedStatement songStatement = connection.prepareStatement(songSql);
      PreparedStatement albumStatement = connection.prepareStatement(albumSql);
      PreparedStatement artistStatement = connection.prepareStatement(artistSql);

      songStatement.setString(1, user);
      albumStatement.setString(1, user);
      artistStatement.setString(1, user);

      ResultSet songResults = songStatement.executeQuery();
      ResultSet albumResults = albumStatement.executeQuery();
      ResultSet artistResults = artistStatement.executeQuery();

      System.out.println("Your collection includes the following:\n");

      System.out.println("Songs:");
      System.out.println("=================================================================");
      while (songResults.next()) {
        int sid = songResults.getInt("sid");
        String title = songResults.getString("title");

        System.out.println(title+"  --  id: "+sid);
      }
      System.out.println("=================================================================");
      System.out.println();

      System.out.println("Albums:");
      System.out.println("=================================================================");
      while (albumResults.next()) {
        int aid = albumResults.getInt("aid");
        String title = albumResults.getString("title");

        System.out.println(title+"  --  id: "+aid);
      }
      System.out.println("=================================================================");
      System.out.println();

      System.out.println("Artists:");
      System.out.println("=================================================================");
      while (artistResults.next()) {
        int arid = artistResults.getInt("arid");
        String name = artistResults.getString("name");

        System.out.println(name+"  --  id: "+arid);
      }
      System.out.println("=================================================================");
      System.out.println();

      songStatement.close();
      albumStatement.close();
      artistStatement.close();
    }
    catch (SQLException throwable) {
      throwable.printStackTrace();
    }
  }

  public void listArtist(int arid) {
    if (connection != null) {
      try {
        Statement statement = connection.createStatement();
        ResultSet artist =
            statement.executeQuery(
                "SELECT name FROM \"Artist\" WHERE arid = " + Integer.toString(arid));

        if (!artist.next()) {
          System.out.println("No artist with id: " + Integer.toString(arid) + "\n");
          return;
        }

        System.out.println(artist.getString("name") + ", published the following albums:");
        System.out.println("=================================================================");

        ResultSet albums =
            statement.executeQuery(
                "SELECT A.aid, A.title FROM \"PublishesAlbum\" P, \"Album\" A WHERE P.arid = "
                    + arid
                    + " AND A.aid = P.aid");
        while (albums.next()) {
          System.out.println(albums.getString("title") + "  --  id:" + albums.getInt("aid"));
        }
        System.out.println("=================================================================");
      } catch (SQLException throwables) {
        throwables.printStackTrace();
      }
    }
  }

  public void listAlbum(int aid) {
    try {
      Statement statement = connection.createStatement();
      ResultSet result = statement.executeQuery("SELECT title FROM \"Album\" WHERE aid = " + aid);
      if (!result.next()) {
        System.out.println("No album with id: " + aid);
        return;
      }

      System.out.println(result.getString("title"));
      System.out.println("=================================================================");

      result =
              statement.executeQuery(
                      "SELECT s.sid, s.title, s.track_num FROM \"Song\" s, \"Album\" a "
                              + "WHERE a.aid = s.aid AND a.aid = "
                              + aid
                              + " ORDER BY s.track_num ASC");
      while (result.next()) {
        System.out.println(
                result.getInt("track_num")
                        + ": "
                        + result.getString("title")
                        + "  --  id: "
                        + result.getInt("sid"));
      }
      System.out.println("=================================================================");
      System.out.println();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  // display a "last played" time
  public void dispSongInfo(int sid) {
    // Create and execute the SQL query
    try {
      Statement statement = connection.createStatement();
      // Selecting from song based on the inputted sid
      ResultSet resultSet =
          statement.executeQuery("SELECT title, track_num, aid FROM \"Song\" WHERE sid = " + sid);
      if(!resultSet.next()) {
        System.out.println("No song exists with id: "+sid);
        return;
      }

      System.out.println("Title: " + resultSet.getString("title"));
      int trackNum = resultSet.getInt("track_num");
      int aid = resultSet.getInt("aid");

      resultSet = statement.executeQuery("SELECT arid FROM \"PublishesAlbum\" WHERE aid = " + aid);
      if(!resultSet.next()) {
        System.out.println("Unexpected error, no such aid PublishesAlbum: "+aid);
        return;
      }

      int arid = resultSet.getInt("arid");
      resultSet = statement.executeQuery("SELECT \"name\" FROM \"Artist\" WHERE arid = " + arid);
      if(!resultSet.next()) {
        System.out.println("Unexpected error, no such arid in Artist: "+arid);
        return;
      }
      System.out.println("Artist: " + resultSet.getString("name")+"  --  id: "+arid);

      resultSet = statement.executeQuery("SELECT title FROM \"Album\" WHERE aid = " + aid);
      resultSet.next();
      System.out.println("Album: " + resultSet.getString("title")+"  --  id: "+aid);
      System.out.println("Track Number: " + trackNum);
      resultSet = statement.executeQuery("SELECT \"time\" FROM \"PlayRecords\" WHERE sid = " + sid+" ORDER BY time DESC");


      if (resultSet.next()) {
        System.out.println("Last played: " + resultSet.getTimestamp("time"));
      } else {
        System.out.println("Never been played");
      }
      resultSet.close();

    } catch (SQLException throwable) {
      throwable.printStackTrace();
    }
  }

  public void dispArtistInfo(int arid) {
    // Create and execute the SQL query
    try {
      Statement statement = connection.createStatement();
      ResultSet resultSet =
          statement.executeQuery("SELECT name FROM \"Artist\" WHERE arid = " + arid);
      if(!resultSet.next()) {
        System.out.println("No artist with id: "+arid);
        return;
      }
      System.out.println("Artist's name is: " + resultSet.getString("name"));
      System.out.println("They published the following albums:");
      System.out.println("=================================================================");
      resultSet =
          statement.executeQuery(
              ""
                  + "SELECT a.aid, a.title FROM \"Album\" a, \"PublishesAlbum\" p WHERE a.aid = p.aid AND p.arid = "
                  + Integer.toString(arid));
      while (resultSet.next()) {
        String title = resultSet.getString("title");
        int aid = resultSet.getInt("aid");
        System.out.println(title + "  --  id: " + Integer.toString(aid));
      }
      System.out.println("=================================================================");
      System.out.println();

      resultSet.close();

    } catch (SQLException throwable) {
      throwable.printStackTrace();
    }
  }

  public void dispAlbumInfo(int aid) {
    // Create and execute the SQL query
    try {
      Statement statement = connection.createStatement();
      // Selecting from song based on the inputted title
      ResultSet resultSet =
          statement.executeQuery("SELECT \"title\" FROM \"Album\" WHERE aid = " + aid);
      if(!resultSet.next()) {
        System.out.println("No album with id: "+aid);
        return;
      }
      System.out.println("Title: " + resultSet.getString("title"));
      resultSet =
          statement.executeQuery("SELECT \"releasedate\" FROM \"Album\" WHERE aid = " + aid);
      resultSet.next();
      System.out.println("Release date: " + resultSet.getDate("releasedate"));
      resultSet = statement.executeQuery("SELECT arid FROM \"PublishesAlbum\" WHERE aid = " + aid);
      resultSet.next();
      int arid = resultSet.getInt("arid");
      resultSet = statement.executeQuery("SELECT \"name\" FROM \"Artist\" WHERE arid = " + arid);
      resultSet.next();
      System.out.println("Artist: " + resultSet.getString("name")+"  --  id: "+arid);
      resultSet.close();

    } catch (SQLException throwable) {
      throwable.printStackTrace();
    }
  }

  // search for anything matching string tok
  public boolean search(String tok) {
    if (connection == null) {
      return false;
    }
    // Create and execute the SQL query
    try {
      // Artist Search
      Statement statement = connection.createStatement();
      ResultSet resultSet =
          statement.executeQuery(
              "SELECT arid, name FROM \"Artist\" WHERE \"name\" LIKE \'%" + tok + "%\'");
      System.out.println("Artist Results:");
      System.out.println("=================================================================");
      while (resultSet.next()) {
        System.out.println(
            resultSet.getString("name")
                + "  --  id: "
                + Integer.toString(resultSet.getInt("arid")));
      }
      System.out.println("=================================================================");
      System.out.println();

      // Album Search
      resultSet =
          statement.executeQuery(
              "SELECT aid, title FROM \"Album\" WHERE \"title\" LIKE \'%" + tok + "%\'");
      System.out.println("Album Results:");
      System.out.println("=================================================================");
      while (resultSet.next()) {
        System.out.println(
            resultSet.getString("title")
                + "  --  id: "
                + Integer.toString(resultSet.getInt("aid")));
      }
      System.out.println("=================================================================");
      System.out.println();

      // Song Search
      resultSet =
          statement.executeQuery(
              "SELECT sid, title FROM \"Song\" WHERE \"title\" LIKE \'%" + tok + "%\'");
      System.out.println("Song Results:");
      System.out.println("=================================================================");
      while (resultSet.next()) {
        System.out.println(
            resultSet.getString("title")
                + "  --  id: "
                + Integer.toString(resultSet.getInt("sid")));
      }
      System.out.println("=================================================================");
      System.out.println();
      resultSet.close();

    } catch (SQLException throwable) {
      throwable.printStackTrace();
    }
    return true;
  }

  // search a user collection
  public boolean searchCollection(String user, String tok) {
    if (connection == null) {
      return false;
    }
    // Create and execute the SQL query
    try {
      Statement statement = connection.createStatement();

      // Artist Search
      ResultSet resultSet =
          statement.executeQuery(
              "SELECT a.arid, a.name "
                  + "FROM \"Artist\" a,  \"Collection\" c, \"ConsistsOfArtist\" ca "
                  + "WHERE c.cid = ca.cid AND ca.arid = a.arid "
                  + "AND a.\"name\" LIKE \'%"
                  + tok
                  + "%\' "
                  + "AND c.\"username\" = \'"
                  + user
                  + "\'");

      System.out.println("Artist Results:");
      System.out.println("=================================================================");
      while (resultSet.next()) {
        System.out.println(
            resultSet.getString("name")
                + "  --  id: "
                + Integer.toString(resultSet.getInt("arid")));
      }
      System.out.println("=================================================================");
      System.out.println();

      // Album Search
      resultSet =
          statement.executeQuery(
              "SELECT a.aid, a.title "
                  + "FROM \"Album\" a, \"Collection\" c, \"ConsistsOfAlbum\" ca "
                  + "WHERE c.cid = ca.cid AND ca.aid = a.aid "
                  + "AND \"title\" LIKE \'%"
                  + tok
                  + "%\' "
                  + "AND c.\"username\" = \'"
                  + user
                  + "\'");

      System.out.println("Album Results:");
      System.out.println("=================================================================");
      while (resultSet.next()) {
        System.out.println(
            resultSet.getString("title")
                + "  --  id: "
                + Integer.toString(resultSet.getInt("aid")));
      }
      System.out.println("=================================================================");
      System.out.println();

      // Song Search
      resultSet =
          statement.executeQuery(
              "SELECT s.sid, s.title "
                  + "FROM \"Song\" s, \"Collection\" c, \"ConsistsOfSong\" cs "
                  + "WHERE c.cid = cs.cid AND cs.sid = s.sid "
                  + "AND \"title\" LIKE \'%"
                  + tok
                  + "%\' "
                  + "AND c.\"username\" = \'"
                  + user
                  + "\'");

      System.out.println("Song Results:");
      System.out.println("=================================================================");
      while (resultSet.next()) {
        System.out.println(
            resultSet.getString("title")
                + "  --  id: "
                + Integer.toString(resultSet.getInt("sid")));
      }
      System.out.println("=================================================================");
      System.out.println();

      resultSet.close();

    } catch (SQLException throwable) {
      throwable.printStackTrace();
    }
    return true;
  }

  public void importSong() {
    if (connection != null) {

      // Create and execute the SQL query
      try {
        Scanner scan = new Scanner(System.in);
        Statement statement = connection.createStatement();
        ResultSet resultSet;
          resultSet = statement.executeQuery("SELECT MAX(sid) FROM \"Song\"");
          int sid = -1;
          while (resultSet.next()) {
              sid = resultSet.getInt("max");
          }
          sid++;
          System.out.println(sid);
        System.out.println("Enter Song Title: ");
        String Title = scan.nextLine();
        System.out.println("Enter Existing Album id");
        int aid = scan.nextInt();
        resultSet = statement.executeQuery("SELECT aid FROM \"Album\" WHERE aid = " + aid);
        if(!resultSet.next()){
          System.out.println("Please try import again with existing aid");
          return;
        }
        else{
          resultSet = statement.executeQuery("SELECT \"track_num\" FROM \"Song\" WHERE aid = " + aid);
          int i = 1;
          while(resultSet.next()){
            i++;
          }
          int trackNum = i;
          System.out.println("Enter Song length (in seconds)");
          int songLen = scan.nextInt();
          PreparedStatement insertStatement =
                  connection.prepareStatement("INSERT INTO \"Song\"(sid, title, track_num, length, aid) VALUES(?, ?, ?, ?, ?)");
          insertStatement.setObject(1, sid);
          insertStatement.setObject(2, Title);
          insertStatement.setObject(3, trackNum);
          insertStatement.setObject(4, songLen);
          insertStatement.setObject(5, aid);
          insertStatement.execute();
          insertStatement.close();

        }
        System.out.println("Imported new song with id: "+sid);
        resultSet.close();
      } catch (SQLException throwable) {
        throwable.printStackTrace();
      } catch(InputMismatchException i) {
        System.out.println("IDs must all be numbers!");
      }
    }
    return;
  }

  public boolean importArtist() {
    if (connection != null) {

      // Create and execute the SQL query
      try {
        Scanner scan = new Scanner(System.in);
        Statement statement = connection.createStatement();
        ResultSet resultSet;
        resultSet = statement.executeQuery("SELECT MAX(arid) FROM \"Artist\"");
        int arid = -1;
        while (resultSet.next()) {
            arid = resultSet.getInt("max");
        }
        arid++;
        System.out.println("Enter Artist Name: ");
        String Title = scan.nextLine();
        resultSet = statement.executeQuery("SELECT arid FROM \"Artist\" WHERE arid = " + arid);
        while (resultSet.next()){
          System.out.println("Artist with that ID already exists please enter new ID");
          arid = scan.nextInt();
          resultSet = statement.executeQuery("SELECT arid FROM \"Artist\" WHERE arid = " + arid);
        }
        PreparedStatement insertStatement =
                  connection.prepareStatement("INSERT INTO \"Artist\"(arid, name) VALUES(?, ?)");
        insertStatement.setObject(1, arid);
        insertStatement.setObject(2, Title);
        insertStatement.execute();
        insertStatement.close();
        System.out.println("Imported new artist with id: "+arid);
        resultSet.close();
      } catch (SQLException throwable) {
        throwable.printStackTrace();
      } catch(InputMismatchException i) {
        System.out.println("IDs must all be numbers!");
      }
    }
    return true;
  }

  public void importAlbum() {
      if (connection != null) {

          // Create and execute the SQL query
          try {
              Scanner scan = new Scanner(System.in);
              Statement statement = connection.createStatement();
              ResultSet resultSet;
              resultSet = statement.executeQuery("SELECT MAX(aid) FROM \"Album\"");
              int aid = -1;
              while (resultSet.next()) {
                  aid = resultSet.getInt("max");
              }
              aid++;
              System.out.println("Enter Album Title: ");
              String Title = scan.nextLine();
              System.out.println("What date was this album released (Format : YYYYMMDD)");
              /*
              int year = scan.nextInt();
              while (year < 1000 || year > 9999){
                  System.out.println("please enter a 4 digit year");
                  year = scan.nextInt();
              }
              System.out.println("What month was this album released");
              int month = 0;
              month = scan.nextInt();
              while (month < 1 || month > 12){
                  System.out.println("Please enter a valid month");
                  month = scan.nextInt();
              }
              System.out.println("What day of the month was this album released");
              int day = scan.nextInt();
              while (day < 1 || day > 31){
                  System.out.println("Please enter a valid day");
                  day = scan.nextInt();
              }*/
              long date = scan.nextLong();
              Date releaseDate = new Date(date);
              System.out.println("Enter Existing Artist id");
              int arid = scan.nextInt();
              resultSet = statement.executeQuery("SELECT arid FROM \"Artist\" WHERE arid = " + arid);
              if (!resultSet.next()) {
                  System.out.println("Please try import again with existing arid");
              } else {
                  System.out.println("Please enter a genre for the album");
                  String genre = scan.next();
                  PreparedStatement insertStatement =
                          connection.prepareStatement("INSERT INTO \"Album\"(aid, title, releasedate, genre) VALUES(?, ?, ?, ?)");
                  insertStatement.setObject(1, aid);
                  insertStatement.setObject(2, Title);
                  insertStatement.setObject(3, releaseDate);
                  insertStatement.setObject(4, genre);
                  insertStatement.execute();
                  insertStatement =
                          connection.prepareStatement("INSERT INTO \"PublishesAlbum\"(arid, aid) VALUES(?, ?)");
                  insertStatement.setObject(1, arid);
                  insertStatement.setObject(2, aid);
                  insertStatement.execute();
                  insertStatement.close();
              }
              System.out.println("Imported new album with id: "+aid);
              resultSet.close();
          } catch (SQLException throwable) {
              throwable.printStackTrace();
          } catch (InputMismatchException i) {
            System.out.println("Dates and IDs must all be numbers!");
          }
      }
  }
}
