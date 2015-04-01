package de.mpicbg.scf.plugin;

import de.mpicbg.scf.JavaMacroEditor;
import de.mpicbg.scf.compiler.CompilerUtils;
import de.mpicbg.scf.term.IntMain;
import ij.IJ;
import ij.ImageJ;
import ij.plugin.PlugIn;

import javax.swing.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by moon on 4/17/15.
 */
public class JavaMacroEditorPlugIn implements PlugIn
{
	String[] jarInfos;
	boolean bStandAlone = true;

	public JavaMacroEditorPlugIn()
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
				//NativeLibrary.addSearchPath( "pty", str.replace( "jar:file:", "" ).replace( "!/de/mpicbg/scf/", "!/lib/" ) );
				String jarFile = str.replace( "jar:file:", "" ).replace( "!/de/mpicbg/scf/", "" );

				// TODO: Support other platforms!!!

				java.util.jar.JarFile jarfile = new java.util.jar.JarFile( new java.io.File( jarFile ) ); //jar file path(here sqljdbc4.jar)
				java.util.Enumeration< java.util.jar.JarEntry > enu = jarfile.entries();
				while ( enu.hasMoreElements() )
				{
					String destdir = jarFile.replace( "taskcommander_fiji.jar", "" );
					java.util.jar.JarEntry je = enu.nextElement();

					//System.out.println(je.getName());

					if ( je.getName().startsWith( "macosx" ) || je.getName().startsWith( "linux" ) || je.getName().startsWith( "win" ) )
					{
						java.io.File fl = new java.io.File( destdir, je.getName() );
						if ( !fl.exists() )
						{
							fl.getParentFile().mkdirs();
							fl = new java.io.File( destdir, je.getName() );
						}
						if ( je.isDirectory() )
						{
							continue;
						}
						java.io.InputStream is = jarfile.getInputStream( je );
						java.io.FileOutputStream fo = new java.io.FileOutputStream( fl );
						while ( is.available() > 0 )
						{
							fo.write( is.read() );
						}
						fo.close();
						is.close();
					}
				}

				//NativeLibrary.addSearchPath( "pty", jarFile.replace( "taskcommander.jar", "" ) );

				jarInfos = new String[ 3 ];
				jarInfos[ 0 ] = str.replace( "jar:file:", "" ).replace( "!/de/mpicbg/scf/", "" );
//				System.out.println( jarInfos[ 0 ] );

				// The below method has a bug
//				str = IJ.getClassLoader().loadClass( "ij.IJ" ).getResource( "" ).toString();
//				jarInfos[ 1 ] = str.replace( "jar:file:", "" ).replace( "!/ij/", "" );
//				System.out.println( jarInfos[ 1 ] );

				str = IJ.class.getResource("IJ.class").getPath();
				jarInfos[ 1 ] = str.replace( "file:", "" ).replace( "!/ij/IJ.class", "" );
//				System.out.println( jarInfos[ 1 ] );

				str = IJ.getClassLoader().loadClass( "fiji.plugin.Bead_Registration" ).getResource( "" ).toString();
				jarInfos[ 2 ] = str.replace( "jar:file:", "" ).replace( "!/fiji/plugin/", "" );
//				System.out.println( jarInfos[ 2 ] );
			}

		}
		catch ( ClassNotFoundException e )
		{
			e.printStackTrace();
		}
		catch ( FileNotFoundException e )
		{
			e.printStackTrace();
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}
	}

	@Override
	public void run( final String args )
	{
		SwingUtilities.invokeLater( new Runnable()
		{
			public void run()
			{
				new JavaMacroEditor( jarInfos, new IntMain() ).setVisible( true );
			}
		} );
	}

	public static void main( final String[] args ) throws ClassNotFoundException
	{
		ImageJ.main( args );
		new JavaMacroEditorPlugIn().run( null );
	}
}