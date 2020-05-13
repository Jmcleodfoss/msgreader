package io.github.jmcleodfoss.msg_example;

import io.github.jmcleodfoss.msg.DirectoryEntryData;
import io.github.jmcleodfoss.msg.MSG;
import io.github.jmcleodfoss.msg.NotCFBFileException;
import io.github.jmcleodfoss.msg.Property;
import io.github.jmcleodfoss.msg.PropertyTags;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.Box;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

class SwingExample extends JFrame
{
	// Worker class for loading new files. Probably overkill for most .msg files.
	class NewFileLoader extends SwingWorker<Box, Object>
	{
		private final String filename;

		NewFileLoader(String filename)
		{
			this.filename = filename;
		}

		@Override
		public Box doInBackground()
		{
			return readMsg(filename);
		}

		@Override
		public void done()
		{
			try {
				SwingExample.this.remove(msgDisplay);
				setTitle("msg Explorer");
				SwingExample.this.add(msgDisplay = get(), BorderLayout.CENTER);
				SwingExample.this.pack();
			} catch (Exception e) { }
		}
	}

	// Worker class for saving attachments
	class AttachmentsSaver extends SwingWorker<ArrayList<String>, Object>
	{
		private Iterator<DirectoryEntryData> attachments;
		private File path;

		AttachmentsSaver(File path, Iterator<DirectoryEntryData> attachments)
		{
			this.attachments = attachments;
			this.path = path;
		}

		@Override
		public ArrayList<String> doInBackground()
		{
			ArrayList<String> filesWithErrors = new ArrayList<String>();

			while (attachments.hasNext()){
				DirectoryEntryData a = attachments.next();
				HashMap<Integer, Property> m = msg.getPropertiesAsHashMap(a);
				String attachmentName = getPropertyValue(msg, m, PropertyTags.PidTagAttachLongFilename);

				// Look for the entry which holds the attachment
				Iterator<DirectoryEntryData> attachmentChildren = msg.getChildIterator(a);
				while (attachmentChildren.hasNext()) {
					DirectoryEntryData c = attachmentChildren.next();
					if (c.propertyTag == PropertyTags.PidTagAttachDataBinary) {
						File attachment = new File(path, attachmentName);
						// Ensure we don't overwrite anything (use Windows style of changing filenames to add a number to guarantee uniqueness)
						if (attachment.exists()){
							int extensionIndex = attachmentName.lastIndexOf('.');
							String baseName = attachmentName.substring(0, extensionIndex);
							String extension = attachmentName.substring(extensionIndex);

							int i = 0;
							do {
								attachment = new File(path, String.format("%s (%d)%s", baseName, i++, extension));
							} while (attachment.exists());
						}

						try {
							FileChannel fCh = new FileOutputStream(attachment).getChannel();
							fCh.write(ByteBuffer.wrap(msg.getFile(c)));
							fCh.close();
						} catch (Exception ex) {
							filesWithErrors.add(attachmentName);
						}
						break;
					}
				}
			}
			return filesWithErrors;
		}

		@Override
		public void done()
		{
			try {
				ArrayList<String> filesWithErrors = get();
				if (filesWithErrors.size() == 0)
					return;

				StringBuilder sb = new StringBuilder("There was a problem saving the following files:\n");
				for (String f: filesWithErrors)
					sb.append(f + "\n");
				JOptionPane.showMessageDialog(null, "Problem saving attachments", sb.toString(), JOptionPane.ERROR_MESSAGE);
			} catch (Exception e) { }
		}
	}

	class SaveAttachmentsActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser fc = new JFileChooser();
			fc.setDialogTitle("Save attachment directory");
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fc.setAcceptAllFileFilterUsed(false);

			if (fc.showOpenDialog(SwingExample.this) == JFileChooser.APPROVE_OPTION)
				new AttachmentsSaver(fc.getSelectedFile(), msg.attachments()).execute();
		}
	}

	// Need to preserve the MSG object to save attachments if requested
	private MSG msg;

	// Need to save the message display Box to remove it cleanly when reading the next file.
	private Box msgDisplay;

	// Need to save the Save Attachments menu item so we can enable or disable it based on whether there are any attachments.
	private JMenuItem saveAttachmentsItem;

	// Constructor. All the msg processing takes place in readMsg and in SaveAttachmentsActionListener.actionPerformed
	SwingExample(String filename)
	{
		super();

		// Set up menus
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu menu;
		menu = new JMenu("File");
		menuBar.add(menu);

		saveAttachmentsItem = new JMenuItem("Save Attachments...");
		saveAttachmentsItem.addActionListener(new SaveAttachmentsActionListener());
		menu.add(saveAttachmentsItem);

		JMenuItem item;
		item = new JMenuItem("Open...");
		menu.add(item);
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.setDialogTitle("Open .msg file");
				fc.setFileFilter(new FileNameExtensionFilter("Outlook .msg file", "msg"));
				if (fc.showOpenDialog(SwingExample.this) == JFileChooser.APPROVE_OPTION) {
					try {
						msg.close();
					} catch (IOException ex) { /* not much we can do here, so go on. */ }
					new NewFileLoader(fc.getSelectedFile().toString()).execute();
				}
			}
		});

		item = new JMenuItem("Exit");
		menu.add(item);
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					msg.close();
				} catch (IOException ex) { /* not much we can do here, so go on. */ }
				dispose();
			}
		});

		setTitle("msg Explorer");
		add(msgDisplay = readMsg(filename), BorderLayout.CENTER);

		pack();
		setVisible(true);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	// Open the given .msg file
	private Box readMsg(String filename)
	{
		// Read the msg file. Display an error message and return an empty component on error.
		try {
			msg = new MSG(filename);
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(null, e.toString(), String.format("%s not found", filename), JOptionPane.ERROR_MESSAGE);
			return Box.createHorizontalBox();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, e.toString(), String.format("Error reading %s", filename), JOptionPane.ERROR_MESSAGE);
			return Box.createHorizontalBox();
		} catch (NotCFBFileException e) {
			JOptionPane.showMessageDialog(null, e.toString(), String.format("%s is not a .msg file", filename), JOptionPane.ERROR_MESSAGE);
			return Box.createHorizontalBox();
		}

		HashMap<Integer, Property> properties = msg.getPropertiesAsHashMap(msg.getDirectoryTree());

		Box msgBox = Box.createVerticalBox();

		// Show basic info (date sent, from, subject, and message body)
		Dimension xspacing = new Dimension(5, 15);

		Box r;
		r = Box.createHorizontalBox();
		r.add(new JLabel("Date sent"));
		r.add(Box.createRigidArea(xspacing));
		r.add(new JTextArea(getPropertyValue(msg, properties, PropertyTags.PidTagClientSubmitTime)));
		msgBox.add(r);

		r = Box.createHorizontalBox();
		r.add(new JLabel("From"));
		r.add(Box.createRigidArea(xspacing));
		r.add(new JTextArea(String.format("%s (%s)", getPropertyValue(msg, properties, PropertyTags.PidTagSenderName), getPropertyValue(msg, properties, PropertyTags.PidTagSenderEmailAddress))));
		msgBox.add(r);

		r = Box.createHorizontalBox();
		r.add(new JLabel("Subject"));
		r.add(Box.createRigidArea(xspacing));
		r.add(new JTextArea(getPropertyValue(msg, properties, PropertyTags.PidTagSubject)));
		msgBox.add(r);

		msgBox.add(new JLabel("Message"));
		msgBox.add(new JScrollPane(new JTextArea(getPropertyValue(msg, properties, PropertyTags.PidTagBody))));

		// Show message recipients
		Iterator<DirectoryEntryData> recipients = msg.recipients();
		if (recipients.hasNext()) {
			msgBox.add(new JLabel("Recipients"));
			Box rList = Box.createVerticalBox();

			while (recipients.hasNext()){
				HashMap<Integer, Property> m = msg.getPropertiesAsHashMap(recipients.next());
				r = Box.createHorizontalBox();
				r.add(new JTextArea(String.format("%s (%s)", getPropertyValue(msg, m, PropertyTags.PidTagDisplayName), getPropertyValue(msg, m, PropertyTags.PidTagEmailAddress))));
				rList.add(r);
			}
			msgBox.add(new JScrollPane(rList));
		}

		// Show attachment data
		Iterator<DirectoryEntryData> attachments = msg.attachments();
		if (attachments.hasNext()) {
			msgBox.add(new JLabel("Attachments"));

			Vector<Vector<String>> attachmentData = new Vector<Vector<String>>();
			while (attachments.hasNext()){
				DirectoryEntryData a = attachments.next();
				HashMap<Integer, Property> m = msg.getPropertiesAsHashMap(a);

				Vector<String> row = new Vector<String>();
				row.add(getPropertyValue(msg, m, PropertyTags.PidTagAttachLongFilename));
				row.add(getPropertyValue(msg, m, PropertyTags.PidTagAttachMimeTag));
				row.add(m.get(PropertyTags.PidTagAttachDataBinary).value());
				attachmentData.add(row);
			}

			Vector<String> columnHeadings = new Vector<String>();
			columnHeadings.add("Long filename");
			columnHeadings.add("Mime type");
			columnHeadings.add("Size (bytes)");

			JTable t = new JTable(attachmentData, columnHeadings);
			r = Box.createHorizontalBox();
			r.add(t);
			r.add(Box.createHorizontalGlue());
			msgBox.add(r);		

			saveAttachmentsItem.setEnabled(true);
		} else {
			saveAttachmentsItem.setEnabled(false);
		}

		setTitle(filename);
		return msgBox;
	}

	// Retrieve the value of a propertyTag
	private static String getPropertyValue(MSG msg, HashMap<Integer, Property> properties, int propertyTag)
	{
		if (properties.keySet().contains(propertyTag))
			return msg.getPropertyValue(properties.get(propertyTag));
		return "not found";
	}

	public static void main(String[] args)
	{
		if (args.length == 0 || args.length > 1) {
			System.out.println("use (assuming the jar files for msg_example and msg are in the classpath):");
			System.out.println();
			System.out.println("\tjava io.github.jmcleodfoss.msg_example.SwingExample msg-file");
			System.exit(0);
		}

		new SwingExample(args[0]);
	}
}
