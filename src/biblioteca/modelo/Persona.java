package biblioteca.modelo;

/** Clase abstracta base de las personas del sistema (usuarios y estudiantes). */
public abstract class Persona {
    private int id;
    private String nombre;
    private String rut;
    private String correo;

    protected Persona() { }

    protected Persona(int id, String nombre, String rut, String correo) {
        this.id = id;
        this.nombre = nombre;
        this.rut = rut;
        this.correo = correo;
    }

    /** Polimorfismo: cada subclase describe su tipo. */
    public abstract String getTipo();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getRut() { return rut; }
    public void setRut(String rut) { this.rut = rut; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    @Override
    public String toString() {
        return nombre + " (" + rut + ")";
    }
}
