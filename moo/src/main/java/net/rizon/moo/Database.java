package net.rizon.moo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

import net.rizon.moo.conf.DatabaseConfiguration;

public class Database
{
	private static final Logger log = Logger.getLogger(Database.class.getName());

	private Connection con = null;

	/**
	 * Constructs a new database connector.
	 * @param database Configuration details for database.
	 * @throws ClassNotFoundException When unable to load class.
	 * @throws SQLException
	 */
	public Database(final DatabaseConfiguration database) throws ClassNotFoundException, SQLException
	{
		Class.forName("org.sqlite.JDBC");
		this.con = DriverManager.getConnection(database.connection);
	}

	public synchronized void shutdown()
	{
		try
		{
			this.con.close();
		}
		catch (SQLException ex) { }
	}

	private PreparedStatement last_statement = null;

	public synchronized PreparedStatement prepare(final String statement) throws SQLException
	{
		try
		{
			this.last_statement.close();
		}
		catch (Exception ex) { }

		this.last_statement = this.con.prepareStatement(statement);
		return this.last_statement;
	}

	public synchronized void detach()
	{
		this.last_statement = null;
	}

	public synchronized int executeUpdate(final String statement)
	{
		try
		{
			this.prepare(statement);
			return this.executeUpdate();
		}
		catch (SQLException ex)
		{
			log.log(Level.SEVERE, "Error executing SQL statement: " + statement, ex);
			return 0;
		}
	}

	public synchronized ResultSet executeQuery(final String statement) throws SQLException
	{
		this.prepare(statement);
		return this.executeQuery();
	}

	public synchronized int executeUpdate()
	{
		try
		{
			log.log(Level.FINE, "Executing query: " + this.last_statement.toString());
			return this.last_statement.executeUpdate();
		}
		catch (SQLException ex)
		{
			log.log(Level.SEVERE, "Error executing SQL statement: " + this.last_statement.toString(), ex);
			return 0;
		}
	}

	public synchronized ResultSet executeQuery() throws SQLException
	{
		log.log(Level.FINE, "Executing query: " + this.last_statement.toString());
		return this.last_statement.executeQuery();
	}

	public synchronized void setAutoCommit(boolean state) throws SQLException
	{
		this.con.setAutoCommit(state);
	}

	public synchronized Connection getConnection()
	{
		return this.con;
	}

	public static void handleException(SQLException ex)
	{
		Logger.getGlobalLogger().log(ex);
	}
}