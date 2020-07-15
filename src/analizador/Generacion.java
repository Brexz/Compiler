package analizador;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Generacion {

    //se declaran algunas variables y Arrays que nos serviran para la generacion de codigo
    int LineaIF = 0;
    int bloqueIF = 0;
    int CountIF = 0;
    int ContMensaje = 0;
    int countAUX = 100;
    int CountDIV = 0;
    boolean simple = false;
    ArrayList<String> NombreVar = new ArrayList<String>();
    ArrayList<String> Valores = new ArrayList<String>();
    ArrayList<String> BloquesIF = new ArrayList<String>();
    ArrayList<Integer> TipoWH = new ArrayList<Integer>();
    ArrayList<Integer> TipoFOR = new ArrayList<Integer>();
    ArrayList<String> CondWH = new ArrayList<String>();
    ArrayList<String> CondFOR = new ArrayList<String>();
    ArrayList<Integer> NumIF = new ArrayList<Integer>();
    ArrayList<Integer> NumWH = new ArrayList<Integer>();
    ArrayList<Integer> NumFOR = new ArrayList<Integer>();    
    Analizador a = new Analizador();

    //Metodo que genera codigo intermedio
    public void Genera() {
        //se declaran algunas variables para controlar los archivos
        File archivo = null;
        FileReader fr = null;
        BufferedReader br = null;
        ArrayList<String> Generado = new ArrayList<String>();
        ArrayList<String> Mensajes = new ArrayList<String>();
        Generacion gen = new Generacion();
        
        //se lee el archivo
        try {
            archivo = new File("src\\analizador\\archivo.txt");
            //leemos el archivo y extraemos el contenido del archivo
            fr = new FileReader(archivo);
            br = new BufferedReader(fr);
            String linea;
            int NumLinea = 0;

            //mientras siga existiendo una linea por leer en el archivo, se haran los procesos necesarios
            
            //se agregaran las primeras lineas necesarias para que el programa funcione
            Generado.add(".MODEL SMALL");
            Generado.add(".CODE");
            Generado.add("Inicio:");
            Generado.add("    mov Ax, @Data");
            Generado.add("    mov Ds,Ax");

            while ((linea = br.readLine()) != null) {
                //Se quitan los primeros y ultimos espacios de la linea
                linea = linea.trim();
                //si la linea en el archivo esta vacia se salta a la siguiente linea
                if (linea.isEmpty()) {
                    continue;
                }
                NumLinea++;
                //Se manda llamar al metodo que genera el codigo
                Generando(linea, ContMensaje, Generado, Mensajes, bloqueIF, LineaIF, NumLinea);
            }
            //despues de haber leido todas las lineas del archivo, se generan algunas sentencias necesarias para que el programa funcione
            Generado.add("    mov AX,4c00h");
            Generado.add("    int 21h");
            Generado.add(".DATA");
            //se agregan los mensajes y variables generados
            for (int i = 0; i < Mensajes.size(); i++) {
                Generado.add("" + Mensajes.get(i));

            }
            //Por ultimo se agregan las lineas finales para que el programa funcione
            Generado.add(".STACK");
            Generado.add("END Inicio");
            
            //Se muestra en consola, el codigo generado
            System.out.println("");
            System.out.println("Codigo Generado :");
            Generado.forEach((a) -> System.out.println(a));
            //Se hara la creacion del archivo con el codigo generado
            try {
                //Establecemos la ruta
                String ruta = "C:/Users/brian/Desktop/CodigoEnsamblador.txt";
                //Creamos algunas variables para generar el archivo
                File file = new File(ruta);
                file.createNewFile();
                FileWriter fw = new FileWriter(file);
                BufferedWriter bw = new BufferedWriter(fw);
                String lin = "";
                //Se agrega al archivo, el codigo generado
                for (int i = 0; i < Generado.size(); i++) {
                    lin = Generado.get(i);
                    bw.write(lin + "\n");
                }
                //se cierra el archivo
                bw.close();
                //si ocurre algun error, se atrapa con una excepcion
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {

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

    public void Generando(String linea, Integer ContMensaje, ArrayList<String> Generado, ArrayList<String> Mensajes, int bloqueIF, int LineaIF, int NumLinea) {

        //Separamos las palabras de la linea en turno
        StringTokenizer token;
        String separadas[] = new String[100];
        token = new StringTokenizer(linea, " ");
        int contador = 0;
        String ID = "(^[A-Z][A-Z 0-9 _]*)";

        //se agregan al arreglo, las palabras que contenga la linea
        while (token.hasMoreTokens()) {
            separadas[contador] = token.nextToken();
            contador++;
        }

        //Dependiendo de la palabra que encuentra primero, sera la sentencia que generara
        switch (separadas[0]) {
            //si concuerda con la palabra PRINTLN
            case "PRINTLN": {
                //Obtengo en que posicion se encuentra la primer comilla
                int Comilla1 = linea.indexOf("'");
                //Obtengo en que posicion se encuentra la segunda comilla
                int Comilla2 = linea.lastIndexOf("'");
                //si se encuentran las comillas, se extrae una subcadena para poder ser presentarse en la pantalla
                if (Comilla1 > 0 && Comilla2 > 0) {
                    this.ContMensaje++;
                    Generado.add("    mov Dx, Offset Mensaje" + ContMensaje);
                    Generado.add("    mov Ah, 9");
                    Generado.add("    int 21h");
                    //Se extrae una subcadena desde la primer posicion en donde se encuentra la comilla, y la ultima posicion de la comilla
                    String letras = linea.substring(Comilla1 + 1, Comilla2);
                    //Se añade 
                    Mensajes.add("Mensaje" + ContMensaje + " db '" + letras + "',10,13,'$'");
                } else {
                    //si no se encuentran las comillas, se trata de una constante entera o flotante, o de un ID
                    if (separadas[2].matches("[0-9]+") | separadas[2].matches("[0-9]+\\.[0-9]+")) {
                        this.ContMensaje++;
                        Generado.add("    mov Dx, Offset Mensaje" + ContMensaje);
                        Generado.add("    mov Ah, 9");
                        Generado.add("    int 21h");
                        Mensajes.add("Mensaje" + ContMensaje + " db '" + separadas[2] + "',10,13,'$'");
                    } else {
                        //si no es un entero o flotante, es un ID
                        Generado.add("    mov Dx, Offset " + separadas[2] + "+2");
                        Generado.add("    mov Ah, 9");
                        Generado.add("    int 21h");
                    }
                }
                break;
            }
            //Si concuerda con la palabra SCAN, se comprueba y se genera lo necesario
            case "SCAN": {
                //Se agregan las siguientes lineas para poder leer las variables
                Generado.add("    mov Dx, offset " + separadas[2]);
                Generado.add("    mov Ah, 0Ah");
                Generado.add("    int 21h");
                break;
            }   
            
            //Si concuerda con la palabra IF, se comprueba y se genera el codigo necesario
            case "IF": {               
                this.LineaIF = NumLinea;
                //Se comprueba si el espacio 1 es un ID
                if (separadas[2].matches("[0-9]+")) {
                   //Se genera el codigo necesario
                    this.countAUX++;
                    Mensajes.add("ayuda" + this.countAUX + " db 2,?,2 dup (\"$\"),10,13,\"$\"");
                    Generado.add("    mov [ayuda" + this.countAUX + "+2], 3" + separadas[2] + "h");
                    Generado.add("    mov ah, [ayuda" + this.countAUX + "+2]");
                } else {
                    //de lo contrario, se trata de una constante
                    Generado.add("    mov ah, [" + separadas[2] + "+2]");
                }
                //Se comprueba si el segundo espacio es un ID
                if (separadas[4].matches("[0-9]+")) {
                    Generado.add("    mov al,3" + separadas[4] + "h");
                } else {
                    //De lo contrario es una constante, se genera el codigo 
                    Generado.add("    mov al, [" + separadas[4] + "+2]");
                }
                //Se agregan una lineas necesarias para que la sentencia funcione correctamente
                Generado.add("    cmp ah,al");
                Generado.add("    ja positivo" + NumLinea);
                Generado.add("    jb negativo" + NumLinea);
                Generado.add("    je igual" + NumLinea);

                //Se comprueba en base al simbolo de la comparacion, el codigo que se tendra que generar para el correcto funcionamiento
                BloquesIF.add("" + NumLinea);
                //En base al simbolo encontrado, sera el caso que se generara
                switch (separadas[3]) {
                    case ">": {
                        this.bloqueIF = 1;
                        Generado.add("    positivo" + NumLinea + ":");
                        break;
                    }
                    case "<": {
                        this.bloqueIF = 2;
                        Generado.add("    positivo" + NumLinea + ":");
                        Generado.add("    jmp salida" + NumLinea);
                        Generado.add("    negativo" + NumLinea + ":");
                        break;
                    }
                    case "==": {
                        this.bloqueIF = 3;
                        Generado.add("    positivo" + NumLinea + ":");
                        Generado.add("    jmp salida" + NumLinea);
                        Generado.add("    negativo" + NumLinea + ":");
                        Generado.add("    jmp salida" + NumLinea);
                        Generado.add("    igual" + NumLinea + ":");
                        break;
                    }
                }
                //Se agrega a la lista, el tipo de sentencia que se tendra que generar una vez encontrada la llave de cierra
                this.NumIF.add(this.bloqueIF);
                break;
            }
            //Si concuerda con la palabra FOR, se comprobara y generara el codigo necesario
            case "FOR": {
                //se creara algunas variables para poder hacer los procesos necearios
                String pasos = "";
                String CondFOR = "";
                this.NumFOR.add(NumLinea);
                //primero se comprueba si se tiene que ir aumentando o decrementando la variable del ciclo
                if (separadas[11].matches("\\++")) {
                    pasos = "add [" + separadas[2] + "+2], 1";
                } else {
                    pasos = "sub [" + separadas[2] + "+2], 1";
                }
                //Se genera el inicio del ciclo en base al valor encontrado
                Generado.add("    mov [" + separadas[2] + "+2], 3" + separadas[4] + "h");
                //Se comprueba en base a la condicion del ciclo, los procesos que se tiene que realizar
                //Si el primer espacio es un numero, se generara
                if (separadas[6].matches("[0-9]+")) {
                    this.countAUX++;
                    Mensajes.add("ayuda" + this.countAUX + " db 2,?,2 dup (\"$\"),10,13,\"$\"");
                    Generado.add("    mov [ayuda" + this.countAUX + "+2], 3" + separadas[6] + "h");
                    //Se comprueba si el segundo espacio se trata de un numero
                    if (separadas[8].matches("[0-9]+")) {
                        //Se genera el codigo para el caso encontrado
                        CondFOR = pasos + "\n    mov ah, ayuda" + this.countAUX + "+2\n    cmp ah, 3" + separadas[8] + "h";
                    } else {
                        //de lo contrario, cambiara el codigo que se tendra que generar
                        CondFOR = pasos + "\n    mov ah, ayuda" + this.countAUX + "+2\n    mov al, " + separadas[8] + "+2\n    cmp ah, al";
                    }
                } else {
                    //El primer espacio sera un ID
                    if (separadas[8].matches("[0-9]+")) {
                        //el segundo espacio sera un numero, se genera el codigo
                        CondFOR = pasos + "\n    mov ah, [" + separadas[6] + "+2]\n    cmp ah, 3" + separadas[8] + "h";
                    } else {
                        //De lo contrario, se generara el codigo cuando es un ID
                        CondFOR = pasos + "\n    mov ah, [" + separadas[6] + "+2]\n    mov al, " + separadas[8] + "+2\n    cmp ah, al";
                    }
                }
                //Se agrega una etiqueta con el numero de la sentencia basada en la linea del archivo donde fue encontrada
                Generado.add("    for" + NumLinea + ":");
                String aux = "";
                //En base a lo que se encuentre como simbolo de condicion, sera el codigo que se genere
                switch (separadas[7]) {
                    case ">": {
                        aux = ("ja for" + NumLinea);
                        this.TipoFOR.add(1);
                        break;
                    }
                    case "<": {
                        aux = ("jb for" + NumLinea);
                        this.TipoFOR.add(2);
                        break;
                    }
                    case "==": {
                        aux = ("je for" + NumLinea);
                        this.TipoFOR.add(3);
                        break;
                    }
                    case "<=": {
                        System.out.println("Es menor igual");
                        aux = ("jna for" + NumLinea);
                        this.TipoFOR.add(4);
                        break;
                    }
                    case ">=": {
                        System.out.println("Es mayor igual");
                        aux = ("jnb for" + NumLinea);
                        this.TipoFOR.add(4);
                        break;
                    }

                }
                //Se agrega la condicion de la comparacion del ciclo a la lista
                CondFOR = "" + CondFOR + "\n    " + aux;
                this.CondFOR.add(CondFOR);
                //Mensajes.add("maximo dw " + separadas[8]);
                break;
            }
            //Si la palabra concuerda con WHILE, se comprobara lo necesario y se generara el codigo necesario
            case "WHILE": {
                //Declaramos algunas variables para hacer procesos
                String condWH = "";
                int TypeWH = 0;
                this.NumWH.add(NumLinea);
                //Comprobamos si el primer espacio es un numero
                if (separadas[2].matches("[0-9]+")) {
                    this.countAUX++;
                    //se agrega una variable auxiliar para hacer los procesos
                    Mensajes.add("ayuda" + this.countAUX + " db 2,?,2 dup (\"$\"),10,13,\"$\"");
                    //se varifica si el segundo espacio es un numero
                    if (separadas[4].matches("[0-9]+")) {
                        condWH = "    mov [ayuda" + this.countAUX + "+2], 3" + separadas[2] + "h\n    mov ah, ayuda" + this.countAUX + "+2\n    cmp ah, 3" + separadas[4] + "h";
                    } else {
                        //de lo contrario, es un ID, se generara el codigo correspondiente
                        condWH = "    mov [ayuda" + this.countAUX + "+2], 3" + separadas[2] + "h\n    mov ah, ayuda" + this.countAUX + "+2\n    mov al, [" + separadas[4] + "+2]\n    cmp ah, al";
                    }
                } else {
                    //El primer espacio es un ID
                    //se verifica si el segundo es un numero o un ID, para poder generar el codigo correcto
                    if (separadas[4].matches("[0-9]+")) {
                        condWH = "    mov ah, [" + separadas[2] + "+2]\n    cmp ah, 3" + separadas[4] + "h";
                    } else {
                        condWH = "    mov ah, [" + separadas[2] + "+2]\n    mov al, [" + separadas[4] + "+2]\n    cmp ah, al";
                    }
                }
                Generado.add(condWH);
                this.CondWH.add(condWH);

                //En base al simbolo encontrado para la condicion, sera el codigo que se genere
                switch (separadas[3]) {
                    case ">": {
                        TypeWH = 1;
                        Generado.add("    ja while" + NumLinea);
                        Generado.add("    jb salidaW" + NumLinea);
                        Generado.add("    je salidaW" + NumLinea);
                        break;
                    }
                    case "<": {
                        TypeWH = 2;
                        Generado.add("    ja salidaW" + NumLinea);
                        Generado.add("    jb while" + NumLinea);
                        Generado.add("    je salidaW" + NumLinea);
                        break;
                    }
                    case "==": {
                        Generado.add("    ja salidaW" + NumLinea);
                        Generado.add("    jb salidaW" + NumLinea);
                        Generado.add("    je while" + NumLinea);
                        TypeWH = 3;
                        break;
                    }
                }
                this.TipoWH.add(TypeWH);
                Generado.add("    while" + NumLinea + ":");
                break;
            }

            //en caso de que se encuentre que es una llave que cierra, quiere decir que cerraremos el bloque de alguna sentencia
            case "}": {                
                //si la lista del IF no esta vacia, quiere decir que pertenece al IF
                if (!this.NumIF.isEmpty()) {
                    //se obtiene los ultimos datos de la lista, que es al que pertenece la llave
                    int tamaño2 = NumIF.size();
                    int opcion = this.NumIF.get(tamaño2 - 1);
                    //System.out.println("Tipo que recibe " + opcion);
                    int tamaño = BloquesIF.size();
                    //en base a la condicion que se habia encontrado, sera el codigo que se genere
                    if (opcion == 1) {
                        Generado.add("    negativo" + BloquesIF.get(tamaño - 1) + ":");
                        Generado.add("    jmp salida" + BloquesIF.get(tamaño - 1));
                        Generado.add("    igual" + BloquesIF.get(tamaño - 1) + ":");
                        Generado.add("    jmp salida" + BloquesIF.get(tamaño - 1));
                        Generado.add("    salida" + BloquesIF.get(tamaño - 1) + ":");
                        this.BloquesIF.remove(tamaño - 1);
                        this.NumIF.remove(tamaño2 - 1);
                        this.LineaIF = 0;
                        this.bloqueIF = 0;
                        this.CountIF++;
                    } else if (opcion == 2) {
                        Generado.add("    igual" + BloquesIF.get(tamaño - 1) + ":");
                        Generado.add("    jmp salida" + BloquesIF.get(tamaño - 1));
                        Generado.add("    salida" + BloquesIF.get(tamaño - 1) + ":");
                        this.BloquesIF.remove(tamaño - 1);
                        this.NumIF.remove(tamaño2 - 1);
                        this.LineaIF = 0;
                        this.bloqueIF = 0;
                        this.CountIF++;
                    } else if (opcion == 3) {
                        Generado.add("    salida" + BloquesIF.get(tamaño - 1) + ":");
                        this.BloquesIF.remove(tamaño - 1);
                        this.NumIF.remove(tamaño2 - 1);
                        this.LineaIF = 0;
                        this.bloqueIF = 0;
                        this.CountIF++;
                    }
                }
                //si la lista del FOR no esta vacia, quiere decir que pertenece a esta sentencia
                if (!this.NumFOR.isEmpty()) {
                    //Se ontienen los ultimos datos, que es a la cual pertence dicha sentencia
                    int tamaño = NumFOR.size();
                    //y se genera el codigo necesario
                    Generado.add("    " + this.CondFOR.get(tamaño - 1));
                    this.CondFOR.remove(tamaño - 1);
                    this.NumFOR.remove(tamaño - 1);
                }
                //si la lista del WHILE no esta vacia, quiere decir que pertenece a dicha sentencia 
                if (!this.NumWH.isEmpty()) {
                    int tamaño = NumWH.size();
                    int opcion = this.TipoWH.get(tamaño - 1);
                    Generado.add(this.CondWH.get(tamaño - 1));
                    //en base al simbolo de la condicion, sera el codigo que se generara
                    if (opcion == 1) {
                        Generado.add("    ja while" + this.NumWH.get(tamaño - 1));
                        Generado.add("    salidaW" + this.NumWH.get(tamaño - 1) + ":");
                    } else if (opcion == 2) {
                        Generado.add("    jb while" + this.NumWH.get(tamaño - 1));
                        Generado.add("    salidaW" + this.NumWH.get(tamaño - 1) + ":");
                    } else if (opcion == 3) {
                        Generado.add("    je while" + this.NumWH.get(tamaño - 1));
                        Generado.add("    salidaW" + this.NumWH.get(tamaño - 1) + ":");
                    }
                    //Se remueve la informacion de la ultima posicion para que no se repita
                    this.NumWH.remove(tamaño - 1);
                    this.TipoWH.remove(tamaño - 1);
                    this.CondWH.remove(tamaño - 1);
                }
                break;
            }
            //si no es ninguna de las anteriores se trata de una asignacion, operacion aritmetica o asignacion
            default: {
                this.simple = false;
                //si no concuerda con ninguna de las etiquetas, continua con la verificacion
                if (!separadas[0].matches("VARIABLES") && !separadas[0].matches("PROGRAMACION")) {
                    //si la primer posicion concuerda con un ID
                    if (separadas[0].matches(ID)) {
                        //si se encuentra el simbolo, se trata de una declaracion de variable
                        if (separadas[1].matches(":")) {
                            this.NombreVar.add(separadas[0]);
                            //se comprueba que el tipo de la variable sea correcto y permitido
                            if (separadas[2].matches("ENT")) {
                                this.Valores.add("0");
                                Mensajes.add("" + separadas[0] + " db 2,?,2 dup (\"$\"),10,13,\"$\"");
                            } else if (separadas[2].matches("CAD")) {
                                Mensajes.add("" + separadas[0] + " db 2,?,2 dup (\"$\"),10,13,\"$\"");
                                this.Valores.add(" ");
                            }
                        }
                        //si se encuntran los simbolos de la asignacion simple, procedemos a verificarlo
                        if (separadas[1].matches("=") && separadas[3].matches(";")) {
                            this.simple = true;
                            //comprobamos si la segunda posicion es un numero o un ID para generar el codigo necesario
                            if (separadas[2].matches("[0-9]+")) {
                                int pos = this.NombreVar.indexOf(separadas[0]);
                                this.Valores.set(pos, separadas[2]);
                                Generado.add("    mov [" + separadas[0] + "+2], 3" + separadas[2] + "h");
                            } else {
                                Generado.add("    mov dl, [" + separadas[2] + "+2]");
                                Generado.add("    mov [" + separadas[0] + "+2], dl");
                            }
                        }
                        //si no fue una asignacion simple, se trata de una operacion aritmetica, procedemos a generar el codigo respectivo
                        if (simple == false) {
                            //comprobamos que se trata de una operacion aritmetica
                            if (separadas[1].matches("=") && separadas[5].matches(";")) {
                                //Comprobamos de que tipo de operacion se trata
                                switch (separadas[3]) {
                                    //si se encuntra el simbolo, se trata de una suma
                                    case "+": {
                                        //en base a las posiciones, comprobando si es un ID o un numero, se generara el codigo para cada caso respectivamente
                                        if (separadas[4].matches("[0-9]+")) {
                                            if (separadas[2].matches("[0-9]+")) {
                                                this.countAUX++;
                                                Generado.add("    add [ayuda" + this.countAUX + "+2], " + separadas[4]);
                                            } else {
                                                Generado.add("    add [" + separadas[2] + "+2], " + separadas[4]);
                                            }
                                        } else {
                                            if (separadas[2].matches("[0-9]+")) {
                                                Generado.add("    add ["+separadas[4]+"+2], "+separadas[2]);
                                                Generado.add("    mov al, ["+separadas[4]+"+2]");
                                                Generado.add("    mov [" + separadas[0] + "+2], al");
                                            } else {
                                                Generado.add("    mov dl, [" + separadas[4] + "+2]");
                                                Generado.add("    sub dl, 30h");
                                                Generado.add("    add [" + separadas[2] + "+2], dl");
                                                Generado.add("    mov al, [" + separadas[2] + "+2]");
                                                Generado.add("    mov [" + separadas[0] + "+2], al");
                                                
                                            }
                                        }                                        
                                        break;
                                    }
                                    //si se encuentra este simbolo, sera una resta
                                    case "-": {
                                        //en base a las posiciones, se comprobara si se trata de un ID o un numero, y se genarara el codigo respectivo para cada caso
                                        if (separadas[2].matches("[0-9]+")) {
                                            Generado.add("    sub [" + separadas[2] + "+2], " + separadas[4]);
                                        } else {
                                            if(separadas[4].matches("[0-9]+")){
                                                
                                            } else {
                                                Generado.add("    mov dl, [" + separadas[4] + "+2]");
                                                Generado.add("    sub dl, 30h");
                                                Generado.add("    sub [" + separadas[2] + "+2], dl");
                                            }
                                        }
                                        Generado.add("    mov al, [" + separadas[2] + "+2]");
                                        Generado.add("    mov [" + separadas[0] + "+2], al");
                                        break;
                                    }
                                    //Si se encuentra este simbolo, sera una multiplicacion
                                    case "*": {
                                        //En base a las posiciones, se verificara si son ID o constantes, y se generara el caso para cada uno
                                        if (separadas[2].matches("[0-9]*")) {
                                            Generado.add("    mov al, " + separadas[2]);
                                        } else {
                                            Generado.add("    add [" + separadas[2] + "+2], -30h");
                                            Generado.add("    mov al, [" + separadas[2] + "+2]");
                                        }
                                        if (separadas[4].matches("[0-9]*")) {
                                            Generado.add("    mov bl, " + separadas[4]);
                                        } else {
                                            Generado.add("    add [" + separadas[4] + "+2], -30h");
                                            Generado.add("    mov bl, [" + separadas[4] + "+2]");
                                        }
                                        Generado.add("    mul bl");
                                        Generado.add("    mov dl, al");
                                        Generado.add("    add dl, 30h");
                                        Generado.add("    mov [" + separadas[0] + "+2], dl");
                                        break;
                                    }
                                    //Si se encuentra este simbolo, sera una division
                                    case "/": {
                                        //En base a las posiciones, se verificara si son ID o constantes, y se generara el caso para cada uno
                                        if (separadas[2].matches("[0-9*]")) {
                                            Generado.add("    mov al, " + separadas[2]);
                                        } else {
                                            Generado.add("    mov al, [" + separadas[2] + "+2]");
                                            Generado.add("    sub ax, 30h");
                                        }
                                        Generado.add("    mov ah, 0");
                                        if (separadas[4].matches("[0-9]*")) {
                                            Generado.add("    mov bl, " + separadas[4]);
                                        } else {
                                            Generado.add("    mov bl, [" + separadas[4] + "+2]");
                                            Generado.add("    sub bx, 30h");
                                        }
                                        Generado.add("    mov bh, 0");
                                        Generado.add("    div bl");
                                        Generado.add("    mov dl, al");
                                        Generado.add("    add dl, 30h");
                                        Generado.add("    mov [" + separadas[0] + "+2], dl");
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    this.simple = false;
                }
            }
            System.out.println("");
        }
    }
}
