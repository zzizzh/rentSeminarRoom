package save;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import DataManagement.roomFile;
import DataManagement.userFile;
import Foundation.roomList;
import Foundation.userList;
import ProblemDomain.User;
import ProblemDomain.companyUser;
import ProblemDomain.personalUser;

public class Server_booking extends Thread// ����������Ŭ����
{
	private ServerSocket server;
	private int port;
	private int clientNumber;
	private static ArrayList<EchoThread> clientList;
	private BookHandle bh;

	private ArrayList<String> bookList;
	private ServerConsole serverConsole; 
	public Server_booking(int port) {
		clientList = new ArrayList<EchoThread>();
		bookList= new ArrayList<String>();
		this.port = port;
		clientNumber = 0;
		bh = new BookHandle(bookList);
		bh.start();
		serverConsole = new ServerConsole();
	}

	public void run() {
		try {
			server = new ServerSocket(port);
			System.out.println("������ ��ٸ��ϴ�.");

			while (true) {

				Socket sock = server.accept(); // ���� ���

				EchoThread echothread = new EchoThread(sock, clientNumber++, bookList); 
				echothread.start(); 
				
				System.out.println("ActiveCount : " + EchoThread.activeCount);
				clientList.add(echothread);

			}

		} catch (Exception e) {

			System.out.println(e);

		}
	}

	public static ArrayList<EchoThread> getEchoThreadList() {
		return clientList;
	}

}

class EchoThread extends Thread { // ��Ŭ���̾�Ʈ�� ��Ƽ������ ����
	private ArrayList<EchoThread> clientList;
	static int activeCount = 0;

	private Socket sock;
	private int clientNumber;

	ObjectOutputStream serverOutputStream;
	OutputStream out;
	ObjectInputStream in;
	PrintWriter pw;
	BufferedReader br;

	private ArrayList<String> bookList;

	public EchoThread(Socket sock, int clientNumber, ArrayList<String> bookList) {
		this.sock = sock;
		activeCount++; // �ش� Ŭ���� ���� �� �� ����
		this.clientNumber = clientNumber;
		Server_booking.getEchoThreadList();
		this.bookList=bookList;
	} // ������

	public int getClientNumber() {
		return clientNumber;
	}

	public ObjectOutputStream getOutput() {
		return serverOutputStream;
	}

	public void run() { // start() ���� �� ȣ��
		try { // I/O �� ��� ����
			InetAddress inetaddr = sock.getInetAddress();
			System.out.println(inetaddr.getHostAddress() + " �κ��� �����Ͽ����ϴ�.");
			serverOutputStream = new ObjectOutputStream(sock.getOutputStream());
			in = new ObjectInputStream(sock.getInputStream());
			Object temp;

			String line = null;

			while(true)
			{
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if((temp = in.readObject()) instanceof String)
				{
					line = (String) temp;
					System.out.println("Ŭ���̾�Ʈ�� ���� ���۹��� ���ڿ� : " + line);
					handleMeg(line);

				}				
			}	


	

		} catch (Exception ex) {
			activeCount--;
			System.out.println(ex);
			clientList = Server_booking.getEchoThreadList();
			for (int i = 0; i < clientList.size(); i++)
			{
				if (clientNumber == clientList.get(i).getClientNumber())
					clientList.remove(i);
			}
		}
	}

	public void handleMeg(String msg)// �� Ŭ���̾�Ʈ�� ���� �Է¹��� ��ɾ �������� ó���ϴ� �޼ҵ�
	// ��Ŀ������� ��� ������ �� . ex #book"ȸ�ǽ�30"�뱸�ϱ�"������"01049497193
	{
		roomFile file = new roomFile();
		roomList roomlist = file.fileRead();

		if (msg.startsWith("#regist")) {
			//
		} else if (msg.startsWith("#login")) {
			Login(msg);
		} else if (msg.startsWith("#joinC")){
			JoinC(msg);
		} else if (msg.startsWith("#joinB")){
			JoinB(msg);
		} else if (msg.startsWith("#search")) {
			Search(msg,roomlist);			
		} else if (msg.startsWith("#book")) {
			addBookList(msg);
		} else if(msg.startsWith("#ViewList")) {
			ViewList(msg,roomlist);
		} else if(msg.startsWith("#updateInfo")) {
			UpdateInfo(msg);
		}
	}



	//@@��� Ŭ���̾�Ʈ���� roomList�� �Ѱ��ִ� �Լ�
	private void sendToMessageAllClientRoomlist(roomList roomlist) {
		clientList = Server_booking.getEchoThreadList();
		for (int i = 0; i < clientList.size(); i++) {
			ObjectOutputStream temp = clientList.get(i).getOutput();
			try {
				temp.writeObject(roomlist);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	//@@���� Ŭ���̾�Ʈ���� roomList�� �Ѱ��ִ� �Լ�
	private void sendToMessageOneClientRoomlist(roomList roomlist) {
		ObjectOutputStream temp = this.getOutput();
		try {
			temp.writeObject(roomlist);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//@@���� Ŭ���̾�Ʈ���� String�� �Ѱ��ִ� �Լ�
	private void sendToClientString(String line) {
		ObjectOutputStream temp = this.getOutput();
		try {
			temp.writeObject(line);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void sendToClientUser(User user)
	{
		ObjectOutputStream temp = this.getOutput();
		try {
			temp.writeObject(user);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void Login(String msg)
	{
		msg = msg.substring(6);
		String[] token = msg.split("%");
		User loginUser = new User(token[0], token[1]);

		userFile userFile = new userFile();
		userList userlist = userFile.fileRead();

		boolean empty = false;
		for (int i = 0; i < userlist.size(); i++) {
			if (loginUser.getemail().equals(userlist.getUser(i).getemail())) {
				if (loginUser.getpassword().equals(
						userlist.getUser(i).getpassword())) {
					empty = true;
					System.out.println("�α��εǾ���");
					
					sendToClientUser(loginUser);
					break;
				} else {
					empty = true;
					System.out.println("��й�ȣ ����");
					sendToClientString("��й�ȣ����");
					break;
				}

			} else {
				continue;
			}
		}
		if (!empty)
			sendToClientString("�̸����� ã�� �� �����ϴ�.");
	}
	private void JoinC(String msg)
	{
		msg = msg.substring(6);
		String[] token = msg.split("%");
		personalUser joinUser = new personalUser(token[0], token[1]);

		userFile userFile = new userFile();
		userList userlist = userFile.fileRead();

		System.out.println("join C!!!!");
		userlist.addUser(joinUser);
		userFile.fileSave(userlist);
	}
	private void JoinB(String msg) {
		msg = msg.substring(6);
		String[] token = msg.split("%");
		companyUser joinUser = new companyUser(token[0], token[1]);

		userFile userFile = new userFile();
		userList userlist = userFile.fileRead();

		System.out.println("join B!!!!");
		userlist.addUser(joinUser);
		userFile.fileSave(userlist);		
	}

	private void Search(String msg, roomList roomlist)
	{
		msg = msg.substring(7);
		String[] token = msg.split("%");
		String date = token[0];
		String address = token[1];
		String num;
		if(token.length==2)
			num="�ο����þ���";
		else
			num = token[2];

		System.out.println(date + " " + address + " " + num);
		boolean isDate = true;
		boolean isAddress = true;
		boolean isNum = true;


		if (address.equals("���ü���"))
			isAddress = false;
		if (date.equals("���ü��ü���"))
			isDate = false;
		if (num.equals("�ο����þ���"))
			isNum = false;

		roomList temp = new roomList();
		temp = SearchRoom(roomlist, address, date, num, isAddress, isDate,
				isNum);
		sendToMessageOneClientRoomlist(temp);
	}

	public roomList SearchRoom(roomList roomlist, String Add, String data, String num, boolean isAddress, boolean isDate, boolean isNum) {
		boolean checkAddress = isAddress;
		boolean checkDate = isDate;
		boolean checkNum = isNum;

		System.out.println(Add+" "+data+" "+num+" "+isAddress+" "+isDate+" "+isNum);
		roomList temp = roomlist;
		if (checkAddress) {
			temp = searchForAddress(Add, temp);
		}
		if (checkDate) {
			// temp=searchForAddress(date, temp);
		}
		if (checkNum) {
			temp = searchForNum(num, temp);
		}

		return temp;
	}
	private roomList searchForAddress(String Add, roomList roomlist) {
		roomList temp = new roomList();
		for (int i = 0; i < roomlist.size(); i++) {
			if (Add.equals(roomlist.getRoom(i).getAddress())) {
				temp.addRoom(roomlist.getRoom(i));
			}
		}
		return temp;
	}

	private roomList searchForDate(String date, roomList roomlist) {
		roomList temp = new roomList();
		for (int i = 0; i < roomlist.size(); i++) {
			if (false)
			{
				//temp.addRoom(roomlist.getRoom(i));
			}
		}
		return temp;
	}

	private roomList searchForNum(String num, roomList roomlist) {
		roomList temp = new roomList();
		for (int i = 0; i < roomlist.size(); i++) {
			if (num.equals(roomlist.getRoom(i).getAcceptPeoNum())) {
				temp.addRoom(roomlist.getRoom(i));
			}	
		}
		return temp;
	}
	//@ ������ �ִ� ���ฮ��Ʈ�� ���
	private void addBookList(String msg)
	{
		msg = msg.substring(5);
		bookList.add(clientNumber+"%"+msg);
	}
	private void ViewList(String msg, roomList roomlist){
		msg = msg.substring(9);
		String[] token = msg.split("%");
		String email = token[0];
		roomList temp = new roomList();
		temp = searchForOwneremail(email, roomlist);
		sendToMessageOneClientRoomlist(temp);
	}
	private roomList searchForOwneremail(String Add, roomList roomlist) {
		roomList temp = new roomList();
		for (int i = 0; i < roomlist.size(); i++) {
			if (Add.equals(roomlist.getRoom(i).getOwnerEmail())) {
				temp.addRoom(roomlist.getRoom(i));
			}
		}
		return temp;
	}
	
	private void UpdateInfo(String msg){
		msg = msg.substring(11);
		
		String[] token = msg.split("%");
		String email = token[0];
		String oripw = token[1];
		String pass = token[2];
		String name = token[3];
		String bn = token[4];
		String tel = token[5];

		userFile userFile = new userFile();
		userList userlist = userFile.fileRead();

		boolean empty = false;
		for (int i = 0; i < userlist.size(); i++) 
		{
			if (token[0].equalsIgnoreCase(userlist.getUser(i).getemail())) 
			{
				if (token[1].equals(userlist.getUser(i).getpassword())) 
				{
					empty = true;
					userlist.getUser(i).setpassword(pass);
					sendToClientString("#update");
					userFile.fileSave(userlist);
					break;
				} else {
					empty = true;
					sendToClientString("��й�ȣ����");
					break;
				}
			} 
			else 
			{
				continue;
			}
		}
	}
	//@ �������� ����ó���ϰ� �ش� Ŭ���̾�Ʈ�� ����� �����ִ� �޼ҵ�
	public void returnBookResult(String result)
	{
		System.out.println("...server�� return book result...");
		sendToClientString(result);
	}
}


class BookHandle extends Thread
{	
	private ArrayList<String> bookList;
	private ArrayList<EchoThread> clientList;
	public BookHandle(ArrayList<String> bookList)
	{	
		this.bookList=bookList;
		clientList=Server_booking.getEchoThreadList();
	}
	public void run()
	{
		while(true)
		{
			roomFile file = new roomFile();
			roomList roomlist = file.fileRead();
			try {
				Thread.sleep(100);

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if(!bookList.isEmpty())
			{
				//���������� ������� �ٽ� Ȯ�� �ϴ� ������ �־����
				//���������� ������� �ٽ� Ȯ�� �ϴ� ������ �־����
				//���������� ������� �ٽ� Ȯ�� �ϴ� ������ �־����

				//���� string �޾Ƽ� ó���ϰ� �� �����ؼ� �� ����
				//file.fileSave(roomlist);


				//�׸��� �� ��ȣ�� client�� ������
				//clientList=Server.getEchoThreadList();
				//clientList.get(1).returnBookResult("BOOK");




				//���� ������ �̸� ��ٸ�
				//clientList.get(1).returnBookResult("FAIL");
				System.out.println("...BOOK HANDLING...");
				String temp=bookList.get(0);
				bookList.remove(0);

				String[] token = temp.split("%");
				int id=Integer.parseInt(token[0]);

				String num=token[1];
				System.out.println(temp+"   "+num);
				System.out.println("...BOOK HANDLING...1");
				clientList.get(id).returnBookResult(num);	
				System.out.println("...BOOK HANDLING...2");
			}
		}
	}

}