import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.swing.filechooser.FileFilter;
import javax.swing.*;

public class FileEncryption extends JFrame implements WindowListener, ActionListener, KeyListener{
	
	private static final long serialVersionUID = 1L;
	
	private JPanel panel, controls, output;
	private JTextArea outputArea;
	
	private boolean file_open;
	private File active_file;
	private String output_text;
	private boolean encrypted;
	private boolean saved;
	
	
	public FileEncryption(){
		
		//A boolean so only one file will be edited at a time
		file_open = false;
		
		//Construct everything
		setUI();
		buildFrame();
		buildPanel();
		addComponents();

		//Finalize the JFrame
		this.add(panel);
		this.setVisible(true);
	}
	
	/**
	 * setUI: Sets java characteristics based on the user's system
	 */
	private void setUI(){
		if(System.getProperties().getProperty("os.name").contains("Windows")){
			try {
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			} catch (Exception e){}
		}
	}
	
	/**
	 * buildFrame: Creates a JFrame for the application and maximizes it to the window
	 */
	private void buildFrame(){
		
		//Your frame
		this.setTitle("Text File Encryption");
		this.setMinimumSize(new Dimension(600, 400));
		this.setResizable(true);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.addWindowListener(this);
	}
	
	/**
	 * buildPanel: Creates a JPanel for the frame using the layout manager gridbaglayout
	 */
	private void buildPanel(){
		panel = new JPanel();
		panel.setLayout(new BorderLayout());
	}
	
	/**
	 * addComponents: Creates and adds all the necessary objects to the panel for the application
	 */
	private void addComponents(){
		
		//The JPanel for the left side of the frame
		controls = new JPanel();
		controls.setLayout(new GridBagLayout());
		
		//The Buttons
		addButton("Open", "folder.png", 0, 0);
		addButton("Encrypt", "lock.png", 0, 1);
		addButton("Decrypt", "key.png", 1, 1);
		addButton("Save", "save.png", 0, 2);
		addButton("Close", "cancel.png", 1, 2);
		
		//The JPanel for the right side of the frame
		output = new JPanel();
		output.setLayout(new GridBagLayout());
		
		//The text area for the output
		outputArea = new JTextArea("To encrypt or decrypt a document,\nclick on the 'Open' button on the\nleft.");
		outputArea.setEditable(false);
		outputArea.addKeyListener(this);
		
		addItem(output,
				new JScrollPane(outputArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS),
				0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH);
		
		panel.add(controls, BorderLayout.WEST);
		panel.add(output, BorderLayout.CENTER);
	}
	
	/**
	 * addButton: A helper method to add a button
	 */
	private void addButton(String label, String icon, int x, int y){
		JLabel temp = new JLabel(label);
		temp.setFont(new Font("Dialog", Font.BOLD, 24));
		addItem(controls, temp, x, 2*y, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE);
		
		JButton tempButton = new JButton(new ImageIcon(icon));
		tempButton.setActionCommand(label);
		tempButton.addActionListener(this);
		addItem(controls, tempButton, x, 2*y + 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE);
		
	}
	
	/**
	 * addItem: A helper method to create the GridBagLayout panel
	 */
	private void addItem(JPanel p, JComponent c, int x, int y, int width, int height, int align, int fill){
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx = x;
		gc.gridy = y;
		gc.gridwidth = width;
		gc.gridheight = height;
		gc.weightx = 100.0;
		gc.weighty = 100.0;
		gc.insets = new Insets(5,5,5,5);
		gc.anchor = align;
		gc.fill = fill;
		p.add(c, gc);
	}
	
	/**
	 * getFilePath: Creates a JFileChooser that will be used to choose
	 * the location to open the file.
	 */
	private String getFilePath(String action){
		if(action.equals("Open")){
			return openJFileChooser();
		}
		return null;
	}
	
	/**
	 * openJFileChooser: All the specifics of opening a file.
	 */
	private String openJFileChooser(){
		
		//Initialize the file chooser
		JFileChooser fc = new JFileChooser();
		fc.setDialogType(JFileChooser.CUSTOM_DIALOG);
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setDialogTitle("What file to you want to encrypt or decrypt?");
		fc.setApproveButtonText("Open File");
		fc.setApproveButtonToolTipText("Open This File");
		fc.setToolTipText("Select a file that to want to encrypt or decrypt");
		
		//Create the filter for only .txt and .enc files
		FileFilter openFilter = new ExtensionFileFilter("TXT and ENC", new String[] { "TXT", "ENC" } );
		fc.setFileFilter(openFilter);
		
		//The return value from the file chooser
		int return_val = fc.showOpenDialog(fc);
		//Decide what it means
		if(return_val == JFileChooser.APPROVE_OPTION){
			File f = fc.getSelectedFile();
			return f.getAbsolutePath();
		}else{
			return null;
		}
	}
	
	/**
	 * openFile: Opens a file if one is not already open
	 * and if the user selects a valid file from the
	 * JFileChooser.
	 */
	private void openFile(){
		//If no files are currently open
		if(!file_open){
			//Retrieve a file path from the JFileChooser
			String file_path = getFilePath("Open");
				
			//If a valid path has been selected, add it to the program
			if(file_path != null && ( file_path.endsWith(".txt") || file_path.endsWith(".enc")) ){
				setEncryptionState(file_path);
				file_open = true;
				saved = true;
				active_file = new File(file_path);
				setOutputText(readInFile());
			}
			
		}else{
			errorBeep();
		}
	}
	
	/**
	 * saveFile: Saves a file to the same location
	 * as the original, with a different file extension.
	 */
	private void saveFile(){
		//If no files are currently open
		if(file_open){
			removeOldFile();
			active_file = new File(determineOutputPath());
			readOutFile();
		}else{
			errorBeep();
		}
	}
	
	/**
	 * removeOldFile: Will remove any valid file specified
	 * by a String path.
	 */
	private void removeOldFile(){
		if(active_file.exists() && active_file.canWrite()){
			active_file.delete();
		}
	}
	
	/**
	 * readOutFile: Takes the current text in the JTextArea
	 * and prints it to a file.
	 */
	private void readOutFile(){
		String[] textArray = output_text.split("\n");
		try {
			PrintWriter p = new PrintWriter(new BufferedWriter(new FileWriter(active_file)));
			for(int i = 0; i < textArray.length; i++){
				p.println(textArray[i]);
			}
			p.close();
			saved = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * determineOutputPath: Returns the file path of the
	 * current file, with a file extension that matches
	 * whether or not the file is encrypted.
	 */
	private String determineOutputPath(){
		//The path is just a directory
		String path = active_file.getAbsolutePath();
		
		path = path.substring(0, path.lastIndexOf("."));
		
		//Add on the extension to the file name
		if(encrypted){
			path += ".enc";
		}else{
			path += ".txt";
		}
		System.out.println(path);
		return path;
	}
	
	/**
	 * readInFile: Returns the text in the file
	 */
	private String readInFile(){
		try {
			ArrayList<String> text = new ArrayList<String>();
			String temp = "";
			BufferedReader b = new BufferedReader(new FileReader(active_file));
			while(true){
				temp = b.readLine();
				if(temp == null){
					break;
				}
				text.add(temp);
			}
			b.close();
			
			String return_text = "";
			for(int i = 0; i < text.size(); i++){
				return_text += text.get(i) + "\n";
			}
			return return_text;
		} catch (Exception e) {
			e.printStackTrace();
			file_open = false;
			return null;
		}
	}
	
	/**
	 * Updates the program global String variable holding the text and updates the JTextArea 
	 */
	private void setOutputText(String text){
		if(text != null){
			output_text = text;
			outputArea.setText(text);
		}
	}
	
	/**
	 * The encryption state is a boolean that is true when the file is a .enc file
	 */
	private void setEncryptionState(String path){
		if(path.endsWith(".txt")){
			encrypted = false;
			outputArea.setEditable(true);
		}else{
			encrypted = true;
			outputArea.setEditable(false);
		}
	}
	
	/**
	 * encryptFile: If the file is not already encrypted,
	 * random key and increment values are generated
	 * and the text is encrypted.  It is updated in global
	 * variables within the program and displayed on the
	 * main text area.
	 */
	private void encryptFile(){
		if(!encrypted){
			int key = generateKey();
			int increment = randomInt(1, 26);
			String header = generateHeader(key, increment);
			setOutputText(encryptText(key, increment, header));
			encrypted = true;
			saved = false;
			outputArea.setEditable(false);
		}
	}
	
	/**
	 * generateKey: Returns a random value for the key between 0 and 94
	 */
	private int generateKey(){
		return randomInt(0, 94);
	}
	
	/**
	 * generateHeader: The header begins with two random characters.
	 * The third character tells you how far you must increment to
	 * find the key.  After that many increments, you will find a
	 * character that is 32 more than the key.  The rest of the
	 * characters are random.
	 */
	private String generateHeader(int key, int increment){
		String header = "";
		//Two random
		for(int i = 0; i < 2; i++){
			header += randomCharacter();
		}
		
		//Increment this far to find the key
		header += (char)(32 + increment);
		
		//Random characters in the increment distance
		for(int i = 0; i < increment; i++){
			header += randomCharacter();
		}
		
		//Add in the key
		header += (char)(32 + key);
		
		//Fill in the rest of the header with random characters
		while(header.length() < 30){
			header += randomCharacter();
		}
		header += "\n";
		return header;
	}
	
	/**
	 * randomCharacter: A random character.
	 */
	private char randomCharacter(){
		return (char)(randomInt(32, 126));
	}
	
	/**
	 * encryptText: The text is looped through character by character
	 * and each one is increased by the value of the key.  The key
	 * itself increases by the value of the increment.
	 */
	private String encryptText(int key, int increment, String header){
		String text = header;
		
		//Loop through every character in the String
		for(int i = 0; i < output_text.length(); i++){
			
			//Increment the character
			char c = output_text.charAt(i);
			if(c > 31 && c < 127){
				c += key;
				if(c > 126){
					c -= 95;
				}
			}
			
			//Add the character to the return text
			text += c;
			
			//Increment the key
			key += increment;
			if(key > 94){
				key -= 95;
			}
		}
		return text;
	}
	
	/**
	 * decryptFile: The Increment and Key Values are read
	 * in from the file, the header is removed, and the
	 * text is decrypted.  The output is displayed in the
	 * text area.
	 */
	private void decryptFile(){
		if(encrypted){
			int increment = getIncrement();
			int key = getKey(increment);
			removeHeader();
			setOutputText(decryptText(key, increment));
			encrypted = false;
			saved = false;
			outputArea.setEditable(true);
		}
	}
	
	/**
	 * getIncrement: Gets the increment value from the header
	 */
	private int getIncrement(){
		return output_text.charAt(2) - 32;
	}
	
	/**
	 * getKey: Gets the key from the header based on the
	 * value of the increment
	 */
	private int getKey(int increment){
		return output_text.charAt(increment + 3) - 32;
	}
	
	/**
	 * removeHeader: Chops off the first line of text
	 */
	private void removeHeader(){
		output_text = output_text.substring(output_text.indexOf("\n") + 1);
	}
	
	/**
	 * decryptText: Increments through the text character by
	 * character and changes the values of each of the chars.
	 * The value of the key itself is incremented by the
	 * increment variable.
	 */
	private String decryptText(int key, int increment){
		String text = "";
		for(int i = 0; i < output_text.length(); i++){
			
			//Convert the character
			char c = output_text.charAt(i);
			if(c > 31 && c < 127){
				c -= key;
				if(c < 32 || c > 126){
					c += 95;
				}
			}
			
			//Append to return text
			text += c;
			
			//Increment the key
			key += increment;
			if(key > 94){
				key -= 95;
			}
		}
		return text;
	}
	
	/**
	 * randomInt: RANDOMNESS?!?!
	 */
	private int randomInt(int min, int max){
		return (int)((max - min) * Math.random() + min);
	}
	
	/**
	 * errorBeep: A method that beeps!
	 */
	private void errorBeep(){
		Toolkit.getDefaultToolkit().beep();
	}
	
	/**
	 * closeFile: If the no file is open, the program shuts down.
	 * If a file is open, it is closed, but not the program.  If
	 * the file is not saved, then the user is prompted whether
	 * or not he would like to save before the file is closed.
	 */
	private void closeFile(int prompt){
		if(file_open){
			if(!saved){
				int result = JOptionPane.showConfirmDialog(null, "Would you like to save the current document?", "Save", prompt);
				if(result == JOptionPane.YES_OPTION){
					saveFile();
					closeAndRestore();
				}else if(result == JOptionPane.NO_OPTION){
					closeAndRestore();
				}else if(result == JOptionPane.CANCEL_OPTION){
				}
			}else{
				closeAndRestore();
			}
		}else{
			System.exit(0);
		}
	}
	
	/**
	 * closeAndRestore: Closes the current file and restores
	 * the default text to the main JTextArea.
	 */
	private void closeAndRestore(){
		setOutputText("To encrypt or decrypt a document,\nclick on the 'Open' button on the\nleft.");
		file_open = false;
		outputArea.setEditable(false);
	}
	
	/**
	 * updateOutputText: After a key is typed into the output textarea,
	 * the program must update the value of the output_text variable in
	 * memory.
	 */
	private void updateOutputText(){
		output_text = outputArea.getText();
	}
	
	//ActionListener
	public void actionPerformed(ActionEvent e) {
		
		//If the open button was pressed
		if(e.getActionCommand().equals("Open")){
			openFile();
		
		//If the encrypt button was pressed
		}else if(e.getActionCommand().equals("Encrypt")){
			if(file_open){
				encryptFile();
			}else{
				errorBeep();
			}
			
		//If the decrypt button was pressed	
		}else if(e.getActionCommand().equals("Decrypt")){
			if(file_open){
				decryptFile();
			}else{
				errorBeep();
			}
			
		//If the save button was pressed
		}else if(e.getActionCommand().equals("Save")){
			saveFile();
		
		//If the close button was pressed
		}else if(e.getActionCommand().equals("Close")){
			closeFile(JOptionPane.YES_NO_CANCEL_OPTION);
		}
	}
	
	//WindowListener
	public void windowActivated(WindowEvent arg0) {}
	public void windowClosed(WindowEvent arg0) {System.exit(0);}
	public void windowClosing(WindowEvent arg0) {closeFile(JOptionPane.YES_NO_OPTION);}
	public void windowDeactivated(WindowEvent arg0) {}
	public void windowDeiconified(WindowEvent arg0) {}
	public void windowIconified(WindowEvent arg0) {}
	public void windowOpened(WindowEvent arg0) {}
	
	//KeyListener
	public void keyPressed(KeyEvent e) {}
	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {updateOutputText();}
	
	//The Main Method - Program starts here
	public static void main(String[] args){
		new FileEncryption();
	}
}