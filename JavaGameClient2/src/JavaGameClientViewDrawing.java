
// JavaObjClientView.java ObjecStram 기반 Client
//실질적인 채팅 창

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.ImageObserver;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Color;

public class JavaGameClientViewDrawing extends JFrame {
    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private String UserName;
    JPanel panel;
    private JLabel lblMouseEvent;
    private Graphics gc;
    private int pen_size = 2; // minimum 2
    // 그려진 Image를 보관하는 용도, paint() 함수에서 이용한다.
    private Image panelImage = null;
    private Graphics gc2 = null;
    public JavaGameClientView mainview;


    private Graphics2D gc3 = null;
    private Point oldCoord;
    private Color penColor = new Color(0, 255, 0);
    private JButton colorRedButton;


    public JavaGameClientViewDrawing(String username, JavaGameClientView view) {
        mainview = view;

        setTitle(username);
        setResizable(false);
        //setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setBounds(300, 100, 440, 634);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);
        setVisible(true);


        //view.AppendText(" Drawing start ");
        UserName = username;

        panel = new JPanel();
        panel.setBorder(new LineBorder(new Color(0, 0, 0)));
        panel.setBackground(Color.WHITE);
        panel.setBounds(11, 10, 400, 520);
        contentPane.add(panel);
        gc = panel.getGraphics();

        // Image 영역 보관용. paint() 에서 이용한다.
        panelImage = createImage(panel.getWidth(), panel.getHeight());
        gc2 = panelImage.getGraphics();
        gc2.setColor(panel.getBackground());
        gc2.fillRect(0, 0, panel.getWidth(), panel.getHeight());
        gc2.setColor(Color.BLACK);
        gc2.drawRect(0, 0, panel.getWidth() - 1, panel.getHeight() - 1);

        gc3 = (Graphics2D) gc2;

        colorRedButton = new JButton("RED");
        colorRedButton.setFont(new Font("굴림", Font.PLAIN, 14));
        colorRedButton.setBounds(11, 539, 400, 40);
        // 버튼이 눌러졌을 때, 자신의 컬러와 네트워크로 컬러를 변경해줌.
        colorRedButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                penColor = Color.decode("#FF0000");
                sendChangeColor("500#FF0000");
            }
        });
        contentPane.add(colorRedButton);

//		lblMouseEvent = new JLabel("<dynamic>");
//		lblMouseEvent.setHorizontalAlignment(SwingConstants.CENTER);
//		lblMouseEvent.setFont(new Font("굴림", Font.BOLD, 14));
//		lblMouseEvent.setBorder(new LineBorder(new Color(0, 0, 0)));
//		lblMouseEvent.setBackground(Color.WHITE);
//		lblMouseEvent.setBounds(11, 539, 400, 40);
//		contentPane.add(lblMouseEvent);
        MyMouseEvent mouse = new MyMouseEvent();
        panel.addMouseMotionListener(mouse);
        panel.addMouseListener(mouse);
        MyMouseWheelEvent wheel = new MyMouseWheelEvent();
        panel.addMouseWheelListener(wheel);
    }

    public void AppendImage(ImageIcon ori_icon) {
        Image ori_img = ori_icon.getImage();
        Image new_img;
        ImageIcon new_icon;
        int width, height;
        double ratio;
        width = ori_icon.getIconWidth();
        height = ori_icon.getIconHeight();
        // Image가 너무 크면 최대 가로 또는 세로 200 기준으로 축소시킨다.
        if (width > 200 || height > 200) {
            if (width > height) { // 가로 사진
                ratio = (double) height / width;
                width = 200;
                height = (int) (width * ratio);
            } else { // 세로 사진
                ratio = (double) width / height;
                height = 200;
                width = (int) (height * ratio);
            }
            new_img = ori_img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            new_icon = new ImageIcon(new_img);
        } else {
            new_img = ori_img;
        }
        // ImageViewAction viewaction = new ImageViewAction();
        // new_icon.addActionListener(viewaction); // 내부클래스로 액션 리스너를 상속받은 클래스로
        // panelImage = ori_img.getScaledInstance(panel.getWidth(), panel.getHeight(), Image.SCALE_DEFAULT);

        gc2.drawImage(ori_img, 0, 0, panel.getWidth(), panel.getHeight(), panel);
        gc.drawImage(panelImage, 0, 0, panel.getWidth(), panel.getHeight(), panel);
    }

    public void paint(Graphics g) {
        super.paint(g);
        // Image 영역이 가려졌다 다시 나타날 때 그려준다.
        gc.drawImage(panelImage, 0, 0, this);
    }


    // Mouse Event 수신 처리 (상대 거 그려주기)
    // 좌표값, type(pressed), pen_size
    public void DoMouseEvent(ChatMsg cm) {
        Color c;
        MouseEvent e = cm.mouse_e; // 그냥 udp로 마우스 좌표값만 알려주기
        if (cm.UserName.matches(UserName)) // 본인 것은 이미 Local 로 그렸다.
            return;
        //c = new Color(255, 0, 0); // 다른 사람 것은 Red
        c = penColor;

        if (cm.type.equals("pressed")) {
            oldCoord = e.getPoint();
        }
//		gc2.setColor(c);
//		gc2.fillOval(cm.mouse_e.getX() - pen_size/2, cm.mouse_e.getY() - cm.pen_size/2, cm.pen_size, cm.pen_size);
        gc3.setColor(c);
        gc3.setStroke(new BasicStroke(cm.pen_size, BasicStroke.CAP_ROUND, 0));
        gc3.drawLine((int) oldCoord.getX(), (int) oldCoord.getY(), e.getX(), e.getY());
        gc.drawImage(panelImage, 0, 0, panel);
        oldCoord = e.getPoint();
    }

    // udp 수신 기능
    public void StartNetwork() {
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
                    // 여기서 마우스 그림 그리기 처리
                    String msg = new String(bb);
                    msg = msg.trim();
                    textArea.append(msg + "\n");
                    textArea.setCaretPosition(textArea.getText().length());
                } // while문 끝
            }// run메소드 끝
        });
        th.start();
    }

    public void sendMessage(String str) {
        byte[] bb = new byte[128];
        bb = str.getBytes();
        DatagramPacket udp_packet = new DatagramPacket(bb, bb.length, ip_addr, port);
        try {
            udp_socket.send(udp_packet);
        } catch (IOException e) {
            textArea.append("Fail drawing send\n");
        }
    }

    // 펜 color 버튼
    public void sendChangeColor(String colorCode) {
        ChatMsg cm = new ChatMsg(UserName, colorCode, "CHANGE COLOR");
        mainview.SendObject(cm);
    }
    public void receiveChangeColor(String colorCode) {
        penColor = Color.decode(colorCode);
    }

    class MyMouseWheelEvent implements MouseWheelListener {
        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            // TODO Auto-generated method stub
            if (e.getWheelRotation() < 0) { // 위로 올리는 경우 pen_size 증가
                if (pen_size < 20)
                    pen_size++;
            }
            else {
                if (pen_size > 2)
                    pen_size--;
            }
        }
    }

    // Mouse Event Handler
    class MyMouseEvent implements MouseListener, MouseMotionListener {
        MouseEvent oldMouse = null;
        @Override
        public void mouseDragged(MouseEvent e) {
            Color c = penColor;
            gc3.setColor(c);
            gc3.setStroke(new BasicStroke(pen_size, BasicStroke.CAP_ROUND, 0));
            gc3.drawLine(oldMouse.getX(), oldMouse.getY(), e.getX(), e.getY());
            oldMouse = e;
            gc.drawImage(panelImage, 0, 0, panel);
            String str = e.getX() + "," + e.getY() + "," + "dragged";
            sendMessage(str);
        }
        @Override
        public void mouseClicked(MouseEvent e) {
            Color c = penColor;
            gc3.setColor(c);
            gc3.fillOval(e.getX() - pen_size / 2, e.getY() - pen_size / 2, pen_size, pen_size);
            gc.drawImage(panelImage, 0, 0, panel);

            String str = e.getX() + "," + e.getY() + "," + "clicked";
            sendMessage(str);
        }
        @Override
        public void mousePressed(MouseEvent e) {
            oldMouse = e;
            String str = e.getX() + "," + e.getY() + "," + "pressed";
            sendMessage(str);
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }
        @Override
        public void mouseExited(MouseEvent e) {
        }
        @Override
        public void mouseReleased(MouseEvent e) {
        }
        @Override
        public void mouseMoved(MouseEvent e) {
        }

    }
}
