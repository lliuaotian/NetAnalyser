import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;


public class test {
	// 全局变量
	// 访问的连接 requests url
	static String RURL = new String();
	// 访问的次数
	static int probesCount = 5;
	// 输出文本框对象
	static JTextArea showText = new JTextArea();
	// 延迟列表
	static float[] msArry = new float[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
	
	static String rtt1s = new String("17<=RTT<20");
	static String rtt2s = new String("20<=RTT<23");
	static String rtt3s = new String("23<=RTT<=28");
	
	static JLabel rtt1 = new JLabel(rtt1s);
	static JLabel rtt2 = new JLabel(rtt2s);
	static JLabel rtt3 = new JLabel(rtt3s);
	
	public static void main(String[] args) {
		// 设置窗口大小位置
		JFrame frame = new JFrame("NetAnalyser");
		int HEIGHT = 300;
		int WIDTH = 1400;

		frame.setSize(WIDTH, HEIGHT);
		frame.setLocation(500, 300);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// 分割窗口为三分，左中右
		// 分割内容输入部分 也就是窗口左边部分
		JPanel input = new JPanel();
		input.setLayout(null);
//		input.setBackground(Color.blue);
		input.setBounds(0, 0, 500, HEIGHT);
		input.setVisible(true);

		// 分割信息显示部分 窗口中间部分
		JPanel show = new JPanel();
		show.setLayout(null);
//		show.setBackground(Color.black);
		show.setBounds(500, 0, 400, HEIGHT);
		// 分割直方图部分，也就是窗口右边
		JPanel histogram = new JPanel();
		histogram.setLayout(null);
//		histogram.setBackground(Color.yellow);
		histogram.setBounds(900, 0, 500, HEIGHT);

		// 添加三个部分到整个框架中
		frame.add(input);
		frame.add(show);
		frame.add(histogram);
		
		// 添加控件
		inputInfo(input);
		showInfo(show, HEIGHT);
		showHistogram(histogram);
		
		// 显示窗口
		frame.setVisible(true);
	}
	
	private static void inputInfo(JPanel panel) {
		// 用法说明
		String explanation_info = new String("Enter Test URL & no. of probes and click on Process.");
		JLabel exp_info = new JLabel(explanation_info);
		exp_info.setBounds(0, 15, 500, 15);
		panel.add(exp_info);
		// 创建输入url的文本框
		JLabel testUrl = new JLabel("Test URL:");
		testUrl.setBounds(0, 55, 70, 15);
		panel.add(testUrl);
		JTextField url = new JTextField(40);
		url.setBounds(70, 45, 500, 30);
		panel.add(url);
		// 探头数获取
		JLabel probes = new JLabel("No. of probes");
		probes.setBounds(100, 100, 100, 40);
		panel.add(probes);
		String[] listData = new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
		JComboBox<String> comboBox = new JComboBox<String>(listData);
		comboBox.setSelectedIndex(0);
		comboBox.setBounds(210, 100, 200, 40);
		panel.add(comboBox);
		
		// 开始按钮
		JButton start = new JButton("Process");
		start.setBounds(230, 200, 100, 40);
		panel.add(start);
		// 绑定事件
		start.addActionListener(new ActionListener() {
//			@Override
			public void actionPerformed(ActionEvent e) {
//				System.out.println("hello");
				// 获取url
				RURL = url.getText();
				probesCount = Integer.parseInt((String)comboBox.getSelectedItem());
				// 清空屏幕
				rtt1.setText(rtt1s);
				rtt2.setText(rtt2s);
				rtt2.setText(rtt3s);
				showText.setText("");
				// 获取数据 并输出中间屏幕
				getCmdInfo();

				// 更改直方图信息输出
				showMaxMin();
				// 清空ms数据
				for (int i = 0; i < msArry.length; i++) {
					msArry[i] = 0;
				}
			}
		});
	}
	
	private static void showMaxMin() {
		// 输出最右侧内容
		int count = 1;
		StringBuffer s1 = new StringBuffer();
		StringBuffer s2 = new StringBuffer();
		StringBuffer s3 = new StringBuffer();
		s1.append(rtt1s);
		s1.append("      ");
		s2.append(rtt2s);
		s2.append("      ");
		s3.append(rtt3s);
		s3.append("      ");
		
		while (count <= probesCount) {
			if ((msArry[count] >= 17) &&  (msArry[count] < 20)) {
				s1.append("X");
				s1.append("      ");
			} else if ((msArry[count] >= 20) &&  (msArry[count] < 23)) {
				s2.append("X");
				s2.append("      ");
			} else if ((msArry[count] >= 23) &&  (msArry[count] <= 26)) {
				s3.append("X");
				s3.append("      ");
			}
			count++;
		}
		rtt1.setText(s1.toString());
		rtt2.setText(s2.toString());
		rtt2.setText(s2.toString());
	}
	
	// 命令行执行命令并且读取返回筛选数据  使用正则表达式
	private static void getCmdInfo() {
		// 因为我用的是linux 假设在Linux环境下
		try {
//			Process p = Runtime.getRuntime().exec("cmd/c = ping -n 5 www.baidu.com");
			StringBuffer execstring = new StringBuffer();
			// windows 用法
//			execstring.append("cmd/c = ping -n ");
			// Linux 用法
			execstring.append("ping -c ");
			execstring.append(probesCount);
			execstring.append(" ");
			execstring.append(RURL);
			Process p = Runtime.getRuntime().exec(execstring.toString());
			try {
				p.waitFor();
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String str = null;
			int msCount = 0;
			String msPattern = "[time|时间]=(.*?)ms";
			while (true) {
				str = reader.readLine();
				if (str == null) {
					break;
				}
				// 输出中间屏幕
				showText.append(str);
				showText.append("\r\n");
				// 提取RTT
				Pattern r = Pattern.compile(msPattern);
				Matcher m = r.matcher(str);
				if (m.find()) {
					msCount++;
					if (msCount > probesCount) {
						break;
					}
					// m.group(1) = 延迟数值
					msArry[msCount] = Float.parseFloat(m.group(1));
					System.out.println(Float.parseFloat(m.group(1)));
				}
			}
			
		} catch (IOException ie) {
			ie.printStackTrace();
		}
	}
	
	
	private static void showInfo(JPanel panel, int height) {
		// 默认显示内容
		String default_info = new String("Your output will appear here..");
		showText.setText(default_info);
		showText.setBounds(0, 0, 400, height);
		// 设置字体
		Font x = new Font("Serif", 0, 14);
		showText.setFont(x);
		// 设置拒绝编辑内容
		showText.setEditable(false);
		// 自动换行
		showText.setLineWrap(true);	

		panel.add(showText);
	}
	
	private static void showHistogram(JPanel panel) {
		String show = new String("Histogram");
		JLabel testUrl = new JLabel(show);
		testUrl.setBounds(900, 55, 70, 15);
		panel.add(testUrl);
		
		// 第一行
		rtt1.setBounds(900, 120, 500, 20);
		panel.add(rtt1);
		
		// 第二行
		rtt2.setBounds(900, 160, 500, 20);
		panel.add(rtt2);
		
		// 第三行
		rtt3.setBounds(900, 200, 500, 20);
		panel.add(rtt3);
	}
}
