import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

/**
 * Clase que contiene los métodos para implementar la suma de los recíprocos de un arreglo usando paralelismo.
 */
public final class ReciprocalArraySumTest extends RecursiveAction {

    private static final int SEQUENTIAL_THRESHOLD = 10;

    private final double data[];
    private double sum = 0;

    public ReciprocalArraySumTest(double data[]) {
        this.data = data;
    }

    public double getSum() {
        return sum;
    }

    @Override
    protected void compute() {
        if (data.length <= SEQUENTIAL_THRESHOLD) {
            // base case
            for (Double l: data) {
                sum += 1 / l;
            }
            System.out.format("Sum of %s: %s\n", Arrays.toString(data), sum);
        } else {
            // recursive case
            // Calculate new range
            int mid = data.length / 2;
            double arr1[] = Arrays.copyOfRange(data, 0, mid);
            double arr2[] = Arrays.copyOfRange(data, mid, data.length);
            ReciprocalArraySumTest firstSubtask = new ReciprocalArraySumTest(arr1);
            ReciprocalArraySumTest secondSubtask = new ReciprocalArraySumTest(arr2);
            firstSubtask.fork(); // queue the first task
            secondSubtask.compute(); // compute the second task
            firstSubtask.join(); // wait for the first task result
            sum = secondSubtask.sum + firstSubtask.sum;
        }
    }

    protected static double parSum( double input[] ){
        ForkJoinPool pool = new ForkJoinPool();
        ReciprocalArraySumTest task = new ReciprocalArraySumTest(input);
        pool.invoke(task);
        System.out.println(task.getSum());
        return task.getSum();
    }
    public static void main(String[] args) {

        double[] data = new double[20];
        for (int i=0; i<data.length; i++) {
            int n = (int) (Math.random() * 9 + 1);
            data[i] = n;
        }

        parSum(data);
    }
}
