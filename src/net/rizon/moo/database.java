package net.rizon.moo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class database
{
	private Connection con = null;
	
	public database() throws ClassNotFoundException, SQLException
	{
		Class.forName("org.sqlite.JDBC");
		this.con = DriverManager.getConnection(moo.conf.getDatabase());
	}
	
	public void shutdown()
	{
		try
		{
			this.con.close();
		}
		catch (SQLException ex) { }
	}
	
	private PreparedStatement last_statement = null;
	
	public PreparedStatement prepare(final String statement) throws SQLException
	{
		try
		{
			this.last_statement.close();
		}
		catch (Exception ex) { }
		
		this.last_statement = this.con.prepareStatement(statement);
		return this.last_statement;
	}
	
	public int executeUpdate(final String statement)
	{
		try
		{
			this.prepare(statement);
			return this.executeUpdate();
		}
		catch (SQLException ex)
		{
			System.out.println("Error executing SQL statement: " + statement);
			ex.printStackTrace();
			return 0;
		}
	}
	
	public ResultSet executeQuery(final String statement) throws SQLException
	{
		this.prepare(statement);
		return this.executeQuery();
	}
	
	public int executeUpdate()
	{
		try
		{
			if (moo.conf.getDebug() > 0)
				System.out.println("Executing query: " + this.last_statement.toString());
			return this.last_statement.executeUpdate();
		}
		catch (SQLException ex)
		{
			System.out.println("Error executing SQL statement: " + this.last_statement.toString());
			ex.printStackTrace();
			return 0;
		}
	}
	
	public ResultSet executeQuery() throws SQLException
	{
		if (moo.conf.getDebug() > 0)
			System.out.println("Executing query: " + this.last_statement.toString());
		return this.last_statement.executeQuery();
	}
	
	public static void handleException(SQLException ex)
	{
		ex.printStackTrace();
	}
}