package de.mpicbg.scf;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextAreaEditorKit;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by moon on 4/8/15.
 */
public abstract class Editor extends JInternalFrame
{
	protected RSyntaxTextArea textArea;
	protected HashMap<String, ActionListener> buttons = new HashMap<String, ActionListener>();
	private JFileChooser fileChooser;

	protected abstract String getFilenameCheck(String filename);

	protected abstract String getSyntaxStyle();

	protected abstract void setLanguage(RSyntaxTextArea ta);

	protected abstract void closingWindow();

	protected abstract JButton[] getExtraButtons();

	protected abstract JFileChooser getFileChooser();

	protected abstract String getEditorType();

	protected String recentFileName;

	protected Editor()
	{
		super("", true, true, true, true);
	}

	protected void initializeComponents()
	{
		fileChooser = getFileChooser();

		JPanel cp = new JPanel(new BorderLayout());

		JPanel bp = new JPanel(new FlowLayout(FlowLayout.LEFT));

		JButton loadBtn = new JButton("Load");
		loadBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				int returnVal = fileChooser.showOpenDialog(getParent());
				if(returnVal == JFileChooser.APPROVE_OPTION)
				{
					String filename = fileChooser.getSelectedFile().getAbsolutePath();
					recentFileName = fileChooser.getSelectedFile().getName();
					setTitle( recentFileName );

					try {
						FileInputStream fis = new FileInputStream(filename);
						InputStreamReader in = new InputStreamReader(fis, "UTF-8");

						textArea.read(in, null);

						in.close();
					} catch (FileNotFoundException e1) {
						e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
					} catch (UnsupportedEncodingException e1) {
						e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
					} catch (IOException e1) {
						e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
					}
				}
			}
		});
		bp.add(loadBtn);

		JButton saveBtn = new JButton("Save");
		saveBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				int returnVal = fileChooser.showSaveDialog(getParent());
				if(returnVal == JFileChooser.APPROVE_OPTION)
				{
					String filename = getFilenameCheck(fileChooser.getSelectedFile().getAbsolutePath());
					recentFileName = fileChooser.getSelectedFile().getName();
					setTitle( recentFileName );

					try {
						FileOutputStream fos = new FileOutputStream(filename);
						OutputStreamWriter out = new OutputStreamWriter(fos, "UTF-8");

						textArea.write(out);

						out.close();

					} catch (FileNotFoundException e1) {
						e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
					} catch (UnsupportedEncodingException e1) {
						e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
					} catch (IOException e1) {
						e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
					}
				}
			}
		});
		bp.add(saveBtn);

		JButton[] extButton = getExtraButtons();
		if(extButton != null)
		{
			for(JButton btn : extButton)
				bp.add(btn);
		}

		for(Map.Entry<String, ActionListener> item : buttons.entrySet())
		{
			JButton btn = new JButton(item.getKey());
			btn.addActionListener(item.getValue());
			bp.add(btn);
		}

		textArea = new RSyntaxTextArea(40, 70);

		textArea.setSyntaxEditingStyle(getSyntaxStyle());
		setLanguage(textArea);

		textArea.setCodeFoldingEnabled(true);
		textArea.setAntiAliasingEnabled(true);
		textArea.setAutoIndentEnabled(true);
		textArea.setCloseCurlyBraces(true);
		textArea.setMarkOccurrences(true);
		textArea.setPaintMarkOccurrencesBorder(true);
		textArea.setPaintMatchedBracketPair(true);
		textArea.setPaintTabLines(true);
		textArea.setTabsEmulated(false);
		//setTheme("eclipse");
		setTheme("dark");

		InputMap im = textArea.getInputMap();
		im.remove(KeyStroke.getKeyStroke( KeyEvent.VK_HOME, 0));
		im.remove(KeyStroke.getKeyStroke(KeyEvent.VK_END, 0));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0), RSyntaxTextAreaEditorKit.beginLineAction);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_END, 0), RSyntaxTextAreaEditorKit.endLineAction);


		RTextScrollPane sp = new RTextScrollPane(textArea);
		sp.setFoldIndicatorEnabled(true);
		cp.add(bp, BorderLayout.NORTH);
		cp.add(sp, BorderLayout.CENTER);

//		this.addWindowListener(
//				new WindowAdapter() {
//					public void windowClosing(WindowEvent we) {
//						closingWindow();
//					}
//					public void windowClosed(WindowEvent we) {
//						closingWindow();
//					}
//				}
//		);

		setContentPane(cp);
		setTitle(getEditorType());
		//setDefaultCloseOperation(EXIT_ON_CLOSE);
		pack();
//		setLocationRelativeTo(null);
		setLocation( 20, 100 );
	}

	public void setTheme(String s)
	{
		try
		{
			Theme t = Theme.load(this.getClass().getClassLoader().getResourceAsStream("themes/" + s + ".xml"));
			t.apply(textArea);
		} catch (IOException e)
		{
			System.out.println("Couldn't load theme");
		}
	}
}
