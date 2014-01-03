package net.rizon.moo.servercontrol.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;

import net.rizon.moo.Command;
import net.rizon.moo.Logger;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.servercontrol.Connection;
import net.rizon.moo.servercontrol.FileDownload;
import net.rizon.moo.servercontrol.FileUpload;
import net.rizon.moo.servercontrol.Protocol;
import net.rizon.moo.servercontrol.ServerInfo;
import net.rizon.moo.servercontrol.UploadProcess;
import net.rizon.moo.servercontrol.servercontrol;

class CSRUploadProcess extends UploadProcess
{
	private String s, t;
	private String server;
	
	CSRUploadProcess(Connection con, File file, String source, String target, String args)
	{
		super(con, file, source, target, args);
		server = args;
		s = source;
		t = target;
	}
	
	@Override
	public void onFinish()
	{
		super.onFinish();

		new File(System.getProperty("java.io.tmpdir"), "moo").mkdir();
		
		new CSRDownloadProcess(con, "ircd/keys/" + server + ".csr", System.getProperty("java.io.tmpdir") + File.separator + "moo" + File.separator + server + ".csr", server, s, t).run();
	}
}

class CSRDownloadProcess extends FileDownload
{
	private String s, t;
	private String server;
	private String dest;
	
	CSRDownloadProcess(Connection con, String file, String dest, String server, String source, String target)
	{
		super(con, file, dest);
		this.server = server;
		s = source;
		t = target;
		this.dest = dest;
	}
	
	@Override
	public void onFinish()
	{
		super.onFinish();
		
		try
		{
			String cmds[] = new String[4];
			cmds[0] = "/bin/sh";
			cmds[1] = "sign.sh";
			cmds[2] = dest;
			cmds[3] = server;
			
			Process proc = Runtime.getRuntime().exec(cmds, null, new File("ssl"));

			BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			for (String s; (s = in.readLine()) != null;)
				for (String ch : Moo.conf.getList("moo_log_channels"))
					Moo.privmsg(ch, "[" + this.con.getServerInfo().name + "] " + s);
			in.close();

			in = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
			for (String s; (s = in.readLine()) != null;)
				for (String ch : Moo.conf.getList("moo_log_channels"))
					Moo.privmsg(ch, "[" + this.con.getServerInfo().name + "] " + s);
			in.close();

			proc.getOutputStream().close();
		}
		catch (IOException ex)
		{
			Logger.getGlobalLogger().log(Level.WARNING, "Error signing certificate request", ex);
		}
		
		/* upload pems/server.pem back now */
		new CertUploader(con, new File("ssl" + File.separator + "pems" + File.separator + server + ".pem"), server, s, t).run();
	}
}

class CertUploader extends FileUpload
{
	private String server, s, t;
	
	public CertUploader(Connection con, File file, String server, String source, String target)
	{
		super(con, file, "ircd/keys/cert.pem");
		this.server = server;
		s = source;
		t = target;
	}
	
	@Override
	public void onFinish()
	{
		super.onFinish();
		
		Moo.reply(s, t, "Finished generating and signing cert for " + server);
	}
}

public class Cert extends Command
{
	public Cert(Plugin pkg)
	{
		super(pkg, "!CERT", "Generate server SSL certificates");
	}

	@Override
	public void execute(String source, String target, String[] params)
	{
		if (params.length < 2)
		{
			Moo.reply(source, target, "Syntax: !CERT server.name [revoke]");
			return;
		}
		
		String server = params[1];
		
		Protocol proto = Protocol.findProtocol("ssh");
		if (proto == null)
		{
			Moo.reply(source, target, "No such protocol SSH?");
			return;
		}
		
		ServerInfo[] server_info = servercontrol.findServers(server, proto.getProtocolName());
		if (server_info == null)
		{
			Moo.reply(source, target, "No servers found for " + server + " using " + proto.getProtocolName());
			return;
		}
		
		if (server_info.length != 1)
		{
			Moo.reply(source, target, "Multiple servers match " + server);
			return;
		}
		
		File f = new File("ssl", "csr.sh");
		if (!f.exists())
		{
			Moo.reply(source, target, "csh.sh does not exist?");
			return;
		}
		
		ServerInfo si = server_info[0];
		
		if (params.length == 3 && params[2].equalsIgnoreCase("revoke"))
		{
			try
			{
				String cmds[] = new String[3];
				cmds[0] = "/bin/sh";
				cmds[1] = "revoke.sh";
				cmds[2] = server;
				
				Process proc = Runtime.getRuntime().exec(cmds, null, new File("ssl"));
	
				BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
				for (String s; (s = in.readLine()) != null;)
					for (String ch : Moo.conf.getList("moo_log_channels"))
						Moo.privmsg(ch, s);
				in.close();
	
				in = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
				for (String s; (s = in.readLine()) != null;)
					for (String ch : Moo.conf.getList("moo_log_channels"))
						Moo.privmsg(ch, s);
				in.close();
	
				proc.getOutputStream().close();
				
				Moo.reply(source, target, "Done");
			}
			catch (IOException ex)
			{
				Logger.getGlobalLogger().log(Level.WARNING, "Error signing certificate request", ex);
			}
			
			return;
		}
		
		try
		{
			Connection con = Connection.findOrCreateConncetion(si);
			CSRUploadProcess proc = new CSRUploadProcess(con, f, source, target, server);
			proc.start();
		}
		catch (Exception ex)
		{
			Moo.reply(source, target, "Error executing command on " + si.host + ": " + ex.getMessage());
		}
	}
}
