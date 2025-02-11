package com.example.zenword;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    // Declaramos variables globales a las que accedemos comunmente

    // Lista de los botones de las letras
    private Set<Button> letterButtons;
    //Array para las letras de los botones
    private char[] letters;

    // Para enseñar la palabra que se está escribiendo
    private TextView palabraActual;

    //Tamaño de la palabra encontrada
    private int wordLength;

    //Lista de palabras con igual o menor longitud que el wordLength(pueden llegar a formarse con palabraOriginal)
    private Set<String> palabrasValidas;
    private Set<String> palabrasValidasAcento;

    //Mapa para separar las palabras válidas sin acento por longitud
    private Map<Integer, LinkedHashSet<String>> palabrasPorLongitud;

    //Almacena la frecuencia que tienen las palabras
    private Map<Character, Integer> frecuenciaLetrasOriginales;

    //Mapa para separar las posibles soluciones(posibles palabras a elejir) por longitud
    private Map<Integer, LinkedHashSet<String>> posiblesSoluciones;

    //Lista de palabras ocultas. La información sobre su posición en la pantalla
    //viene dada por su índice: la que está en la posicion 0, es la primera en la pantalla
    private LinkedHashSet<String> palabrasOcultas;
    //cantidad de palabras a encontrar(las que nos hacen ganar)
    private int cantPalabrasOcultas;
    //Cantidad de palabras ocultas encontradas por el usuario
    private int cantPalOcultEncontradas;

    private TreeSet<String> palabrasEncontradas;
    //cant de palabras encontradas seas las ocultas o no
    int contPalabrasEncontradas;

    //Cantidad de puntos de bonus que tiene el usuario
    private int cantBonus;

    // Cada elemento representa una fila (en formato array de TextViews) de las palabras
    private final int maxCantPalabras = 5;
    private final int minCantLetrasPalabras = 3;
    private final int maxCantLetrasPalabra = 7;
    public TextView[][] filasPalabras;


    //Cantidad de palabras que hay entre todas las posibles soluciones
    private int tamPosSol = 0;

    //Cantidad de bonus necesarios para poder obtener una ayuda
    private final int puntParaAyuda = 5;

    //Mapa con combinacion de colores y sus degradados
    private Map<Integer, Integer> combinacionColores;
    private final int[] colores = new int[2];

    //TextView que contiene las palabras encontradas y la cantidad total de palabras a encontrar
    private TextView txtPalEncontradas;
    //String que representa la última palabra que el usuario ha repetido
    String ultimaPalRepetida = null;

    private ConstraintSet constraintSet;
    private ConstraintLayout constraintApp;
    ////////////////////////
    // VARIABLES DRAWABLE //
    ////////////////////////
    GradientDrawable drawableCirculo;
    GradientDrawable drawablePalabras;
    private int margenEntreTextView;
    private int anchoTextView;
    private int alturaTextView;
    private int widthDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Configuraciones
        iniciarConfiguraciones();
        empezarPartida();
    }

    /**
     * Empieza la partida al inicializar las variables y reestablecer valores
     */
    private void empezarPartida() {

        cantBonus = 0;
        cantPalabrasOcultas = 0;
        cantPalOcultEncontradas = 0;

        Random rand = new Random();
        //Genera un número aleatorio entre 3 (0+3) y 7 (4+3)

        int bound = maxCantLetrasPalabra - minCantLetrasPalabras + 1; //1 porque la bound no se incluye

        wordLength = rand.nextInt(bound) + minCantLetrasPalabras;

        //Habilitamos los view
        enableViews(R.id.layoutPrincipal);

        //Obtenemos una palabraAleatoria de longitud wordLength
        String palOrig = obtenerPalabraOriginal();

        //Separamos las palabras válidas anteriores por su longitud y lo almacenamos en palabrasPorLongitud
        separarPalabrasValidasPorLongitud();

        //Obtenemos la frecuencia de aparición de cada letra de la palabra Original
        iniciarFrecuenciaLetras(palOrig); //La guardamos en frecuenciaLetrasOriginales

        //Usamos todos los datos anteriores para obtener
        //todas las posibles soluciones para cada una de las longitudes
        posiblesSoluciones = obtenerPosiblesSoluciones(palOrig);

        //Ahora tan solo nos queda elejir las soluciones reales (palabrasOcultas)
        obtenerPalabrasOcultas();

        palabrasEncontradas = new TreeSet<>(); //Reestablecemos su valor
        contPalabrasEncontradas = 0;

        //Ahora tan solo queda esperar a que el usuario interaccione con los elementos

        //////////////////////////////////////////
        //  Reiniciamos Elementos de la Partida //
        //////////////////////////////////////////
        botonClear(new View(this));
        reiniciarPalabrasEscondidas();
        reiniciarBotonesLetras();
        reiniciarTxtPalEncontradas();
        actualizarCantidadBonus();
        actualizarColores();
    }

    /**
     * Inicializa las configuracion que seran constantes para todas las partidas
     */
    private void iniciarConfiguraciones() {
        //Iniciamos el mapa de colores
        iniciarColores();

        // Obtener el ConstraintLayout
        constraintApp = findViewById(R.id.layoutPrincipal);

        //Inicializa el TextView que contiene la palabra creándose
        palabraActual = findViewById(R.id.PalabraActual);

        // Obtener las dimensiones de la pantalla
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        /////////////////////////
        // VARIABLES textView ///
        // de palabras ocultas///
        /////////////////////////
        widthDisplay = metrics.widthPixels;
        int heightDisplay = metrics.heightPixels;

        // Configuraciones comunes
        margenEntreTextView = 0;
        anchoTextView = widthDisplay / 7; // 14% de la pantalla
        alturaTextView = heightDisplay * 6 / 100; // 6% de la pantalla

        // Formato para el circulo de letra
        iniciarDrawableCirculo();
        // Formato para los textview
        iniciarDrawablePalabras();

        // Iniciamos los botones de letras
        iniciarBotonesLetras();
    }

    /**
     * Genera 5 combinacion de colores predefinidas
     */
    private void iniciarColores() {
        combinacionColores = new HashMap<>();
        combinacionColores.put(Color.CYAN, Color.parseColor("#006f6f"));
        combinacionColores.put(Color.RED, Color.parseColor("#6f0000"));
        combinacionColores.put(Color.GREEN, Color.parseColor("#006f00"));
        combinacionColores.put(Color.MAGENTA, Color.parseColor("#6f006f"));
        combinacionColores.put(Color.YELLOW, Color.parseColor("#6f6f00"));
    }

    /**
     * Formato al circulo de letras
     */
    private void iniciarDrawableCirculo() {
        // Crear un GradientDrawable con degradado de arriba a abajo
        drawableCirculo = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.RED, Color.RED}
        );

        // Configurar la forma como un círculo
        drawableCirculo.setShape(GradientDrawable.OVAL);

        // Encontrar el ImageView por su ID
        ImageView circuloDeLetras = findViewById(R.id.CirculoDeLetras);

        // Aplicar el drawable al ImageView
        circuloDeLetras.setBackground(drawableCirculo);
    }

    /**
     * Formato al background de las palabrasOcultas
     */
    private void iniciarDrawablePalabras() {
        // Crea un objeto GradientDrawable para definir la forma del borde
        drawablePalabras = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM, new int[]{Color.RED, Color.RED});

        drawablePalabras.setShape(GradientDrawable.RECTANGLE); // Forma rectangular
        drawablePalabras.setCornerRadius(20); // Radio de esquina en píxeles
        // Establece el color de fondo y el color del borde
        drawablePalabras.setStroke(2, Color.WHITE); // Ancho del borde y color
    }

    /**
     * Genera una fila de textview para una palabra
     *
     * @param guia    guia de ayuda para  la posicion de los TextView
     * @param lletres cantidad de letras
     * @return un array de TextView que representa la fila de una palabra oculta
     */
    public TextView[] crearFilaTextViews(int guia, int lletres) {

        //Array con todos los textView
        TextView[] fila = new TextView[lletres];

        // Crear un ConstraintSet para todas las restricciones
        constraintSet = new ConstraintSet();

        //margen lateral
        int margenLateral = (widthDisplay - (anchoTextView * lletres)) / 2;

        //Generamos tantos textView como cantidad de letras tenga la palabra
        for (int i = 0; i < lletres; i++) {

            // Crear y configurar el TextView
            fila[i] = new TextView(this);

            //Generamos un id para el text view
            int id = View.generateViewId();

            //Inicializamos los valores del textView
            fila[i].setId(id);
            fila[i].setText("");

            //le damos formato
            fila[i].setTextColor(Color.WHITE);
            fila[i].setGravity(Gravity.CENTER); // Centra el texto horizontal y verticalmente
            fila[i].setTextSize(15);
            fila[i].setAllCaps(true);

            // Aplica el objeto GradientDrawable como fondo del TextView
            fila[i].setBackground(drawablePalabras);

            // Añadir el TextView al ConstraintLayout
            constraintApp.addView(fila[i]);

            // Conectar el TextView a las restricciones adecuadas
            if (i == 0) {
                // Conectar el primer TextView al margen izquierdo y a la guía verticalmente
                constraintSet.connect(id, ConstraintSet.START, ConstraintSet.PARENT_ID,
                        ConstraintSet.START, margenLateral);
            } else {
                // Conectar los TextViews siguientes al anterior en la fila
                constraintSet.connect(id, ConstraintSet.START, fila[i - 1].getId(),
                        ConstraintSet.END, margenEntreTextView);
            }
            // Conectar todos los TextViews a la guía verticalmente
            constraintSet.connect(id, ConstraintSet.TOP, guia, ConstraintSet.TOP, 0);

            // Establecer las dimensiones del TextView
            constraintSet.constrainWidth(id, anchoTextView);
            constraintSet.constrainHeight(id, alturaTextView);
        }

        // Aplicar todas las restricciones al ConstraintSet
        constraintSet.applyTo(constraintApp);

        return fila;
    }

    /**
     * Inicializa los botones de las letras
     */
    private void iniciarBotonesLetras() {

        letterButtons = new LinkedHashSet(maxCantLetrasPalabra);

        // Añade los botones a la lista
        letterButtons.add(findViewById(R.id.btnLetra1));
        letterButtons.add(findViewById(R.id.btnLetra2));
        letterButtons.add(findViewById(R.id.btnLetra3));
        letterButtons.add(findViewById(R.id.btnLetra4));
        letterButtons.add(findViewById(R.id.btnLetra5));
        letterButtons.add(findViewById(R.id.btnLetra6));
        letterButtons.add(findViewById(R.id.btnLetra7));

        Iterator<Button> iterador = letterButtons.iterator();

        while (iterador.hasNext()) {
            Button b = iterador.next();
            b.setClickable(true);
            b.setTextColor(Color.WHITE);
        }
    }

    /**
     * Generea las filas de TextView para las palabras ocultas de la partida actual
     */
    private void reiniciarPalabrasEscondidas() {
        // Limpiar y ocultar todas las vistas existentes
        if (filasPalabras != null) {
            for (TextView[] filasPalabra : filasPalabras) {
                if (filasPalabra != null) {
                    for (TextView textView : filasPalabra) {
                        if (textView != null) {
                            textView.setVisibility(View.GONE);
                            textView.setText("");
                        }
                    }
                }
            }
        }

        // Contar el número de elementos en palabrasOcultas
        int count = 0;
        Iterator<String> iteratorCount = palabrasOcultas.iterator();
        while (iteratorCount.hasNext()) {
            iteratorCount.next();
            count++;
        }

        // Inicializar el array de filas de palabras
        filasPalabras = new TextView[count][];

        // Crear un iterador para recorrer palabrasOcultas
        Iterator<String> iterator = palabrasOcultas.iterator();
        int i = 0;

        // Crear y mostrar las filas de TextView según las palabras ocultas
        while (iterator.hasNext()) {
            String palabra = iterator.next();
            int longitud = palabra.length();

            // Crear la fila de TextView
            filasPalabras[i] = crearFilaTextViews(getGuideLineId(i), longitud);

            for (int j = 0; j < longitud; j++) {
                filasPalabras[i][j].setVisibility(View.VISIBLE);
            }
            i++;
        }
    }

    /**
     * Obtiene el ID de la guía (guideline) correspondiente para cada fila.
     *
     * @param fila índice de la fila
     * @return ID de la guía (guideline)
     */
    private int getGuideLineId(int fila) {
        switch (fila) {
            case 0:
                return R.id.guideline;
            case 1:
                return R.id.guideline7;
            case 2:
                return R.id.guideline8;
            case 3:
                return R.id.guideline9;
            case 4:
                return R.id.guideline10;
            default:
                throw new IllegalArgumentException("Índice de fila no válido: " + fila);
        }
    }

    /**
     * Actualiza los botones de las letras con las letras de Palabra Orignial
     */
    private void reiniciarBotonesLetras() {

        Iterator<Button> iterador = letterButtons.iterator();
        Button b;
        int idx = 0;

        //Reiniciamos la visibilidad a los botones que podian estar ocultos
        while (iterador.hasNext()) {
            b = iterador.next();

            //Dependiendo de wordLength, ocultamos los botones que no se deberian ver
            if (idx >= wordLength) {
                b.setVisibility(View.GONE);
            } else {
                b.setVisibility(View.VISIBLE);
            }
            idx++;
        }

        //Asignamos las letras correspondientes a cada boton
        asignarLetras();
    }

    /**
     * Asigna las letras de letters a sus botones correspondientes
     */
    private void asignarLetras() {
        // Llenar la lista letters con las letras del mapa de frecuencias
        llenarListaLetras();

        //Iterador para los botones
        Iterator<Button> iterBut = letterButtons.iterator();
        Button b;

        //Iterador para las letras
        int idx = 0;
        String s;

        //Recorremos los botones y le asignamos las letras de la lista
        while (iterBut.hasNext() && idx < wordLength) {
            b = iterBut.next();
            s = String.valueOf(letters[idx]);
            b.setText(s);
            idx++;
        }
    }

    /**
     * Inicia el texto que enseña qué palabras se han encontrado
     */
    private void reiniciarTxtPalEncontradas() {
        tamPosSol = 0;

        //Obtenemos el panel de texto de PalabrasConstruidas (las que enseñan todas las palabras, sean ocultas o no)
        txtPalEncontradas = findViewById(R.id.PalabrasConstruidas);

        Iterator<Map.Entry<Integer, LinkedHashSet<String>>> iterador = posiblesSoluciones.entrySet().iterator();
        Map.Entry<Integer, LinkedHashSet<String>> entrada;

        while (iterador.hasNext()) {
            entrada = iterador.next();
            //Obtenemos su lista y sumamos la cantidad de palabras que tiene
            LinkedHashSet<String> listaDePalabras = entrada.getValue();

            // Contar manualmente el número de elementos en el LinkedHashSet
            int count = 0;
            Iterator<String> iterator = listaDePalabras.iterator();
            while (iterator.hasNext()) {
                iterator.next();
                count++;
            }

            tamPosSol += count;
        }

        //Recorremos todas las longitudes
        txtPalEncontradas.setText("Has acertado " + contPalabrasEncontradas + " de " + tamPosSol + " posibles:");

    }

    /**
     * Cargar la lista de letras de PalabraOriginal en letters
     */
    private void llenarListaLetras() {

        letters = new char[wordLength];
        int idx = 0;

        Iterator<Map.Entry<Character, Integer>> iterador = frecuenciaLetrasOriginales.entrySet().iterator();

        while (iterador.hasNext()) {
            Map.Entry<Character, Integer> entrada = iterador.next();

            char letra = entrada.getKey();
            int frecuencia = entrada.getValue();

            for (int i = 0; i < frecuencia; i++) {
                letters[idx] = letra;
                idx++;
            }
        }
    }

    /**
     * Cambia los colores a los background del circulo y las palabras ocultas
     */
    public void actualizarColores() {

        //Seleciconar colores aleatoriamente
        seleccionarColoresActuales();

        //Actualizar color circulo letras
        if (drawableCirculo != null) {
            //Cambiar los colores del degradado
            drawableCirculo.setColors(colores);

            //Aplicar el drawable actualizado al ImageView
            ImageView circuloDeLetras = findViewById(R.id.CirculoDeLetras);
            circuloDeLetras.setBackground(drawableCirculo);
        }
        //Actualizar color palabras ocultas
        if (drawablePalabras != null) {
            //Cambiar los colores del degradado
            drawablePalabras.setColors(colores);

            //Aplicar el drawable actualizado a todos los textView
            for (TextView[] filasPalabra : filasPalabras) {
                for (TextView textView : filasPalabra) {
                    if (textView.getVisibility() == View.VISIBLE) {
                        textView.setBackground(drawablePalabras);
                    }
                }
            }
        }
    }

    /**
     * Selecciona un color aleatorio del mapa de colores
     */
    private void seleccionarColoresActuales() {

        Random random = new Random();

        //Obtener un iterador sobre las entradas del mapa
        Iterator<Map.Entry<Integer, Integer>> iterator = combinacionColores.entrySet().iterator();
        Map.Entry<Integer, Integer> randomEntry = null;
        int currentIndex = 0;

        while (iterator.hasNext()) {
            Map.Entry<Integer, Integer> entry = iterator.next();

            //Utilizar el método de la librería random para seleccionar aleatoriamente
            if (random.nextInt(++currentIndex) == 0) {
                randomEntry = entry; // Seleccionar aleatoriamente la entrada
            }
        }

        // Asignar los colores actuales si se encontró una entrada aleatoria válida
        if (randomEntry != null) {
            colores[0] = randomEntry.getKey();
            colores[1] = randomEntry.getValue();
        }
    }

    /**
     * Mezcla el orden de las letras manteniendo su estado
     *
     * @param view objeto que llama a la funcion
     */
    public void shuffleLetters(View view) {

        // Guardar el estado actual de los botones con letras asignadas
        EstadoBoton[] buttonStates = new EstadoBoton[wordLength];

        Iterator<Button> iterador = letterButtons.iterator();
        Button b;
        int idx = 0;

        while (iterador.hasNext() && idx < wordLength) {
            b = iterador.next();

            EstadoBoton estado = new EstadoBoton(
                    b.getText().toString(),
                    b.getCurrentTextColor(),
                    b.isClickable()
            );
            buttonStates[idx] = estado;

            idx++;
        }

        //Mezclar el estado de los botones usando el algoritmo de mezcla Fisher-Yates
        Random rand = new Random();

        for (int i = buttonStates.length - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);

            //Intercambiar buttonStates[i] con buttonStates[j]
            EstadoBoton temp = buttonStates[i];
            buttonStates[i] = buttonStates[j];
            buttonStates[j] = temp;
        }

        //Asignar los estados mezclados a los botones
        iterador = letterButtons.iterator();
        idx = 0;

        while (iterador.hasNext() && idx < wordLength) {
            b = iterador.next();

            EstadoBoton estado = buttonStates[idx];
            b.setText(estado.getText());
            b.setTextColor(estado.getTextColor());
            b.setClickable(estado.isClickable());

            idx++;
        }
    }

    /**
     * Limpia la palabra actual y reinicia las letras presionadas
     *
     * @param view Objeto que llama a la funcion
     */
    public void botonClear(View view) {
        palabraActual.setText(""); //Palabra vacía


        Iterator<Button> iterador = letterButtons.iterator();
        Button b;

        // Reestablecer botones
        while (iterador.hasNext()) {
            b = iterador.next();

            b.setClickable(true);
            b.setTextColor(Color.WHITE);
        }
    }

    /**
     * Metodo para el boton bonus
     *
     * @param view Objeto que llama a la funcion
     */
    public void botonBonus(View view) {

        // Construye el mensaje para mostrar en el AlertDialog
        StringBuilder message = new StringBuilder();

        Iterator<String> iterador = palabrasEncontradas.iterator();
        String palabra;

        while (iterador.hasNext()) {
            palabra = iterador.next();
            message.append(palabra).append("\n");
        }

        //Crea y muestra el AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Has acertado " + contPalabrasEncontradas + " de " + tamPosSol + " posibles:");
        builder.setMessage(message.toString());

        //Un boton OK para cerrar la ventana
        builder.setPositiveButton("OK", null);

        //Mostrar el AlertDialog en la pantalla
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Añade una letra a la palabra actual
     *
     * @param view Objeto que llama a la funcion
     */
    public void setLetra(View view) {
        //Recuperamos el botón que ha desatado la llamada
        Button btn = (Button) view;

        //Podemos tomar, por ejemplo, el texto del boton
        String letra = btn.getText().toString();

        //Sumamos la letra a la palabra que teníamos hasta la fecha
        palabraActual.setText(palabraActual.getText() + letra.toUpperCase());

        //Modificamos el estado del botón
        btn.setTextColor(Color.DKGRAY);
        btn.setClickable(false);           //Ya no se puede clickar
    }

    /**
     * indique si la palabra2 se puede generar a partir de las letras contenidas
     * en la palabra1.
     *
     * @param palabra1 palabra "principal"
     * @param palabra2 palabra que se quiere generar
     * @return true si se puede generar la palabra2 a partir de palabra1
     */
    private boolean esPalabraSolucio(String palabra1, String palabra2) {
        //Crear un mapa para almacenar la frecuencia de cada letra en palabra1
        Map<Character, Integer> frecuenciaPalabra1 = new HashMap<>(frecuenciaLetrasOriginales);

        //Verificar si palabra2 se puede formar a partir de las letras en palabra1
        for (char letra : palabra2.toCharArray()) {

            Integer count = frecuenciaPalabra1.get(letra);
            if (count == null || count == 0) {
                return false; //La letra no está presente o no hay suficientes
            }

            // Decrementar el contador de la letra
            frecuenciaPalabra1.put(letra, count - 1);
        }

        //Todas las letras en palabra2 están presentes en palabra1 con suficiente frecuencia
        return true;
    }

    /**
     * Inicializa la frecuencia de cada letra en la palabra original.
     *
     * @param palOrig La palabra original para analizar la frecuencia de sus letras.
     */
    private void iniciarFrecuenciaLetras(String palOrig) {
        // Crear un mapa para almacenar la frecuencia de cada letra en palabra1
        frecuenciaLetrasOriginales = new HashMap<>();

        // Iteramos sobre cada letra de la palabra original

        for (int i = 0; i < palOrig.length(); i++) {
            char letra = palOrig.charAt(i); // Obtenemos la letra en la posición i

            // Obtener la frecuencia actual de la letra
            Integer frecuencia = frecuenciaLetrasOriginales.get(letra);

            // Si la letra ya está presente en el mapa, incrementamos su frecuencia en 1
            if (frecuencia != null) {
                frecuenciaLetrasOriginales.put(letra, frecuencia + 1);
            } else {
                // Si no está presente, la agregamos al mapa con frecuencia 1
                frecuenciaLetrasOriginales.put(letra, 1);
            }
        }
    }

    /**
     * Muestra una palabra
     *
     * @param s       palabra que se muestra
     * @param posicio número de fila de las palabras ocultas
     */
    private void mostraParaula(String s, int posicio) {

        //Convertimos el string en un array de caracteres
        char[] arrayChar = s.toCharArray();

        //Recorremos cada letra de la fila
        for (int idx = 0; idx < filasPalabras[posicio].length; idx++) {

            //Escribimos cada letra en la posición adecuada
            filasPalabras[posicio][idx].setText(String.valueOf(arrayChar[idx]));
        }
    }

    /**
     * Muestra la primera letra de una palabra
     *
     * @param s       palabra de la que se muestra la primera letra
     * @param posicio número de fila de las palabras ocultas
     */
    private void mostraPrimeraLletra(String s, int posicio) {
        // Obtenemos la primera posición de la fila que se nos pide modificar
        char[] letras = s.toCharArray();

        //Si intentamos acceder a una fila que no existe -> no accedemos a ninguna
        if (posicio <= maxCantPalabras) {
            filasPalabras[posicio][0].setText(String.valueOf(letras[0]));
        }
    }

    /**
     * Muestra un mennsaje
     *
     * @param s     mensaje a enseñar
     * @param llarg true = mensaje largo; false = mensaje corto (en referencia al tiempo
     *              que se muestra)
     */
    private void mostraMissatge(String s, boolean llarg) {
        int duracion;

        if (llarg) { //Si es largo, ponemos que el tiempo sea largo
            duracion = Toast.LENGTH_LONG;

        } else { //Si es corto, ponemos que el tiempo sea corto
            duracion = Toast.LENGTH_SHORT;
        }

        Toast toast = Toast.makeText(this, s, duracion);
        toast.show();
    }

    /**
     * Reinicia la partida
     *
     * @param view boton que llama a la funcion
     */
    public void botonReiniciar(View view) {
        //Borrar de la pantalla las casillas de las letras escondidas actuales
        empezarPartida();
    }

    /**
     * Habilita todos los hijos de un ConstraintLayout, excepto los botones de Bonus y Reiniciar.
     *
     * @param pariente ID del ConstraintLayout que contiene las vistas a deshabilitar
     */
    private void enableViews(int pariente) {
        // Obtener el ConstraintLayout
        ConstraintLayout layout = findViewById(pariente);

        // Recorrer todos los hijos y habilitarlos
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            child.setEnabled(true);
        }
    }

    /**
     * Deshabilita todos los hijos de un ConstraintLayout, excepto los botones de Bonus y Reiniciar.
     *
     * @param pariente ID del ConstraintLayout que contiene las vistas a deshabilitar
     */
    private void disableViews(int pariente) {
        // Obtener el ConstraintLayout
        ConstraintLayout layout = findViewById(pariente);

        // Recorrer todos los hijos y deshabilitarlos, excepto los botones de Bonus y Reiniciar
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            int childId = child.getId();

            // Verificar si el ID del hijo es el botón de Bonus o Reiniciar
            if (childId != R.id.btnBonus && childId != R.id.btnReiniciar) {
                child.setEnabled(false);
            }
        }
    }

    /**
     * Lee palabras de paraules2.dic y retorna una palabra aleatoria con la misma longitud que
     * wordLength.
     *
     * @return una palabra aleatoria de la longitud especificada
     */
    private String obtenerPalabraOriginal() {
        //Creamos una lista de Strings que contiene las palabras cuya longitud sea wordLength
        LinkedHashSet<String> palMismaLong = new LinkedHashSet<>();

        palabrasValidas = new LinkedHashSet();
        palabrasValidasAcento = new LinkedHashSet();

        try {
            //Abrimos el lector del diccionario
            InputStream is = getResources().openRawResource(R.raw.paraules2);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            String s = "";

            while ((s = reader.readLine()) != null) { //1 iteración = 1 palabra leída

                //Almacenamos la línea leída en un array de caracteres
                char[] linea = s.toCharArray();
                int idxLinea = 0;

                while (linea[idxLinea] != ';') { //Avanzamos el array hasta el ";" (nos saltamos la palabra con acento)
                    idxLinea++;
                }

                //Ahora estamos en el ';' -> la siguiente es la primera posición real
                idxLinea++;

                //Creamos dos arrays para sacar las palabras, uno con acentos y otro sin acentos
                char[] auxSinAcento = new char[linea.length - idxLinea];
                char[] auxAcento = new char[auxSinAcento.length];


                //Misma o menor longitud que wordLength -> palabraValida y puede que misma longitud
                if (auxSinAcento.length <= wordLength) {

                    if (auxSinAcento.length < minCantLetrasPalabras) { //Longitud menor a 3 -> no es válida
                        continue;
                    }

                    //Llenamos el array auxiliar con los valores restantes de la línea (palabra sin acentos)
                    for (int idxAux = 0; idxAux < auxSinAcento.length; idxAux++) {
                        //Escribimos a partir del ';'
                        auxSinAcento[idxAux] = linea[idxLinea];

                        //Escribimos desde el principio de la línea (inicio palabra con acento)
                        auxAcento[idxAux] = linea[idxAux];

                        idxLinea++;
                    }

                    //Misma longitud -> posible elejida
                    if (auxSinAcento.length == wordLength) {
                        palMismaLong.add(new String(auxSinAcento));
                    }

                    //Es menor o igual -> candidata a poder ser formada por las de longitud wordLength
                    palabrasValidas.add(new String(auxSinAcento));
                    palabrasValidasAcento.add(new String(auxAcento));
                }
            }

        } catch (EOFException ex) {
            //Al llegar al final del fichero, no hacemos nada (ya hemos leído todo)

        } catch (FileNotFoundException ex) {
            Log.d("FileNotFoundException", "No se ha encontrado el archivo ");

        } catch (IOException ex) {
            Log.d("IOException", "Error al leer la línea");
        }

        //Ya hemos leído todas las palabras y almacenado en palMismaLong todas las palabras con la misma longitud
        //Escojemos una aleatoriamente y la devolvemos
        Random rand = new Random();

        Iterator<String> it = palMismaLong.iterator();
        int limite = 0;
        while (it.hasNext()) {
            it.next();
            limite++;
        }
        //Reiniciamos iterador
        String resultado = null;
        it = palMismaLong.iterator();
        for (int i = 0; i <= rand.nextInt(limite); i++) {
            resultado = it.next();
        }

        return resultado;
    }

    /**
     * Separa las palabras válidas y las organiza en el mapa palabrasPorLongitud
     */
    private void separarPalabrasValidasPorLongitud() {
        //Instanciamos el mapa
        palabrasPorLongitud = new HashMap<>();

        Iterator<String> iterador = palabrasValidas.iterator();
        String palabra;

        // Clasificamos las palabras según las letras que tienen
        while (iterador.hasNext()) {
            palabra = iterador.next();

            int longitud = palabra.length();

            // Obtener la lista correspondiente a esta longitud
            LinkedHashSet<String> listaDePalabras = palabrasPorLongitud.get(longitud);

            // Si la lista es null, significa que no hay ninguna lista para esta longitud, entonces la creamos
            if (listaDePalabras == null) {
                listaDePalabras = new LinkedHashSet();
                palabrasPorLongitud.put(longitud, listaDePalabras); // Añadimos la lista asociada a esa longitud
            }

            // Añadimos la palabra a la lista
            listaDePalabras.add(palabra);
        }
    }

    /**
     * Introduce en posiblesSoluciones todas las palabras que pueden ser formadas por palOrig,
     * separadas por longitud
     *
     * @param palOrig La palabra de la cual se obtienen las posibles soluciones.
     * @return Mapa donde claves son longitudes y valores son listas de palabras que se pueden formar
     */
    private Map<Integer, LinkedHashSet<String>> obtenerPosiblesSoluciones(String palOrig) {
        //Instanciamos posiblesSoluciones
        posiblesSoluciones = new HashMap<>();

        Iterator<Map.Entry<Integer, LinkedHashSet<String>>> iterador = palabrasPorLongitud.entrySet().iterator();
        Map.Entry<Integer, LinkedHashSet<String>> entrada;

        //Iteramos sobre cada entrada (contiene longitud y Lista con las palabras) de palabrasPorLongitud
        while (iterador.hasNext()) {
            entrada = iterador.next();

            int longitud = entrada.getKey();
            Set<String> palabrasEstaLong = entrada.getValue();


            Iterator<String> iterPal = palabrasEstaLong.iterator();
            String palabraActual;

            //Iteramos sobre cada palabra de esta longitud
            while (iterPal.hasNext()) {

                palabraActual = iterPal.next();

                //Si se puede formar con las letras de la palabra Original la añadimos a la lista
                if (esPalabraSolucio(palOrig, palabraActual)) {

                    LinkedHashSet<String> listaPalabras = posiblesSoluciones.get(longitud);

                    if (listaPalabras == null) {
                        //Si no hay una lista para esta longitud -> la creamos
                        listaPalabras = new LinkedHashSet();
                        posiblesSoluciones.put(longitud, listaPalabras);

                    }

                    // Si no ha sido metida anteriormente -> la metemos
                    if (!listaPalabras.contains(palabraActual)) {
                        listaPalabras.add(palabraActual);
                    }
                }

            }
        }
        return posiblesSoluciones;
    }

    /**
     * Selecciona por orden de longitud y alfabético las palabras que habrá que descubrir y
     * las almacena en palabrasSolucion
     */
    private void obtenerPalabrasOcultas() {
        palabrasOcultas = new LinkedHashSet();
        Random rand = new Random();

        int pos;

        //Intentamos seleccionar al menos una palabra de cada longitud (3 a 7)
        for (int longitud = minCantLetrasPalabras; longitud <= maxCantLetrasPalabra; longitud++) {
            // Verificar si existen palabras para la longitud actual

            LinkedHashSet<String> palabrasLongitudActual = posiblesSoluciones.get(longitud);

            if (palabrasLongitudActual != null) {
                Iterator<String> iterador = palabrasLongitudActual.iterator();
                int cantPal = 0;

                //Calculamos la cantidad de palabras que hay
                while (iterador.hasNext()) {
                    iterador.next();
                    cantPal++;
                }

                // Escojemos una aleatoria (obtenemos la lista de esta longitud y miramos su size)
                pos = rand.nextInt(cantPal);

                String pal = "-1";

                iterador = palabrasLongitudActual.iterator();
                while (iterador.hasNext()) {
                    String temp = iterador.next();

                    if (pos == 0) {
                        pal = temp;
                        break;
                    }
                    pos--;
                }

                // Añadimos la palabra al resultado si es diferente a -1
                if (!pal.equals("-1")) {
                    palabrasOcultas.add(pal);
                    cantPalabrasOcultas++;
                }
            }
        } //Hemos seleccionado 1 de cada longitud que sea posible


        //Ahora rellenamos los espacios que quedan con palabras de longitud 3
        LinkedHashSet<String> palLong3 = posiblesSoluciones.get(minCantLetrasPalabras);

        //Iterador para recorrer las palabras de los sets que sean necesarios
        Iterator<String> iterador;

        if (palLong3 != null) { //Si existen palabras de longitud 3

            //Obtenemos la lista de longitud 3
            int cantPalLong3 = 0;
            iterador = palLong3.iterator();

            int idx = 0;

            while (iterador.hasNext()) {
                iterador.next();
                cantPalLong3++;
            }

            //Creamos una lista con los índices
            int[] indices = new int[cantPalLong3];

            //Iniciamos y aleatorizamos los índices
            FisherYates(indices);

            //Recorremos cada palabra de longitud 3
            while (cantPalabrasOcultas < maxCantPalabras && idx < cantPalLong3) {

                int posicion = indices[idx];

                iterador = palLong3.iterator();

                String palabra = "-1";

                while (iterador.hasNext()) {
                    String temp = iterador.next();

                    if (posicion == 0) {
                        palabra = temp;
                    }
                    posicion--;
                }

                //Nos aseguramos de que la palabra no se ha introducido anteriormente
                if (!palabrasOcultas.contains(palabra) && !palabra.equals("-1")) {
                    palabrasOcultas.add(palabra);
                    cantPalabrasOcultas++;
                }
                idx++;
            }
        }

        //Creamos un mapa auxiliar para almacenar las palabras seleccionadas y su longitud
        TreeMap<Integer, TreeSet<String>> auxOrdenado = new TreeMap<>();

        iterador = palabrasOcultas.iterator();
        String palabra;

        while (iterador.hasNext()) {
            palabra = iterador.next();
            int longitud = palabra.length();

            //Obtenemos la lista de palabras de esta longitud
            TreeSet<String> listaDePalabras = auxOrdenado.get(longitud);

            //No existe -> la creamos
            if (listaDePalabras == null) {
                listaDePalabras = new TreeSet();
                auxOrdenado.put(longitud, listaDePalabras);
            }

            //Añadimos la palabra
            listaDePalabras.add(palabra);
        }


        //Creamos una última lista para almacenar todas las palabras
        LinkedHashSet<String> auxFinal = new LinkedHashSet();


        Iterator<Map.Entry<Integer, TreeSet<String>>> iter = auxOrdenado.entrySet().iterator();
        Map.Entry<Integer, TreeSet<String>> entrada;

        //Recorremos las entradas del mapa ordenado por longitud
        while (iter.hasNext()) {
            entrada = iter.next();

            //Iterador para recorrer cada palabra de esta longitud
            iterador = entrada.getValue().iterator();

            while (iterador.hasNext()) {
                //Los almacenamos por orden alfabético (provienen de un TreeSet)
                auxFinal.add(iterador.next());
            }
        }

        //Asignar el resultado ordenado de vuelta a palabrasOcultas
        palabrasOcultas = auxFinal;


        //Imprimir el resultado usando Log.d
        /*Log.d("FINAAAAAAAAAAL", "Las palabras seleccionadas son:");
        for (String pal : palabrasOcultas) {
            Log.d("FINAAAAAAAAAAL", pal);
        }*/

        //Para evitar un error al darle al botón send
        palabrasEncontradas = new TreeSet();
    }

    /**
     * Introduce una palabra en el sistema y se evalúa si es correcta
     *
     * @param view Objeto que llama la funcion
     */
    public void botonSend(View view) {
        //Guardamos la palabra introducida por el usuario
        String palIntroducida = palabraActual.getText().toString();
        palIntroducida = palIntroducida.toLowerCase();

        //Al acabar el proceso, reestablecemos las teclas y limpiamos el texto de la palabra
        botonClear(new View(this));

        //Si es menor a 3 o mayor a 7 -> palabra no válida
        if (palIntroducida.length() < minCantLetrasPalabras || palIntroducida.length() > maxCantLetrasPalabra) {
            mostraMissatge("Palabra no válida!", false);
        } else { //Longitud adecuada
            //Introducimos la palabra y comprobamos
            introducirPalabra(palIntroducida);

            //Si hemos encontrado todas -> hemos ganado
            if ((cantPalabrasOcultas - cantPalOcultEncontradas) == 0) {
                mostraMissatge("Enhorabuena! Has ganado!", true);

                //Desactivamos el poder interaccionar con todos los botones
                disableViews(R.id.layoutPrincipal);
            }
        }
    }

    /**
     * Maneja la palabra introducida por el usuario
     * Verifica si es una palabra oculta, una posible solución o una palabra válida.
     *
     * @param palIntroducida La palabra introducida por el usuario.
     */
    private void introducirPalabra(String palIntroducida) {
        //Si la palabra ya ha sido introducida
        if (palabrasEncontradas.contains(palIntroducida)) {

            mostraMissatge("Esta ya la tienes!", false);

            //Enseñamos la palabra en rojo en el contador
            ultimaPalRepetida = palIntroducida;

        } else { //Si aún no se ha introducido -> miramos si es correcta

            int pos = -1;

            Iterator<String> it = palabrasOcultas.iterator();
            int idx = 0;

            while (it.hasNext()) {
                String aux = it.next();
                Log.d("PALABRA OCULTA NÚMERO", "PAL OCULTA Nº " + String.valueOf(idx) + " = " + aux);
                if (palIntroducida.equals(aux)) {
                    pos = idx;
                    break; // Salimos del bucle una vez encontrada la palabra
                }
                idx++;
            }

            //Mayor o igual a 0 -> se ha encontrado una palabra que coincide
            if (pos >= 0) {
                Log.d("POSICIÓN = ", String.valueOf(pos));
                mostraParaula(palIntroducida, pos);

                //La añadimos a la lista de palabrasEncontradas
                palabrasEncontradas.add(palIntroducida);
                contPalabrasEncontradas++;


                //Mostrar mensage
                mostraMissatge("Enhorabuena! Era una palabra oculta! ", false);

                //Eliminamos la palabra de palabrasOcultas mediante el string "." ya que nunca será escrito
                //Creamos un set auxiliar para actualizar palabrasOcultas
                Iterator<String> iterador = palabrasOcultas.iterator();
                LinkedHashSet<String> setAuxiliar = new LinkedHashSet();
                String palOculta;

                //Recorremos cada palabra oculta
                while (iterador.hasNext()) {
                    palOculta = iterador.next();

                    //Si es la posición de la palabra que hemos encontrado, metemos un "."
                    if (pos == 0) {
                        switch(cantPalOcultEncontradas){
                            case 0: setAuxiliar.add("."); break;
                            case 1: setAuxiliar.add(","); break;
                            case 2: setAuxiliar.add(";"); break;
                            case 3: setAuxiliar.add(":"); break;
                            case 4: setAuxiliar.add("_"); break;
                        }

                    } else {
                        //La palabra no es la encontrada
                        setAuxiliar.add(palOculta);
                    }
                    pos--;
                }

                //Ahora palabrasOcultas tiene un "." en la palabra encontrada
                palabrasOcultas = setAuxiliar;
                cantPalOcultEncontradas++; //Incrementamos el contador

                //palabrasOcultas.set(pos, "."); //Mantenemos la posición para mantener el orden original -> nos indica donde escribir la palabra

            } else { //No se ha encontrado -> puede ser una solución posible

                //Obtenemos la lista de posiblesSoluciones de longitud igual a la palabra introducida
                LinkedHashSet<String> tempSolucion = posiblesSoluciones.get(palIntroducida.length());


                //Para saber qué debemos comprobar y qué no
                boolean yaEncontrada = false;

                //Iterador para recorrer los Set que contienen String
                Iterator<String> iterador;

                //Si existen posibles soluciones para esta longitud
                if (tempSolucion != null) {
                    iterador = tempSolucion.iterator();
                    String posibleSol;

                    //Recorremos todas las palabras de la lista
                    while (iterador.hasNext()) {
                        posibleSol = iterador.next();

                        //Si la palabra es una solución pero no es una palabra oculta
                        if (palIntroducida.equals(posibleSol)) {

                            //La añadimos a palabras encontradas
                            palabrasEncontradas.add(palIntroducida);
                            contPalabrasEncontradas++;

                            //Sumamos 1 al bonus
                            cantBonus++;
                            actualizarCantidadBonus();

                            mostraMissatge("Palabra válida! Tienes un bonus más.", false);

                            yaEncontrada = true;
                            break; //Salimos de la búsqueda
                        }

                    }//Fin de la lista de posibles Soluciones
                }

                //Si no es una posible solución -> miramos si es válida
                if (!yaEncontrada) {

                    //Obtenemos la lista de palabras válidas de longitud igual a la palabra introducida
                    LinkedHashSet<String> tempValida = palabrasPorLongitud.get(palIntroducida.length());

                    iterador = tempValida.iterator();
                    String palValida;

                    //Recorremos cada palabra válida de dicha longitud
                    while (iterador.hasNext()){
                        palValida = iterador.next();

                        //Si es igual a la introducida -> la palabra introducida es válida
                        if (palIntroducida.equals(palValida)) {
                            yaEncontrada = true;
                            break;
                        }

                    } //Fin del recorrido

                    //Si seguimos sin haber encontrado ninguna igual -> la palabra no es válida
                    if (!yaEncontrada) {
                        mostraMissatge("Palabra no válida!", false);
                    }
                }
            }
            //No es repetida -> no hay ninguna en rojo
            ultimaPalRepetida = ".";
        }
        //Antes de salir, actualizamos las palabras encontradas
        setTextoPalEncontradas();
    }

    /**
     * Añade las palabras adivinadas al TextView de arriba de la pantalla
     */
    private void setTextoPalEncontradas() {

        String txtOriginal = ("Has acertado " + contPalabrasEncontradas + " de " + tamPosSol + " posibles: ");

        //Mensaje a añadir al txtOriginal
        StringBuilder palEncontradasFormateadas = new StringBuilder();

        Iterator<String> iterador = palabrasEncontradas.iterator();
        String palabra;

        //Añadimos todas las palabras separadas por comas
        while (iterador.hasNext()) {
            palabra = iterador.next();

            //Es la palabra en rojo -> la formateamos a color rojo <font color = ’red ’>red </ font >
            if (palabra.equals(ultimaPalRepetida)) {
                //Añadimos los comandos de HTML para pintar en rojo
                String aux = "<font color = 'red'>";
                aux += palabra;
                aux += "</font>";

                palabra = aux;
            }

            palEncontradasFormateadas.append(palabra).append(", ");
        }

        String resultadoFinal = txtOriginal + palEncontradasFormateadas;

        //Ponemos el texto en formato HTML para poder imprimirlo en rojo
        txtPalEncontradas.setText(Html.fromHtml(resultadoFinal, Html.FROM_HTML_MODE_LEGACY));
    }

    //Algoritmo de FisherYates para crear índices aleatorios
    private void FisherYates(int[] array) {
        //Creamos los índices
        for (int idx = 0; idx < array.length; idx++) {
            array[idx] = idx;
        }

        //Implementar el algoritmo de mezcla Fisher-Yates
        Random rand = new Random();
        for (int i = array.length - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            // Intercambiar indices[i] con indices[j]
            int temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
    }

    /**
     * Se revela la primera letra de alguna palabra cuya primera letra no se vea a cambio de 5 bonus
     */
    public void botonAyuda(View view) {

        //Si tiene menos de 5 bonus, no se hace nada
        if (cantBonus < puntParaAyuda) {
            int necesarios = puntParaAyuda - cantBonus;
            mostraMissatge("Te faltan " + necesarios + " puntos!", false);
        } else { //Enseñamos la primera letra de una palabra oculta

            // Crear un array de enteros para los índices
            int[] indices = new int[filasPalabras.length];
            FisherYates(indices);


            boolean fin = false;
            
            Iterator<String> iterator = palabrasOcultas.iterator();
            String palabraOculta = null;

            // Recorremos las palabras en orden aleatorio
            for (int idxPalabra = 0; idxPalabra < filasPalabras.length; idxPalabra++) {
                int idxAleatorio = indices[idxPalabra]; //Almacenamos el índice aleatorio

                //Recorremos cada palabra en orden aleatorio
                if (filasPalabras[idxAleatorio] != null && iterator.hasNext()) { //Si la palabra existe -> miramos sus letras
                    palabraOculta = iterator.next();

                    //Recorremos cada letra
                    for (TextView textView : filasPalabras[idxAleatorio]) { //Si la letra existe -> miramos su valor
                        if (textView.getText() == "") { //Si es "" -> llenamos la primera posición

                            //Mostramos la primera letra del string oculto en la posición del índice aleatorio
                            mostraPrimeraLletra(palabraOculta, idxAleatorio);

                            fin = true; //Ya hemos llenado una -> acabamos la ejecución de la función
                        }
                        //else -> está llena -> pasamos a la siguiente palabra
                        break;
                    }

                    //Antes de acabar, restamos la puntuación pertinente
                    if (fin) {
                        cantBonus -= puntParaAyuda;
                        actualizarCantidadBonus();
                        break;
                    }
                }
            }
        }
    }

    /**
     * Actualiza el número encima de bonus con la cantidad de bonus que se tienen ahora mismo
     */
    private void actualizarCantidadBonus() {

        //Cojemos el botón de bonus
        Button botonBonus = findViewById(R.id.btnBonus);

        String texto = "";
        texto = texto + cantBonus;

        //Añadimos y ajustamos el texto
        botonBonus.setText(texto);
        botonBonus.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        botonBonus.setTextSize(19);
    }
}


