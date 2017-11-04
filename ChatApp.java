import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.*;



public class ChatApp extends JFrame{
	private String path = "";
	private final int interval = 1500;
	
	private JButton btnsend;
	private JTextField txtmessage;
	private JTextArea txtChats;
	
	private String name = "";
	private String tempname = "";
	private String computerName;
	private final int FRAME_WIDTH = 640;
	private final int FRAME_HEIGHT = 480;

	private InetAddress localMachine;
	private Timer timer;
	
	public ChatApp(){
		//Frame Title
		super("Chat Application");
		//Frame Size
		setSize(FRAME_WIDTH,FRAME_HEIGHT);
		//Put the frame on center
		setLocationRelativeTo(null);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//Sets computer name and ask for user name
		computerName = getComputerName();
		changeName();
		
		
		//*******MENU BAR **************//
		final JMenuBar menuBar = new JMenuBar();
		
		//======Creating "File" Menu====//
		JMenu fileMenu = new JMenu("File");
	    JMenuItem exitMenuItem = new JMenuItem("Exit");
	    exitMenuItem.setMnemonic(KeyEvent.VK_X);
	    exitMenuItem.addActionListener(new ActionListener() {
	    	@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
	    fileMenu.add(exitMenuItem);
	    //===============================//
	     
	     
	    //======Creating "Edit" Menu====//
	    
		JMenu editMenu = new JMenu("Edit"); 
		
						//For Changing name//
		JMenuItem changeMenuItem = new JMenuItem("Change Name");
		changeMenuItem.setMnemonic(KeyEvent.VK_C);
		changeMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tempname = name;
				name = "";
				changeName();
			}
		});
		editMenu.add(changeMenuItem);
		
		
		
		JMenuItem chPathMenuItem = new JMenuItem("Change Path of Logs");
		chPathMenuItem.setMnemonic(KeyEvent.VK_L);
		chPathMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				changePath();
			}
		});
		editMenu.add(chPathMenuItem);
		
		
		//===============================//
		
		//Adding Menus to MenuBar and setting the MenuBar
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
	    setJMenuBar(menuBar);
		//*******MENU BAR **************//
	    
	    
	     
	      
	    
		txtmessage = new JTextField();
		txtmessage.setPreferredSize(new Dimension(400, 20));
		txtChats = new JTextArea();
		txtChats.setEditable(false);
		
		//Change TextArea font to 14;
		txtChats.setFont(txtChats.getFont().deriveFont(14f));
		
		//If message is too long, continue it to the next line.
		txtChats.setLineWrap(true);
		txtChats.setWrapStyleWord(true);
		
		
		//Add Vertical Scroll bars to our text area.
		JScrollPane scroll = new JScrollPane (txtChats);
	    scroll.setVerticalScrollBarPolicy ( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
	    
	    
		//Write to text file then read the logs
		btnsend = new JButton("Send");
		btnsend.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				writeToChatLogs(String.format("[%s: %s] %s", computerName, name,  txtmessage.getText()));
				readChatLogs();
				txtmessage.setText("");
			}
		});
		
		
		//Adding our components to the panels
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(scroll, BorderLayout.CENTER);
		
		JPanel southPanel = new JPanel();
		southPanel.add(txtmessage);
		southPanel.add(btnsend);
		mainPanel.add(southPanel, BorderLayout.SOUTH);
		
		setContentPane(mainPanel);
		
		
		//Timer Declaration. Timer execute task (Reading logs) on set interval.
		timer = new Timer(interval, new ActionListener() {
			  @Override
			  public void actionPerformed(ActionEvent arg0) {
			    readChatLogs();
			  }
			});
		timer.setRepeats(true);
		
		//Start the timer
		timer.start();
		
		//Focus cursor to text box message
		txtmessage.requestFocus();
		
		//Adding a "Default Key" so you just need to hit enter to send
		JRootPane rootPane = SwingUtilities.getRootPane(btnsend); 
		rootPane.setDefaultButton(btnsend);
		
		
		
		//========Window Listener=======//
		addWindowListener(new WindowListener() {
			@Override
			public void windowOpened(WindowEvent e) {
				
			}
			
			@Override
			public void windowIconified(WindowEvent e) {
				//If Frame is minimized, write [Computer Name: Username] is idle!; or tell other user that you are idle
				if(fileExist()){
					writeToChatLogs("\t\tUser: " + String.format("[%s: %s] %s", computerName, name, " is Idle!"));
				}
				//You can stop the timer that reads the chat logs here if you want since it does not make sense to refresh the logs if the user does not see it.
				if(timer.isRunning()){
					timer.stop();
				}
			}
			
			@Override
			public void windowDeiconified(WindowEvent e) {}
			@Override
			public void windowDeactivated(WindowEvent e) {
				//Tells other user if he / she is idle when you navigated away from the frame.
				if(fileExist())
					if(name.equals(""))		//if the user is changing his name.
						writeToChatLogs("\t\tUser: " + String.format("[%s: %s] %s", computerName, tempname, " is Idle!"));
					else 
						writeToChatLogs("\t\tUser: " + String.format("[%s: %s] %s", computerName, name, " is Idle!"));
			}
			
			@Override
			public void windowClosing(WindowEvent e) {
				//Ask the user if he / she wants to exit.
				if (JOptionPane.showConfirmDialog(null, "Are you sure you want to Exit", "Leave?", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
	                return;
	            }else{
	            	if(fileExist())
					writeToChatLogs("\t\tUser: " + String.format("[%s: %s] %s", computerName, name, " left the conversation!"));
		            System.exit(0);
	            }
			}
			
			@Override
			public void windowClosed(WindowEvent e) {}
			
			@Override
			public void windowActivated(WindowEvent e) {
				//Notify other user if the he / she is active or is in the frame.
				if(fileExist())
				writeToChatLogs("\t\tUser: " + String.format("[%s: %s] %s", computerName, name, " is now Active!"));
				
				//If timer timer is stopped because the user minimize the app, start it .
				if(!timer.isRunning()){
					timer.start();
				}
				
			}
		});
		//==============================//
		
		
		//Show the Frame or Window
		setVisible(true);
		
	}
	
	//IF the current path is not set or is invalid, you will be asked to find the txt file in your network.
	public void changePath(){
		try {
			JFileChooser fc = new JFileChooser();
			fc.addChoosableFileFilter(new FileNameExtensionFilter("Text File", "txt"));
	        int returnVal = fc.showOpenDialog(this);

	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	            File file = fc.getSelectedFile();
	            String newPath = file.getAbsolutePath();
	            path = newPath;
	            JOptionPane.showMessageDialog(this, "Path set by user to ." + newPath);
	        } else {
	            JOptionPane.showMessageDialog(this, "Open command cancelled by user." );
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//This block of code is used to change the name of the user
	public void changeName(){
		try {
			while(name.length() < 4l){
				name = JOptionPane.showInputDialog(this, "Enter your name: ");
				if(name.length() < 4){
					JOptionPane.showMessageDialog(this, "Minimum of 4 characters required!");
				}
			}
		} catch (Exception e) {
			if(tempname.equals("") || tempname == null){
				name = "Anonymous";
			}
			else
			{
				name = tempname;
			}
		}
	}
	//This method gets the name of the computer and return it.
	public String getComputerName(){
		try {
			localMachine = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return localMachine.getHostName();
	}
	
	//This block of code is responsible for writting the message to chat logs.
	public void writeToChatLogs(String message){
		try {
			FileWriter mywriter = new FileWriter(path, true);
			mywriter.append("\n" + message);
			mywriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	//Shows an error if the txt file is not found or is deleted.
	public void checkIfFileExist()
	{
		if(!fileExist()){
			JOptionPane.showMessageDialog(this,
				    "Please select a chat log txt file.",
				    "File Not Found",
				    JOptionPane.ERROR_MESSAGE);
			changePath();
		}
	}
	
	//Check if file exist
	public boolean fileExist(){
		File chatfile = new File(path);
		return chatfile.exists();
	}
	
	
	//Responsible for reading the chat logs and appending it to the JTextArea
	public void readChatLogs()
	{
		checkIfFileExist();
		txtChats.setText("");
		try {
			FileReader reader = new FileReader(path);
			BufferedReader br = new BufferedReader(reader);
			String line;
			
			while((line = br.readLine()) != null){
				txtChats.append("\n" + line);
			}
			br.close();
		} catch (FileNotFoundException e) {
			checkIfFileExist();
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		//Running the app in a thread safe manner.
	      SwingUtilities.invokeLater(new Runnable() {
	         @Override
	         public void run() {
	            new ChatApp();
	         }
	      });
	}
}
