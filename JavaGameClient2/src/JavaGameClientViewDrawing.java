
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
	public JavaGameClientViewDrawing(String username, JavaGameClientView view)  {
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
		gc2.fillRect(0,0, panel.getWidth(),  panel.getHeight());
		gc2.setColor(Color.BLACK);
		gc2.drawRect(0,0, panel.getWidth()-1,  panel.getHeight()-1);
		
		gc3 = (Graphics2D) gc2;
		
		lblMouseEvent = new JLabel("<dynamic>");
		lblMouseEvent.setHorizontalAlignment(SwingConstants.CENTER);
		lblMouseEvent.setFont(new Font("굴림", Font.BOLD, 14));
		lblMouseEvent.setBorder(new LineBorder(new Color(0, 0, 0)));
		lblMouseEvent.setBackground(Color.WHITE);
		lblMouseEvent.setBounds(11, 539, 400, 40);
		contentPane.add(lblMouseEvent);
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

		gc2.drawImage(ori_img,  0,  0, panel.getWidth(), panel.getHeight(), panel);
		gc.drawImage(panelImage, 0, 0, panel.getWidth(), panel.getHeight(), panel);
	}
	public void paint(Graphics g) {
		super.paint(g);
		// Image 영역이 가려졌다 다시 나타날 때 그려준다.
		gc.drawImage(panelImage, 0, 0, this);
	}
	
	
	// Mouse Event 수신 처리
	// todo 펜 사이즈가 상대의 펜사이즈가 아닌 자신의 펜사이즈로 적용, 한 번 클릭해야 반응함.
	public void DoMouseEvent(ChatMsg cm) {
		Color c;
		MouseEvent e = cm.mouse_e;
		if (cm.UserName.matches(UserName)) // 본인 것은 이미 Local 로 그렸다.
			return;
		c = new Color(255, 0, 0); // 다른 사람 것은 Red
		
		if(cm.type.equals("pressed")) {
			oldCoord = e.getPoint();
		}
//		gc2.setColor(c);
//		gc2.fillOval(cm.mouse_e.getX() - pen_size/2, cm.mouse_e.getY() - cm.pen_size/2, cm.pen_size, cm.pen_size);
		gc3.setColor(c);
		gc3.setStroke(new BasicStroke(cm.pen_size, BasicStroke.CAP_ROUND, 0));
		gc3.drawLine((int)oldCoord.getX(), (int)oldCoord.getY(), e.getX(), e.getY());
		gc.drawImage(panelImage, 0, 0, panel);
		oldCoord = e.getPoint();
	}

	public void SendMouseEvent(MouseEvent e, String type) {
		ChatMsg cm = new ChatMsg(UserName, "500", "MOUSE");
		cm.mouse_e = e;
		cm.pen_size = pen_size;
		cm.type = type;
		mainview.SendObject(cm);
	}

	class MyMouseWheelEvent implements MouseWheelListener {
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			// TODO Auto-generated method stub
			if (e.getWheelRotation() < 0) { // 위로 올리는 경우 pen_size 증가
				if (pen_size < 20)
					pen_size++;
			} else {
				if (pen_size > 2)
					pen_size--;
			}
			lblMouseEvent.setText("mouseWheelMoved Rotation=" + e.getWheelRotation() 
				+ " pen_size = " + pen_size + " " + e.getX() + "," + e.getY());

		}
		
	}
	// Mouse Event Handler
	class MyMouseEvent implements MouseListener, MouseMotionListener {
		MouseEvent oldMouse = null;
		@Override
		public void mouseDragged(MouseEvent e) {
			lblMouseEvent.setText(e.getButton() + " mouseDragged " + e.getX() + "," + e.getY());// 좌표출력가능
			Color c = new Color(0,0,255);
			// gc2.setColor(c);
			// gc2.fillOval(e.getX()-pen_size/2, e.getY()-pen_size/2, pen_size, pen_size);
			// panelImnage는 paint()에서 이용한다.
			gc3.setColor(c);
			gc3.setStroke(new BasicStroke(pen_size, BasicStroke.CAP_ROUND, 0));
			gc3.drawLine(oldMouse.getX(), oldMouse.getY(), e.getX(), e.getY());
			oldMouse = e;
			gc.drawImage(panelImage, 0, 0, panel);
			SendMouseEvent(e, "dragged");
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			lblMouseEvent.setText(e.getButton() + " mouseMoved " + e.getX() + "," + e.getY());
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			lblMouseEvent.setText(e.getButton() + " mouseClicked " + e.getX() + "," + e.getY());
			Color c = new Color(0,0,255);
			gc3.setColor(c);
			gc3.fillOval(e.getX()-pen_size/2, e.getY()-pen_size/2, pen_size, pen_size);
			gc.drawImage(panelImage, 0, 0, panel);
			SendMouseEvent(e, "clicked");
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			lblMouseEvent.setText(e.getButton() + " mouseEntered " + e.getX() + "," + e.getY());
			// panel.setBackground(Color.YELLOW);

		}

		@Override
		public void mouseExited(MouseEvent e) {
			lblMouseEvent.setText(e.getButton() + " mouseExited " + e.getX() + "," + e.getY());
			// panel.setBackground(Color.CYAN);

		}

		@Override
		public void mousePressed(MouseEvent e) {
			oldMouse = e;
			SendMouseEvent(e, "pressed");
			lblMouseEvent.setText(e.getButton() + " mousePressed " + e.getX() + "," + e.getY());

		}

		@Override
		public void mouseReleased(MouseEvent e) {
			lblMouseEvent.setText(e.getButton() + " mouseReleased " + e.getX() + "," + e.getY());
			// 드래그중 멈출시 보임

		}
	}
}
