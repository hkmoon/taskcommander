package de.mpicbg.scf.term;

import com.google.common.base.Predicate;
import com.jediterm.terminal.RequestOrigin;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.ui.TabbedTerminalWidget;
import com.jediterm.terminal.ui.TerminalPanelListener;
import com.jediterm.terminal.ui.TerminalSession;
import com.jediterm.terminal.ui.TerminalWidget;
import com.jediterm.terminal.ui.settings.DefaultTabbedSettingsProvider;

import javax.swing.*;

import java.awt.*;

public abstract class AbstractTerminal
{

	final protected JInternalFrame window = new JInternalFrame("Terminal", true, false, true, true);

	protected TerminalWidget term;

	private void openSession(TerminalWidget terminal) {
		if (terminal.canOpenSession()) {
			openSession(terminal, createTtyConnector());
		}
	}

	public void openSession(TerminalWidget terminal, TtyConnector ttyConnector) {
		TerminalSession session = terminal.createTerminalSession(ttyConnector);
		session.start();
	}

	public abstract TtyConnector createTtyConnector();

	protected void setTitle(String title) {
		window.setTitle(title);
	}

	protected void initializeComponents() {

		term = new TabbedTerminalWidget(new DefaultTabbedSettingsProvider(), new Predicate<TerminalWidget>() {
			@Override
			public boolean apply(TerminalWidget terminalWidget) {
				openSession(terminalWidget);
				return true;
			}
		});

		term.setTerminalPanelListener(new TerminalPanelListener() {
			public void onPanelResize(final Dimension pixelDimension, final RequestOrigin origin) {
				if (origin == RequestOrigin.Remote) {
					sizeFrameForTerm(window);
				}
			}

			@Override
			public void onSessionChanged(final TerminalSession currentSession) {
				window.setTitle(currentSession.getSessionName());
			}

			@Override
			public void onTitleChanged(String title) {
				window.setTitle(term.getCurrentSession().getSessionName());
			}
		});

		openSession(term);

		//add the panel to the window
		window.getContentPane().add(term.getComponent());

		//setup window display
		window.setSize(450, 450);
		window.pack();

		window.setVisible(true);
	}

	private void sizeFrameForTerm(final Component frame) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Dimension d = term.getPreferredSize();

				d.width += frame.getWidth() - window.getContentPane().getWidth();
				d.height += frame.getHeight() - window.getContentPane().getHeight();
				frame.setSize(d);
			}
		});
	}
}

