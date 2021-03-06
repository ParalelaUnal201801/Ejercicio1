import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * Clase que contiene los métodos para implementar la suma de los recíprocos de un arreglo usando paralelismo.
 */
public final class ReciprocalArraySum {

    /**
     * Constructor.
     */
    private ReciprocalArraySum() {
    }

    /**
     * Calcula secuencialmente la suma de valores recíprocos para un arreglo.
     *
     * @param input Arreglo de entrada
     * @return La suma de los recíprocos del arreglo de entrada
     */
    protected static double seqArraySum(final double[] input) {
        double sum = 0;

        long startTimeSP = System.nanoTime();
        // Calcula la suma de los recíprocos de los elementos del arreglo
        for (int i = 0; i < input.length; i++) {
            sum += 1 / input[i];
        }
        long timeInNanosSP = System.nanoTime() - startTimeSP;
        System.out.println("Resultados sin paralelizar (seqArraySum)");
        System.out.format("Tiempo: %s, Suma: %s\n", timeInNanosSP, sum);

        return sum;
    }

    /**
     * calcula el tamaño de cada trozo o sección, de acuerdo con el número de secciones para crear
     * a través de un número dado de elementos.
     *
     * @param nChunks El número de secciones (chunks) para crear
     * @param nElements El número de elementos para dividir
     * @return El tamaño por defecto de la sección (chunk)
     */
    private static int getChunkSize(final int nChunks, final int nElements) {
        // Función techo entera
        return (nElements + nChunks - 1) / nChunks;
    }

    /**
     * Calcula el índice del elemento inclusivo donde la sección/trozo (chunk) inicia,
     * dado que hay cierto número de secciones/trozos (chunks).
     *
     * @param chunk la sección/trozo (chunk) para cacular la posición de inicio
     * @param nChunks Cantidad de seciiones/trozos (chunks) creados
     * @param nElements La cantidad de elementos de la sección/trozo que debe atravesarse
     * @return El indice inclusivo donde esta sección/trozo (chunk) inicia en el conjunto de
     *         nElements
     */
    private static int getChunkStartInclusive(final int chunk,
                                              final int nChunks, final int nElements) {
        final int chunkSize = getChunkSize(nChunks, nElements);
        return chunk * chunkSize;
    }

    /**
     * Calcula el índice del elemento exclusivo que es proporcionado al final de la sección/trozo (chunk),
     * dado que hay cierto número de secciones/trozos (chunks).
     *
     * @param chunk LA sección para calcular donde termina
     * @param nChunks Cantidad de seciiones/trozos (chunks) creados
     * @param nElements La cantidad de elementos de la sección/trozo que debe atravesarse
     * @return El índice de terminación exclusivo para esta sección/trozo (chunk)
     */
    private static int getChunkEndExclusive(final int chunk, final int nChunks,
                                            final int nElements) {
        final int chunkSize = getChunkSize(nChunks, nElements);
        final int end = (chunk + 1) * chunkSize;
        if (end > nElements) {
            return nElements;
        } else {
            return end;
        }
    }

    /**
     * Este pedazo de clase puede ser completada para para implementar el cuerpo de cada tarea creada
     * para realizar la suma de los recíprocos del arreglo en paralelo.
     */
    private static class ReciprocalArraySumTask extends RecursiveAction {
        static int SEQUENTIAL_THRESHOLD = 500000;
        /**
         * Iniciar el índice para el recorrido transversal hecho por esta tarea.
         */
        private final int startIndexInclusive;
        /**
         * Concluir el índice para el recorrido transversal hecho por esta tarea.
         */
        private final int endIndexExclusive;
        /**
         * Arreglo de entrada para la suma de recíprocos.
         */
        private final double[] input;
        /**
         * Valor intermedio producido por esta tarea.
         */
        private double value;

        /**
         * Constructor.
         * @param setStartIndexInclusive establece el indice inicial para comenzar
         *        el recorrido trasversal.
         * @param setEndIndexExclusive establece el indice final para el recorrido trasversal.
         * @param setInput Valores de entrada
         */
        ReciprocalArraySumTask(final int setStartIndexInclusive,
                               final int setEndIndexExclusive, final double[] setInput) {
            this.startIndexInclusive = setStartIndexInclusive;
            this.endIndexExclusive = setEndIndexExclusive;
            this.input = setInput;
        }

        /**
         * Adquiere el valor producido por esta tarea.
         * @return El valor producido por esta tarea
         */
        public double getValue() {
            return value;
        }

        @Override
        protected void compute() {
            // Para hacer
            if( input.length <= SEQUENTIAL_THRESHOLD ){
                for (int i = 0; i < input.length; i++) {
                    value += 1 / input[i];
                }
                // seqArraySum(input);
                //System.out.format("Sum of %s: %s\n", Arrays.toString(input), value);
            } else {
                int n = input.length;
                int mid = n/2;
                double arr1[] = Arrays.copyOfRange(input, 0, mid);
                double arr2[] = Arrays.copyOfRange(input, mid, n);

                ReciprocalArraySumTask sum1 = new ReciprocalArraySumTask(0, 0, arr1 );
                ReciprocalArraySumTask sum2 = new ReciprocalArraySumTask(0, 0, arr2 );

                sum1.fork();
                sum2.compute();
                sum1.join();
                value = sum1.value + sum2.value;
            }
        }
    }

    /**
     * Para hacer: Modificar este método para calcular la misma suma de recíprocos como le realizada en
     * seqArraySum, pero utilizando dos tareas ejecutándose en paralelo dentro del framework ForkJoin de Java
     * Se puede asumir que el largo del arreglo de entrada
     * es igualmente divisible por 2.
     *
     * @param input Arreglo de entrada
     * @return La suma de los recíprocos del arreglo de entrada
     */
    protected static double parArraySum(final double[] input) {
        assert input.length % 2 == 0;

        long startTimeP = System.nanoTime();
        ForkJoinPool pool = new ForkJoinPool();
        ReciprocalArraySumTask sum = new ReciprocalArraySumTask(0,0, input);
        pool.invoke( sum );
        long timeInNanosP = System.nanoTime() - startTimeP;
        System.out.println("Resultados paralelizando (parArraySum)");
        System.out.format("Tiempo: %s, Suma: %s\n", timeInNanosP, sum.getValue());
        return sum.getValue();
    }

    /**
     * Para hacer: extender el trabajo hecho para implementar parArraySum que permita utilizar un número establecido
     * de tareas para calcular la suma del arreglo recíproco.
     * getChunkStartInclusive y getChunkEndExclusive pueden ser útiles para cacular
     * el rango de elementos indice que pertenecen a cada sección/trozo (chunk).
     *
     * @param input Arreglo de entrada
     * @param numTasks El número de tareas para crear
     * @return La suma de los recíprocos del arreglo de entrada
     */
    protected static double parManyTaskArraySum(final double[] input,
                                                final int numTasks) {
        double sum = 0;

        // Calcula la suma de los recíprocos de los elementos del arreglo
        for (int i = 0; i < input.length; i++) {
            sum += 1 / input[i];
        }

        return sum;
    }

    public static void main (String args[]){

        // Input
        double[] input = new double[1000000];
        for (int i=0; i<input.length; i++) {
            int n = (int) (Math.random() * 9 + 1);
            input[i] = n;
        }

        // Sin paralelizar
        seqArraySum(input);

        // Paralelizando
        parArraySum(input);
    }
}
