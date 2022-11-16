// Java Chatting Server

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.SwingConstants;
import java.net.InetAddress;

// class 선언 시 room 참가자의 userVector 즉, 통신을 위한 ip, adress와 유저를 식별할 userName을 받는다.
public class JavaDrawingUdpServer {
    private int Port = 30000 // 포트번호
    private Vector vc = new Vector(); // 연결된 사용자를 저장할 벡터

    // 유저와 통신을 위한 ip주소, port번호, 그리고 유저 식별을 위한 userName
    public Vector<InetAddress> userAdresses = new Vector<InetAddress>();
    public Vector<Int> userPorts = new Vector<Int>();
    public Vector<String> userNames = new Vector<String>();

    public void serverStart() {
        Thread th = new Thread(new Runnable() { // 사용자 접속을 받을 스레드
            @SuppressWarnings("resource")
            @Override
            public void run() {
                // udp 소켓 만들기
                DatagramSocket udp_socket = null;
                try {
                    udp_socket = new DatagramSocket(Port);
                } catch (SocketException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                byte[] bb = new byte[128];
                DatagramPacket udp_packet = new DatagramPacket(bb, bb.length);
                while (true) {
                    for (int i = 0; i < bb.length; i++) {
                        bb[i] = 0;
                    }

                    // 수신 부분
                    try {
                        udp_socket.receive(udp_packet);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    String data = new String(bb);
                    String args = data.split(",");
                    String userName = args[1];
                    if(args[0].equals("connect")) {
                        userAdresses.add(udp_packet.getAddress());
                        userPorts.add(udp_packet.getPort());
                        userNames.add(userName);
                    }

                    // 송신 부분
                    try {
                        // room 안의 모든 사용자에게 전달
                        for (int i = 0; i < userNames.size(); i++) {
                            InetAddress address = userAdresses.get(i);
                            int port = userPorts.get(i);
                            String name = userNames.get(i);
                            // 어차피 서버는 받고 보내주기만 하면 되는거라서 굳이 name은 필요 없을 듯??
                            // todo 문제는 사용자가 나가면 어떻게 이 서버에다가 알려줄건지, tcp가 어떻게 알려줄건지 문제
                            udp_packet = new DatagramPacket(bb, bb.length, address, port);
                            udp_socket.send(udp_packet);
                        }
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });
        th.start();
    }

// 내부 userinfo클래스끝
}
