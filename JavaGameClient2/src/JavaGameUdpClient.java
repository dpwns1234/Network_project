// MainView.java : Java Chatting Client 의 핵심부분
// read keyboard --> write to network (Thread 로 처리)
// read network --> write to textArea

import java.awt.Canvas;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class JavaUdpEchoClientView extends JFrame {
    private JPanel contentPane;
    private JTextField textField; // 보낼 메세지 쓰는곳
    private String id;
    private InetAddress ip_addr;
    private int port;
    private Canvas canvas;
    JButton sendBtn; // 전송버튼
    JTextArea textArea; // 수신된 메세지를 나타낼 변수
    private DatagramSocket udp_socket; // 연결소켓
    private InputStream is;
    private OutputStream os;
    private DataInputStream dis;
    private DataOutputStream dos;


    public JavaUdpEchoClientView(String id, String ip, int port) {
        this.id = id;
        try {
            this.ip_addr = InetAddress.getByName(ip);
        } catch (UnknownHostException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        this.port = port;
        InitScreen();
        textArea.append("Connecting: " + ip + " " + port + "\n");
        try {
            udp_socket = new DatagramSocket();
        } catch (IOException e) {
            textArea.append("소켓 생성 에러!!\n");
        }
        StartNetwork();
    }

    public void StartNetwork() { // 실직적인 메소드 연결부분
        Thread th = new Thread(new Runnable() { // 스레드를 돌려서 서버로부터 메세지를 수신
            public void run() {
                byte[] bb = new byte[128];
                DatagramPacket udp_packet = new DatagramPacket(bb, bb.length);
                while (true) {
                    for (int i = 0; i < bb.length; i++) {
                        bb[i] = 0;
                    }
                    try {
                        udp_socket.receive(udp_packet); // packet 수신
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    String msg = new String(bb);
                    msg = msg.trim();
                    textArea.append(msg + "\n");
                    textArea.setCaretPosition(textArea.getText().length());
                } // while문 끝
            }// run메소드 끝
        });
        th.start();
    }

    public void send_Message(String str) { // 서버로 메세지를 보내는 메소드
        byte[] bb = new byte[128];
        bb = str.getBytes();
        DatagramPacket udp_packet = new DatagramPacket(bb, bb.length, ip_addr, port);
        try {
            udp_socket.send(udp_packet);
        } catch (IOException e) {
            textArea.append("메세지 송신 에러!!\n");
        }
    }

    public void InitScreen() { // 화면구성 메소드
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 288, 392);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(0, 0, 272, 302);
        contentPane.add(scrollPane);
        textArea = new JTextArea();
        scrollPane.setViewportView(textArea);
        //textArea.setForeground(new Color(255,0,0));
        textArea.setDisabledTextColor(new Color(0, 0, 0));
        textField = new JTextField();
        textField.setBounds(0, 312, 155, 42);
        contentPane.add(textField);
        textField.setColumns(10);
        sendBtn = new JButton("전   송");
        sendBtn.setBounds(161, 312, 111, 42);
        contentPane.add(sendBtn);
        textArea.setEnabled(false); // 사용자가 수정못하게 막는다
        setVisible(true);
        Myaction action = new Myaction();
        sendBtn.addActionListener(action); // 내부클래스로 액션 리스너를 상속받은 클래스로
        textField.addActionListener(action);
    }

    class Myaction implements ActionListener // 내부클래스로 액션 이벤트 처리 클래스
    {
        @Override
        public void actionPerformed(ActionEvent e) {
            // 액션 이벤트가 sendBtn일때 또는 textField 에세 Enter key 치면
            if (e.getSource() == sendBtn || e.getSource() == textField) {
                String msg = null;
                msg = String.format("[%s] %s\n", id, textField.getText());
                send_Message(msg);
                textField.setText(""); // 메세지를 보내고 나면 메세지 쓰는창을 비운다.
                textField.requestFocus(); // 메세지를 보내고 커서를 다시 텍스트 필드로 위치시킨다
            }
        }
    }
}