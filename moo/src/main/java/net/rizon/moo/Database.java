package net.rizon.moo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import net.rizon.moo.conf.DatabaseConfiguration;

public class Database
{
	private static final Logger logger = LoggerFactory.getLogger(Database.class);

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

	public synchronized ResultSet executeQuery(PreparedStatement ps) throws SQLException
	{
		logger.debug("Executing query: {}", ps.toString());
		return ps.executeQuery();
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
			logger.error("Error preparing SQL statement: " + statement, ex);
			return 0;
		}

		return this.executeUpdate(ps);
	}

	public synchronized int executeUpdate(PreparedStatement ps)
	{
		try
		{
			logger.debug("Executing query: {}", ps.toString());
			return ps.executeUpdate();
		}
		catch (SQLException ex)
		{
			logger.error("Error executing SQL statement: " + ps.toString(), ex);
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
			logger.error("Failure to close PreparedStatement", e);
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
		logger.error("Database exception", ex);
	}
}
