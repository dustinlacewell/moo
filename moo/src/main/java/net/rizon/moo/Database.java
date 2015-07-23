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

	public synchronized PreparedStatement prepare(final String statement) throws SQLException
	{
		return this.con.prepareStatement(statement);
	}

	public synchronized ResultSet executeQuery(final String statement) throws SQLException
	{
		return this.executeQuery(this.prepare(statement));
	}

	public synchronized ResultSet executeQuery(PreparedStatement ps) throws SQLException
	{
		log.log(Level.FINE, "Executing query: " + ps.toString());
		ResultSet rs = ps.executeQuery();
		//closeStatement(ps);
		return rs;
	}

	public synchronized int executeUpdate(final String statement)
	{
		PreparedStatement ps;

		try
		{
			ps = this.prepare(statement);
		}
		catch (SQLException ex)
		{
			log.log(Level.SEVERE, "Error preparing SQL statement: " + statement, ex);
			return 0;
		}

		return this.executeUpdate(ps);
	}

	public synchronized int executeUpdate(PreparedStatement ps)
	{
		try
		{
			log.log(Level.FINE, "Executing query: " + ps.toString());
			return ps.executeUpdate();
		}
		catch (SQLException ex)
		{
			log.log(Level.SEVERE, "Error executing SQL statement: " + ps.toString(), ex);
			return 0;
		}
		finally
		{
			closeStatement(ps);
		}
	}

	private void closeStatement(PreparedStatement ps)
	{
		try
		{
			ps.close();
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, "Failure to close PreparedStatement", e);
		}
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
