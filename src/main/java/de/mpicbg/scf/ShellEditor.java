package de.mpicbg.scf;

import de.mpicbg.scf.term.IntMain;
import de.mpicbg.scf.term.IntMainFiji;
import org.fife.rsta.ac.LanguageSupport;
import org.fife.rsta.ac.LanguageSupportFactory;
import org.fife.rsta.ac.sh.ShellLanguageSupport;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import javax.swing.*;
import javax.swing.text.BadLocationException;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * Created by moon on 4/17/15.
 */
public class ShellEditor extends Editor
{
	private IntMain intMain;

	public ShellEditor(IntMain intMain)
	{
		initializeComponents();

		this.intMain = intMain;

		textArea.getInputMap().put( KeyStroke.getKeyStroke( KeyEvent.VK_ENTER, ActionEvent.SHIFT_MASK ), "shift+enter" );
		textArea.getActionMap().put( "shift+enter",
				new SendCommandEditorAction( "shift+enter" ) );

	}

	@Override protected String getFilenameCheck( String filename )
	{
		if ( !filename.endsWith( ".sh" ) && textArea.getSyntaxEditingStyle().equals( "text/unix" ) )
		{
			filename += ".sh";
		}
		return filename;
	}

	@Override protected String getSyntaxStyle()
	{
		return SyntaxConstants.SYNTAX_STYLE_UNIX_SHELL;
	}

	@Override protected void setLanguage( RSyntaxTextArea ta )
	{
		LanguageSupportFactory lsf = LanguageSupportFactory.get();
		LanguageSupport support = lsf.getSupportFor( SyntaxConstants.SYNTAX_STYLE_UNIX_SHELL );
		ShellLanguageSupport sls = ( ShellLanguageSupport ) support;
		sls.setAutoCompleteEnabled( true );
		sls.setParameterAssistanceEnabled( true );
		sls.setShowDescWindow( false );
		sls.setAutoActivationDelay( 300 );
		textArea.setLineWrap( true );
		//jls.setAutoActivationEnabled(true);

		lsf.register( textArea );
	}

	@Override protected JButton[] getExtraButtons()
	{
		JButton[] buttons = new JButton[1];

		JButton btn = new JButton("Run");
		btn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				run();
			}
		});

		buttons[0] = btn;

		return buttons;
	}

	private void run()
	{
		String lines = null;
		lines = textArea.getSelectedText();
		if(null != lines)
		{
			intMain.eval( lines );
		}
		else
		{
			intMain.eval( textArea.getText() );
		}
	}

	@Override protected JFileChooser getFileChooser()
	{
		JFileChooser c = new JFileChooser();

		//c.addChoosableFileFilter( new ExtensionFileFilter("Shell","sh") );

		c.setFileFilter(new ExtensionFileFilter("Shell","sh"));

		return c;
	}

	@Override protected String getEditorType()
	{
		return "Shell Editor";
	}

	@Override protected void closingWindow()
	{
	}

	public class SendCommandEditorAction extends AbstractAction
	{

		public SendCommandEditorAction(String name)
		{
			super(name);
		}

		@Override public void actionPerformed( ActionEvent actionEvent )
		{
			if(textArea.getSyntaxEditingStyle().equals( "text/unix" ))
			{
				try
				{
					int lineOffset = textArea.getLineEndOffsetOfCurrentLine() - textArea.getLineStartOffsetOfCurrentLine();
					String line = textArea.getText( textArea.getLineStartOffsetOfCurrentLine(), lineOffset );
					if(line.endsWith( "\n" ))
						line = line.substring( 0, line.lastIndexOf( "\n" ) );

					intMain.eval( line );

					textArea.setCaretPosition( textArea.getLineEndOffsetOfCurrentLine() );
					//				System.out.print( line );
				}
				catch ( BadLocationException e )
				{
					//e.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) throws ClassNotFoundException
	{
		//
		//		final String finalJarInfo = jarInfo;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new ShellEditor( new IntMain() ).setVisible(true);
			}
		});
	}
}
