package de.mpicbg.scf;

import de.mpicbg.scf.compiler.CompilerUtils;
import de.mpicbg.scf.term.IntMain;
import ij.IJ;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by moon on 4/24/15.
 */
public class TaskCommander extends JFrame implements ActionListener
{
	String[] jarInfos;
	boolean bStandAlone = true;

	private int m_count;
	private int m_tencount;
	private JButton m_newJavaEditor;
	private JButton m_newShellEditor;
	private JButton m_terminal;
	IntMain intMain;

	private JDesktopPane m_desktop;
	private HashMap<String, JInternalFrame> frames = new HashMap< String, JInternalFrame >(  );

	public TaskCommander() {
		super("TaskCommander");
		init();

		m_count = m_tencount = 0;

		m_desktop = new JDesktopPane();
		m_desktop.putClientProperty("JDesktopPane.dragMode","outline");

		m_newJavaEditor = new JButton("New Java Editor");
		m_newJavaEditor.addActionListener(this);

		m_newShellEditor = new JButton("New Shell Editor");
		m_newShellEditor.addActionListener(this);

		m_terminal = new JButton("Terminal");
		m_terminal.addActionListener(this);

		JPanel topPanel = new JPanel(true);
		topPanel.add(m_terminal);
		topPanel.add(m_newJavaEditor);
		topPanel.add(m_newShellEditor);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add("North", topPanel);
		getContentPane().add("Center", m_desktop);

		setSize(800,600);
		Dimension dim = getToolkit().getScreenSize();
		setLocation(dim.width/2-getWidth()/2,
				dim.height/2-getHeight()/2);
		setVisible(true);


		WindowListener l = new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				closingWindow();
			}
			public void windowClosed(WindowEvent we) {
				closingWindow();
			}
		};
		addWindowListener(l);

		intMain = new IntMain();

		JInternalFrame jif = intMain.getWindow();
		//				jif.setBounds(20*(m_count%10) + m_tencount*80,
		//						20*(m_count%10), 200, 200);

		m_desktop.add( jif );
		jif.setVisible( true );

		m_count++;
		if ( m_count % 10 == 0 )
		{
			if ( m_tencount < 3 )
				m_tencount++;
			else
				m_tencount = 0;
		}
	}

	void init()
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

//				System.out.println(Arrays.asList(System.getProperty("java.class.path").split(File.pathSeparator)));

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

				jarInfos = new String[ 5 ];
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

				str = IJ.getClassLoader().loadClass( "mpicbg.spim.data.sequence.SequenceDescription" ).getResource( "" ).toString();
				jarInfos[ 3 ] = str.replace( "jar:file:", "" ).replace( "!/mpicbg/spim/data/sequence/", "" );

				str = IJ.getClassLoader().loadClass( "mpicbg.models.Point" ).getResource( "" ).toString();
				jarInfos[ 4 ] = str.replace( "jar:file:", "" ).replace( "!/mpicbg/models/", "" );
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

	void closingWindow()
	{
		if(intMain != null)
		{
			m_desktop.remove(intMain.getWindow());
			intMain.Close();
			intMain = null;
		}
		System.exit(0);
	}

	public IntMain getIntMain()
	{
		return intMain;
	}

	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == m_terminal)
		{
			getIntMain();
			intMain.requestFocus();
		}
		else {

			JInternalFrame jif = null;

			if(e.getSource() == m_newJavaEditor)
			{
				jif = new JavaMacroEditor( jarInfos, getIntMain() );
			}
			else if(e.getSource() == m_newShellEditor)
			{
				jif = new ShellEditor(getIntMain());
			}
			if(jif != null) {

//				jif.setBounds(20*(m_count%10) + m_tencount*80,
//						20*(m_count%10), 200, 200);

				m_desktop.add(jif);
				jif.setVisible( true );

				m_count++;
				if (m_count%10 == 0) {
					if (m_tencount < 3)
						m_tencount++;
					else
						m_tencount = 0;
				}
			}
		}
	}




	public static void main(String[] args) {
		new TaskCommander();
	}
}