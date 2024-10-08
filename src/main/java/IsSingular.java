import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;




import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;




public class IsSingular {
    public double[][] getMatrix() {
        return matrix;
    }

    public double[][] matrix;

    public double[][] getResultMatrix() {
        return resultMatrix;
    }

    public double[][] resultMatrix;

    private List<Double> lossHistory = new ArrayList<>();
    private List<Double> accuracyHistory = new ArrayList<>();


    public IsSingular(String imagePath) throws IOException {
        loadImageAsMatrix(imagePath);
    }


    public void loadTargetImage(String imagePath) throws IOException {
        BufferedImage image = ImageIO.read(new File(imagePath));
        int width = image.getWidth();
        int height = image.getHeight();

        double[][] targetMatrix = new double[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                targetMatrix[y][x] = (0.299 * ((rgb >> 16) & 0xFF) +
                        0.587 * ((rgb >> 8) & 0xFF) +
                        0.114 * (rgb & 0xFF));
            }
        }
    }


    public void printMatrix() {
        for (double[] row : matrix) {
            for (double value : row) {
                System.out.printf("%.2f ", value);
            }
            System.out.println();
        }
    }

    public void loadImageAsMatrix(String imagePath) throws IOException {
        BufferedImage image = ImageIO.read(new File(imagePath));
        int width = image.getWidth();
        int height = image.getHeight();

        matrix = new double[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                matrix[y][x] = (0.299 * ((rgb >> 16) & 0xFF) +
                        0.587 * ((rgb >> 8) & 0xFF) +
                        0.114 * (rgb & 0xFF));
            }
        }
    }

    public void checkSingularity() {
        RealMatrix realMatrix = MatrixUtils.createRealMatrix(matrix);
        SingularValueDecomposition svd = new SingularValueDecomposition(realMatrix);
        double[] singularValues = svd.getSingularValues();

        double tolerance = 0;

        for (double value : singularValues) {
            if (Math.abs(value) < 1e-10) {
                System.out.println("Матриця сингулярна");
                return;
            }
        }

        System.out.println("Матриця не сингулярна");
    }




    public void gradientDescent(int iterations, double learningRate, double epsilon) {


        for (int i = 0; i < iterations; i++) {
            double loss = computeLoss();
            double accuracy = computeAccuracy();
            lossHistory.add(loss);
            accuracyHistory.add(accuracy);
            double[] gradients = computeGradients();



            for (int y = 0; y < matrix.length; y++) {
                for (int x = 0; x < matrix[0].length; x++) {
                    matrix[y][x] -= learningRate * gradients[y * matrix[0].length + x];
                }
            }


            System.out.printf("Ітерація %d: Втрата = %.6f, Точність = %.6f%n", i + 1, loss, computeAccuracy());


            if (i > 0 && (Math.abs(lossHistory.get(i - 1) - loss) < epsilon || accuracy < epsilon)) {
                break;
            }

            // Перевірка на точність
            //            if (Math.abs(previousLoss - loss) < tolerance || computeAccuracy() < epsilon) {
            //                break;
            //            }
        }
        double bestAccuracy = Collections.min(accuracyHistory);
        int bestIteration = accuracyHistory.indexOf(bestAccuracy) + 1;
        System.out.printf("Найкраща точність: %.6f досягнута на ітерації %d%n", bestAccuracy, bestIteration);
        System.out.println("Оптимальне початкове наближення (остання матриця):");
//            printMatrix();
        plotAccuracyHistory();

        double[][] resultMatrix = new double[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++) {
            System.arraycopy(matrix[i], 0, resultMatrix[i], 0, matrix[0].length);
        }

        this.resultMatrix = resultMatrix;
        System.out.printf("Розмірність матриці оптимального початковго наближення : %d x %d%n", matrix.length, matrix[0].length);
        printMatrix();

    }


    private void plotAccuracyHistory() {
        XYSeries series = new XYSeries("Accuracy over Iterations");
        for (int i = 0; i < accuracyHistory.size(); i++) {
            series.add(i, accuracyHistory.get(i));
        }

        XYSeriesCollection dataset = new XYSeriesCollection(series);
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Gradient Descent Accuracy",
                "Iteration",
                "Accuracy",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        //    XYPlot plot = chart.getXYPlot();
        //    XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        //    renderer.setSeriesPaint(0, Color.BLUE);
        //    renderer.setSeriesStroke(0, new BasicStroke(2.0f));
        //    plot.setRenderer(renderer);
        //    plot.setBackgroundPaint(Color.white);
        //
        //    ApplicationFrame frame = new ApplicationFrame("Gradient Descent Visualization");
        //    frame.setContentPane(new ChartPanel(chart));
        //    frame.pack();
        //    RefineryUtilities.centerFrameOnScreen(frame);
        //    frame.setVisible(true);
    }

    private void plotLossHistory() {
        XYSeries series = new XYSeries("Loss over Iterations");
        for (int i = 0; i < lossHistory.size(); i++) {
            series.add(i, lossHistory.get(i));
        }

        XYSeriesCollection dataset = new XYSeriesCollection(series);
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Gradient Descent Loss",
                "Iteration",
                "Loss",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.RED);
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));
        plot.setRenderer(renderer);
        plot.setBackgroundPaint(Color.white);

        ApplicationFrame frame = new ApplicationFrame("Gradient Descent Visualization");
        frame.setContentPane(new ChartPanel(chart));
        frame.pack();
        RefineryUtilities.centerFrameOnScreen(frame);
        frame.setVisible(true);
    }

    private double computeLoss() {
        double loss = 0.0;
        // Цільова матриця (в даному випадку, всі нулі)
        double[][] targetMatrix = new double[matrix.length][matrix[0].length];

        for (int y = 0; y < matrix.length; y++) {
            for (int x = 0; x < matrix[0].length; x++) {
                double diff = matrix[y][x] - targetMatrix[y][x];
                loss += diff * diff; // Середня квадратна похибка
            }
        }

        return loss / (matrix.length * matrix[0].length); // Середнє значення
    }

    private double[] computeGradients() {
        double[] gradients = new double[matrix.length * matrix[0].length];
        double[][] targetMatrix = new double[matrix.length][matrix[0].length]; // Цільова матриця

        for (int y = 0; y < matrix.length; y++) {
            for (int x = 0; x < matrix[0].length; x++) {
                // Градієнт обчислюється як 2 * (значення - ціль)
                gradients[y * matrix[0].length + x] = 2 * (matrix[y][x] - targetMatrix[y][x]);
            }
        }

        return gradients;
    }

    private double computeAccuracy() {
        double accuracy = 0.0;
        double[][] targetMatrix = new double[matrix.length][matrix[0].length];

        for (int y = 0; y < matrix.length; y++) {
            for (int x = 0; x < matrix[0].length; x++) {
                accuracy += Math.abs(matrix[y][x] - targetMatrix[y][x]);
            }
        }

        return accuracy / (matrix.length * matrix[0].length); // Середнє значення
    }
}