package de.mpicbg.scf.term;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jediterm.pty.PtyProcessTtyConnector;
import com.jediterm.terminal.LoggingTtyConnector;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.ui.UIUtil;
import com.pty4j.PtyProcess;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.DefaultConfiguration;

import javax.swing.*;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by moon on 4/2/15.
 */
public class IntMain extends AbstractTerminal
{
	PrintStream writer = null;

	public IntMain()
	{
		super.initializeComponents();
		super.setTitle( "Terminal" );
	}

	@Override
	public TtyConnector createTtyConnector() {
		try {
			Map<String, String> envs = Maps.newHashMap( System.getenv() );
			envs.put("TERM", "xterm-256color");
			envs.put("COLORFGBG", "7;0");
			String[] command = new String[]{"/bin/bash", "--login"};

			if ( UIUtil.isWindows) {
				command = new String[]{"cmd.exe"};
			}

			PtyProcess process = PtyProcess.exec(command, envs, null);
			writer = new PrintStream(process.getOutputStream());

			return new LoggingPtyProcessTtyConnector(process, Charset.forName( "UTF-8" ));
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public static void main(final String[] arg) {
		Configurator.initialize(new DefaultConfiguration());
		Configurator.setRootLevel(Level.INFO);
		new IntMain();
	}


	public static class LoggingPtyProcessTtyConnector extends PtyProcessTtyConnector implements LoggingTtyConnector
	{
		private List<char[]> myDataChunks = Lists.newArrayList();

		public LoggingPtyProcessTtyConnector(PtyProcess process, Charset charset) {
			super(process, charset);
		}

		@Override
		public int read(char[] buf, int offset, int length) throws IOException
		{
			int len = super.read(buf, offset, length);
			if (len > 0) {
				char[] arr = Arrays.copyOfRange( buf, offset, len );
				myDataChunks.add(arr);
			}
			return len;
		}

		public List<char[]> getChunks() {
			return Lists.newArrayList(myDataChunks);
		}
	}

	public void requestFocus()
	{
		window.requestFocus();
	}

	public Object eval(String text) {

		writer.println(text);
		return null;
	}

	public Object EOT()
	{
		writer.print( '\u0004' );
		return null;
	}

	public void Close()
	{

	}

	public JInternalFrame getWindow()
	{
		return window;
	}

	public void setLocation(int x, int y)
	{
		window.setLocation( x, y );
	}

	public void setVisible( boolean visible )
	{
		window.setVisible( visible );
	}

	protected String getLineCommentMark() {
		return "$";
	}
}
