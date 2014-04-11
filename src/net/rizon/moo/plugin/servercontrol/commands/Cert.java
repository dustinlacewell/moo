package net.rizon.moo.plugin.servercontrol.commands;

import java.io.File;

import net.rizon.moo.Command;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.plugin.servercontrol.Connection;
import net.rizon.moo.plugin.servercontrol.EchoProcess;
import net.rizon.moo.plugin.servercontrol.FileDownload;
import net.rizon.moo.plugin.servercontrol.FileUpload;
import net.rizon.moo.plugin.servercontrol.ServerInfo;
import net.rizon.moo.plugin.servercontrol.servercontrol;

class CertCommand extends Thread
{
	private String source, target;
	private String server;
	
	CertCommand(String source, String target, String server)
	{
		this.source = source;
		this.target = target;
		this.server = server;
	}
	
	@Override
	public void run()
	{
		ServerInfo si = servercontrol.findGeoServer(server);
		
		if (si == null)
		{
			Moo.reply(source, target, "No such server " + server);
			return;
		}
		
		String caserver = Moo.conf.getString("servercontrol.cert.caserver");
		ServerInfo siCA = servercontrol.findGeoServer(caserver);
		if (siCA == null)
		{
			Moo.reply(source, target, "Unable to find CA server");
			return;
		}
		
		Moo.reply(source, target, "Creating certificate ...");
		
		Connection con = Connection.findOrCreateConncetion(siCA), destCon = Connection.findOrCreateConncetion(si);
		
		new EchoProcess(con, source, target, "EASYRSA=" + Moo.conf.getString("servercontrol.cert.easyrsa") + " " + Moo.conf.getString("servercontrol.cert.easyrsa") + "/easyrsa --req-cn=" + server + " gen-req " + server + " nopass").run();
		new EchoProcess(con, source, target, "EASYRSA=" + Moo.conf.getString("servercontrol.cert.easyrsa") + " " + Moo.conf.getString("servercontrol.cert.easyrsa") + "/easyrsa sign-req server " + server).run();
		
		new File(System.getProperty("java.io.tmpdir"), "moo").mkdir();
		new FileDownload(con, Moo.conf.getString("servercontrol.cert.easyrsa") + "/pki/issued/" + server + ".crt", System.getProperty("java.io.tmpdir") + File.separator + "moo" + File.separator + server + ".crt").run();
		new FileDownload(con, Moo.conf.getString("servercontrol.cert.easyrsa") + "/pki/private/" + server + ".key", System.getProperty("java.io.tmpdir") + File.separator + "moo" + File.separator + server + ".key").run();
		
		new FileUpload(destCon, new File(System.getProperty("java.io.tmpdir") + File.separator + "moo", server + ".crt"), "ircd/keys/cert.pem").run();
		new FileUpload(destCon, new File(System.getProperty("java.io.tmpdir") + File.separator + "moo", server + ".key"), "ircd/keys/" + server + ".key").run();
		
		new EchoProcess(destCon, source, target, "openssl rsa -in ircd/keys/" + server + ".key -pubout -out ircd/keys/" + server + ".pub").run();
		
		Moo.reply(source, target, "Done!");
	}
}

class CertCommandRevoke extends Thread
{
	private String source, target;
	private String server;
	
	CertCommandRevoke(String source, String target, String server)
	{
		this.source = source;
		this.target = target;
		this.server = server;
	}
	
	@Override
	public void run()
	{
		String caserver = Moo.conf.getString("servercontrol.cert.caserver");
		ServerInfo siCA = servercontrol.findGeoServer(caserver);
		if (siCA == null)
		{
			Moo.reply(source, target, "Unable to find CA server");
			return;
		}
		
		Moo.reply(source, target, "Revoking certificate...");
		
		String command = Moo.conf.getString("servercontrol.cert.easyrsa") + "/easyrsa revoke " + server + " && " + Moo.conf.getString("servercontrol.cert.easyrsa") + "/easyrsa gen-crl";
		
		Connection con = Connection.findOrCreateConncetion(siCA);
		new EchoProcess(con, source, target, command).run();
	}
}

public class Cert extends Command
{

	public Cert(Plugin pkg)
	{
		super(pkg, "!CERT", "Generate server SSL certificates");
		this.requiresChannel(Moo.conf.getList("admin_channels"));
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
		String arg = params.length > 2 ? params[2] : null;
		
		if (arg != null && arg.equals("revoke"))
		{
			new CertCommandRevoke(source, target, server).start();
			return;
		}
		
		new CertCommand(source, target, server).start();
	}
}
