package de.krien.machinelearning.letterrecognition.view;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.Objdetect;
import org.opencv.utils.Converters;

import com.sun.javafx.geom.Vec2f;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.layout.AnchorPane;

public class GuiController implements Initializable {

	private Image image;

	@FXML
	ImageView imageView;

	@FXML
	AnchorPane anchorPane;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		String imagePath = getClass().getResource("/img/letters.gif").toString();
		image = new Image(imagePath);
		imageView.setImage(image);
	}

	@FXML
	protected void blur() {
		Mat mat = getMatFromImage();
		Mat newMat = new Mat();
		Imgproc.GaussianBlur(mat, newMat, new Size(19, 19), 0);
		Image newImage = getImageFromMat(newMat);
		image = newImage;
		imageView.setImage(newImage);
	}

	@FXML
	protected void treshold() {
		Mat mat = getMatFromImage();
		Mat newMat = new Mat();
		Imgproc.adaptiveThreshold(mat, newMat, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);
		Image newImage = getImageFromMat(newMat);
		image = newImage;
		imageView.setImage(newImage);
	}

	@FXML
	protected void invert() {
		Mat mat = getMatFromImage();
		Mat newMat = new Mat();
		Core.bitwise_not(mat, newMat);
		Image newImage = getImageFromMat(newMat);
		image = newImage;
		imageView.setImage(newImage);
	}

	@FXML
	protected void erode() {
		Mat mat = getMatFromImage();
		Mat newMat = new Mat();
		Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2));
		Imgproc.erode(mat, newMat, element);
		Image newImage = getImageFromMat(newMat);
		image = newImage;
		imageView.setImage(newImage);
	}

	@FXML
	protected void dilate() {
		Mat mat = getMatFromImage();
		Mat newMat = new Mat();
		Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));
		Imgproc.dilate(mat, newMat, element);
		Image newImage = getImageFromMat(newMat);
		image = newImage;
		imageView.setImage(newImage);
	}

	@FXML
	protected void outlines() {
		Mat mat = getMatFromImage();
		List<MatOfPoint> contours = new ArrayList<>();
		Mat hierachy = new Mat();
		Imgproc.findContours(mat, contours, hierachy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
		Mat newMat = getColorMatFromImage();

		List<Rect> rectangles = new ArrayList<>();
		for (int i = 0; i < contours.size(); i++) {
				MatOfPoint contour = contours.get(i);
				Rect rect = Imgproc.boundingRect(contour);
				rectangles.add(rect);
		}
		
		ArrayList<Entry<Rect, Rect>> pairs = new ArrayList<>();
		for(int i = 0; i < rectangles.size(); i++) {
			for(int j = 0; j < rectangles.size(); j++) {
				if(i != j) {
					Vec2f pos1 = new Vec2f(rectangles.get(i).x, rectangles.get(i).y);
					Vec2f pos2 = new Vec2f(rectangles.get(j).x, rectangles.get(j).y);
					if (pos1.distance(pos2) < 30) {
						pairs.add(new AbstractMap.SimpleEntry<Rect, Rect>(rectangles.get(i), rectangles.get(j)));
					}
				}
			}
		}

		for(Entry<Rect, Rect> entry : pairs) {
			Rect rectangle1 = entry.getKey();
			Rect rectangle2 = entry.getValue();
			int left = rectangle1.x < rectangle2.x ? rectangle2.x : rectangle1.x;
			int top = rectangle1.y < rectangle2.y ? rectangle2.y : rectangle1.y;
			int right = (rectangle1.x + rectangle1.width) < (rectangle2.x + rectangle2.width) ? (rectangle2.x + rectangle2.width) : (rectangle1.x + rectangle1.width);
			int bottom = (rectangle1.y + rectangle1.height) < (rectangle2.y + rectangle2.height) ? (rectangle2.y + rectangle2.height) : (rectangle1.y + rectangle1.height);
			rectangles.remove(rectangle1);
			rectangles.remove(rectangle2);
			Rect newRect = new Rect(left, top, (right - left), (bottom-top));
			rectangles.add(newRect);
		}
		
		for (Rect rect : rectangles) {
			Imgproc.rectangle(newMat, rect.tl(), rect.br(), new Scalar(0, 0, 255));
		}

		Image newImage = getImageFromMat(newMat);
		image = newImage;
		imageView.setImage(newImage);
	}
	

	@FXML
	protected void all() {
		blur();
		treshold();
		invert();
		erode();
//		dilate();
		outlines();
	}

	private Image getImageFromMat(Mat mat) {
		MatOfByte byteMat = new MatOfByte();
		Imgcodecs.imencode(".bmp", mat, byteMat);
		return new Image(new ByteArrayInputStream(byteMat.toArray()));
	}

	private Mat getMatFromImage() {
		try {
			int w = (int) image.getWidth();
			int h = (int) image.getHeight();
			byte[] imageAsByteArray = new byte[w * h * 4];
			image.getPixelReader().getPixels(0, 0, w, h, PixelFormat.getByteBgraInstance(), imageAsByteArray, 0, w * 4);

			Mat mat = new Mat((int) image.getHeight(), (int) image.getWidth(), CvType.CV_8UC4);
			Mat grayMat = new Mat((int) image.getHeight(), (int) image.getWidth(), CvType.CV_8UC1);
			mat.put(0, 0, imageAsByteArray);
			Imgproc.cvtColor(mat, grayMat, Imgproc.COLOR_BGRA2GRAY);
			return grayMat;
		} catch (Exception e) {
			System.out.println("Could not create OpenCV mat from JavaFX image: ");
			e.printStackTrace();
		}
		return null;
	}

	private Mat getColorMatFromImage() {
		try {
			int w = (int) image.getWidth();
			int h = (int) image.getHeight();
			byte[] imageAsByteArray = new byte[w * h * 4];
			image.getPixelReader().getPixels(0, 0, w, h, PixelFormat.getByteBgraInstance(), imageAsByteArray, 0, w * 4);

			Mat mat = new Mat((int) image.getHeight(), (int) image.getWidth(), CvType.CV_8UC4);
			mat.put(0, 0, imageAsByteArray);
			return mat;
		} catch (Exception e) {
			System.out.println("Could not create OpenCV mat from JavaFX image: ");
			e.printStackTrace();
		}
		return null;
	}

}
