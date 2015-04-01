package de.mpicbg.scf.plugin;

import de.mpicbg.scf.ShellEditor;
import de.mpicbg.scf.TaskCommander;
import de.mpicbg.scf.term.IntMain;
import ij.ImageJ;
import ij.plugin.PlugIn;

import javax.swing.*;

/**
 * Created by moon on 4/24/15.
 */
public class TaskCommanderPlugIn implements PlugIn
{
	public TaskCommanderPlugIn()
	{
	}

	@Override
	public void run( final String args )
	{
		SwingUtilities.invokeLater( new Runnable()
		{
			public void run()
			{
				new TaskCommander();
			}
		} );
	}

	public static void main( final String[] args ) throws ClassNotFoundException
	{
		ImageJ.main( args );
		new TaskCommanderPlugIn().run( null );
	}
}