package server;
/*This program is created at 20120218
 * It is created for Dica3 system
 * This server will include:
 * 1.Recording connecting ip.
 * 2.顯示進入人IP
 * 3.顯示系統狀況
 * 4.控制手機端畫面
 * 5.新增Dialog顯示窗部份(2012/3/18)
 * 
 * (2012/4/7)
 * 新增專線給Dica4救災人的使用者
 * 一樣的做法，只是我再多做了XXX+ForHelper的函示名稱
 * 這個用來作為第二個聯繫方式(我PORT設為8079)
 * (2012/4/8)
 *  新增專線給Dica4救災人的使用者
 * 一樣的做法，只是我再多做了XXX+ForDatabase的函示名稱
 * 這個用來作為傳送使用者資料(我PORT設為8078)
 * 
 * 新增解碼號碼3334567
 * 當傳送這個時則表示是使用者個人資料進來了
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
	
	//底下做為跟介面相關區
	static server frm = new server();	//初始畫面
	static Button btn1 = new Button("Broadcast");	//訂定元件，用來傳送訊息
	static Button startSysBtn = new Button("Start System");	//訂定按鍵，用來啟動伺服器
	static Label lab1 = new Label("Please keyin message on the field:");	//增加顯示元件
	static Label lab2 = new Label("History messages:");	//增加顯示元件
	static Label lab3 = new Label("Helping requests:");	//增加顯示元件
	static Label lab4 = new Label("System messages:");	//增加顯示元件
	static Label lab5 = new Label("IP manager:");	//增加顯示元件
	static Label lab6 = new Label("Requesters IP:");	//增加顯示元件
	static Label lab7 = new Label("Helpers IP:");	//增加顯示元件
	static Label lab8 = new Label("說明:");	//增加顯示元件
	static TextArea txf = new TextArea("",4,19,TextArea.SCROLLBARS_NONE);	//用來輸入訊息
	static TextArea txa = new TextArea("",4,19,TextArea.SCROLLBARS_BOTH);	//用來顯示聊天室目前狀況
	static TextArea sysMsgTxa = new TextArea("",4,19,TextArea.SCROLLBARS_BOTH);	//用來顯示系統連接狀況
	static TextArea showIpTxa = new TextArea("",4,19,TextArea.SCROLLBARS_BOTH);	//用來顯示系統連接IP狀況
	static TextArea showHelpMsgTxa = new TextArea("",4,19,TextArea.SCROLLBARS_BOTH);	//用來顯示求救狀況
	static TextArea showRequesterIPTxa = new TextArea("",4,19,TextArea.SCROLLBARS_BOTH);	//用來顯示求救者IP
	static TextArea showHelperIPTxa = new TextArea("",4,19,TextArea.SCROLLBARS_BOTH);	//用來顯示幫助者IP
	static TextArea showProgammersTxa = new TextArea("",4,19,TextArea.SCROLLBARS_BOTH);	//用來顯示求救狀況
	
	static Wlis wlis = new Wlis();	//做視窗預設按鍵的聆聽者
	
	public boolean textFeildFlag = false;	//鎖定解決一開始跳行的問題~
	
	
	/*底下作為其他API區
	 * */
	/*------------------------底下開始為網路協定區-------------------------------*/
	private static int serverport = 8080;	//紀錄PORT號碼
	
	/**---------------------------------------------------------------------------------------------------------------
	 * 2012/4/8新增
	 * 底下改成每隔一秒發出執行緒
	 * */
	private static int serverportForHelper = 8079;  //紀錄給救災者的PORT號碼，這個是只給救災者上面TAB的專線
	private static int serverportForDatabase = 8078;	//紀錄給救災者上傳個人資料的PORT號碼專線
	/**---新增STOP-----------------------------------------------------------*/
	
	
	private static ServerSocket serverSocket;
	
	private static String callerIP;
	private static String loginerIP;
	public static String SERVERIP = "";
	
	// 用串列來儲存每個client
	private static ArrayList<Socket> players=new ArrayList<Socket>();	//紀錄總共使用者
	private static ArrayList<Socket> helpers=new ArrayList<Socket>();	//紀錄總共幫助者
	private static ArrayList<Socket> callers=new ArrayList<Socket>();	//紀錄總共求救者
	
	public static int userNumber = 0;	//用來記錄目前有幾個人已連接近伺服器
	static Socket sock[] = new Socket[3];	//用來存曾經有過的IP
	public static int users = 0;	//用來記錄裡使至今有多少人連進來
	
	private static int s =0;	//暫存總共使用人
	private static int caller = 0;
	
	/*------------------------網路協定區結束-------------------------------------*/
	
	public static void main(String args[]) {
		// TODO Auto-generated method stub
		

		//介面底層區
		frm.setTitle("Dica3 server");
		frm.setSize(800,800);
		frm.setLayout(null);		
		
		//介面元件區
		startSysBtn.setBounds(20, 40, 200, 100);
		btn1.setBounds(220, 40, 200, 100);
		
		/*左邊*/
		lab1.setBounds(20, 150, 200, 25);
		txf.setBounds(20, 175, 300, 100);
		lab2.setBounds(20, 275, 200, 25);
		txa.setBounds(20, 300, 300, 200);
		lab3.setBounds(20, 500, 200, 25);
		showHelpMsgTxa.setBounds(20, 525, 350, 100);
		lab8.setBounds(10, 625, 200, 25);
		showProgammersTxa.setBounds(10, 650, 400, 100);
		
		/*右邊*/
		lab4.setBounds(425, 30, 200, 25);
		sysMsgTxa.setBounds(425, 55, 300, 200);
		lab5.setBounds(425, 255, 200, 25);
		showIpTxa.setBounds(425, 280, 300, 200);
		lab6.setBounds(425, 480, 200, 25);
		showRequesterIPTxa.setBounds(425, 505, 300, 100);
		lab7.setBounds(425, 605, 200, 25);
		showHelperIPTxa.setBounds(425, 630, 300, 100);
		
		
		//事件聆聽者區
		frm.addWindowListener(wlis);
		btn1.addActionListener(frm);
		startSysBtn.addActionListener(frm);
		txf.addKeyListener(new KeyLis());		
		
		//介面顯示管理
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
		
		//其他底層功能增加區
		//startServer();
		showProgammersTxa.setText(
				"本程式是由白繕維、林俊佑、陳以龍所製\n" +
				"使用前請先按下上方Start system按鍵\n" +
				"接著便可以看到伺服器成功建置資訊\n" +
				"您可以在按鍵下方的輸入框中輸入要廣播給其他使用者的訊息\n" +
				"輸入完之後按下Broadcast按鍵後即可傳送\n");
		
	}
	
	
	/*底下作為按鍵反應區
	 * */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		Button btn = (Button) arg0.getSource();
		

		if(textFeildFlag == true && btn == btn1){
			//frm.setBackground(Color.yellow);
			
			String msg = txa.getText() + "Server: "+txf.getText() + "\n";
			txa.setText(msg);
			castMsg("Server: " + txf.getText());	// 廣播訊息給其它的客戶端
			
			txf.setText("");
			//startServer();
		}
		
		/*為了避免跟上面那段作衝突，我這便把上面那段一樣的放在下面
		 * 實際上以邏輯來說，下面這段會比上面那段先執行
		 * 下面是用來處理一開始會出現"\n"的BUG的
		 * */
		if(textFeildFlag == false && btn == btn1){
			
			textFeildFlag = true;
			
			String msg = txa.getText() + "Server: "+txf.getText() + "\n";
			txa.setText(msg);
			castMsg("Server: " + txf.getText());	// 廣播訊息給其它的客戶端
			
			txf.setText("");
		}
		
		/**---------------------------------------------------------------------------------------------------------------
		 * 2012/4/8新增
		 * 底下改成每隔一秒發出執行緒
		 * */
		
		
		if(btn == startSysBtn){
			//顯示對話框告訴使用者伺服器已經開啟
			CustomDialog dialog = new CustomDialog(this, "訊息", "Server is start.", true);
			
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
	
	
	/*底下作為視窗底層反應區
	 * */
	static class Wlis extends WindowAdapter{
		public void windowClosing(WindowEvent e){	//按下關閉紐
			System.out.println("System is closing");
			frm.dispose();	//關閉視窗並釋放資源
		}
		public void windowClosed(WindowEvent e){	//關閉視窗
			
		}
		public void windowDeactivated(WindowEvent e){	//變成非作用視窗
			
		}
		public void windowActivated(WindowEvent e){	//變成作用視窗
	
		}

	}
	
	/*底下定義打字反應區
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
	
	
	/*底下為功能設定函式區
	 * */
	
	synchronized static void startServer(){
		// 以新的執行緒來執行
					Thread t = new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								
								serverSocket = new ServerSocket(serverport);
								sysMsgTxa.setText(sysMsgTxa.getText() + "Server is startting...\n" + "已監聽到"+serverport+"埠！\n");
								System.out.println("已監聽到"+serverport+"埠！");
								System.out.println("Server is startting...");
								
								// 當Server運作中時
								while (!serverSocket.isClosed()) {
									// 顯示等待客戶端連接
									sysMsgTxa.setText(sysMsgTxa.getText() + "Wait new clinet connect...\n");
									System.out.println("Wait new clinet connect");
									// 呼叫等待接受客戶端連接
									waitNewPlayer();
								}

								} catch (IOException e) {
									sysMsgTxa.setText(sysMsgTxa.getText() + "Server Socket ERROR...\n");
									System.out.println("Server Socket ERROR");
								}
						}
					});
					
					// 啟動執行緒
					t.start();
		
		/*try {
			
			serverSocket = new ServerSocket(serverport);
			sysMsgTxa.setText(sysMsgTxa.getText() + "Server is startting...\n" + "已監聽到8888埠！\n");
			System.out.println("已監聽到8888埠！");
			System.out.println("Server is startting...");
			
			// 當Server運作中時
			while (!serverSocket.isClosed()) {
				// 顯示等待客戶端連接
				sysMsgTxa.setText(sysMsgTxa.getText() + "Wait new clinet connect...\n");
				System.out.println("Wait new clinet connect");
				// 呼叫等待接受客戶端連接
				waitNewPlayer();
			}

		} catch (IOException e) {
			sysMsgTxa.setText(sysMsgTxa.getText() + "Server Socket ERROR...\n");
			System.out.println("Server Socket ERROR");
		}*/
	}
	
	
	// 等待接受客戶端連接
	public static void waitNewPlayer() {
		try {
			Socket socket = serverSocket.accept();
				
			// 呼叫創造新的使用者
			createNewPlayer(socket);
		} catch (IOException e) {

		}

	}

	// 創造新的使用者
	synchronized static void createNewPlayer(final Socket socket) {
		// 以新的執行緒來執行
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					// 增加新的使用者
					players.add(socket);
					
					userNumber++;	//用來計算目前使用人數
					users++;	//同上，但是用來計算歷史人數的
					System.out.println("users: " + userNumber);
					sock[userNumber] = socket;
					//Socket oriSocket = socket;
					s = players.size();
					
					showIpTxa.setText("");	//先清空，讓之後的東西刷新
					
					/* 底下做確認人員是否重複的情況
					 * 但現在因為IP都是同一個的問題所以只暫時先做到這裡(理論上應該已經做好過濾的動作了)
					 * */
					/*if(userNumber == 0){	//表示為0時只做增加人數的動作
						userNumber++;	//用來計算目前使用人數
						
						//每次做確認，確保沒有重複的使用者名稱
						boolean checkFlag = false;
						for(int i=0;i<userNumber;i++){
							if(socket.equals(sock[i]))
								checkFlag = true;	//true表示有重複
						}
						
						if(checkFlag == false){	//到這裡仍然為false，表示沒有相同的
							users++;	//同上，但是用來計算歷史人數的
							System.out.println("users: " + userNumber);
							sock[userNumber] = socket;
						}
					}else if(userNumber != 0){
						//每次做確認，確保沒有重複的使用者名稱
						boolean checkFlag = false;
						for(int i=0;i<userNumber;i++){
							if(socket.equals(sock[i]))
								checkFlag = true;	//true表示有重複
						}
						
						if(checkFlag == false){	//到這裡仍然為false，表示沒有相同的
							userNumber++;	//用來計算目前使用人數
							users++;	//同上，但是用來計算歷史人數的
							System.out.println("users: " + userNumber);
							sock[userNumber] = socket;
						}
					}*/
					
					//顯示目前使用人的所有IP
					for(int i=0;i<players.size();i++){
						//System.out.println("sock["+userNumber+"]ip: " + sock[userNumber].getInetAddress());//列印用戶端IPt
						showIpTxa.setText(showIpTxa.getText() + "sock["+i+"]ip: " + players.get(i).getInetAddress() + "\n");
						//showIpTxa.setText(showIpTxa.getText() + "sock["+userNumber+"]ip: " + sock[userNumber].getLocalAddress() + "\n");
					}	//黏字串
						
					// 取得網路串流 
					BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF8"));

					// 當Socket已連接時連續執行
					while (socket.isConnected()) {
						// 取得網路串流的訊息
						String msg= br.readLine();
							
						// 收到空字串時判定為斷線
						if (msg==null)
							break;
						
						System.out.println("msg:"+msg);
						
						// 輸出傳送給大家的訊息
						/*解讀密碼端...
						 * 這裡要用contains不能用equal跟indexof，因為手機端打出來的訊號不是單純的密碼，他打出來都是"player01: XXXXXXXXXXX"
						 * 所以要用包含字串，而不是字元也不是相等
						 * */
						
						
						/**----------2012/4/10修正
						 * 將其他非主連線的1134567給砍掉作專一化
						 * 
						 * 
						 * */
						if(msg.contains("1134567")){
							//msg = "test";
							//castMsgForSignal("1134567");	//專給傳訊號用的
							
							//txa.setText(txa.getText() + msg + "\n");
							castMsg("1134567");	// 廣播訊息給其它的客戶端
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
								// 取得網路輸出串流
								bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
								// 寫入訊息
								bw.write("2234567"+users+"\n");
								// 立即發送
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
							showHelperIPTxa.append(loginerIP+"(支援者)"+"\n");
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
							castMsg1(msg);	// 廣播訊息給其它的客戶端
							System.out.println("msg.equals.else is called");
							//System.out.println("msg:"+msg);
						}*/else{
							txa.setText(txa.getText() + msg + "\n");
							castMsg(msg);	// 廣播訊息給其它的客戶端
							System.out.println("msg.equals.else is called");
							//System.out.println("msg:"+msg);
						}
						
						/*txa.setText(txa.getText() + msg + "\n");
						castMsg(msg);	// 廣播訊息給其它的客戶端
						System.out.println("msg.equals.else is called");*/

					}

				} catch (IOException e) {

				}
					
				// 移除客戶端
				players.remove(socket);
				userNumber--;
				System.out.println("users: " + players.size());
				showIpTxa.setText("");	//先清空，讓之後的東西刷新
					
				for(int i=0;i<players.size();i++){
					//System.out.println("sock["+userNumber+"]ip: " + sock[userNumber].getInetAddress());//列印用戶端IPt
					showIpTxa.setText(showIpTxa.getText() + "sock["+i+"]ip: " + players.get(i).getInetAddress() + "\n");
					//showIpTxa.setText(showIpTxa.getText() + "sock["+userNumber+"]ip: " + sock[userNumber].getLocalAddress() + "\n");
				}	//黏字串
			}
		});
			
		// 啟動執行緒
		t.start();
	}
	
	
	/**---------------------------------------------------------------------------------------------------------------
	 * 2012/4/8新增
	 * 底下新增專線
	 * */
	
	/*------------------------------------------------------------------------------專門用來接收救災人士的伺服器區-------------------------------------------------*/
	//紀錄給救災者的PORT號碼，這個是只給救災者上面TAB的專線
	/*底下為功能設定函式區
	 * */
	
	synchronized static void startServerForHelper(){				//救災者上面TAB的專線
		// 以新的執行緒來執行
					Thread t = new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								
								serverSocket = new ServerSocket(serverportForHelper);
								sysMsgTxa.setText(sysMsgTxa.getText() + "ServerForHelper is startting...\n" + "已監聽到"+serverportForHelper+"埠！\n");
								System.out.println("已監聽到"+serverportForHelper+"埠！");
								System.out.println("Server is startting...");
								
								// 當Server運作中時
								while (!serverSocket.isClosed()) {
									// 顯示等待客戶端連接
									sysMsgTxa.setText(sysMsgTxa.getText() + "Wait new clinet connect...\n");
									System.out.println("Wait new clinet connect");
									// 呼叫等待接受客戶端連接
									waitNewPlayerForHelper();
								}

								} catch (IOException e) {
									sysMsgTxa.setText(sysMsgTxa.getText() + "Server Socket ERROR...\n");
									System.out.println("Server Socket ERROR");
								}
						}
					});
					
					// 啟動執行緒
					t.start();

	}
	
	
	// 等待接受客戶端連接
	public static void waitNewPlayerForHelper() {		//救災者上面TAB的專線
		try {
			Socket socket = serverSocket.accept();
				
			// 呼叫創造新的使用者
			createNewPlayerForHelper(socket);
		} catch (IOException e) {

		}

	}

	// 創造新的使用者
	synchronized static void createNewPlayerForHelper(final Socket socket) {			//救災者上面TAB的專線
		// 以新的執行緒來執行
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					// 增加新的使用者
//					showIpTxa.setText("");	//先清空，讓之後的東西刷新

					// 取得網路串流 
					BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF8"));

					// 當Socket已連接時連續執行
					while (socket.isConnected()) {
						// 取得網路串流的訊息
						String msg= br.readLine();
							
						// 收到空字串時判定為斷線
						if (msg==null)
							break;
						
						System.out.println("msg:"+msg);
						
						// 輸出傳送給大家的訊息
						/*解讀密碼端...
						 * 這裡要用contains不能用equal跟indexof，因為手機端打出來的訊號不是單純的密碼，他打出來都是"player01: XXXXXXXXXXX"
						 * 所以要用包含字串，而不是字元也不是相等
						 * */
						if(msg.contains("1134567")){
							//msg = "test";
							//castMsgForSignal("1134567");	//專給傳訊號用的
							
							//txa.setText(txa.getText() + msg + "\n");
//							castMsg("1134567");	// 廣播訊息給其它的客戶端
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
//							castMsg(msg);	// 廣播訊息給其它的客戶端
//							System.out.println("msg.equals.else is called");
//							//System.out.println("msg:"+msg);
						}

					}

				} catch (IOException e) {

				}
			}
		});
		// 啟動執行緒
		t.start();
	}
	
	
	/*------------------------------------------------------------------------------專門用來接收救災人士的個人資料伺服器區-------------------------------------------------*/
	//紀錄給救災者的PORT號碼，這個是只給救災者上傳個人資料的專線
	/*底下為功能設定函式區
	 * */
	
	synchronized static void startServerForDatabase(){				//救災者上面TAB的專線
		// 以新的執行緒來執行
					Thread t = new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								
								serverSocket = new ServerSocket(serverportForDatabase);
								sysMsgTxa.setText(sysMsgTxa.getText() + "ServerForHelper is startting...\n" + "已監聽到"+serverportForDatabase+"埠！\n");
								System.out.println("已監聽到"+serverportForDatabase+"埠！");
								System.out.println("Server is startting...");
								
								// 當Server運作中時
								while (!serverSocket.isClosed()) {
									// 顯示等待客戶端連接
									sysMsgTxa.setText(sysMsgTxa.getText() + "Wait new clinet connect...\n");
									System.out.println("Wait new clinet connect");
									// 呼叫等待接受客戶端連接
									waitNewPlayerForHelper();
								}

								} catch (IOException e) {
									sysMsgTxa.setText(sysMsgTxa.getText() + "Server Socket ERROR...\n");
									System.out.println("Server Socket ERROR");
								}
						}
					});
					
					// 啟動執行緒
					t.start();

	}
	
	
	// 等待接受客戶端連接
	public static void waitNewPlayerForDatabase() {		//救災者上面TAB的專線
		try {
			Socket socket = serverSocket.accept();
				
			// 呼叫創造新的使用者
			createNewPlayerForDatabase(socket);
		} catch (IOException e) {

		}

	}

	// 創造新的使用者
	synchronized static void createNewPlayerForDatabase(final Socket socket) {			//救災者上面TAB的專線
		// 以新的執行緒來執行
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					// 增加新的使用者
//					showIpTxa.setText("");	//先清空，讓之後的東西刷新

					// 取得網路串流 
					BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF8"));

					// 當Socket已連接時連續執行
					while (socket.isConnected()) {
						// 取得網路串流的訊息
						String msg= br.readLine();
							
						// 收到空字串時判定為斷線
						if (msg==null)
							break;
						
						System.out.println("msg:"+msg);
						
						// 輸出傳送給大家的訊息
						/*解讀密碼端...
						 * 這裡要用contains不能用equal跟indexof，因為手機端打出來的訊號不是單純的密碼，他打出來都是"player01: XXXXXXXXXXX"
						 * 所以要用包含字串，而不是字元也不是相等
						 * */
						if(msg.contains("1134567")){
//							castMsg("1134567");	// 廣播訊息給其它的客戶端
//							System.out.println("msg.equals(test) is called");
//							
//							showHelpMsgTxa.setText(showHelpMsgTxa.getText() + msg.substring(0, 8) + " needs help!\n");
//							Date date = new Date();
//							showHelpMsgTxa.setText(showHelpMsgTxa.getText() + date.toString() + "\n");
//							showHelpMsgTxa.setText(showHelpMsgTxa.getText() + "---------------------------" + "\n");
							
						}else{
//							txa.setText(txa.getText() + msg + "\n");
//							castMsg(msg);	// 廣播訊息給其它的客戶端
//							System.out.println("msg.equals.else is called");
//							//System.out.println("msg:"+msg);
						}

					}

				} catch (IOException e) {

				}
			}
		});
		// 啟動執行緒
		t.start();
	}
	
	/*END-------------------------------------------------------------------------------------------------------------*/
	
	
	

	
	/*--------------------------------------------------------------------------------------------------------------------------------
	 * --------------------------------------------------------------------------------------------------------------------------------
	 *																						 broadcast區
	 * --------------------------------------------------------------------------------------------------------------------------------
	 * --------------------------------------------------------------------------------------------------------------------------------
	 * */

	// 廣播訊息給其它的客戶端(封包)
	public static void castMsg(String Msg){
		// 創造socket陣列，紀錄IP位置
		Socket[] ps=new Socket[players.size()]; 
	
		// 將players轉換成陣列存入ps
		players.toArray(ps);
			
		// 走訪ps中的每一個元素
		for (Socket socket :ps ) {
			try {
				// 創造網路輸出串流
				BufferedWriter bw;
				bw = new BufferedWriter( new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
					
				// 寫入訊息到串流
				bw.write(Msg+"\n");
					
				// 立即發送
				bw.flush();
					
				System.out.println("castMsg() is called");
			} catch (IOException e) {

			}
		}
	
	}
	
		
	// 廣播訊息給其它的客戶端
	public static void castMsgFromServer(String Msg){
				//////////////////////////////////////////////////////////////////////////
				for(int i=0;i<userNumber;i++){
					if(sock[i].isConnected()){
						BufferedWriter bw;
						try {
							// 取得網路輸出串流
							bw = new BufferedWriter(new OutputStreamWriter(sock[i].getOutputStream(), "UTF8"));
							// 寫入訊息
							bw.write(Msg+"\n");
							// 立即發送
							bw.flush();
								
							System.out.println("castMsgFromServer() is called");
						} catch (IOException e) {
								
						}
							
					}	
					
				}
	}
	
	public static void castMsgForSignal(String signal){

		// 創造socket陣列，紀錄IP位置
				Socket[] ps=new Socket[players.size()]; 
					
				// 將players轉換成陣列存入ps
				players.toArray(ps);
					
				// 走訪ps中的每一個元素
				for (Socket socket :ps ) {
					try {
						// 創造網路輸出串流
						BufferedWriter bw;
						bw = new BufferedWriter( new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
							
						// 寫入訊息到串流
						bw.write(signal);
							
						// 立即發送
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
				// 創造網路輸出串流
				BufferedWriter bw;
				bw = new BufferedWriter( new OutputStreamWriter(players.get(i).getOutputStream(), "UTF8"));
					
				// 寫入訊息到串流
				bw.write(Msg+"\n");
					
				// 立即發送
				bw.flush();			
								
				System.out.println("castMsg() is called");
				}
			} catch (IOException e) {

			}
		}
	
	}
	
	// 廣播支援者名單給求救者(封包)
	public static void castcaller(String Msg,String IP,int s){

			 for (int i = 0;i<players.size();i++) {	
				try {
					if(i != s-1)
					{
					// 創造網路輸出串流
					BufferedWriter bw;
					bw = new BufferedWriter( new OutputStreamWriter(players.get(i).getOutputStream(), "UTF8"));
						
					// 寫入訊息到串流
					bw.write(Msg+IP+"\n");
						
					// 立即發送
					bw.flush();
					
					
					}
				} catch (IOException e) {

				}	
			 }
		}
	
	public static void casthelper(String Msg,String Msg1,String Msg2,String Msg3,String Msg4,int s){
		
		// 走訪ps中的每一個元素
		for (int i=0;i<players.size();i++) {
			try {
				
				if(i!=s-1)
				{
				// 創造網路輸出串流
				BufferedWriter bw;
				bw = new BufferedWriter( new OutputStreamWriter(players.get(i).getOutputStream(), "UTF8"));
					
				// 寫入訊息到串流
				bw.write(Msg+"\n");
					
				// 立即發送
				bw.flush();
				
				bw.write(Msg1+"\n");
					
					// 立即發送
				bw.flush();
					
	            bw.write(Msg2+"\n");
					
					// 立即發送
				bw.flush();
					
	            bw.write(Msg3+"\n");
					
					// 立即發送
				bw.flush();
				
				bw.write(Msg4+"\n");
				
				// 立即發送
			    bw.flush();
					
				System.out.println("castMsg() is called");
				}
			} catch (IOException e) {

			}
		}
	
	}
	
	
	
}//End class
