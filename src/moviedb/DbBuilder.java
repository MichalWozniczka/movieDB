package moviedb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javafx.util.Pair;

public class DbBuilder {
	
	public static void createTables(Connection conn) {
		System.out.println("creating tables");
		String createFilmTable = 
				"create table films (" +
					"film_id char(9) primary key," +
					"title text," +
					"year int," +
					"runtime int," +
					"genres text[]," +
					"crew_ids text[]," +
					"imdb float," +
					"imdb_numVotes int," +
					"rt float," +
					"rt_numVotes int," +
					"regions text[]" +
				");";
		
		String createCrewTable = 
				"create table crew (" +
					"name_id char(9) primary key," +
					"name text," +
					"titles text[]" +
				");";
				
		try {
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(createFilmTable);
			stmt.close();
		}
		catch(SQLException e) {
			System.out.println("Message: " + e.getMessage());
		}
		
		try {
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(createCrewTable);
			stmt.close();
		}
		catch(SQLException e) {
			System.out.println("Message: " + e.getMessage());
		}
	}
	
	//used to update film table by passing lines from title basics file
	public static void insertRowToFilmTableFromBasics(Connection conn, String inputLine) {
		String[] line = inputLine.trim().split("\t");
		if(!line[1].equals("movie")) return;
		String[] genres = line[8].trim().split(",");
		
		String genresString = "'{";
		if(genres[0].equals("\\N")) {
			genresString = "null";
		}
		else {
			for(int i = 0; i < genres.length; i++) {
				genresString += "\"" + genres[i] + "\",";
			}
			genresString = genresString.substring(0, genresString.length()-1);
			genresString += "}'";
		}
		
		line[2] = line[2].replaceAll("'", "''");
		line[5] = line[5].replaceAll("\\\\N", "null");
		line[7] = line[7].replaceAll("\\\\N", "null");
		
		String insertString = "insert into films (film_id,title,year,runtime,genres) values ('" + 
				line[0] + "','" +
				line[2] + "'," +
				line[5] + "," +
				line[7] + "," +
				genresString +
				");";
		
		try {
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(insertString);
			stmt.close();
		}
		catch(SQLException e) {
			System.out.println("Message: " + e.getMessage());
			System.out.println(insertString);
		}
	}
	
	public static void updateRowInFilmTableWithCrew(Connection conn, String inputLine) {
		String[] line = inputLine.trim().split("\t");
		
		String updateString = "update films " +
			"set crew_ids = crew_ids || '{\"" + line[2] + "\"}'" +
			" where film_id = '" + line[0] + "';";
		
		try {
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(updateString);
			stmt.close();
		}
		catch(SQLException e) {
			System.out.println("Message: " + e.getMessage());
			System.out.println(updateString);
		}
	}
	
	public static void updateRowInFilmTableWithRatings(Connection conn, String inputLine) {
		String[] line = inputLine.trim().split("\t");
		
		String updateString = "update films " +
			"set imdb = " + line[1] + "," +
				"imdb_numVotes = " + line[2] +
			" where film_id = '" + line[0] + "';";
		
		try {
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(updateString);
			stmt.close();
		}
		catch(SQLException e) {
			System.out.println("Message: " + e.getMessage());
			System.out.println(updateString);
		}
	}
	
	public static void updateRowInFilmTableWithRegions(Connection conn, String inputLine) {
		String[] line = inputLine.trim().split("\t");
		
		String updateString = "update films " +
		    "set regions = regions || '{\"" + line[3] + "\"}'" +
			" where film_id = '" + line[0] + "';";
		
		try {
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(updateString);
			stmt.close();
		}
		catch(SQLException e) {
			System.out.println("Message: " + e.getMessage());
			System.out.println(updateString);
		}
	}
	
	//used to update film table by passing lines from title basics file
	public static void insertRowToCastAndCrewTableFromBasics(Connection conn, String inputLine) {
		String[] line = inputLine.trim().split("\t");
		String[] titles = line[5].trim().split(",");
		
		String titlesString = "'{";
		for(int i = 0; i < titles.length; i++) {
			titlesString += "\"" + titles[i] + "\",";
		}
		titlesString = titlesString.substring(0, titlesString.length()-1);
		titlesString += "}'";
		
		line[1] = line[1].replaceAll("'", "''");
		
		String insertString = "insert into cast_and_crew values ('" + 
				line[0] + "','" +
				line[1] + "'," +
				titlesString +
				");";
		
		try {
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(insertString);
			stmt.close();
		}
		catch(SQLException e) {
			System.out.println("Message: " + e.getMessage());
			System.out.println(insertString);
		}
	}
	
	public static ArrayList<Pair<String, String>> getCharToURLDict() {
		ArrayList<Pair<String, String>> m = new ArrayList<Pair<String, String>>();

		m.add(new Pair<>("%", "%25"));
		m.add(new Pair<>("!", "%21"));
		m.add(new Pair<>("\"", "%22"));
		m.add(new Pair<>("#", "%23"));
		m.add(new Pair<>("\\$", "%24"));
		m.add(new Pair<>("&", "%26"));
		m.add(new Pair<>("'", "%27"));
		m.add(new Pair<>("\\(", "%28"));
		m.add(new Pair<>("\\)", "%29"));
		m.add(new Pair<>("\\*", "%2A"));
		m.add(new Pair<>("\\+", "%2B"));
		m.add(new Pair<>(",", "%2C"));
		m.add(new Pair<>("-", "%2D"));
		m.add(new Pair<>("\\.", "%2E"));
		m.add(new Pair<>("/", "%2F"));
		m.add(new Pair<>(":", "%3A"));
		m.add(new Pair<>(";", "%3B"));
		m.add(new Pair<>("<", "%3C"));
		m.add(new Pair<>("=", "%3D"));
		m.add(new Pair<>(">", "%3E"));
		m.add(new Pair<>("\\?", "%3F"));
		m.add(new Pair<>("@", "%40"));
		m.add(new Pair<>("\\[", "%5B"));
		m.add(new Pair<>("\\\\", "%5C"));
		m.add(new Pair<>("]", "%5D"));
		m.add(new Pair<>("\\^", "%5E"));
		m.add(new Pair<>("_", "%5F"));
		m.add(new Pair<>("`", "%7A"));
		m.add(new Pair<>("\\{", "%7B"));
		m.add(new Pair<>("\\|", "%7C"));
		m.add(new Pair<>("\\}", "%7D"));
		m.add(new Pair<>("~", "%7E"));
		m.add(new Pair<>(" ", "+"));
		
		return m;
	}
	
	public static ArrayList<Pair<String, String>> getCharToCSSDict() {
		ArrayList<Pair<String, String>> m = new ArrayList<Pair<String, String>>();

		m.add(new Pair<>("%", "\\\\%"));
		m.add(new Pair<>("!", "\\\\!"));
		m.add(new Pair<>("\"", "\\\""));
		m.add(new Pair<>("#", "\\\\#"));
		m.add(new Pair<>("\\$", "\\\\$"));
		m.add(new Pair<>("&", "\\\\&"));
		m.add(new Pair<>("'", "\\\\'"));
		m.add(new Pair<>("\\(", "\\\\("));
		m.add(new Pair<>("\\)", "\\\\)"));
		m.add(new Pair<>("\\*", "\\\\*"));
		m.add(new Pair<>("\\+", "\\\\+"));
		m.add(new Pair<>(",", "\\\\,"));
		m.add(new Pair<>("\\.", "\\\\."));
		m.add(new Pair<>("/", "\\\\/"));
		m.add(new Pair<>(":", "\\\\:"));
		m.add(new Pair<>(";", "\\\\;"));
		m.add(new Pair<>("<", "\\\\<"));
		m.add(new Pair<>("=", "\\\\="));
		m.add(new Pair<>(">", "\\\\>"));
		m.add(new Pair<>("\\?", "\\\\?"));
		m.add(new Pair<>("@", "\\\\@"));
		m.add(new Pair<>("\\[", "\\\\["));
		m.add(new Pair<>("\\\\", "\\\\"));
		m.add(new Pair<>("]", "\\\\]"));
		m.add(new Pair<>("\\^", "\\\\^"));
		m.add(new Pair<>("`", "\\\\`"));
		m.add(new Pair<>("\\{", "\\\\{"));
		m.add(new Pair<>("\\|", "\\\\|"));
		m.add(new Pair<>("\\}", "\\\\}"));
		m.add(new Pair<>("~", "\\\\~"));
		
		return m;
	}
	
	public static String fixString(String in, ArrayList<Pair<String, String>> dict) {
		for(Pair<String, String> p : dict) {
			in = in.replaceAll(p.getKey(), p.getValue());
		}
		
		return in;
	}
	
	public static String isolateJSON(String elem) {
		int front_count = 0;
		int front_pos = 0;
		int back_count = 0;
		int back_pos = elem.length();
		for(int i = 0; i < elem.length(); i++) {
			if(elem.charAt(i) == '{') {
				front_count++;
			}
			if(front_count == 2) {
				front_pos = i;
				break;
			}
		}
		for(int i = elem.length()-1; i >= 0; i--) {
			if(elem.charAt(i) == '}') {
				back_count++;
			}
			if(back_count == 2) {
				back_pos = i;
				break;
			}
		}
		
		return elem.substring(front_pos, back_pos+1);
	}
	
	public static void updateFilmTableWithRTRatings(Connection conn) throws IOException, ParseException {
		String selectString = "select film_id, title, year from films where year > 1966 order by year";
		
		ResultSet rs = null;
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(selectString);
		}
		catch(SQLException e) {
			System.out.println("Message: " + e.getMessage());
			System.out.println(selectString);
		}
		
		ArrayList<Pair<String, String>> charToURL = getCharToURLDict();
		
		try {
			while(rs.next()) {
			//rs.next();
				String film_id = rs.getString("film_id");
				String title = rs.getString("title");
				String ftitle = fixString(title, charToURL);
				if(rs.getString("year") == null) {
					continue;
				}
				int year = Integer.parseInt(rs.getString("year"));
				System.out.println(title + " " + year);

				String url = "https://www.rottentomatoes.com/search/?search=" + ftitle;
				//url = "https://www.rottentomatoes.com/search/?search=hot+rod";
				//year = 1979;
				System.out.println(url);
				Document doc = Jsoup.connect(url).get();
				
				if(doc.select("h1.center.noresults").size() == 0) {
					Element main_container = doc.selectFirst("#main_container");
					Element script = main_container.selectFirst("script");
					
					JSONParser parser = new JSONParser();
					JSONObject json = (JSONObject) parser.parse(isolateJSON(script.toString()));
					System.out.println(isolateJSON(script.toString()));
					
					if(((long)json.get("movieCount")) == 0) {
						continue;
					}
					JSONArray movies = (JSONArray) json.get("movies");
					Iterator<JSONObject> it = movies.iterator();
					while(it.hasNext()) {
						JSONObject movie = it.next();
						if(((String)movie.get("name")).equals(title) && movie.get("year") != null && Math.abs(year - (long)movie.get("year")) <= 2 && !((String)movie.get("meterClass")).equals("N/A")) {
							String score = Long.toString((long)movie.get("meterScore"));
							Statement stmt2 = conn.createStatement();
							stmt2.executeUpdate("update films set rt = " + score + " where film_id = '" + film_id + "'");
							stmt2.close();
							System.out.println("Score: " + score + "\n");
						}
					}
				}
			}
			stmt.close();
		} catch (SQLException e) {
			System.out.println("Message: " + e.getMessage());
		}
	}
	
	public static void dropTables(Connection conn, String tables) {
		System.out.println("dropping tables");
		String dropString = "drop table " + tables;
		try {
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(dropString);
			stmt.close();
		}
		catch(SQLException e) {
			System.out.println("Message: " + e.getMessage());
		}
	}
	
	public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException, ParseException {
		System.out.println("loading driver");
		try {
			Class.forName("org.postgresql.Driver");
		}
		catch (ClassNotFoundException e) {}
		System.out.println("driver loaded");
		System.out.println("Connecting to DB");
		System.out.println("Connected to DB");
		
		/*dropTables(conn, "films");
		
		createTables(conn);

		System.out.println("Adding titles.");
		try(BufferedReader br = new BufferedReader(new FileReader("titles.tsv"))) {
			String line = br.readLine();
			while((line = br.readLine()) != null) {
				insertRowToFilmTableFromBasics(conn, line);
			}
		} catch (IOException e) {
			System.out.println("file not found");
		}
		System.out.println("Titles added.");
		
		System.out.println("Adding cast and crew.");
		try(BufferedReader br = new BufferedReader(new FileReader("principals.tsv"))) {
			String line = br.readLine();
			while((line = br.readLine()) != null) {
				updateRowInFilmTableWithCrew(conn, line);
			}
		} catch (IOException e) {
			System.out.println("file not found");
		}
		System.out.println("Cast and crew added.");
		
		System.out.println("Adding ratings.");
		try(BufferedReader br = new BufferedReader(new FileReader("ratings.tsv"))) {
			String line = br.readLine();
			while((line = br.readLine()) != null) {
				updateRowInFilmTableWithRatings(conn, line);
			}
		} catch (IOException e) {
			System.out.println("file not found");
		}
		System.out.println("Ratings added.");*/
		
		/*System.out.println("Adding regions.");
		try(BufferedReader br = new BufferedReader(new FileReader("akas.tsv"))) {
			String line = br.readLine();
			while((line = br.readLine()) != null) {
				updateRowInFilmTableWithRegions(conn, line);
			}
		} catch (IOException e) {
			System.out.println("file not found");
		}
		System.out.println("Regions added.");
		
		System.out.println("Adding names.");
		try(BufferedReader br = new BufferedReader(new FileReader("names.tsv"))) {
			String line = br.readLine();
			while((line = br.readLine()) != null) {
				insertRowToCastAndCrewTableFromBasics(conn, line);
			}
		} catch (IOException e) {
			System.out.println("file not found");
		}
		System.out.println("Names added.");*/
		
		updateFilmTableWithRTRatings(conn);
	}
}