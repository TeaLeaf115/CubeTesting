import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Comparator;

public class Cube3D extends JFrame {
	// Set initial coordinates to center the cube on the screen
	int centerX = getWidth() / 2;
	int centerY = getHeight() / 2;
	
	int[][] vertexTable = new int[][]{
	//  {X, Y, Z}
		{centerX + 50, centerY + 50, 50}, // A:0
		{centerX + 50, centerY - 50, 50}, // B:1
		{centerX - 50, centerY - 50, 50}, // C:2
		{centerX - 50, centerY + 50, 50}, // D:3
		{centerX + 50, centerY + 50, -50}, // E:4
		{centerX + 50, centerY - 50, -50}, // F:5
		{centerX - 50, centerY - 50, -50}, // G:6
		{centerX - 50, centerY + 50, -50}  // H:7
	};
	
	int[][] projVTable = new int[8][2];
	int[][] edgeTable = {
	//      {Vertex_A to Vertex_B}
			{0, 1}, // AB:0
			{1, 2}, // BC:1
			{2, 3}, // CD:2
			{3, 0}, // DA:3
			{4, 5}, // EF:4
			{5, 6}, // FG:5
			{6, 7}, // GH:6
			{7, 4}, // HE:7
			{0, 4}, // AE:8
			{1, 5}, // BF:9
			{2, 6}, // CG:10
			{3, 7}  // DH:11
	};
	
	Color[] colors = {
			new Color(0xffffff),
			new Color(0x00ffff),
			new Color(0x2e8b57),
			new Color(0xff00ff),
			new Color(0x808000),
			new Color(0x800080),
			new Color(0x00bfff),
			new Color(0xffa500),
			new Color(0xdda0dd),
			new Color(0x7f0000),
			new Color(0x00fa9a),
			new Color(0x4169e1),
			new Color(0x808080),
			new Color(0xff4500),
			new Color(0x0000ff),
			new Color(0x00ff00),
			new Color(0xfa8072),
			new Color(0xeee8aa),
			new Color(0xffff00),
			new Color(0xff1493)
	};
	
	int focalLength = 600;
	
	double[] YPR = {
			Math.toRadians(0), // α (Yaw) X:0
			Math.toRadians(0), // β (Pitch) Y:1
			Math.toRadians(0)  // γ (Roll) Z:2
	};
	int xShift, yShift, zShift = 0;
	
	int pointDiameter = 4;
	boolean isRotating = false;
	
	public Cube3D() {
		JPanel panel = new JPanel(new BorderLayout()) {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Cube3D.this.paintCube(g);
				System.out.printf("[Width: %d | Height: %d]\n", getWidth(), getHeight());
				System.out.println(Arrays.deepToString(projVTable));
			}
		};
		
		this.setTitle("Cube3D");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(true);
		this.setIconImage(new ImageIcon("res/WindowIcon.png").getImage());
//		this.setPreferredSize(new Dimension(500, 500));
		this.setPreferredSize(new Dimension(514, 537));
		
		this.add(panel);
		panel.setBackground(Color.BLACK);
		
		JSlider yawSlider = new JSlider(0, 360, 0);
		yawSlider.setPreferredSize(new Dimension(this.getWidth(), 10));
		yawSlider.setAlignmentY(SwingConstants.BOTTOM);
		yawSlider.setName("Yaw");
		yawSlider.addChangeListener(e -> {
			YPR[0] = Math.toRadians(yawSlider.getValue());
			panel.repaint();
		});
		
		JSlider pitchSlider = new JSlider(0, 360, 0);
		pitchSlider.setPreferredSize(new Dimension(this.getWidth(), 10));
		pitchSlider.setAlignmentY(SwingConstants.BOTTOM);
		pitchSlider.setName("Pitch");
		pitchSlider.addChangeListener(e -> {
			YPR[1] = Math.toRadians(pitchSlider.getValue());
			panel.repaint();
		});
		
		JSlider rollSlider = new JSlider(0, 360, 0);
		rollSlider.setPreferredSize(new Dimension(this.getWidth(), 10));
		rollSlider.setAlignmentY(SwingConstants.BOTTOM);
		rollSlider.setName("Roll");
		rollSlider.addChangeListener(e -> {
			YPR[2] = Math.toRadians(rollSlider.getValue());
			panel.repaint();
		});
		
		JSlider xSlider = new JSlider(-400, 400, 0);
		xSlider.setPreferredSize(new Dimension(this.getWidth(), 10));
		xSlider.setAlignmentY(SwingConstants.TOP);
		xSlider.setName("X");
		xSlider.addChangeListener(e -> {
			xShift = xSlider.getValue();
			panel.repaint();
		});

		JSlider ySlider = new JSlider(-400, 400, 0);
		ySlider.setPreferredSize(new Dimension(this.getWidth(), 10));
		ySlider.setAlignmentY(SwingConstants.TOP);
		ySlider.setName("Y");
		ySlider.addChangeListener(e -> {
			yShift = ySlider.getValue();
			panel.repaint();
		});

		JSlider zSlider = new JSlider(-400, 400, 0);
		zSlider.setPreferredSize(new Dimension(this.getWidth(), 10));
		zSlider.setAlignmentY(SwingConstants.TOP);
		zSlider.setName("Z");
		zSlider.addChangeListener(e -> {
			zShift = zSlider.getValue();
			panel.repaint();
		});
		
		JPanel translationPanel = new JPanel();
		translationPanel.setLayout(new BoxLayout(translationPanel, BoxLayout.Y_AXIS));
		translationPanel.add(xSlider);
		translationPanel.add(ySlider);
		translationPanel.add(zSlider);
		
		JPanel rotationPanel = new JPanel();
		rotationPanel.setLayout(new BoxLayout(rotationPanel, BoxLayout.Y_AXIS));
		rotationPanel.add(yawSlider);
		rotationPanel.add(pitchSlider);
		rotationPanel.add(rollSlider);
		
		JButton autoRotate = new JButton("Auto-Rotate");
		autoRotate.addActionListener(e -> {
			if (!isRotating) {
				isRotating = true;
				new Thread(() -> {
					int counter = 0;
					while (isRotating) {
						int finalCounter = counter;
						SwingUtilities.invokeLater(() -> {
							yawSlider.setValue(finalCounter);
							pitchSlider.setValue(finalCounter);
							rollSlider.setValue(finalCounter);
						});
						
						try {
							Thread.sleep(10); // Adjust the delay as needed
						} catch (InterruptedException ex) {
							ex.printStackTrace();
						}
						
						counter = (counter + 1) % 360;
						if (counter == 0) {
							counter = 0;
						}
					}
				}).start();
			} else {
				isRotating = false;
			}
		});
		autoRotate.setSize(105, 20);
		autoRotate.setVisible(true);
		autoRotate.setAlignmentY(SwingConstants.BOTTOM);
		
		panel.add(autoRotate, BorderLayout.SOUTH);
		panel.add(translationPanel, BorderLayout.NORTH);
		panel.add(rotationPanel, BorderLayout.SOUTH);
		
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
		
	}
	
	public void paintCube(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		
		int counter = 0;
		
		// Create an array to store the distance of each vertex from the viewer
		double[] distances = new double[8];
		
		for (int i = 0; i < vertexTable.length; i++) {
			int[] v = vertexTable[i];
			int[] centeredV = unshift(v);
			int[] rotatedV = rotateVertex(centeredV);
			int[] translatedV = shift(rotatedV);
			int[] projVertex = projectVertex(translatedV);
			
			// Calculate the distance from the viewer for depth sorting
			distances[i] = Math.sqrt(Math.pow(rotatedV[0], 2) + Math.pow(rotatedV[1], 2) + Math.pow(rotatedV[2], 2));
			
			projVTable[i] = projVertex;
		}
		
		// Sort faces based on distance from the viewer
		Integer[] faceOrder = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7};
		Arrays.sort(faceOrder, Comparator.comparingDouble(i -> distances[i]));
		
		// Draw faces in the sorted order
		for (int i : faceOrder) {
			g2.setColor(colors[counter]);
			
			g2.fillOval(projVTable[i][0], projVTable[i][1], pointDiameter, pointDiameter);
			
			counter++;
		}
		
		for (int[] ln : edgeTable) {
			g2.setColor(colors[counter]);
			
			g2.drawLine(
					projVTable[ln[0]][0] + pointDiameter / 2,
					projVTable[ln[0]][1] + pointDiameter / 2,
					projVTable[ln[1]][0] + pointDiameter / 2,
					projVTable[ln[1]][1] + pointDiameter / 2
			);
			
			counter++;
		}
	}
	
	public int[] projectVertex(int[] vertex) {
		int projX = (focalLength * vertex[0]) / (focalLength + vertex[2]) + getWidth() / 2;
		int projY = (focalLength * vertex[1]) / (focalLength + vertex[2]) + getHeight() / 2;
		
		return new int[]{projX, projY};
	}
	public int[] rotateVertex(int[] vertex) {
		double[][] rotationMatrix = {
				{
						Math.cos(YPR[1]) * Math.cos(YPR[2]),
						(Math.sin(YPR[0]) * Math.sin(YPR[1]) * Math.cos(YPR[2])) - (Math.cos(YPR[0]) * Math.sin(YPR[2])),
						(Math.cos(YPR[0]) * Math.sin(YPR[1]) * Math.cos(YPR[2])) + (Math.sin(YPR[0]) * Math.sin(YPR[2]))
				},
				{
						Math.cos(YPR[1]) * Math.sin(YPR[2]),
						(Math.sin(YPR[0]) * Math.sin(YPR[1]) * Math.sin(YPR[2])) + (Math.cos(YPR[0]) * Math.cos(YPR[2])),
						(Math.cos(YPR[0]) * Math.sin(YPR[1]) * Math.sin(YPR[2])) - (Math.sin(YPR[0]) * Math.cos(YPR[2]))
				},
				{
						-Math.sin(YPR[1]),
						Math.sin(YPR[0]) * Math.cos(YPR[2]),
						Math.cos(YPR[0]) * Math.cos(YPR[2])
				}
		};
		
		int[] result = new int[3];

		result[0] = (int) Math.ceil(vertex[0] * rotationMatrix[0][0] + vertex[1] * rotationMatrix[1][0] + vertex[2] * rotationMatrix[2][0]);
		result[1] = (int) Math.ceil(vertex[0] * rotationMatrix[0][1] + vertex[1] * rotationMatrix[1][1] + vertex[2] * rotationMatrix[2][1]);
		result[2] = (int) Math.ceil(vertex[0] * rotationMatrix[0][2] + vertex[1] * rotationMatrix[1][2] + vertex[2] * rotationMatrix[2][2]);

		return result;
	}
	
	public int[] shift(int[] vertex) {
		return new int[]{vertex[0] + xShift, vertex[1] + yShift, vertex[2] + zShift};
	}

	public int[] unshift(int[] vertex) {
		return new int[]{-vertex[0], -vertex[1], -vertex[2]};
	}
	
	public static void main(String[] args) {
		new Cube3D();
	}
}