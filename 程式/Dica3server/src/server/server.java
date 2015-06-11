package server;
/*This program is created at 20120218
 * It is created for Dica3 system
 * This server will include:
 * 1.Recording connecting ip.
 * 2.��ܶi�J�HIP
 * 3.��ܨt�Ϊ��p
 * 4.�������ݵe��
 * 5.�s�WDialog��ܵ�����(2012/3/18)
 * 
 * (2012/4/7)
 * �s�W�M�u��Dica4�Ϩa�H���ϥΪ�
 * �@�˪����k�A�u�O�ڦA�h���FXXX+ForHelper����ܦW��
 * �o�ӥΨӧ@���ĤG���pô�覡(��PORT�]��8079)
 * (2012/4/8)
 *  �s�W�M�u��Dica4�Ϩa�H���ϥΪ�
 * �@�˪����k�A�u�O�ڦA�h���FXXX+ForDatabase����ܦW��
 * �o�ӥΨӧ@���ǰe�ϥΪ̸��(��PORT�]��8078)
 * 
 * �s�W�ѽX���X3334567
 * ��ǰe�o�Ӯɫh��ܬO�ϥΪ̭ӤH��ƶi�ӤF
 * */


import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;



public class server extends Frame implements ActionListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param args
	 */
	
	//���U�����򤶭�������
	static server frm = new server();	//��l�e��
	static Button btn1 = new Button("Broadcast");	//�q�w����A�ΨӶǰe�T��
	static Button startSysBtn = new Button("Start System");	//�q�w����A�ΨӱҰʦ��A��
	static Label lab1 = new Label("Please keyin message on the field:");	//�W�[��ܤ���
	static Label lab2 = new Label("History messages:");	//�W�[��ܤ���
	static Label lab3 = new Label("Helping requests:");	//�W�[��ܤ���
	static Label lab4 = new Label("System messages:");	//�W�[��ܤ���
	static Label lab5 = new Label("IP manager:");	//�W�[��ܤ���
	static Label lab6 = new Label("Requesters IP:");	//�W�[��ܤ���
	static Label lab7 = new Label("Helpers IP:");	//�W�[��ܤ���
	static Label lab8 = new Label("����:");	//�W�[��ܤ���
	static TextArea txf = new TextArea("",4,19,TextArea.SCROLLBARS_NONE);	//�Ψӿ�J�T��
	static TextArea txa = new TextArea("",4,19,TextArea.SCROLLBARS_BOTH);	//�Ψ���ܲ�ѫǥثe���p
	static TextArea sysMsgTxa = new TextArea("",4,19,TextArea.SCROLLBARS_BOTH);	//�Ψ���ܨt�γs�����p
	static TextArea showIpTxa = new TextArea("",4,19,TextArea.SCROLLBARS_BOTH);	//�Ψ���ܨt�γs��IP���p
	static TextArea showHelpMsgTxa = new TextArea("",4,19,TextArea.SCROLLBARS_BOTH);	//�Ψ���ܨD�Ϫ��p
	static TextArea showRequesterIPTxa = new TextArea("",4,19,TextArea.SCROLLBARS_BOTH);	//�Ψ���ܨD�Ϫ�IP
	static TextArea showHelperIPTxa = new TextArea("",4,19,TextArea.SCROLLBARS_BOTH);	//�Ψ�������U��IP
	static TextArea showProgammersTxa = new TextArea("",4,19,TextArea.SCROLLBARS_BOTH);	//�Ψ���ܨD�Ϫ��p
	
	static Wlis wlis = new Wlis();	//�������w�]���䪺��ť��
	
	public boolean textFeildFlag = false;	//��w�ѨM�@�}�l���檺���D~
	
	
	/*���U�@����LAPI��
	 * */
	/*------------------------���U�}�l��������w��-------------------------------*/
	private static int serverport = 8080;	//����PORT���X
	
	/**---------------------------------------------------------------------------------------------------------------
	 * 2012/4/8�s�W
	 * ���U�令�C�j�@��o�X�����
	 * */
	private static int serverportForHelper = 8079;  //�������Ϩa�̪�PORT���X�A�o�ӬO�u���Ϩa�̤W��TAB���M�u
	private static int serverportForDatabase = 8078;	//�������Ϩa�̤W�ǭӤH��ƪ�PORT���X�M�u
	/**---�s�WSTOP-----------------------------------------------------------*/
	
	
	private static ServerSocket serverSocket;
	
	private static String callerIP;
	private static String loginerIP;
	public static String SERVERIP = "";
	
	// �Φ�C���x�s�C��client
	private static ArrayList<Socket> players=new ArrayList<Socket>();	//�����`�@�ϥΪ�
	private static ArrayList<Socket> helpers=new ArrayList<Socket>();	//�����`�@���U��
	private static ArrayList<Socket> callers=new ArrayList<Socket>();	//�����`�@�D�Ϫ�
	
	public static int userNumber = 0;	//�ΨӰO���ثe���X�ӤH�w�s������A��
	static Socket sock[] = new Socket[3];	//�ΨӦs���g���L��IP
	public static int users = 0;	//�ΨӰO���̨Ϧܤ����h�֤H�s�i��
	
	private static int s =0;	//�Ȧs�`�@�ϥΤH
	private static int caller = 0;
	
	/*------------------------������w�ϵ���-------------------------------------*/
	
	public static void main(String args[]) {
		// TODO Auto-generated method stub
		

		//�������h��
		frm.setTitle("Dica3 server");
		frm.setSize(800,800);
		frm.setLayout(null);		
		
		//���������
		startSysBtn.setBounds(20, 40, 200, 100);
		btn1.setBounds(220, 40, 200, 100);
		
		/*����*/
		lab1.setBounds(20, 150, 200, 25);
		txf.setBounds(20, 175, 300, 100);
		lab2.setBounds(20, 275, 200, 25);
		txa.setBounds(20, 300, 300, 200);
		lab3.setBounds(20, 500, 200, 25);
		showHelpMsgTxa.setBounds(20, 525, 350, 100);
		lab8.setBounds(10, 625, 200, 25);
		showProgammersTxa.setBounds(10, 650, 400, 100);
		
		/*�k��*/
		lab4.setBounds(425, 30, 200, 25);
		sysMsgTxa.setBounds(425, 55, 300, 200);
		lab5.setBounds(425, 255, 200, 25);
		showIpTxa.setBounds(425, 280, 300, 200);
		lab6.setBounds(425, 480, 200, 25);
		showRequesterIPTxa.setBounds(425, 505, 300, 100);
		lab7.setBounds(425, 605, 200, 25);
		showHelperIPTxa.setBounds(425, 630, 300, 100);
		
		
		//�ƥ��ť�̰�
		frm.addWindowListener(wlis);
		btn1.addActionListener(frm);
		startSysBtn.addActionListener(frm);
		txf.addKeyListener(new KeyLis());		
		
		//������ܺ޲z
		frm.add(btn1);
		frm.add(lab1);
		frm.add(lab2);
		frm.add(lab3);
		frm.add(lab4);
		frm.add(lab5);
		frm.add(lab6);
		frm.add(lab7);
		frm.add(lab8);
		frm.add(showHelpMsgTxa);
		frm.add(startSysBtn);
		frm.add(sysMsgTxa);
		frm.add(showIpTxa);
		frm.add(showRequesterIPTxa);
		frm.add(showHelperIPTxa);
		frm.add(showProgammersTxa);
		frm.add(txf);
		frm.add(txa);
		frm.setVisible(true);
		//frm.setBackground(Color.LIGHT_GRAY);
		txf.setEditable(true);
		txa.setEditable(false);
		sysMsgTxa.setEditable(false);
		showIpTxa.setEditable(false);
		showHelpMsgTxa.setEditable(false);
		showRequesterIPTxa.setEditable(false);
		showHelperIPTxa.setEditable(false);
		showProgammersTxa.setEditable(false);
		
		//��L���h�\��W�[��
		//startServer();
		showProgammersTxa.setText(
				"���{���O�ѥ�µ���B�L�T���B���H�s�һs\n" +
				"�ϥΫe�Х����U�W��Start system����\n" +
				"���۫K�i�H�ݨ���A�����\�ظm��T\n" +
				"�z�i�H�b����U�誺��J�ؤ���J�n�s������L�ϥΪ̪��T��\n" +
				"��J��������UBroadcast�����Y�i�ǰe\n");
		
	}
	
	
	/*���U�@�����������
	 * */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		Button btn = (Button) arg0.getSource();
		

		if(textFeildFlag == true && btn == btn1){
			//frm.setBackground(Color.yellow);
			
			String msg = txa.getText() + "Server: "+txf.getText() + "\n";
			txa.setText(msg);
			castMsg("Server: " + txf.getText());	// �s���T�����䥦���Ȥ��
			
			txf.setText("");
			//startServer();
		}
		
		/*���F�קK��W�����q�@�Ĭ�A�ڳo�K��W�����q�@�˪���b�U��
		 * ��ڤW�H�޿�ӻ��A�U���o�q�|��W�����q������
		 * �U���O�ΨӳB�z�@�}�l�|�X�{"\n"��BUG��
		 * */
		if(textFeildFlag == false && btn == btn1){
			
			textFeildFlag = true;
			
			String msg = txa.getText() + "Server: "+txf.getText() + "\n";
			txa.setText(msg);
			castMsg("Server: " + txf.getText());	// �s���T�����䥦���Ȥ��
			
			txf.setText("");
		}
		
		/**---------------------------------------------------------------------------------------------------------------
		 * 2012/4/8�s�W
		 * ���U�令�C�j�@��o�X�����
		 * */
		
		
		if(btn == startSysBtn){
			//��ܹ�ܮاi�D�ϥΪ̦��A���w�g�}��
			CustomDialog dialog = new CustomDialog(this, "�T��", "Server is start.", true);
			
			startServer();
			try{
				  //do what you want to do before sleeping
				  Thread.currentThread().sleep(1000);//sleep for 1000 ms
				  //do what you want to do after sleeptig
			}
				catch(InterruptedException ie){
				//If this thread was intrrupted by nother thread 
			}
			startServerForHelper();
			try{
				  //do what you want to do before sleeping
				  Thread.currentThread().sleep(1000);//sleep for 1000 ms
				  //do what you want to do after sleeptig
			}
			catch(InterruptedException ie){
				//If this thread was intrrupted by nother thread 
			}
			startServerForDatabase();
		}
	}
	
	
	/*���U�@���������h������
	 * */
	static class Wlis extends WindowAdapter{
		public void windowClosing(WindowEvent e){	//���U������
			System.out.println("System is closing");
			frm.dispose();	//��������������귽
		}
		public void windowClosed(WindowEvent e){	//��������
			
		}
		public void windowDeactivated(WindowEvent e){	//�ܦ��D�@�ε���
			
		}
		public void windowActivated(WindowEvent e){	//�ܦ��@�ε���
	
		}

	}
	
	/*���U�w�q���r������
	 * */
	static class KeyLis extends KeyAdapter{
		public void keypressed(KeyEvent e){
			txa.setText("");
			if(e.isActionKey())
				txa.setText(txf.getText());
			else
				txa.append(e.getKeyChar()+" is pressed\n");
		}
		public void keyReleased(KeyEvent e){
			
		}
		public void keyTyped(KeyEvent e){
			
		}
	}
	
	
	/*���U���\��]�w�禡��
	 * */
	
	synchronized static void startServer(){
		// �H�s��������Ӱ���
					Thread t = new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								
								serverSocket = new ServerSocket(serverport);
								sysMsgTxa.setText(sysMsgTxa.getText() + "Server is startting...\n" + "�w��ť��"+serverport+"��I\n");
								System.out.println("�w��ť��"+serverport+"��I");
								System.out.println("Server is startting...");
								
								// ��Server�B�@����
								while (!serverSocket.isClosed()) {
									// ��ܵ��ݫȤ�ݳs��
									sysMsgTxa.setText(sysMsgTxa.getText() + "Wait new clinet connect...\n");
									System.out.println("Wait new clinet connect");
									// �I�s���ݱ����Ȥ�ݳs��
									waitNewPlayer();
								}

								} catch (IOException e) {
									sysMsgTxa.setText(sysMsgTxa.getText() + "Server Socket ERROR...\n");
									System.out.println("Server Socket ERROR");
								}
						}
					});
					
					// �Ұʰ����
					t.start();
		
		/*try {
			
			serverSocket = new ServerSocket(serverport);
			sysMsgTxa.setText(sysMsgTxa.getText() + "Server is startting...\n" + "�w��ť��8888��I\n");
			System.out.println("�w��ť��8888��I");
			System.out.println("Server is startting...");
			
			// ��Server�B�@����
			while (!serverSocket.isClosed()) {
				// ��ܵ��ݫȤ�ݳs��
				sysMsgTxa.setText(sysMsgTxa.getText() + "Wait new clinet connect...\n");
				System.out.println("Wait new clinet connect");
				// �I�s���ݱ����Ȥ�ݳs��
				waitNewPlayer();
			}

		} catch (IOException e) {
			sysMsgTxa.setText(sysMsgTxa.getText() + "Server Socket ERROR...\n");
			System.out.println("Server Socket ERROR");
		}*/
	}
	
	
	// ���ݱ����Ȥ�ݳs��
	public static void waitNewPlayer() {
		try {
			Socket socket = serverSocket.accept();
				
			// �I�s�гy�s���ϥΪ�
			createNewPlayer(socket);
		} catch (IOException e) {

		}

	}

	// �гy�s���ϥΪ�
	synchronized static void createNewPlayer(final Socket socket) {
		// �H�s��������Ӱ���
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					// �W�[�s���ϥΪ�
					players.add(socket);
					
					userNumber++;	//�Ψӭp��ثe�ϥΤH��
					users++;	//�P�W�A���O�Ψӭp����v�H�ƪ�
					System.out.println("users: " + userNumber);
					sock[userNumber] = socket;
					//Socket oriSocket = socket;
					s = players.size();
					
					showIpTxa.setText("");	//���M�šA�����᪺�F���s
					
					/* ���U���T�{�H���O�_���ƪ����p
					 * ���{�b�]��IP���O�P�@�Ӫ����D�ҥH�u�Ȯɥ�����o��(�z�פW���Ӥw�g���n�L�o���ʧ@�F)
					 * */
					/*if(userNumber == 0){	//��ܬ�0�ɥu���W�[�H�ƪ��ʧ@
						userNumber++;	//�Ψӭp��ثe�ϥΤH��
						
						//�C�����T�{�A�T�O�S�����ƪ��ϥΪ̦W��
						boolean checkFlag = false;
						for(int i=0;i<userNumber;i++){
							if(socket.equals(sock[i]))
								checkFlag = true;	//true��ܦ�����
						}
						
						if(checkFlag == false){	//��o�̤��M��false�A��ܨS���ۦP��
							users++;	//�P�W�A���O�Ψӭp����v�H�ƪ�
							System.out.println("users: " + userNumber);
							sock[userNumber] = socket;
						}
					}else if(userNumber != 0){
						//�C�����T�{�A�T�O�S�����ƪ��ϥΪ̦W��
						boolean checkFlag = false;
						for(int i=0;i<userNumber;i++){
							if(socket.equals(sock[i]))
								checkFlag = true;	//true��ܦ�����
						}
						
						if(checkFlag == false){	//��o�̤��M��false�A��ܨS���ۦP��
							userNumber++;	//�Ψӭp��ثe�ϥΤH��
							users++;	//�P�W�A���O�Ψӭp����v�H�ƪ�
							System.out.println("users: " + userNumber);
							sock[userNumber] = socket;
						}
					}*/
					
					//��ܥثe�ϥΤH���Ҧ�IP
					for(int i=0;i<players.size();i++){
						//System.out.println("sock["+userNumber+"]ip: " + sock[userNumber].getInetAddress());//�C�L�Τ��IPt
						showIpTxa.setText(showIpTxa.getText() + "sock["+i+"]ip: " + players.get(i).getInetAddress() + "\n");
						//showIpTxa.setText(showIpTxa.getText() + "sock["+userNumber+"]ip: " + sock[userNumber].getLocalAddress() + "\n");
					}	//�H�r��
						
					// ���o������y 
					BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF8"));

					// ��Socket�w�s���ɳs�����
					while (socket.isConnected()) {
						// ���o������y���T��
						String msg= br.readLine();
							
						// ����Ŧr��ɧP�w���_�u
						if (msg==null)
							break;
						
						System.out.println("msg:"+msg);
						
						// ��X�ǰe���j�a���T��
						/*��Ū�K�X��...
						 * �o�̭n��contains�����equal��indexof�A�]������ݥ��X�Ӫ��T�����O��ª��K�X�A�L���X�ӳ��O"player01: XXXXXXXXXXX"
						 * �ҥH�n�Υ]�t�r��A�Ӥ��O�r���]���O�۵�
						 * */
						
						
						/**----------2012/4/10�ץ�
						 * �N��L�D�D�s�u��1134567���屼�@�M�@��
						 * 
						 * 
						 * */
						if(msg.contains("1134567")){
							//msg = "test";
							//castMsgForSignal("1134567");	//�M���ǰT���Ϊ�
							
							//txa.setText(txa.getText() + msg + "\n");
							castMsg("1134567");	// �s���T�����䥦���Ȥ��
							System.out.println("msg.equals(test) is called");
							//System.out.println("msg:"+msg);
							
							String a = msg.replace("1134567", "I'm needs help! \n");
							//msg.substring(0, 8)
							showHelpMsgTxa.setText(showHelpMsgTxa.getText() + a+"\n");
							Date date = new Date();
							showHelpMsgTxa.setText(showHelpMsgTxa.getText() + date.toString() + "\n");
							showHelpMsgTxa.setText(showHelpMsgTxa.getText() + "---------------------------" + "\n");
							
						}else if(msg.contains("2234567")){
							BufferedWriter bw;
							try {
								// ���o������X��y
								bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
								// �g�J�T��
								bw.write("2234567"+users+"\n");
								// �ߧY�o�e
								bw.flush();
								System.out.println("castMsgFromServer(2234567) is called");
								System.out.println("users is " + users);
							} catch (IOException e) {
									
							}
						}else if(msg.contains("3334567")){
							System.out.println("Test connection");
							castuser(msg,s);
						}else if(msg.contains("4434567")){
							helpers.add(socket);
							loginerIP = socket.getInetAddress().toString();
							showHelperIPTxa.append(loginerIP+"(�䴩��)"+"\n");
							castcaller(msg,loginerIP,caller);
						}
						else if(msg.contains("0034567")){
							
						}
						/*else if(msg.contains("3334567")){
							castMsg(msg);
							System.out.println("msg.equals.else is called");							
						}*/
						/*else{
							txa.setText(txa.getText() + msg + "\n");
							castMsg1(msg);	// �s���T�����䥦���Ȥ��
							System.out.println("msg.equals.else is called");
							//System.out.println("msg:"+msg);
						}*/else{
							txa.setText(txa.getText() + msg + "\n");
							castMsg(msg);	// �s���T�����䥦���Ȥ��
							System.out.println("msg.equals.else is called");
							//System.out.println("msg:"+msg);
						}
						
						/*txa.setText(txa.getText() + msg + "\n");
						castMsg(msg);	// �s���T�����䥦���Ȥ��
						System.out.println("msg.equals.else is called");*/

					}

				} catch (IOException e) {

				}
					
				// �����Ȥ��
				players.remove(socket);
				userNumber--;
				System.out.println("users: " + players.size());
				showIpTxa.setText("");	//���M�šA�����᪺�F���s
					
				for(int i=0;i<players.size();i++){
					//System.out.println("sock["+userNumber+"]ip: " + sock[userNumber].getInetAddress());//�C�L�Τ��IPt
					showIpTxa.setText(showIpTxa.getText() + "sock["+i+"]ip: " + players.get(i).getInetAddress() + "\n");
					//showIpTxa.setText(showIpTxa.getText() + "sock["+userNumber+"]ip: " + sock[userNumber].getLocalAddress() + "\n");
				}	//�H�r��
			}
		});
			
		// �Ұʰ����
		t.start();
	}
	
	
	/**---------------------------------------------------------------------------------------------------------------
	 * 2012/4/8�s�W
	 * ���U�s�W�M�u
	 * */
	
	/*------------------------------------------------------------------------------�M���Ψӱ����Ϩa�H�h�����A����-------------------------------------------------*/
	//�������Ϩa�̪�PORT���X�A�o�ӬO�u���Ϩa�̤W��TAB���M�u
	/*���U���\��]�w�禡��
	 * */
	
	synchronized static void startServerForHelper(){				//�Ϩa�̤W��TAB���M�u
		// �H�s��������Ӱ���
					Thread t = new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								
								serverSocket = new ServerSocket(serverportForHelper);
								sysMsgTxa.setText(sysMsgTxa.getText() + "ServerForHelper is startting...\n" + "�w��ť��"+serverportForHelper+"��I\n");
								System.out.println("�w��ť��"+serverportForHelper+"��I");
								System.out.println("Server is startting...");
								
								// ��Server�B�@����
								while (!serverSocket.isClosed()) {
									// ��ܵ��ݫȤ�ݳs��
									sysMsgTxa.setText(sysMsgTxa.getText() + "Wait new clinet connect...\n");
									System.out.println("Wait new clinet connect");
									// �I�s���ݱ����Ȥ�ݳs��
									waitNewPlayerForHelper();
								}

								} catch (IOException e) {
									sysMsgTxa.setText(sysMsgTxa.getText() + "Server Socket ERROR...\n");
									System.out.println("Server Socket ERROR");
								}
						}
					});
					
					// �Ұʰ����
					t.start();

	}
	
	
	// ���ݱ����Ȥ�ݳs��
	public static void waitNewPlayerForHelper() {		//�Ϩa�̤W��TAB���M�u
		try {
			Socket socket = serverSocket.accept();
				
			// �I�s�гy�s���ϥΪ�
			createNewPlayerForHelper(socket);
		} catch (IOException e) {

		}

	}

	// �гy�s���ϥΪ�
	synchronized static void createNewPlayerForHelper(final Socket socket) {			//�Ϩa�̤W��TAB���M�u
		// �H�s��������Ӱ���
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					// �W�[�s���ϥΪ�
//					showIpTxa.setText("");	//���M�šA�����᪺�F���s

					// ���o������y 
					BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF8"));

					// ��Socket�w�s���ɳs�����
					while (socket.isConnected()) {
						// ���o������y���T��
						String msg= br.readLine();
							
						// ����Ŧr��ɧP�w���_�u
						if (msg==null)
							break;
						
						System.out.println("msg:"+msg);
						
						// ��X�ǰe���j�a���T��
						/*��Ū�K�X��...
						 * �o�̭n��contains�����equal��indexof�A�]������ݥ��X�Ӫ��T�����O��ª��K�X�A�L���X�ӳ��O"player01: XXXXXXXXXXX"
						 * �ҥH�n�Υ]�t�r��A�Ӥ��O�r���]���O�۵�
						 * */
						if(msg.contains("1134567")){
							//msg = "test";
							//castMsgForSignal("1134567");	//�M���ǰT���Ϊ�
							
							//txa.setText(txa.getText() + msg + "\n");
//							castMsg("1134567");	// �s���T�����䥦���Ȥ��
//							System.out.println("msg.equals(test) is called");
//							//System.out.println("msg:"+msg);
//							
//							String a = msg.replace("1134567", "I'm");
//							//msg.substring(0, 8)
//							showHelpMsgTxa.setText(showHelpMsgTxa.getText() + a + " needs help!\n");
//							Date date = new Date();
//							showHelpMsgTxa.setText(showHelpMsgTxa.getText() + date.toString() + "\n");
//							showHelpMsgTxa.setText(showHelpMsgTxa.getText() + "---------------------------" + "\n");
							
							
						}else{
//							txa.setText(txa.getText() + msg + "\n");
//							castMsg(msg);	// �s���T�����䥦���Ȥ��
//							System.out.println("msg.equals.else is called");
//							//System.out.println("msg:"+msg);
						}

					}

				} catch (IOException e) {

				}
			}
		});
		// �Ұʰ����
		t.start();
	}
	
	
	/*------------------------------------------------------------------------------�M���Ψӱ����Ϩa�H�h���ӤH��Ʀ��A����-------------------------------------------------*/
	//�������Ϩa�̪�PORT���X�A�o�ӬO�u���Ϩa�̤W�ǭӤH��ƪ��M�u
	/*���U���\��]�w�禡��
	 * */
	
	synchronized static void startServerForDatabase(){				//�Ϩa�̤W��TAB���M�u
		// �H�s��������Ӱ���
					Thread t = new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								
								serverSocket = new ServerSocket(serverportForDatabase);
								sysMsgTxa.setText(sysMsgTxa.getText() + "ServerForHelper is startting...\n" + "�w��ť��"+serverportForDatabase+"��I\n");
								System.out.println("�w��ť��"+serverportForDatabase+"��I");
								System.out.println("Server is startting...");
								
								// ��Server�B�@����
								while (!serverSocket.isClosed()) {
									// ��ܵ��ݫȤ�ݳs��
									sysMsgTxa.setText(sysMsgTxa.getText() + "Wait new clinet connect...\n");
									System.out.println("Wait new clinet connect");
									// �I�s���ݱ����Ȥ�ݳs��
									waitNewPlayerForHelper();
								}

								} catch (IOException e) {
									sysMsgTxa.setText(sysMsgTxa.getText() + "Server Socket ERROR...\n");
									System.out.println("Server Socket ERROR");
								}
						}
					});
					
					// �Ұʰ����
					t.start();

	}
	
	
	// ���ݱ����Ȥ�ݳs��
	public static void waitNewPlayerForDatabase() {		//�Ϩa�̤W��TAB���M�u
		try {
			Socket socket = serverSocket.accept();
				
			// �I�s�гy�s���ϥΪ�
			createNewPlayerForDatabase(socket);
		} catch (IOException e) {

		}

	}

	// �гy�s���ϥΪ�
	synchronized static void createNewPlayerForDatabase(final Socket socket) {			//�Ϩa�̤W��TAB���M�u
		// �H�s��������Ӱ���
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					// �W�[�s���ϥΪ�
//					showIpTxa.setText("");	//���M�šA�����᪺�F���s

					// ���o������y 
					BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF8"));

					// ��Socket�w�s���ɳs�����
					while (socket.isConnected()) {
						// ���o������y���T��
						String msg= br.readLine();
							
						// ����Ŧr��ɧP�w���_�u
						if (msg==null)
							break;
						
						System.out.println("msg:"+msg);
						
						// ��X�ǰe���j�a���T��
						/*��Ū�K�X��...
						 * �o�̭n��contains�����equal��indexof�A�]������ݥ��X�Ӫ��T�����O��ª��K�X�A�L���X�ӳ��O"player01: XXXXXXXXXXX"
						 * �ҥH�n�Υ]�t�r��A�Ӥ��O�r���]���O�۵�
						 * */
						if(msg.contains("1134567")){
//							castMsg("1134567");	// �s���T�����䥦���Ȥ��
//							System.out.println("msg.equals(test) is called");
//							
//							showHelpMsgTxa.setText(showHelpMsgTxa.getText() + msg.substring(0, 8) + " needs help!\n");
//							Date date = new Date();
//							showHelpMsgTxa.setText(showHelpMsgTxa.getText() + date.toString() + "\n");
//							showHelpMsgTxa.setText(showHelpMsgTxa.getText() + "---------------------------" + "\n");
							
						}else{
//							txa.setText(txa.getText() + msg + "\n");
//							castMsg(msg);	// �s���T�����䥦���Ȥ��
//							System.out.println("msg.equals.else is called");
//							//System.out.println("msg:"+msg);
						}

					}

				} catch (IOException e) {

				}
			}
		});
		// �Ұʰ����
		t.start();
	}
	
	/*END-------------------------------------------------------------------------------------------------------------*/
	
	
	

	
	/*--------------------------------------------------------------------------------------------------------------------------------
	 * --------------------------------------------------------------------------------------------------------------------------------
	 *																						 broadcast��
	 * --------------------------------------------------------------------------------------------------------------------------------
	 * --------------------------------------------------------------------------------------------------------------------------------
	 * */

	// �s���T�����䥦���Ȥ��(�ʥ])
	public static void castMsg(String Msg){
		// �гysocket�}�C�A����IP��m
		Socket[] ps=new Socket[players.size()]; 
	
		// �Nplayers�ഫ���}�C�s�Jps
		players.toArray(ps);
			
		// ���Xps�����C�@�Ӥ���
		for (Socket socket :ps ) {
			try {
				// �гy������X��y
				BufferedWriter bw;
				bw = new BufferedWriter( new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
					
				// �g�J�T�����y
				bw.write(Msg+"\n");
					
				// �ߧY�o�e
				bw.flush();
					
				System.out.println("castMsg() is called");
			} catch (IOException e) {

			}
		}
	
	}
	
		
	// �s���T�����䥦���Ȥ��
	public static void castMsgFromServer(String Msg){
				//////////////////////////////////////////////////////////////////////////
				for(int i=0;i<userNumber;i++){
					if(sock[i].isConnected()){
						BufferedWriter bw;
						try {
							// ���o������X��y
							bw = new BufferedWriter(new OutputStreamWriter(sock[i].getOutputStream(), "UTF8"));
							// �g�J�T��
							bw.write(Msg+"\n");
							// �ߧY�o�e
							bw.flush();
								
							System.out.println("castMsgFromServer() is called");
						} catch (IOException e) {
								
						}
							
					}	
					
				}
	}
	
	public static void castMsgForSignal(String signal){

		// �гysocket�}�C�A����IP��m
				Socket[] ps=new Socket[players.size()]; 
					
				// �Nplayers�ഫ���}�C�s�Jps
				players.toArray(ps);
					
				// ���Xps�����C�@�Ӥ���
				for (Socket socket :ps ) {
					try {
						// �гy������X��y
						BufferedWriter bw;
						bw = new BufferedWriter( new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
							
						// �g�J�T�����y
						bw.write(signal);
							
						// �ߧY�o�e
						bw.flush();

						System.out.println("castMsgForSignal is called");
					} catch (IOException e) {

					}
				}
	}
	
	public static void castuser(String Msg,int number){
		for (int i=0;i<players.size();i++) {
			try {
				
				if(i!=number-1)
				{
				// �гy������X��y
				BufferedWriter bw;
				bw = new BufferedWriter( new OutputStreamWriter(players.get(i).getOutputStream(), "UTF8"));
					
				// �g�J�T�����y
				bw.write(Msg+"\n");
					
				// �ߧY�o�e
				bw.flush();			
								
				System.out.println("castMsg() is called");
				}
			} catch (IOException e) {

			}
		}
	
	}
	
	// �s���䴩�̦W�浹�D�Ϫ�(�ʥ])
	public static void castcaller(String Msg,String IP,int s){

			 for (int i = 0;i<players.size();i++) {	
				try {
					if(i != s-1)
					{
					// �гy������X��y
					BufferedWriter bw;
					bw = new BufferedWriter( new OutputStreamWriter(players.get(i).getOutputStream(), "UTF8"));
						
					// �g�J�T�����y
					bw.write(Msg+IP+"\n");
						
					// �ߧY�o�e
					bw.flush();
					
					
					}
				} catch (IOException e) {

				}	
			 }
		}
	
	public static void casthelper(String Msg,String Msg1,String Msg2,String Msg3,String Msg4,int s){
		
		// ���Xps�����C�@�Ӥ���
		for (int i=0;i<players.size();i++) {
			try {
				
				if(i!=s-1)
				{
				// �гy������X��y
				BufferedWriter bw;
				bw = new BufferedWriter( new OutputStreamWriter(players.get(i).getOutputStream(), "UTF8"));
					
				// �g�J�T�����y
				bw.write(Msg+"\n");
					
				// �ߧY�o�e
				bw.flush();
				
				bw.write(Msg1+"\n");
					
					// �ߧY�o�e
				bw.flush();
					
	            bw.write(Msg2+"\n");
					
					// �ߧY�o�e
				bw.flush();
					
	            bw.write(Msg3+"\n");
					
					// �ߧY�o�e
				bw.flush();
				
				bw.write(Msg4+"\n");
				
				// �ߧY�o�e
			    bw.flush();
					
				System.out.println("castMsg() is called");
				}
			} catch (IOException e) {

			}
		}
	
	}
	
	
	
}//End class
