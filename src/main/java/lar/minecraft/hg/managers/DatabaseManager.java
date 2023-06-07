package lar.minecraft.hg.managers;
import java.lang.reflect.Field;
import java.sql.*;
import lar.minecraft.hg.dbModels.imodel;

public class DatabaseManager {
	
	private static String connectionString = "jdbc:mysql://localhost:3306/hunger_games";
	private static String databaseUser = "multicraft";
	private static String databasePassword = "4E3M4dYSlP9g2yC#";
	private static Connection dbConnection;
	
	private static void connectToDatabase() throws SQLException, ClassNotFoundException {
		if (dbConnection != null) {
			if (!dbConnection.isClosed()) {
				return;
			}
		}
		
		Class.forName("com.mysql.jdbc.Driver");
		dbConnection = DriverManager.getConnection(connectionString, databaseUser, databasePassword);
	}
	
	private static void disconnectToDatabase() throws SQLException, ClassNotFoundException {
		if (dbConnection != null) {
			if (!dbConnection.isClosed()) {
				dbConnection.close();				
			}
		}
	}

	public static int ExecuteRead(String table, String whereCondition) throws SQLException, ClassNotFoundException {
		connectToDatabase();
		int count = 0;

		Statement statement = dbConnection.createStatement();
		ResultSet resultSet = statement.executeQuery(getReadQuery(table, whereCondition));
		
		while (resultSet.next()) {
			count++; 
		}
		disconnectToDatabase();
        return count;
	}
	
	public static void ExecuteInsert(String table, imodel model) throws SQLException, ClassNotFoundException {
		connectToDatabase();

		Statement statement = dbConnection.createStatement();
		try {
			statement.executeUpdate(getInsertQuery(table, model));
		} catch (IllegalArgumentException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		disconnectToDatabase();
	}
	
	public static void ExecuteUpdate(String table, String fieldsToChange, String whereCondition) throws SQLException, ClassNotFoundException {
		connectToDatabase();

		Statement statement = dbConnection.createStatement();
		try {
			statement.executeUpdate(getupdatetQuery(table, fieldsToChange, whereCondition));
		} catch (IllegalArgumentException | SQLException e) {
			e.printStackTrace();
		}
		
		disconnectToDatabase();
	}
	
	/**
	 * Retrieve read query
	 * @param model of the object to read
	 * @return string for read
	 */
	public static String getReadQuery(String table, String whereCondition) {
		if (table == null) {
			throw new NullPointerException();
		}
		
		String query = "SELECT * FROM " + table;
		if (whereCondition != null) {
			query = query + " WHERE " + whereCondition;
		}
		
		return query;
	}
	
	/**
	 * Retrieve insert query
	 * @param model of the object to insert
	 * @return string for insert
	 */
	public static String getInsertQuery(String table, imodel model){
		if (table == null) {
			throw new NullPointerException();
		}
		if (model == null) {
			throw new NullPointerException();
		}
		
		Field[] fields = model.getClass().getDeclaredFields();
		int i = 0;

		String query = "INSERT INTO " + table + " (";
		for (Field field : fields) {
			query = query + field.getName();
			if( i < fields.length - 1) {
	            query = query + ",";	
			}
            i++;
        }
		i = 0;
		query = query + ") VALUES (";
		for (Field field : fields) {
			String fieldType = field.getType().toString();
            try {
            	
				if(fieldType.compareToIgnoreCase("int") == 0) {
					query = query + field.getInt(model);
				}else {
					if (field.get(model) == null) {
						query = query + "null";
					}else {
						query = query + "'" + field.get(model).toString() + "'";
					}
				}

    			if( i < fields.length - 1) {
    	            query = query + ",";	
    			}
                i++;
			} catch (IllegalArgumentException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
		query = query + ");";
		return query;
	}
	
	/**
	 * Retrieve update query
	 * @param model of the object to change
	 * @param fieldsToChange in SQL format
	 * @param whereCondition in SQL format
	 * @return string for update
	 */
	public static String getupdatetQuery(String table, String fieldsToChange, String whereCondition){
		if (table == null) {
			throw new NullPointerException();
		}
		if (fieldsToChange == null) {
			throw new NullPointerException();
		}
		
		String query = "UPDATE " + table + " SET " + fieldsToChange;
		if (whereCondition != null) {
			query = query + " WHERE " + whereCondition;
		}
		query = query + ";";
		return query;
	}
}
