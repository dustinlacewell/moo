package net.rizon.moo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

public final class ClassLoader extends java.lang.ClassLoader
{
	private static final Logger log = Logger.getLogger(ClassLoader.class.getName());
	private static final String bin = "target/classes";
	
	private String base;
	
	public ClassLoader(String base, java.lang.ClassLoader cl)
	{
		super(cl);
		this.base = base;
	}
	
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException
	{
		if (!name.startsWith(base))
			return super.loadClass(name);
		
		log.log(Level.FINE, "Loading " + name);
		
		File f;
		try
		{
			f = new File(ClassLoader.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		}
		catch (Exception ex)
		{
			f = null;
		}
		
		InputStream is = null;
		JarFile jf = null;
		
		try
		{
			if (f != null && f.getName().endsWith(".jar"))
			{
				jf = new JarFile(f);
				JarEntry je = jf.getJarEntry(name.replace('.', '/') + ".class");
				is = jf.getInputStream(je);
			}
			else
			{
				is = new FileInputStream(bin + name.replace('.', '/') + ".class");
			}
	
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		
			for (int c; (c = is.read()) != -1;)
				buffer.write(c);
				
			byte[] newClass = buffer.toByteArray();

			Class<?> c = this.defineClass(name, newClass, 0, newClass.length);
			return c;
		}
		catch (Exception ex)
		{
			log.log(Level.SEVERE, "Exception while loading class", ex);
			throw new ClassNotFoundException(ex.getMessage());
		}
		finally
		{
			try { is.close(); } catch (Exception ex) { }
			try { jf.close(); } catch (Exception ex) { }
		}
	}
}