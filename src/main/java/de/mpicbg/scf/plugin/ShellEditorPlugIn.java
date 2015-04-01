package de.mpicbg.scf.plugin;

import de.mpicbg.scf.ShellEditor;
import de.mpicbg.scf.term.IntMain;
import ij.ImageJ;
import ij.plugin.PlugIn;

import javax.swing.*;

/**
 * Created by moon on 4/17/15.
 */
public class ShellEditorPlugIn implements PlugIn
{
	public ShellEditorPlugIn()
	{
	}

	@Override
	public void run( final String args )
	{
		SwingUtilities.invokeLater( new Runnable()
		{
			public void run()
			{
				new ShellEditor( new IntMain() ).setVisible( true );
			}
		} );
	}

	public static void main( final String[] args ) throws ClassNotFoundException
	{
		ImageJ.main( args );
		new ShellEditorPlugIn().run( null );
	}
}