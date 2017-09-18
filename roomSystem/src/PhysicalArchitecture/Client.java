package PhysicalArchitecture;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import Foundation.CbookList;
import Foundation.roomList;
import ProblemDomain.User;
import ProblemDomain.bookedRoom;
import ProblemDomain.conferenceRoom;


public class Client
{
	Socket sock;
	clientWrite clientW;
	clientRead clientR;
	private ClientControl cControl;

	public Client(String host, int port)
	{
		cControl=new ClientControl();
		try {
			System.out.println("-----Ŭ���̾�Ʈ�� ����Ǿ����ϴ�.");
			sock = new Socket(host, port);

		} catch (IOException e) {
			e.printStackTrace();
		} 

		clientW=new clientWrite(sock);
		clientR=new clientRead(sock,cControl);
		clientW.start();
		clientR.start();
	}
	public void sendToServer(bookedRoom book) 
	{
		clientW.sendToServerbookedRoom(book);
	}
	public void sendToServer(String msg)
	{
		clientW.sendToServer(msg);
	}
	public void sendToServer(roomList list)
	{
		clientW.sendToServerRoomList(list);
	}
	public void sendToServer(conferenceRoom room)
	{
		clientW.sendToServerRoom(room);
	}
	public void sendToServerBookList(CbookList list)
	{
		clientW.sendToServerBookList(list);
	}
	public ClientControl getcControl() {
		return cControl;
	}
	public void setcControl(ClientControl cControl) {
		this.cControl = cControl;
	}
}
class clientRead extends Thread//������ ���� �޼��� �ޱ�.
{
	Socket socket;
	private ClientControl cControl;

	public clientRead(Socket socket,ClientControl cControl)
	{
		this.socket=socket;
		this.cControl=cControl;
	}
	public void run()
	{
		try {
			ObjectInputStream clientInputStream = new ObjectInputStream(socket.getInputStream());
			Object temp;
			while(true)
			{
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				temp = clientInputStream.readObject();
				if(temp instanceof roomList)
				{					
					roomList roomlist=new roomList();
					roomlist = (roomList) temp;
					System.out.println("-----�����κ��� �븮��Ʈ�� ����");
					cControl.addRoomList(roomlist);					
				}
				else if(temp instanceof User)
				{
					User user = (User) temp;
					System.out.println("-----�����κ��� ���� ������ ����");
					cControl.addUser(user);
				}
				else if(temp instanceof CbookList)
				{
					CbookList booklist = (CbookList) temp;
					System.out.println("-----�����κ��� ���� ������ ����");
					cControl.addBookList(booklist);
				}
				else
				{
					String line = (String) temp;
					System.out.println("-----������ ���� msg >: "+line);
					cControl.addString(line);
				}
			}	
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			try {

				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}	


}

class clientWrite extends Thread//������ �޼��� ������
{
	private Socket socket;
	private String console;
	private roomList console1;
	private conferenceRoom console2;
	private CbookList console3;
	private bookedRoom console4;
	private boolean sendToReadyString;
	private boolean sendToReadyList;
	private boolean sendToReadyRoom;
	private boolean sendToReadyBook;
	private boolean sendToReadybookedRoom;
	public clientWrite(Socket socket)
	{
		this.socket=socket;
		sendToReadyString=false;
		sendToReadyList=false;
		sendToReadyRoom=false;
		sendToReadyBook=false;
		sendToReadybookedRoom=false;
	}
	public void run()
	{
		ObjectOutputStream out;

		try {
			out = new ObjectOutputStream(socket.getOutputStream());

			while (true) //&��ü thread�� �ݺ���
			{			
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				while(sendToReadyString==true)//& �����غ� �ɶ��� ���൵�� �ϴ� �ݺ���
				{
					out.writeObject(console);
					sendToReadyString=false;
					System.out.println("-----server�� -�޼���-����");					
				}
				while(sendToReadyList==true)//& �����غ� �ɶ��� ���൵�� �ϴ� �ݺ���
				{
					out.writeObject(console1);
					sendToReadyList=false;
					System.out.println("-----server�� -list-����");					
				}
				while(sendToReadyRoom==true)//& �����غ� �ɶ��� ���൵�� �ϴ� �ݺ���
				{
					out.writeObject(console2);
					sendToReadyRoom=false;
					System.out.println("-----server�� -room-����");					
				}
				while(sendToReadyBook==true)//& �����غ� �ɶ��� ���൵�� �ϴ� �ݺ���
				{
					out.writeObject(console3);
					sendToReadyBook=false;
					System.out.println("-----server�� -book-����");					
				}
				while(sendToReadybookedRoom==true)//& �����غ� �ɶ��� ���൵�� �ϴ� �ݺ���
				{
					out.writeObject(console4);
					sendToReadybookedRoom=false;
					System.out.println("-----server�� -bookedRoom-����");               
				}
			}		


		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public void sendToServer(String msg)
	{
		console=msg;
		sendToReadyString=true;
	}
	public void sendToServerRoomList(roomList list)
	{
		console1=list;
		sendToReadyList=true;
	}
	public void sendToServerRoom(conferenceRoom room)
	{
		console2=room;
		sendToReadyRoom=true;
	}
	public void sendToServerBookList(CbookList list)
	{
		console3=list;
		sendToReadyBook=true;
	}
	public void sendToServerbookedRoom(bookedRoom book)
	{
		console4=book;
		sendToReadybookedRoom=true;
	}
}