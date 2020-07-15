package analizador;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Analizador {

    public static void main(String[] args) {
        //definimos las variables necesarias para poder leer el archivo 
        File archivo = null;
        FileReader fr = null;
        BufferedReader br = null;
        int ErrorL = 0;
        
        //Declaramos algunos ArrayList que nos serviran
        ArrayList<Integer> ListaErrores = new ArrayList<Integer>();
        ArrayList<Integer> ErrorLinea = new ArrayList<Integer>();
        ArrayList<String> tabla = new ArrayList<String>();
        ArrayList<String> TVariable = new ArrayList<String>();
        ArrayList<String> bloques = new ArrayList<String>();
        ArrayList<Integer> LineaBloque = new ArrayList<Integer>();
        ArrayList<String> Codigo = new ArrayList<String>();
        //definimos un objeto para poder hacer la llamada de los metodos
        Analizador metodo = new Analizador();
        Generacion GenCodigo = new Generacion();
        int variable = -1;
        String separadas[];
        int v = 0;
        boolean declarando = true;

        //Definimos la tabla con todos los errores posibles
        String[] errores = new String[25];
        errores[0] = "faltan datos o la sentencia esta mal escrita";
        errores[1] = "caracter no valido";
        errores[2] = "token no valido";
        errores[3] = "falta la etiqueta de inicio de variables o esta mal escrita";
        errores[4] = "ID no escrito correctamente";
        errores[5] = "Error en la declaracion de variables";
        errores[6] = "ID no declarado";
        errores[7] = "sintaxis incorrecta en la sentencia SCAN";
        errores[8] = "sintaxis incorrecta en la sentencia PRINTLN";
        errores[9] = "sintaxis incorrecta en la sentencia FOR";
        errores[10] = "sintaxis incorrecta en la expresion WHILE";
        errores[11] = "sintaxis incorrecta en la sentencia IF";
        errores[12] = "no esta bien hecha la asignacion ID no declarado";
        errores[13] = "la linea no esta declarando ninguna sentencia";
        errores[14] = "ID no puede ser una palabra reservada";
        errores[15] = "ID repetido";
        errores[16] = "se esperaba una llave que abre el bloque ";
        errores[17] = "Imcompatibilidad de tipos en las variables";
        errores[18] = "Sintaxis incorrecta en la asignacion";
        errores[19] = "La condicion de incremento o decremento de la sentencia FOR esta mal escrita";
        errores[20] = "Todos los ID tienen que ser el mismo";

        //Definimos el alfabeto, el lenguaje y las reglas de produccion del compilador mediante expresiones regulares 
        String ID = "(^[A-Z][A-Z 0-9 _]*)";
        String alfabeto = "[A-Z 0-9 \\. \\, \\{ \\} \\( \\) \" \\' \\< \\> \\= ! \\; \\: \\+ \\- \\* \\/ \\s \\_ ]*";
        String lenguaje = "([ " + ID + "|PRINTLN|SCAN|IF|FOR|WHILE|ENT|CAD|FLOT|VARIABLES|PROGRAMACION|:|;|.|,|{|}|(|)|\"|\\'|<|>|=|!|\\+|\\-|\\*|\\/|\\_]+)";
        String reservadas = "(PRINTLN|SCAN|IF|FOR|WHILE|ENT|CAD|FLOT|VARIABLES|PROGRAMACION|:|;|.|,|{|}|(|)|\"|\\'|<|>|=|==|!|+|-|*|\\_)";
        String alfa = "[^A-Z 0-9 . , { } ( ) \" \\' < > \\= \\! \\; \\: \\+ \\- \\* \\/ \\s \\_ ]*";
        String letra = "([A-Z]+)";
        String digito = "([0-9]+)";
        String tipo = "(ENT|CAD|FLOT)";
        String declaracion = "(^" + ID + "\\s\\:\\s" + tipo + "\\s\\;$)";
        String ODI = "(\\--|\\++)";
        String OA = "([\\+|\\-|\\*|\\/])";
        String OP = "(\\<|\\>|\\==|\\<=|\\>=|!=)";
        String guion = "(\\-|\\_)";
        String entero = "([0-9]+)";
        String flotante = "([0-9]+\\.[0-9]+)";
        String cadena = "([A-Z]+|[0-9]+|" + guion + ")";
        String constante = "(" + entero + "|" + flotante + "|" + cadena + ")";
        String valor = "([" + ID + "]+|[" + constante + "]+)";
        String asignacion = "([" + ID + "]+\\s\\=\\s[" + valor + "]+\\s\\;)";
        String expresion = "([" + ID + "]+\\s\\=\\s[" + valor + "]+\\s" + OA + "\\s[" + valor + "]+\\s\\;)";
        String scan = "(^SCAN\\s\\'\\s[" + ID + "]+\\s\\'\\s\\;)";
        String println = "(^PRINTLN\\s\\(\\s[[\\'[" + alfabeto + "|" + alfa + "]*\\']|[" + ID + "]*]*\\s\\)\\s\\;)";
        String FOR = "(^FOR\\s\\(\\s[" + ID + "]+\\s\\=\\s[" + entero + "|" + ID + "]+\\s\\;\\s[" + ID + "|" + constante + "]+\\s" + OP + "\\s[" + constante + "|" + ID + "]+\\s\\;\\s[" + ID + "]+\\s" + ODI + "\\s\\)\\s\\{[\\s\\}]*)";
        String IF = "(^IF\\s\\(\\s[" + ID + "|" + entero + "|" + flotante + "]+\\s" + OP + "\\s[" + ID + "|" + entero + "|" + flotante + "]+\\s\\)\\s\\{[\\s\\}]*)";
        String WHILE = "(^WHILE\\s\\(\\s[" + ID + "|" + entero + "|" + flotante + "]+\\s" + OP + "\\s[" + ID + "|" + entero + "|" + flotante + "]+\\s\\)\\s\\{[\\s\\}]*)";

        //intentamos leer el archivo
        try {
            //definiendo la ruta del archivo
            archivo = new File("src\\analizador\\archivo.txt");
            //leemos el archivo y extraemos el contenido del archivo
            fr = new FileReader(archivo);
            br = new BufferedReader(fr);
            String linea;
            //mientras siga existiendo una linea por leer en el archivo, se haran los procesos necesarios
            while ((linea = br.readLine()) != null) {
                //Quitamos todos los espacios de inicio y fin de la cadena
                linea = linea.trim();
                //Si la linea en el archivo no contiene ningun caracter, se salta a la siguiente linea
                if (linea.isEmpty()) {
                    continue;
                }
                ErrorL++;
                v++;
                
                //Verificamos si se trata de la etiqueta de las sentencias, si se encuentra, significa que ya no se declararan mas variables
                if (linea.matches("PROGRAMACION")) {
                    declarando = false;

                }
                //mandamos llamar al metodo para comprobar las sentencias del archivo
                metodo.checa(linea, alfabeto, ErrorL, lenguaje, variable, tabla, TVariable, bloques, LineaBloque, ListaErrores, ErrorLinea, tipo, v, declarando, alfa, reservadas, letra, digito, ID, declaracion, asignacion, expresion, ODI, OA, OP, guion, entero, flotante, cadena, constante, valor, scan, println, FOR, IF, WHILE);
                if (declarando == true) {
                    variable++;
                }
                
                Codigo.add(linea);
                System.out.println("");

            }

            //Si la lista de errores esta vacia, procedemos a generaro codigo intermedio
            if (ListaErrores.isEmpty() && bloques.isEmpty()) {
                System.out.println("No se encontraron errores");
                GenCodigo.Genera();
            } else {
                //de lo contrario, se muestran los errores encontrados
                System.out.println("Errores encontrados: ");
                for (int i = 0; i < ListaErrores.size(); i++) {
                    System.out.println("Error en la linea " + ErrorLinea.get(i) + ", " + errores[ListaErrores.get(i)]);
                }
            }
            //si la lista de bloques de sentencias no esta vacia, se muestran los bloques que no han sido cerrados
            if (!bloques.isEmpty()) {
                for (int i = 0; i < bloques.size(); i++) {
                    System.out.println("Falto cerrar el bloque " + bloques.get(i) + " de la linea " + LineaBloque.get(i));
                }
            }
            //si ocurre un error, se atrapa en una excepcion
        } catch (Exception e) {
            System.out.println("Error, faltan datos o la sentencia esta mal escrita ");
        } finally {
            //nos aseguramos de cerrar el archivo para que no existan errores
            try {
                if (fr != null) {
                    fr.close();
                }
            } catch (Exception e2) {
                System.out.println("Error con el contenido del archivo");
            }
        }
    }

    //metodo que verifica que la linea extraida respectivamente, sea correcta
    public void checa(String cadena, String alfabeto, int linea, String lenguaje, int variable, ArrayList<String> tabla, ArrayList<String> TVariable, ArrayList<String> bloques, ArrayList<Integer> LineaBloque, ArrayList<Integer> ListaErrores, ArrayList<Integer> ErrorLinea, String tipo, int v, boolean declarando, String alfa, String reservadas, String letras, String digito, String ID, String declaracion, String asignacion, String expresion, String ODI, String OA, String OP, String guion, String entero, String flotante, String cadenas, String constante, String valor, String scan, String println, String FOR, String IF, String WHILE) {
        //Indicamos la Expresion Regular
        Pattern patron;
        Matcher macher;
        //hacemos la comparacion de la cadena con el patron a comprobar
        patron = Pattern.compile(alfabeto);
        //definimos algunas variables para poder hacer algunos procesos necesarios
        boolean bien1 = true;
        char aux;
        String letra;
        int c = 0;
        boolean SB = false, VB = false;
        StringTokenizer token;

        try {
            //imprimimos la linea a evaluar en turno
            System.out.println("Linea " + linea + ": " + cadena);
            //recorremos la linea y extraemos caracter por caracter para poder comprobar que formen parte del alfabeto
            for (int i = 0; i < cadena.length(); i++) {
                //extraemos el caracter a evaluar
                aux = cadena.charAt(i);
                letra = "" + aux;
                macher = patron.matcher(letra);
                //si concuerda con la expresion regular es correcto
                if (macher.matches()) {
                    //en el caso de las cadenas de caracteres comprobamos que lo que ingresemos tiene que ser correcto, puesto que debemos permitir lo que sea del teclado
                    if (letra.matches("'")) {
                        c++;
                    }
                    //si no concuerda con el alfabeto, se mostrara el error
                } else {
                    //si solo hemos encontrado una comilla significa que todo que lo siga forma parte de la cadena de caracteres, por lo tanto que tenemos que aceptar todo mientras este dentro
                    if (c == 1) {
                    } else {
                        //en caso de que no concuerde con la cadena de caracteres, es un error
                        bien1 = false;
                        //System.out.println("Error lexico en la linea " + linea + " columna " + (i + 1) + " caracter no valido : " + letra);
                        ListaErrores.add(1);
                        ErrorLinea.add(linea);

                    }
                    //si encontramos la segunda comilla, significa que acabo la cadena de caracteres y hacemos que lo demas que sea que no es del alfabeto sea erroneo
                    if (c == 2) {
                        c = 0;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error Lexico");
        }
        //ahora comprobamos los tokens contra el lenguaje
        patron = Pattern.compile(lenguaje);
        //separamos por tokens (palabras) la linea correspondiente al archivo para poder comprobar que son parte del lenguaje
        String separadas[] = new String[1000];
        int contador = 0, cuantas = 0;
        token = new StringTokenizer(cadena, " ");
        cuantas = token.countTokens();

        //Se verifica el numero de palabras en la declaracion de variables, si no son las correctas es un error
        if (declarando == true) {
            if (v >= 2) {
                if (cuantas > 4) {
                    ListaErrores.add(5);
                    ErrorLinea.add(linea);
                }
            }
        }

        //separamos y contamos las palabras en la linea en turno
        while (token.hasMoreTokens()) {
            separadas[contador] = token.nextToken();
            contador++;
        }

        int palabras;
        boolean bien2 = true;
        //recorremos el arreglo de los tokens
        for (int i = 0; i < contador; i++) {
            //comprobamos que sea parte del lenguaje
            macher = patron.matcher(separadas[i]);
            //si no concuerda con el patron (lenguaje) es un error
            if (!macher.matches()) {
                //comprobamos si se trara de una cadena de caracteres
                char a = separadas[i].charAt(0);
                String signo = "" + a;
                //si no es una cadena de caracteres, mostramos el error, de lo contrario, debemos aceptar lo que sea como parte de la cadena de caracteres
                if (!signo.matches("'")) {
                    bien2 = false;
                    //System.out.println("Error lexico en la linea " + linea + " Token no valido : " + separadas[i]);
                    ListaErrores.add(2);
                    ErrorLinea.add(linea);
                }
            }
        }
        //Creamos una variable en donde guardaremos la sentencia que evaluaremos
        String auxiliar, auxiliar2;
        boolean VE = false;
        //Verificamos si estamos en la declaracion de variables
        if (declarando == true) {
            if (v == 1) {
                //Corroboramos de que se encuentre la etiqueta de variables en la primer linea del programa
                if (separadas[0].matches("(VARIABLES)")) {
                    if (cuantas > 1) {
                        ListaErrores.add(3);
                        ErrorLinea.add(linea);
                    }
                } else {
                    //Si no se encuentra correctamente bien escrito o en la primera linea, error de sintaxis
                    //System.out.println("Error de sintaxis, falta la etiqueta de inicio de variables o esta mal escrita");
                    ListaErrores.add(3);
                    ErrorLinea.add(linea);
                }
            }
            if (v >= 2) {
                //Apartir de la segunda linea comprobamos si se encuentra la etiqueta de programacion, significa que ya no estara declarando variables y comenzara a escribir las sentencias del programa
                if (declarando == false) {

                } else {
                    try {
                        //Verificamos que el ID no sea una palabra reservada
                        if (separadas[0].matches("SCAN|PRINTLN|IF|FOR|WHILE|ENT|FLOAT|CAD|VARIABLES|PROGRAMACION")) {
                            ListaErrores.add(14);
                            ErrorLinea.add(linea);
                            variable--;
                        } else {
                            if (separadas[2].matches(tipo)) {
                                //Ahora comprobamos que el ID fue escrito de manera correcta
                                if (separadas[0].matches(ID)) {
                                    if (v >= 3) {
                                        //Comprobamos si el ID ya existe
                                        ChecaID(variable, separadas[0], tabla);
                                        if (esta == true) {
                                            //Si ya existe, error
                                            ListaErrores.add(15);
                                            ErrorLinea.add(linea);
                                            variable--;
                                        } else {
                                            //Si no existe el ID, se agrega
                                            tabla.add(separadas[0]);
                                            TVariable.add(separadas[2]);
                                            VB = true;
                                        }
                                    } else if (v == 2) {
                                        //Verificamos si es el primer ID declarado, se agrega a la tabla de simbolos
                                        tabla.add(separadas[0]);
                                        TVariable.add(separadas[2]);
                                        VB = true;
                                    }
                                } else {
                                    //Si no fue escrito correctamente siguiendo las reglas, error
                                    ListaErrores.add(4);
                                    ErrorLinea.add(linea);
                                }
                            } else {
                                //Si no fue bien hecha la declaracion de la variable, error
                                ListaErrores.add(5);
                                ErrorLinea.add(linea);
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Error en las variables");
                        ListaErrores.add(c);
                        ErrorLinea.add(linea);
                    }

                }
            }
            esta = false;
        } else {
            //Si no estamos declarando variables, significa que vamos a evaluar las posibles sentencias correctas que se comprobaran a continuacion
            //Asignamos la primer palabra (que debe de ser la sentencia a comprobar) para poder hacer las operaciones necesarias 
            try {
                auxiliar = separadas[0];
                //Si no estamos declarando variables, verificamos si se trata de la etiqueta de las sentencias
                if (declarando == false) {
                    if (separadas[0].matches("PROGRAMACION")) {
                        SB = true;
                    }
                }

                //Si la palabra concuerda con alguna de las posibles sentencias, se debera de comprobar la sintaxis de la linea completa
                if (auxiliar.matches("SCAN")) {
                    SB = true;
                    //Comprobamos que el ID fue declarado, si no fue declarado, error
                    try {
                        ChecaID(variable, separadas[2], tabla);
                        //Si se declaro la variable, es correcto
                        if (esta == true) {
                            VE = true;
                        } else {
                            //Si no se declaro la variable, error
                            ListaErrores.add(6);
                            ErrorLinea.add(linea);
                        }
                        //Si la sintaxis de la linea esta mal o el ID no fue declarado, error
                        if (!cadena.matches(scan)) {
                            ListaErrores.add(7);
                            ErrorLinea.add(linea);
                        }
                    } catch (Exception e) {
                        ListaErrores.add(7);
                        ErrorLinea.add(linea);
                    }
                }
                esta = false;
                VE = false;
                //Si la palabra concuerda con la sentencia se comprobara la sintaxis de la linea
                if (auxiliar.matches("PRINTLN")) {
                    SB = true;
                    char a1;
                    String caracter;
                    int count = 0;
                    //Se comprueba si se trata de imprimir una constante, o un ID
                    for (int i = 0; i < cadena.length(); i++) {
                        a1 = cadena.charAt(i);
                        caracter = "" + a1;
                        if (caracter.matches("'")) {
                            count++;
                        }
                    }
                    //Si encuentra las comillas, se trata de una constante de tipo cadena
                    if (count == 2) {
                        //Si no concuerda la sintaxis de la linea, error
                        if (!cadena.matches(println)) {
                            ListaErrores.add(8);
                            ErrorLinea.add(linea);
                        }
                    }
                    //Si no encuentra ninguna comilla, se trata de un ID
                    if (count == 0) {
                        //Se comprueba la sintaxis
                        if (cadena.matches(println)) {
                            //se comprueba si la variable fue declarada
                            ChecaID(variable, separadas[2], tabla);
                            if (esta == false) {
                                //Si no se encuentra, se comprueba si es un entero o flotante
                                if (!separadas[2].matches(entero)) {
                                    if (!separadas[2].matches(flotante)) {
                                        //error, ID no declarado
                                        ListaErrores.add(6);
                                        ErrorLinea.add(linea);
                                    }
                                }
                            }
                         //si la sintaxis no concuerda, error   
                        } else {
                            ListaErrores.add(8);
                            ErrorLinea.add(linea);
                        }
                    }
                    //si el numero de comillas es diferente a 0 o 2, error
                    if (count != 2 && count != 0) {
                        ListaErrores.add(8);
                        ErrorLinea.add(linea);
                    }
                }
                esta = false;
                //Si la palabra concuerda con la sentencia se comprobara la sintaxis de la linea
                if (auxiliar.matches("FOR")) {
                    //System.out.println("Checando sentencia FOR");
                    SB = true;
                    boolean v1 = false, v2 = false, v3 = false;
                    int p1 = 0, p2 = 0, p3 = 0;
                    //Se comprueba que la primera variable fue declarada
                    if (!tabla.contains(separadas[2])) {
                        ListaErrores.add(6);
                        ErrorLinea.add(linea);
                    }
                    //se comprueba si la segunda variable fue declarada
                    if (!tabla.contains(separadas[4])) {    
                        if (!separadas[4].matches(constante)) {
                            ListaErrores.add(6);
                            ErrorLinea.add(linea);
                        }
                    }
                    //se comprueba si la tercera variable fue declarada
                    if (!tabla.contains(separadas[6])) {
                        if (!separadas[6].matches(constante)) {
                            ListaErrores.add(6);
                            ErrorLinea.add(linea);
                        }
                    }
                    //Se comprueba si la cuarta variable fue declarada
                    if (!tabla.contains(separadas[8])) {
                        if (!separadas[8].matches(constante)) {
                            ListaErrores.add(6);
                            ErrorLinea.add(linea);
                        }
                    }
                    //Se comprueba si la quinta variable fue declarada
                    if (!tabla.contains(separadas[10])) {
                        if (!separadas[10].matches(constante)) {
                            ListaErrores.add(6);
                            ErrorLinea.add(linea);
                        }
                    }
                    //se comprueba que el indicador de aumento o decremento fue escrito correctamente
                    if (!separadas[11].matches(ODI)) {
                        ListaErrores.add(19);
                        ErrorLinea.add(linea);
                    }
                    //Se comprueba que los ID usados, sean los mismos, puesto que no se permite ID diferentes
                    if (separadas[2].matches(separadas[10])) {
                        boolean si = true;
                        if (!separadas[2].matches(separadas[6])) {
                            si = false;
                        }
                        if (si == false) {
                            if (!separadas[2].matches(separadas[8])) {
                                ListaErrores.add(20);
                                ErrorLinea.add(linea);
                            }
                        }
                        //si alguno de ellos no concuerda error
                    } else {
                        ListaErrores.add(20);
                        ErrorLinea.add(linea);
                    }

                    //Se declaran dos Array, para comprobar los tipos de las variables y constantes
                    ArrayList<String> vars = new ArrayList<String>();
                    ArrayList<String> cons = new ArrayList<String>();

                    //se verifica en todos y cada uno de los campos especificos, si se trata de un ID o de una constante
                    if (separadas[2].matches(ID)) {
                        vars.add(TVariable.get(tabla.indexOf(separadas[2])));
                    } else {
                        cons.add("" + separadas[2]);
                    }
                    if (separadas[4].matches(ID)) {
                        vars.add(TVariable.get(tabla.indexOf(separadas[4])));
                    } else {
                        cons.add("" + separadas[4]);
                    }
                    if (separadas[6].matches(ID)) {
                        vars.add(TVariable.get(tabla.indexOf(separadas[6])));
                    } else {
                        cons.add("" + separadas[6]);
                    }
                    if (separadas[8].matches(ID)) {
                        vars.add(TVariable.get(tabla.indexOf(separadas[8])));
                    } else {
                        cons.add("" + separadas[8]);
                    }
                    if (separadas[10].matches(ID)) {
                        vars.add(TVariable.get(tabla.indexOf(separadas[10])));
                    } else {
                        cons.add("" + separadas[10]);
                    }
                    //se declaran algunas variables para hacer la comprobacion de los tipos de las variables
                    String comp = vars.get(0);
                    boolean bien = true;
                    //se comprueba con cada espacio del Array, si son del mismo tipo, si no lo son, error
                    for (int i = 0; i < vars.size(); i++) {
                        if (!comp.equals(vars.get(i))) {
                            bien = false;
                        }
                    }
                    //se comprobara la compatibilidad de los tipos de variables y constantes
                    if (bien == true) {
                        if (!cons.isEmpty()) {
                            String type = vars.get(0);
                            if (cons.get(0).matches(flotante)) {
                                for (int i = 0; i < cons.size(); i++) {
                                    if (!cons.get(i).matches(flotante)) {
                                        bien = false;
                                    }
                                    if (type.matches("FLOT")) {
                                        if (!cons.get(i).matches(flotante)) {
                                            bien = false;
                                        }
                                    }
                                }
                            }
                            if (cons.get(0).matches(entero)) {
                                for (int i = 0; i < cons.size(); i++) {
                                    if (!cons.get(i).matches(entero)) {
                                        bien = false;
                                    }
                                    if (type.matches("ENT")) {
                                        if (!cons.get(i).matches(entero)) {
                                            bien = false;
                                        }
                                    }
                                }

                            }

                        }
                    }
                    //Si el tipo de alguna variable o alguna constante no coincide con las demas, error
                    if (bien == false) {
                        ListaErrores.add(17);
                        ErrorLinea.add(linea);
                    }

                    //Se comprueba la sintaxis de la linea
                    if (!cadena.matches(FOR)) {
                        //Si no concuerda la sintaxis de la linea, error de sintaxis en la sentencia FOR
                        ListaErrores.add(9);
                        ErrorLinea.add(linea);
                    }
                }
                //Si la palabra concuerda con la sentencia se comprobara la sintaxis de la linea
                if (auxiliar.matches("WHILE")) {
                    SB = true;
                    boolean v1 = false, v2 = false, c1 = false, c2 = false;
                    int p1 = 0, p2 = 0, t1 = 0, t2 = 0;
                    //Se comprueba que el primer campo es un ID o una constante
                    ChecaID(variable, separadas[2], tabla);
                    if (esta == true) {
                        //Si se encontro en la tabla de simbolos, busca el tipo de variable que es
                        posicion(variable, separadas[2], tabla);
                        p1 = p;
                        
                        v1 = true;
                    } else {
                        //si no es ID, se verifica si es una constante
                        CTE(separadas[2]);
                        if (cte == 1 || cte == 2) {
                            t1 = cte;
                            c1 = true;
                        } else {
                            //Si tampoco es una constante, error
                            ListaErrores.add(6);
                            ErrorLinea.add(linea);
                        }
                    }
                    p = 0;
                    cte = 0;
                    esta = false;
                    //Comprobamos la segunda variable
                    ChecaID(variable, separadas[4], tabla);
                    if (esta == true) {
                        //si la encontro en la tabla de simbolos, se busca el tipo de la variable
                        v2 = true;
                        posicion(variable, separadas[4], tabla);
                        p2 = p;
                        //System.out.println("Variable encontrado en indice " + p2);
                    } else {
                        //Si es una constante esta correcto
                        CTE(separadas[4]);
                        if (cte == 1 | cte == 2) {
                            
                            t2 = cte;
                            c2 = true;
                        } else {
                            //si no es constante, error
                            ListaErrores.add(6);
                            ErrorLinea.add(linea);
                        }
                    }
                    p = 0;
                    esta = false;
                    cte = 0;
                    //se verifica si fueron correctos los campos en base a los ID o constantes
                    if (v1 == true && v2 == true || c1 == true && c2 == true || v1 == true && c2 == true || c1 == true && v2 == true) {
                        Compatibilidad(WHILE, TVariable, ListaErrores, ErrorLinea, cadena, linea, v1, v2, c1, c2, p1, p2, t1, t2);
                    }
                    compatibles = false;
                }
                //Si la palabra concuerda con la sentencia se comprobara la sintaxis de la linea
                if (auxiliar.matches("IF")) {
                    SB = true;
                    boolean v1 = false, v2 = false, c1 = false, c2 = false;
                    int p1 = 0, p2 = 0, t1 = 0, t2 = 0;
                    //se verifica si el primero espacio es un ID
                    ChecaID(variable, separadas[2], tabla);
                    if (esta == true) {
                        //si se encuentra, se busca el tipo de variable que es
                        posicion(variable, separadas[2], tabla);
                        p1 = p;
                        
                        v1 = true;
                    } else {
                        CTE(separadas[2]);
                        if (cte == 1 || cte == 2) {
                            
                            t1 = cte;
                            c1 = true;
                        } else {
                            //si no es constante, error ID no declarado
                            ListaErrores.add(6);
                            ErrorLinea.add(linea);
                        }
                    }
                    //Comprobamos la primer variable, si se trata de un ID o de un digito
                    p = 0;
                    cte = 0;
                    esta = false;
                    //Comprobamos la segunda variable
                    //System.out.println("Busqueda de ID 2");
                    ChecaID(variable, separadas[4], tabla);
                    if (esta == true) {
                        v2 = true;
                        posicion(variable, separadas[4], tabla);
                        p2 = p;
                        
                    } else {
                        //Si es una constante esta correcto
                        CTE(separadas[4]);
                        if (cte == 1 | cte == 2) {
                            
                            t2 = cte;
                            c2 = true;
                        } else {
                            
                            ListaErrores.add(6);
                            ErrorLinea.add(linea);
                        }
                    }
                    p = 0;
                    esta = false;
                    cte = 0;
                    if (v1 == true && v2 == true || c1 == true && c2 == true || v1 == true && c2 == true || c1 == true && v2 == true) {
                        Compatibilidad(IF, TVariable, ListaErrores, ErrorLinea, cadena, linea, v1, v2, c1, c2, p1, p2, t1, t2);
                    }
                    compatibles = false;
                }
                for (int i = 0; i < contador; i++) {
                    
                    if (separadas[i].matches("\\{")) {
                        bloques.add(separadas[0]);
                        LineaBloque.add(linea);
                        SB = true;
                    }
                }
                for (int i = 0; i < contador; i++) {
                    if (separadas[i].matches("\\}")) {
                        if (bloques.isEmpty()) {
                            ListaErrores.add(16);
                        } else {
                            int tamaño = bloques.size();
                            bloques.remove(tamaño - 1);
                            LineaBloque.remove(tamaño - 1);
                            SB = true;
                        }
                    }
                }
                //si no se trata de una sentencia, verificaremos si es una asignacion u operacion aritmetica
                if (SB == false) {
                    //Comprobamos si es una variable en la primera posicion
                    ChecaID(variable, separadas[0], tabla);
                    if (esta == false) {
                        //si no es un ID, no es ninguna sentencia
                        ListaErrores.add(12);
                        ErrorLinea.add(linea);
                    } else {
                        //Buscamos el tipo de la variable que encontro
                        posicion(variable, separadas[0], tabla);
                        int t1 = p;
                        p = 0;
                        //si la linea concuerda con la sintaxis de la asignacion u operacion aritmetica, se comprobara lo demas dependiendo del caso
                        if (cadena.matches(asignacion) | cadena.matches(expresion)) {
                            //si el tipo de variable que se encontro en la primera posicion es entera, las demas tendran que ser del mismo tipo
                            if (TVariable.get(t1).matches("ENT")) {
                                //se comprobara los tipos de las demas posiciones del caso, si alguna no concuerda, error de tipos
                                esta = false;
                                ChecaID(variable, separadas[2], tabla);
                                if (esta == true) {
                                    posicion(variable, separadas[2], tabla);
                                    int t2 = p;
                                    p = 0;
                                    if (TVariable.get(t2).matches("ENT")) {
                                        
                                        if (cadena.matches(expresion)) {
                                            esta = false;
                                            ChecaID(variable, separadas[4], tabla);
                                            if (esta == true) {
                                                
                                                posicion(variable, separadas[4], tabla);
                                                int t3 = p;
                                                p = 0;
                                                if (!TVariable.get(t3).matches("ENT")) {
                                                    ListaErrores.add(17);
                                                    ErrorLinea.add(linea);
                                                }
                                            } else {
                                                if (!separadas[4].matches(entero)) {
                                                    if (separadas[4].matches(flotante)) {
                                                        ListaErrores.add(17);
                                                        ErrorLinea.add(linea);
                                                    } else {
                                                        //ID no declarado
                                                        ListaErrores.add(6);
                                                        ErrorLinea.add(linea);
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        ListaErrores.add(17);
                                        ErrorLinea.add(linea);
                                    }
                                } else {
                                    if (separadas[2].matches(entero)) {
                                        
                                        if (cadena.matches(expresion)) {
                                            esta = false;
                                            ChecaID(variable, separadas[4], tabla);
                                            if (esta == true) {
                                                posicion(variable, separadas[4], tabla);
                                                int t3 = p;
                                                
                                                if (TVariable.get(t3).matches("ENT")) {
                                                    
                                                } else {
                                                    //ID no declarado
                                                    ListaErrores.add(17);
                                                    ErrorLinea.add(linea);
                                                }
                                            } else {
                                                if (separadas[4].matches(flotante)) {           
                                                    ListaErrores.add(17);
                                                    ErrorLinea.add(linea);
                                                } else {
                                                    //ID no declarado
                                                    ListaErrores.add(6);
                                                    ErrorLinea.add(linea);
                                                }
                                            }
                                        }
                                    } else {
                                        if (separadas[2].matches(flotante)) {
                                            ListaErrores.add(17);
                                            ErrorLinea.add(linea);
                                        } else {
                                            //ID no declarado
                                            ListaErrores.add(6);
                                            ErrorLinea.add(linea);
                                        }
                                    }
                                }
                            } else {
                                if (TVariable.get(t1).matches("FLOT")) {
                                    esta = false;
                                    ChecaID(variable, separadas[2], tabla);
                                    if (esta == true) {
                                        posicion(variable, separadas[2], tabla);
                                        int t2 = p;
                                        p = 0;
                                        if (TVariable.get(t2).matches("FLOT")) {
                                            if (cadena.matches(expresion)) {
                                                esta = false;
                                                ChecaID(variable, separadas[4], tabla);
                                                if (esta == true) {
                                                    posicion(variable, separadas[4], tabla);
                                                    int t3 = p;
                                                    p = 0;
                                                    if (!TVariable.get(t3).matches("FLOT")) {
                                                        ListaErrores.add(17);
                                                        ErrorLinea.add(linea);
                                                    } 
                                                } else {
                                                    if (!separadas[4].matches(flotante)) {
                                                        
                                                        ListaErrores.add(6);
                                                        ErrorLinea.add(linea);
                                                    }
                                                }
                                            }
                                        } else {
                                            ListaErrores.add(17);
                                            ErrorLinea.add(linea);
                                        }
                                    } else {
                                        if (separadas[2].matches(flotante)) {
                                            if (cadena.matches(expresion)) {
                                                esta = false;
                                                ChecaID(variable, separadas[4], tabla);
                                                if (esta == true) {
                                                    posicion(variable, separadas[4], tabla);
                                                    int t3 = p;
                                                    
                                                    if (TVariable.get(t3).matches("FLOT")) {
                                                    } else {
                                                        //ID no declarado
                                                        ListaErrores.add(17);
                                                        ErrorLinea.add(linea);
                                                    }
                                                } else {
                                                    if (separadas[4].matches(flotante)) {
                                                        //System.out.println("La tercera posicion es un numero, todo esta bien");
                                                    } else {
                                                        //ID no declarado
                                                        ListaErrores.add(6);
                                                        ErrorLinea.add(linea);
                                                    }
                                                }
                                            }
                                        } else {
                                            //ID no declarado
                                            ListaErrores.add(6);
                                            ErrorLinea.add(linea);
                                        }
                                    }
                                }
                            }
                        } else {
                            ListaErrores.add(18);
                            ErrorLinea.add(linea);
                        }
                    }
                    esta = false;
                }

                //Si no se esta declarando una variable en la parte de declaracion, error
                if (declarando == true) {
                    if (!separadas[0].matches("VARIABLES")) {
                        if (VB == false) {
                            
                        }
                    }
                }
                //si no se esta realizando ninguna sentencia, error
            } catch (Exception e3) {
                
            }
        }
    }

    boolean esta = false;

    //Metodo que verifica si el objeto buscado es un ID
    public boolean ChecaID(int variable, String buscar, ArrayList<String> tabla) {
        try {
            for (int i = 0; i < variable; i++) {
                if (buscar.equals(tabla.get(i))) {
                    esta = true;
                    break;
                }
            }
        } catch (Exception e) {
            
        }
        return esta;
    }

    int p = 0;

    //metodo que verifica la posicion en la lista donde se encuentra la variable
    public int posicion(int variable, String buscar, ArrayList<String> tabla) {
        try {
            for (int i = 0; i < variable; i++) {
                if (buscar.equals(tabla.get(i))) {
                    p = i;
                }
            }
        } catch (Exception e) {
            
        }
        return p;
    }

    boolean compatibles = false;

    //Metodo que busca el tipo de variable que se encontro en la posicion
    public boolean tipos(ArrayList<String> TVariables, int p1, int p2) {
        if (TVariables.get(p1).equals(TVariables.get(p2))) {
            compatibles = true;
        } 
        return compatibles;
    }

    int cte = 0;

    //metodo que verifica si un campo es constante
    public int CTE(String busca) {
        if (busca.matches("[0-9]+")) {
            cte = 1;
        }
        if (busca.matches("[0-9]+\\.[0-9]")) {
            cte = 2;
        }
        return cte;
    }

    //Metodo que verifica la compatibilidad de las variables
    public void Compatibilidad(String sintaxis, ArrayList<String> TVariable, ArrayList<Integer> ListaErrores, ArrayList<Integer> ErrorLinea, String cadena, int linea, boolean v1, boolean v2, boolean c1, boolean c2, int p1, int p2, int t1, int t2) {
        if (v1 == true && v2 == true) {
            tipos(TVariable, p1, p2);
            if (compatibles == true) {
                if (!cadena.matches(sintaxis)) {
                    ListaErrores.add(11);
                    ErrorLinea.add(linea);
                }
            } else {
                ListaErrores.add(17);
                ErrorLinea.add(linea);
            }
        } else if (c1 == true && c2 == true) {
            if (t1 == t2) {
                if (!cadena.matches(sintaxis)) {
                    ListaErrores.add(11);
                    ErrorLinea.add(linea);
                }
            } else {
                //imcompatibilidad de tipos 
                ListaErrores.add(17);
                ErrorLinea.add(linea);
            }
        } else if (v1 == true && c2 == true) {
            if (TVariable.get(p1).matches("ENT")) {
                if (t2 == 1) {
                    if (!cadena.matches(sintaxis)) {
                        
                        ListaErrores.add(11);
                        ErrorLinea.add(linea);
                    }
                } else {
                    //Imcompatibilidad de tipos
                    ListaErrores.add(17);
                    ErrorLinea.add(linea);
                }
            } else if (TVariable.get(p1).matches("FLOT")) {
                if (t2 == 2) {
                    if (!cadena.matches(sintaxis)) {
                        
                        ListaErrores.add(11);
                        ErrorLinea.add(linea);
                    }
                } else {
                    //Incompatibilidad de tipos
                    ListaErrores.add(17);
                    ErrorLinea.add(linea);
                }
            }
        } else if (c1 == true && v2 == true) {
            if (TVariable.get(p2).matches("ENT")) {
                if (t1 == 1) {
                    if (!cadena.matches(sintaxis)) {
                        ListaErrores.add(11);
                        ErrorLinea.add(linea);
                    }
                } else {
                    //Imcompatibilidad de tipos
                    ListaErrores.add(17);
                    ErrorLinea.add(linea);
                }
            } else if (TVariable.get(p2).matches("FLOT")) {
                if (t1 == 2) {
                    //si no concuerda con la sintaxis, error
                    if (!cadena.matches(sintaxis)) {
                        ListaErrores.add(11);
                        ErrorLinea.add(linea);
                    }
                } else {
                    //Imcompatibilidad de tipos
                    ListaErrores.add(17);
                    ErrorLinea.add(linea);
                }
            }
        }

    }
}
