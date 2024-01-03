package de.mpicbg.scf;

import de.mpicbg.scf.compiler.CompilerUtils;
import de.mpicbg.scf.compiler.PluginRuntime;

import de.mpicbg.scf.term.IntMain;
import org.fife.rsta.ac.LanguageSupport;
import org.fife.rsta.ac.LanguageSupportFactory;
import org.fife.rsta.ac.java.JarManager;
import org.fife.rsta.ac.java.JavaLanguageSupport;
import org.fife.rsta.ac.java.buildpath.JarLibraryInfo;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by moon on 4/17/15.
 */
public class JavaMacroEditor extends Editor
{
	protected Class plugin;
	protected HashMap<String, ActionListener> buttons = new HashMap<String, ActionListener>();
	protected String[] jarInfos;
	private IntMain intMain;

	public JavaMacroEditor(String[] jarInfos, IntMain intMain) {
		this.jarInfos = jarInfos;
		this.intMain = intMain;

		if(jarInfos != null)
			addClassPaths();

		initializeComponents();
	}

	@Override protected String getFilenameCheck( String filename )
	{
		if ( !filename.endsWith( ".java" )  && textArea.getSyntaxEditingStyle().equals( "text/java" ))
		{
			filename += ".java";
		}
		return filename;
	}

	@Override protected String getSyntaxStyle()
	{
		return SyntaxConstants.SYNTAX_STYLE_JAVA;
	}

	@Override protected void setLanguage(RSyntaxTextArea ta)
	{
		LanguageSupportFactory lsf = LanguageSupportFactory.get();
		LanguageSupport support = lsf.getSupportFor( SyntaxConstants.SYNTAX_STYLE_JAVA);
		JavaLanguageSupport jls = (JavaLanguageSupport)support;
		jls.setAutoCompleteEnabled(true);
		jls.setParameterAssistanceEnabled(true);
		jls.setShowDescWindow(false);
		jls.setAutoActivationDelay(300);
		//jls.setAutoActivationEnabled(true);

		try {
			jls.getJarManager().addCurrentJreClassFileSource();
			if(jarInfos != null)
			{
				// include jar files in order to provide code suggestions
				addClassPathForJLS(jls.getJarManager());
			}
			else
			{
				String jar = "res/lib/ij-1.49q.jar";

				if(new File(jar).exists())
				{
					JarLibraryInfo ijJarInfo = new JarLibraryInfo( jar );
					jls.getJarManager().addClassFileSource(ijJarInfo);
				}


				jls.getJarManager().addClassFileSource( new JarLibraryInfo( new File("res/lib/SPIM_Registration-2.2.0.jar") ) );
			}


		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		lsf.register(ta);
	}

	@Override protected JButton[] getExtraButtons()
	{
		JButton[] buttons = new JButton[2];

		JButton btn = new JButton("Compile/Run");
		btn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				compile();
			}
		});

		buttons[0] = btn;

		btn = new JButton("Send to Term");
		btn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sendTerm();
			}
		});

		buttons[1] = btn;

		return buttons;
	}

	private void sendTerm()
	{
		if(intMain != null)
		{
			intMain.eval( "cat > " + recentFileName );
			try
			{
				Thread.sleep( 100 );
			}
			catch ( InterruptedException e )
			{
				e.printStackTrace();
			}
			intMain.eval( textArea.getText() );
			try
			{
				Thread.sleep( 100 );
			}
			catch ( InterruptedException e )
			{
				e.printStackTrace();
			}
			intMain.EOT();
		}
	}

	@Override protected JFileChooser getFileChooser()
	{
		JFileChooser c = new JFileChooser();

		//c.addChoosableFileFilter( new ExtensionFileFilter("Java","java") );

		c.setFileFilter(new ExtensionFileFilter("Java","java"));

		return c;
	}

	@Override protected String getEditorType()
	{
		return "Java Macro Editor";
	}

	@Override protected void closingWindow()
	{
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

	protected void unload()
	{
		System.out.println("Unloaded");
	}



	private void addClassPaths()
	{
		for(int i = 0; i < jarInfos.length; i++)
		{
			if(new File(jarInfos[i]).exists())
				CompilerUtils.addClassPath( jarInfos[ i ] );
		}
	}

	private void addClassPathForJLS(JarManager jarManager ) throws IOException
	{
		for(int i = 0; i < jarInfos.length; i++)
		{
			if(new File(jarInfos[i]).exists())
				jarManager.addClassFileSource( new JarLibraryInfo( new File(jarInfos[i]) ) );
		}
	}

	private void compile() {
		if(plugin != null)
		{
			unload();
		}

		StringWriter writer = new StringWriter();

		try {
			textArea.write(writer);
		} catch (IOException e) {
			e.printStackTrace();
		}

		PluginRuntime runtime = new PluginRuntime();

		String code = writer.toString();

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
			//JFileChooser chooser = getJavaFileChooser();

			//runtime.storeClassFiles( chooser.getCurrentDirectory().getAbsolutePath(), className, writer.toString() );

			try {
				plugin = runtime.instanciate(className, writer.toString());
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

	public static void main(String[] args) throws ClassNotFoundException
	{
		//
		//		final String finalJarInfo = jarInfo;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new JavaMacroEditor( null, new IntMain() ).setVisible(true);
			}
		});
	}
}
