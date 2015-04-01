package de.mpicbg.scf;

import de.mpicbg.scf.compiler.CompilerUtils;
import de.mpicbg.scf.compiler.PluginRuntime;
import ij.IJ;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

import javax.swing.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by moon on 4/10/15.
 */
public class JavaTask implements PlugIn
{
	protected Class plugin;
	boolean bStandAlone = true;

	public JavaTask()
	{
		// Add necessary classes for CompilerUtils and JavaLanguageSupport for editor
		try
		{
			String str = IJ.getClassLoader().loadClass( "de.mpicbg.scf.Editor" ).getResource( "" ).toString();

			//			System.err.println(str);

			if ( str.startsWith( "jar:file" ) )
				bStandAlone = false;

			if ( bStandAlone )
			{
				str = "res/lib/ij-1.49q.jar";

				if ( new File( str ).exists() )
					CompilerUtils.addClassPath( str );

				CompilerUtils.addClassPath( "res/lib/SPIM_Registration-2.2.0.jar" );
			}
			else
			{
				// plugins
				String pluginJarFile = str.replace( "jar:file:", "" ).replace( "!/de/mpicbg/scf/", "" );
				File parent = new File(pluginJarFile).getParentFile();

				for(File f : parent.listFiles())
				{
					if(f.isFile())
					{
						CompilerUtils.addClassPath( f.getPath() );
					}
				}

				// jars
				str = IJ.class.getResource("IJ.class").getPath();
				String ijJar = str.replace( "file:", "" ).replace( "!/ij/IJ.class", "" );
				parent = new File(ijJar).getParentFile();

				for(File f : parent.listFiles())
				{
					if(f.isFile())
					{
						CompilerUtils.addClassPath( f.getPath() );
					}
				}
			}

		}
		catch ( ClassNotFoundException e )
		{
			e.printStackTrace();
		}
	}

	static String readFile(File file, String charset)
			throws IOException
	{
		FileInputStream fileInputStream = new FileInputStream(file);
		byte[] buffer = new byte[fileInputStream.available()];
		int length = fileInputStream.read(buffer);
		fileInputStream.close();
		return new String(buffer, 0, length, charset);
	}

	protected void load(Class clazz)
	{
		Object obj = null;
		try
		{
			obj = clazz.newInstance();
		}
		catch ( InstantiationException e )
		{
			e.printStackTrace();
		}
		catch ( IllegalAccessException e )
		{
			e.printStackTrace();
		}

		Method method = null;
		try {
			method = clazz.getMethod("run", String.class);
			method.invoke(obj, new String());
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	@Override public void run( String s )
	{
		if(s == null || s.equals( "" ))
		{
			final GenericDialog gd = new GenericDialog("Java File");
			gd.addStringField("inputfile", "");
			gd.showDialog();
			if (gd.wasCanceled()) return;

			s = gd.getNextString();
		}

		System.out.println(s);
		IJ.log(s);

		File file = new File(s);
		if(!file.exists())
			return;

		PluginRuntime runtime = new PluginRuntime();

		String code = null;
		try
		{
			code = readFile(file, "UTF-8");
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}

		if(code.trim().isEmpty())
		{
			System.out.println("No code is provided.");
			return;
		}

		// Remove package declaration
		Pattern pkg = Pattern.compile("[\\s]*package (.*?);");
		Matcher pkgMatcher = pkg.matcher(code);
		boolean isPkg = pkgMatcher.find();
		String pkgName = "";

		if(isPkg)
			pkgName = pkgMatcher.group(1);

		// Find a plugin class name
		Pattern pattern = Pattern.compile("[\\s]*public class (.*?) ");
		Matcher m = pattern.matcher(code);

		m.find();
		String className = m.group(1);

		if(isPkg)
			className = pkgName + "." + className;

		if(runtime.compile(className, code))
		{

			try {
				plugin = runtime.instanciate(className, code);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			} catch (IllegalAccessException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			} catch (InstantiationException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}

			load(plugin);
		}
	}
}
